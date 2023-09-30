# SaS-RESTful-API


GetUsers: http://fi1.bot-hosting.net:5260/users

GetUser: http://fi1.bot-hosting.net:5260/users/validationID

PatchTransaction: http://fi1.bot-hosting.net:5260/transaction

```
{
    "validation_active": "Vt7MOgWGWm",
    "validation_passive": "rVUoPYJ0pS",
    "money": 24
}
```

name ist optional
PostCreate: http://fi1.bot-hosting.net:5260/create?name=Hummel, Laurin      //name = Hummel, Laurin
PostCreate: http://fi1.bot-hosting.net:5260/create                          //name = not set

GetHistory: http://fi1.bot-hosting.net:5260/create
GetHistory: http://fi1.bot-hosting.net:5260/create/validationID

API-Key wird über den header übertragen:
Authentication: KEY