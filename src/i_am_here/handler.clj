(ns i-am-here.handler
  (:import (org.joda.time.format ISODateTimeFormat)
           (org.joda.time ReadableInstant DateTime)
           (java.io ByteArrayInputStream))
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.string :as str]
            [ring.util.codec :as codec]
            [ring.util.response :as r]
            [clj-time.core :as t]
            [clj-time.format :as fmt]
            [clj-time.coerce :as coerce]

            )
  (:use korma.db
        korma.core)
  (:use [ring.middleware.params         :only [wrap-params]]
        [ring.middleware.json :only [wrap-json-response]]))

; use an H2 (an embedded Java DB) instance
(def db (h2 {:db "i-am-here.db"
             :user "sa"
             :password ""
             :naming {:keys str/lower-case
                      ;; set map keys to lower
                      :fields str/upper-case}}))
; table creation sql
(def occupancy-table-sql "CREATE TABLE IF NOT EXISTS occupancy(
                id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
                name VARCHAR,
                arrive DATETIME NOT NULL,
                update DATETIME NOT NULL,
                depart DATETIME,
                image_id BIGINT);")
(def image-table-sql "CREATE TABLE IF NOT EXISTS image(
                id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
                b64 VARCHAR NOT NULL
                );
                CREATE INDEX IF NOT EXISTS b64_idx ON image(b64);")
(defdb korma-db db)

; return a helper function that apply f to the values associated to each given key in the rec (a map)
(defn apply-to-keys [f keys]
  (fn [rec]
    (reduce (fn [rec k]
              (let [v (k rec)]
                (if v
                  (assoc rec k (f (k rec)))
                  rec)
                )
              )
            rec keys)))


; Occupancy is the main model of the app. It contains the id, the person's name (:name),
; the arrival time (:arrive), the last update time (:update), an the departure time (:depart).

(declare occupancy)
(declare image)
(defentity image
           (pk :id)
           (entity-fields :id :b64)
           )
(defentity occupancy
           (pk :id)
           (entity-fields :id :name :arrive :update :depart :image_id)
           ; convert joda time to sql time (and vice versa)
           (prepare (apply-to-keys coerce/to-sql-time [:arrive :update :depart]))
           (transform (apply-to-keys coerce/from-sql-time [:arrive :update :depart]))
           )
; Assume the person has left the place, if no update from it for more than 30 minutes
(def timeout (t/minutes 30))

; base select query (ordered by arrival time)
(def base-select (-> (select* occupancy)
                     (order :arrive)))

; the where clause for checking the presence of an occupancy entry.
; An entry is considered to be present if the dpart = null and
; the last update time is no more than 30 minutes before the current time
(defn presence-clause [query] (where query { :depart [= nil]
                                             :update [> (coerce/to-sql-time (t/minus (t/now) timeout))]}))

; the where cluase for querying the record by id
(defn id-clause [query id] (where query {:id id}))

; helper function that checks if a record exists and is considered to be present.
; If so, execute the function f, otherwise return 404.
(defn check-existence-then [id f]
  (if-let [occ (first
                 (-> base-select
                     (id-clause id)
                     (presence-clause)
                     (select)))]
    (f)
    {:status 404
     :body "The given key does not exist"}))
(defn id [rec] (first
                 (vals rec)))
(defroutes app-routes
  ; return all the currently prent occupancy entries
  (GET "/occupancy" [] (-> base-select
                           (presence-clause)
                           (select)))
  ; return all the occupancy entries ordered by arrival time
  (GET "/history" [] (-> base-select
                           (select)))
  ; return the image content of the given image id along with the proper content-type
  (GET "/image/:id" [id] (if-let [img (first (select image (where {:id id})) )]
                           (-> (identity {:body (ByteArrayInputStream. (codec/base64-decode (:b64 img)))})
                               (r/header "Content-Type" "image/jpeg; charset=utf-8"))
                           {:status 404}))
  ; insert a new occupany entries with the given user name and
  ; an optional profile image (encoded in Base64), set the
  ; arrival time and update time as the current time, and return
  ; the newly created entry.
  (POST "/occupancy" [name image-b64] (if-not name
                              {:status 400
                               :body "required parameter: name is missing!"}

                              (let [ image-id (if image-b64  ; if image-64 given, get the image id or insert a new image
                                                (if-let [img (first (select image
                                                                            (where {:b64 image-b64})))]
                                                  (:id img)
                                                  (id (insert image (values {:b64 image-b64})))))

                                     new-id (id (insert occupancy  ; insert the new occupancy entry and get the id
                                                        (values {:name name :arrive (t/now) :update (t/now) :image_id image-id})))]
                                ; return the newly created occupancy entry
                                {:body (first
                                         (select occupancy
                                                 (id-clause new-id)))}
                                )))
  ; update the update time of the entry with the given id
  ; return "Succeed!", or 404 if the entry does not exist or has departed
  (PUT "/occupancy/:key/update" [key] (check-existence-then
                                        key
                                        #(do
                                          (update occupancy
                                                  (set-fields {:update (t/now)})
                                                  (where {:id key}))
                                          (identity "Succeed!"))))
  ; set the departue time time of the entry of the given id
  ; return "Succeed!", or 404 if the entry does not exisit or has departed
  (PUT "/occupancy/:key/depart" [key] (check-existence-then
                                        key
                                        #(do
                                          (update occupancy
                                                  (set-fields {:depart (t/now)})
                                                  (where {:id key}))
                                          (identity "Succeed!"))))

  (route/resources "/")
  (route/not-found "Not Found"))

(defn init []
  (exec-raw [occupancy-table-sql])
  (exec-raw [image-table-sql])

  )
; register serializer for Joda datetime
(try
  (cheshire.generate/add-encoder DateTime
                                 (fn [^DateTime dt ^com.fasterxml.jackson.core.json.WriterBasedJsonGenerator generator]
                                   (.writeString generator (.print (ISODateTimeFormat/dateTime) ^ReadableInstant dt))))
  (catch Throwable t
    false))

(def app
  (handler/site (wrap-json-response (wrap-params app-routes)) ))
