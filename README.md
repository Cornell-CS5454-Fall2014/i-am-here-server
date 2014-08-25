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
    - Return a JSON list of the occupant who currently present at the place.
* **POST** /occupancy parameter  (required parameter: name)
    - Submit an new occupancy entry. 
    - Return the newly created occupancy entry with its it.
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
  
