i-am-here-server
================

The server-side component for the "I am here" app. 
It is designed to interact with an "I am here" mobile app 
and track the number of people who currently present at the Cornell Tech campus.
It uses an Occupancy model to track the occupancy. An occupancy entry contains:

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
* **POST** /occupancy parameter  (required parameter: **name**)
    - Submit a new occupancy entry. 
    - Return the newly created occupancy entry with its id. E.g.:
    
    ```json
    { "arrive" : "2014-08-25T19:07:46.146Z",
      "depart" : null,
      "id" : 48,
      "name" : "Andy Hsieh",
      "update" : "2014-08-25T19:07:46.146Z"
    }
    ``` 
    
* **PUT** /occupancy/*:id*/update
    - Update an occupancy entry. The *:id* segment of the URI must be the id of a valid occupancy entry.
    - An entry that does not get updated for more than 30 minutes will be considered to have already departed the place.
    - Return "Succeed", or 404 when the given id is invalid.    
* **PUT** /occupancy/*:id*/depart
    - Set an occupancy entry to be "departed". The *:id* segment of the URI must be the id of a valid occupancy entry.
    - Return "Succeed", or 404 when the given id is invalid.   
* **GET** /history
    - Return all occupancy entries.
    
# What an "I am here" mobile app should do?

An "I am here" mobile app is responsible to submit an occupancy entry when the user enters Cornell Tech, 
and update it at least every 30 minutes when the user still is at the place. 
When the user leaves Cornell Tech, the app should set the entry to be "departed."
  
# TODO

* Do not return id's in the GET /occupancy and GET /history APIs.
* Allow uploading profile images.