i-am-here-server
================

The server-side component for the "I am here" app. 
It is designed to interact with an "I am here" mobile app 
and track the number of people who currently present at the Cornell Tech campus.
It uses an Occupancy model to track occupants. An occupancy entry contains:

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
    [ { "arrive" : "2014-08-25T00:24:01.453Z",
        "depart" : "2014-08-25T00:29:36.590Z",
        "name" : "Andy Hsieh",
        "update" : "2014-08-25T00:31:01.140Z"
      },
      { "arrive" : "2014-08-25T01:18:32.405Z",
        "depart" : null,
        "name" : "Faisal Alquaddoomi",
        "update" : "2014-08-25T01:18:32.406Z"
      }
    ]
    ```    
* **POST** /occupancy (required parameter: **name**, optional parameter: **image-b64**, and **floor**)
    - Submit a new occupancy entry with name, and optionally a profile image encoded in Base64 format and the name of the floor the user stays. 
    - Return the newly created occupancy entry along with its id, which is required when updating the entry E.g.:
    
    ```json
    { "id" : 48,
      "arrive" : "2014-08-25T19:07:46.146Z",
      "depart" : null,
      "name" : "Andy Hsieh",
      "floor": "3rd",
      "update" : "2014-08-25T19:07:46.146Z"
    }
    ``` 
    
* **POST** (or **PUT**) /occupancy/*:id*/update (optional parameter: **floor**)
    - Update an occupancy entry. The *:id* segment of the URI must be the id of a valid occupancy entry. You can also update the floor if it has changed.
    - An entry that does not get updated for more than 30 minutes will be considered to have already departed the place.
    - Return "Succeed!", or 404 when the given id is invalid.    
* **POST** (or **PUT**) /occupancy/*:id*/depart
    - Set an occupancy entry to be "departed". The *:id* segment of the URI must be the id of a valid occupancy entry.
    - Return "Succeed!", or 404 when the given id is invalid.   
* **GET** /history
    - Return all occupancy entries.
* **GET** /image/*:id:*
    - Return the image of the given image id. The returned content-type is "image/jpeg"
    
*(I made a mistake in using PUT method for updates. The POST method is more appropriate for this scenario (see [explanation](http://stackoverflow.com/questions/630453/put-vs-post-in-rest)). For consistency, however, both POST and PUT are supported.)*
# What an "I am here" mobile app should do?

An "I am here" mobile app is responsible to submit an occupancy entry when the user enters Cornell Tech, 
and update it at least every 30 minutes while the user stays at Cornell Tech. 
When the user leaves Cornell Tech, the app should set the entry to be "departed."

In a nutshell, an app should implement the following Finite State Machine:

![alt text](https://github.com/Cornell-CS5454-Fall2014/i-am-here-server/blob/master/resources/public/FSM.png "I am here app FSM")



# TODO

* Do not return id's in the GET /occupancy and GET /history APIs.
* ~~Allow uploading profile images.~~