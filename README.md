Simple Http4s + Doobie demo service

run the service with `sbt run` command and then hit the below curls

POST request:
```
curl --header "Content-Type: application/json" \

--request POST \

--data '{"name":"John"}' \

http://localhost:8080/customer
```

GET request :
```
curl -i http://localhost:8080/customer/1 
```

