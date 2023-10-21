# SaS-RESTful-API


GetUsers: http://fi1.bot-hosting.net:5260/users

GetUser: http://fi1.bot-hosting.net:5260/users/validationID

PatchTransaction: http://fi1.bot-hosting.net:5260/transaction <br>
```
{
    "validation_active": "Vt7MOgWGWm",   //Unternehmen/Finanzamt
    "validation_passive": "rVUoPYJ0pS",  //Person/Unternehmen
    "money": 24                          // x > 0  -->  Geld wird an active gezahlt
}
```

name ist optional <br>
PostCreate: http://fi1.bot-hosting.net:5260/create?name=Hummel, Laurin      --> name = Hummel, Laurin <br>
PostCreate: http://fi1.bot-hosting.net:5260/create                          --> name = null


GetHistory: http://fi1.bot-hosting.net:5260/history <br>
GetHistory: http://fi1.bot-hosting.net:5260/history/validationID

PatchEmployee: http://fi1.bot-hosting.net:5260/employee/
```
{
	"enterprise": "AOH2G4GDWi",   //Dem Unternehmen wird ein Mitarbeiter zugeordnet
	"employee": "YMfybTEPrH"      //Arbeitet der Mitarbeiter bereits bei diesem Unternehmen, wird er entlassen
}
```

GetEnterprise: http://fi1.bot-hosting.net:5260/enterprise/ --> Alle Unternehmen mit ihren Mitarbeitern
GetEnterprise: http://fi1.bot-hosting.net:5260/enterprise/userValID --> Alle Unternehmen, die diese Person beschäftigen
GetEnterprise: http://fi1.bot-hosting.net:5260/enterprise/enterpriseValID --> Spezielles Unternehmen mit ihren Mitarbeitern

API-Key wird über den header übertragen: <br>
Authentication: KEY
