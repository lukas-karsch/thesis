To do the performance tests, I will dockerize the application. 
## Steps 
1. Make sure both applications can build with maven 
2. Dockerize both applications 
	1. Dockerfile 
	2. docker-compose.yml 
3. Install k6 
4. Write first test script 
## Problems 
### Maven / Build 
My apps are not building.
What I had to do to fix it:
- **Lombok**: add build plugin: https://projectlombok.org/setup/maven
- **Dependencies**: Had trouble with the "test-jar" dependency on `test-suite`. Had to add a `<goal>` to the `test-suite` pom.xml
