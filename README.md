
# safety-and-security-entry-declarations

## API Documentation
Name | Method | Path
-------- |------- |  ---
Upsert declaration | POST | [/safety-and-security-entry-declarations/declaration/:eori/:lrn](#upsert-declaration) 
Save declaration event | POST | [/safety-and-security-entry-declarations/declaration/:eori/:lrn/event](#save-declaration-event)
Set outcome | POST | [/safety-and-security-entry-declarations/declaration/:eori/:lrn/outcome](#set-outcome)
Get Declaration | GET | [/safety-and-security-entry-declarations/declaration/:eori/:lrn](#get-declaration)
Get Declarations | GET | [/safety-and-security-entry-declarations/declaration/:eori](#get-declarations)

### Upsert declaration
Submit a new, or replace an existing, declaration.
#### Request

***Example Request***
```$xslt
POST  /safety-and-security-entry-declarations/declaration/GB205672212000/myLocalReference
{
    "data": {
        "field1":"value1",
        "field2": [
            "field2Child":"field2ChildValue
        ]
        "field3: {
            "field3Child":44
        }
    }
    "declarationEvents": [ //Optional
        {
            "correlationId": "some-correlation-id",
            "declarationEvent": {
                "messageType": "submission",
                "outcome": { //Optional
                    "correlationId": "some-correlation-id",
                    "timestamp": "2022-01-10T15:23:44Z",
                    "messageType": "submission"
            }
        }
    ]
}

```

### Save declaration event
Save a new declaration event against an existing declaration
#### Request

***Example Request***
```$xslt
POST  /safety-and-security-entry-declarations/declaration/GB205672212000/myLocalReference/event
{
    "correlationId": "some-correlation-id",
    "messageType": "submission",
    "outcome": { //Optional
        "correlationId": "some-correlation-id",
        "timestamp": "2022-01-10T15:23:44Z",
        "messageType": "submission"
   }   
}

```

### Set outcome
Set the outcome of a saved declaration event
#### Request

***Example Request***
```$xslt
POST  /safety-and-security-entry-declarations/declaration/GB205672212000/myLocalReference/outcome
//Accepted
{
    "outcome": {
        "correlationId": "some-correlation-id",
        "timestamp": "2022-01-10T15:23:44Z",
        "messageType": "submission",
        "mrn": "movementReferenceNumber"
   }   
}

//Rejected
{
    "outcome": {
        "correlationId": "some-correlation-id",
        "timestamp": "2022-01-10T15:23:44Z",
        "messageType": "submission",
        "reason": "someReasonForRejection"
   }   
}

```

### Get Declaration
Get a declaration by EORI & LRN
#### Request

***Example Request***
```$xslt
GET  /safety-and-security-entry-declarations/declaration/GB205672212000/myLocalReference
```

***Example Response***
```$xslt
200 Ok
{
    "eori": "GB205672212000",
    "lrn": "myReference",
    "data": {
        "someUndefined": "JsObject"
    }
    "declarationEvents": [
        {
            "correlationId": "some-correlation-id",
            "declarationEvent": {
                "messageType": "submission",
                "outcome": {
                    "correlationId": "some-correlation-id",
                    "timestamp": "2022-01-10T15:23:44Z",
                    "messageType": "submission",
                    "reason": "someReasonForRejection"
                }
            }
        }
    ]
    "lastUpdated": "2022-01-10T15:23:44Z"
}
```

### Get Declarations
Get all declarations for a given EORI
#### Request

***Example Request***
```$xslt
GET  /safety-and-security-entry-declarations/declaration/GB205672212000
```

***Example Response***
```$xslt
200 Ok
[
    {
        "eori": "GB205672212000",
        "lrn": "myReference",
        "data": {
            "someUndefined": "JsObject"
        }
        "declarationEvents": [
            {
                "correlationId": "some-correlation-id",
                "declarationEvent": {
                    "messageType": "submission",
                    "outcome": {
                        "correlationId": "some-correlation-id",
                        "timestamp": "2022-01-10T15:23:44Z",
                        "messageType": "submission",
                        "reason": "someReasonForRejection"
                    }
                }
            }
        ]
        "lastUpdated": "2022-01-10T15:23:44Z"
    }
]
```

### Errors

Errors come in the following format, expected errors detailed in the table below:
```$xslt
{
    "code": "SOME_CODE",
    "message": "Human readable error message"
}
```

Http Code | error code | error message
-------- |------- |  ---
400 | INVALID_REQUEST | The request body did not match the format expected
400 | INVALID_LRN | The provided LocalReferenceNumber did not match the expected format
503 | UNKNOWN_ERROR | An unexpected error occurred trying to insert the given document
401 | MISSING_SS_ENROLMENT | The consumer does not have the required authorisation to make this request
401 | UNAUTHORISED | The consumer does not have the required authorisation to make this request"
404 | DECLARATION_NOT_FOUND | The request tried to update a record that doesn't exist
404 | DECLARATION_EVENT_NOT_FOUND | The request tried to update a record that doesn't exist


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").