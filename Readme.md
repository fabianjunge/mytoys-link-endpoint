# mytoys link endpoint

This application runs on [http://localhost:8080](http://localhost:8080).  
With the environment variable `SPRING_APPLICATION_JSON` it is possible to set a different port. See Section "Run with docker".

## Notes

I tried Kotlin for the first time and started programming the API client. After I fetched and parsed all entries with khttp/Gson, 
I extracted all links of the tree-ish structure into a list and added corresponding parent information to the label.
Next I implemented the sort and parent-filter function.

In retrospection a lot of time was used to get into Kotlin, but it was very interesting!
The code has definitely some points where I don't use Kotlin language features. 

## Run endpoint with docker

```
docker build -t mytoys-link-endpoint-fj .

docker run -d --name mytoys-link-endpoint \
--env SPRING_APPLICATION_JSON='{"server.port":8080}' \
--publish 8080:8080 \
mytoys-link-endpoint-fj
```

## Run endpoint local without docker

```
gradlew bootRun
```

## Requirements

* ✅  Implement in Kotlin
* ✅ Docker
* ✅ HTTP status codes
* (✅) [Swagger (only added to the project)](http://localhost:8080/swagger-ui.html)
* ✅ Test (format, sorting)
* ✅ Notes
* ✅ Readme
* ✅ Frameworks and Libraries
    * Spring boot, web
    * khttp
