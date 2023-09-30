# SaS-RESTful-API


GetUsers: http://fi1.bot-hosting.net:5260/users

GetUser: http://fi1.bot-hosting.net:5260/users/validationID

PatchTransaction: http://fi1.bot-hosting.net:5260/transaction <br>
```
{
    "validation_active": "Vt7MOgWGWm",
    "validation_passive": "rVUoPYJ0pS",
    "money": 24
}
```

name ist optional <br>
PostCreate: http://fi1.bot-hosting.net:5260/create?name=Hummel, Laurin      --> name = Hummel, Laurin (Leerzeichen erlaubt) <br>
PostCreate: http://fi1.bot-hosting.net:5260/create                          --> name = not set


GetHistory: http://fi1.bot-hosting.net:5260/history <br>
GetHistory: http://fi1.bot-hosting.net:5260/history/validationID

API-Key wird über den header übertragen: <br>
Authentication: KEY
