# emp-inventory
# Running project on local system
===================================
  
  1. Clone the repository - https://github.com/gdkhosla/emp-inventory.git
  2. Navigate inside project directory : emp-inventory
  3. Do maven build : mvn clean install
  4. Application jar would be created inside emp-inventory-app module. To run the application execute this command:
     java -jar emp-inventory-app/target/emp-inventory-app-0.0.1-SNAPSHOT.jar
     
 #Uploading emp data file
===================================

On postman :
1. Select request type POST 
2. URL - http://localhost:8080/api/employee?action=upload
3. Headers:
   MD5-Checksum (optional): (Calculated md5 checksum of employee data file. Running md5 command on system :  md5 <filepath>)
4. Go to body section-
    Select “form-data”
    Enter key value pair with key as “file”. 
    From value type dropdown, select “File” and then browse and select data file.
5. Send request.
6. Response would be in below format:
   {
    "taskId": "1",
    "queryUrl": "v1/task/1",
    "status": "INPROGRESS",
    "description": "Uploading employee data"
    }
    
 # Processing steps:
 ====================
   
   1. Uploade  file REST API accepts an optional header - "MD5-Checksum". If provided, the uploaded files integrity would be
      calculated against this checksum. If unmatched, operation will fail.
   2. A temp file is written on server. Task is created and response is sent back.
   3. Employee records are processed from temp file. Status is updated in Task table.
   4. After processing employee records, the file is uploaded to DropBox. A reference to uploaded file is stored in table - FileContent.
   5. Temp file is deleted.

# Accessing task status:
=======================================
   
   GET http://localhost:8080/v1/task/1
   
# Employee CRUD operations
=======================================
   
   GET all employees : http://localhost:8080/v1/employee
   
   GET employee by ID : http://localhost:8080/v1/employee/{id}
   
   POST : http://localhost:8080/v1/employee
         
         Request body : {"name":"test name", "age":25}
         
         Response:
            Status : 200 OK
            Header : "location" header containing the resource URI.
   
   PUT : http://localhost:8080/v1/employee/{empId}
         
         Request body : {"name":"test name", "age":25}
         
         Response:
            Status : 202 Accepted
   
   DELETE : http://localhost:8080/v1/employee/{empId}
          
          Response:
             Status : 200 OK