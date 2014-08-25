i-am-here-server
================

The server-side component for the "I am here" app. 
It is designed to interact with an "I am here" mobile app 
and track the number of people currently present at the Cornell Tech campus.
It uses an Occupancy model to track the occupancy of a place. An occupancy instance contains:

    1. id
    2. the occupant's name 
    3. the arrival time
    4. the last update time
    5. the departure time
    
For example:

```json
{"id":1,
 "name":"andy",
 "arrive":"2014-08-25T00:24:01.453Z",
 "update":"2014-08-25T00:31:01.140Z",
  "depart":"2014-08-25T00:29:36.590Z"
 }
```
    
The following Web APIs are implemented:

* **GET** /occupancy
    - Return a JSON list of the occupant who currently present at the place ordered by arrival time. E.g. :
    
    ```json
    [{"depart":"2014-08-25T00:29:36.590Z","update":"2014-08-25T00:31:01.140Z","arrive":"2014-08-25T00:24:01.453Z","name":"andy","id":1},{"depart":null,"update":"2014-08-25T01:18:32.406Z","arrive":"2014-08-25T01:18:32.405Z","name":null,"id":33},{"depart":null,"update":"2014-08-25T01:19:59.721Z","arrive":"2014-08-25T01:19:59.721Z","name":"Andy\n","id":34},{"depart":null,"update":"2014-08-25T01:21:43.134Z","arrive":"2014-08-25T01:21:43.134Z","name":"Andy\n","id":35},{"depart":null,"update":"2014-08-25T01:21:49.097Z","arrive":"2014-08-25T01:21:49.097Z","name":null,"id":36},{"depart":null,"update":"2014-08-25T01:22:36.302Z","arrive":"2014-08-25T01:22:36.302Z","name":null,"id":37},{"depart":null,"update":"2014-08-25T01:23:36.968Z","arrive":"2014-08-25T01:23:36.968Z","name":null,"id":38},{"depart":null,"update":"2014-08-25T01:37:43.785Z","arrive":"2014-08-25T01:37:43.785Z","name":"goodman","id":39},{"depart":null,"update":"2014-08-25T01:44:01.818Z","arrive":"2014-08-25T01:44:01.816Z","name":"goodman","id":40},{"depart":null,"update":"2014-08-25T01:46:36.418Z","arrive":"2014-08-25T01:46:36.418Z","name":"goodman","id":41},{"depart":null,"update":"2014-08-25T01:48:01.262Z","arrive":"2014-08-25T01:48:01.261Z","name":"goodman","id":42},{"depart":null,"update":"2014-08-25T01:48:54.696Z","arrive":"2014-08-25T01:48:54.695Z","name":"goodman","id":43},{"depart":null,"update":"2014-08-25T03:24:56.738Z","arrive":"2014-08-25T03:24:56.738Z","name":"andy","id":44},{"depart":"2014-08-25T03:45:03.646Z","update":"2014-08-25T03:44:14.674Z","arrive":"2014-08-25T03:26:19.951Z","name":"andy","id":45},{"depart":null,"update":"2014-08-25T03:47:24.189Z","arrive":"2014-08-25T03:47:24.189Z","name":"Andy","id":46}]
    ```    
* **POST** /occupancy parameter  (required parameter: name)
    - Submit a new occupancy entry. 
    - Return the newly created occupancy entry with its id. E.g.:
    
    ```json
    {"depart":null,"update":"2014-08-25T19:07:46.146Z","arrive":"2014-08-25T19:07:46.146Z","name":"Andy Hsieh","id":48}
    ``` 
* **PUT** /occupancy/*:id*/update
    - Update an occupancy entry. The *:id* segment of the URI must be the id of valid occupancy entry.
    - An entry that does not get updated for more than 30 minutes will be considered to have already departed the place.
    - Return "Succeed", or 404 when the given id is invalid.    
* **PUT** /occupancy/*:id*/depart
    - Set an occupancy entry to be "departed". The *:id* segment of the URI must be the id of valid occupancy entry.
    - Return "Succeed", or 404 when the given id is invalid.   
* **GET** /history
    - Return all occupancy entries.
    
# What an "I am here" mobile app should do?

An "I am here" mobile app is responsible to submit an occupancy entry when the user enters Cornell Tech, 
and update it at least every 30 minutes when the user still is at the place. 
When the user leaves Cornell Tech, the app should set the entry to "departed."
  
