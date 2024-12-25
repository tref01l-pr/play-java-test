# Play Framework + MongoDB Backend

## 🚀 Getting Started
### 1. Prerequisites
Ensure you have the following installed:
Java (JDK 11 or higher)
sbt (Scala Build Tool)
MongoDB (local or cloud instance)

### 2. Clone the Repository
```git clone https://github.com/tref01l-pr/play-java-test.git```
```cd to-do-rest-api```

### 3. Configure MongoDB
go to ./app/utils/Config.java and set your username and password. 
if you have local db, you should go to ./app/services/MongoDb.java. Change your host port if it's different

### 4. Run the Application
Start the Play application using sbt:
```sbt run```



## 🔬 Testing
You can check some tests in ./to-do-rest-api/test
run them using:
```sbt test```
