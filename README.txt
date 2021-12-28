
This project creates a scalable, distributed system supporting a fictitious skier data tracking app.
API defined here: https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.15#/SeasonsList

The pieces of this project are intended to be hosted on AWS, therefore this is the code that either runs on the instances in the cloud or interacts with the system there.

Terms to know:
Simple Queueing Service (SQS): A distributed queue offered by AWS
Elastic Compute Cloud (EC2): Scalable compute resources hosted on AWS
Application Load Balancer: A switch that is configured to send incoming data to multiple other AWS technologies
Data Access Object (DAO): A class that models the data stored in the database for a particular entity

CloudServices Packages and Classes:


There are currently two modules in Cloud Services: Server and ReceivingProgram.
	
•	SharedUtilities Classes:Both the Server and the ReceivingProgram utilize a package called SharedLibrary
	o	Abstract Record: Abstract that provides a standard set of functionalities for any record being stored in the DynamoDB backend
	o	AbstractSqsInteractor: Abstract class that all implementations of SQS clients extend
			SQS clients are used to interface with an SQS queue detected from the context of the EC2 instance the code is being run on
	o	ContentValidationUtility: A variety of methods to validate requests and data content
	o	DatabaseInteractor: Parent class to extend for any class intended to interact with a DynamoDB table
	o	ErrorBuilder: Simple error building class
			Created for future standardization of errors and logging but has little current functionality
	o	InterfaceSkierDataObject: Contains required methods for all DAOs, currently isValid() is the only requirement
	o	LiftRide: Models a Lift Ride data object
	o	RecordCreationUtility: A variety of methods to standardize creation of records to be sent to the database
	o	ResortLite: A pared down version of a resort record intended for certain read scenarios wherein most of a resort's info isn't required by the requestor
	o	ResortRecord: DAO for a resort's data
	o	Season: DAO for a season's data	
	o	SkierRecord: DAO for a skier's data



•	Server: Contains the servlets and supporting utility classes to validate requests according to the Swagger doc and then send them onwards to an SQS queue
	Utilizing four EC2 micro instances, can handle ~99k HTTP requests per minute (~1650 per second)before becoming a bottleneck
	o	DatabaseReader: Extends DatabaseInteractor and supports all read operations from the database used for GET requests
	o	HealthServlet: Returns success to any POST or GET request
			Used for health checks from the AWS Application Load Balancer
	o	ReadWriteUtility: Contains all methods pertaining to reading from and writing to the client
	o	ResortGetUniqueSkiers: Contains all methods pertaining to reading from and writing to the client
	o	ResortsServlet: Logic for handling Resorts POST and GET requests
	o	SkiersServlet: Logic for handling Skiers POST and GET requests
	o	SqsSend: Extends AbstractSqsInteractor to provide all logic for sending a client request to an SQS queue and receiving confirmation of receipt in return 
			Implemented using a singleton design pattern as queue connections are high-cost
	o	StatusCodes: Static classes to provide HTTP response codes for responding to client requests


•	ReceivingProgram: Contains the code to be run on EC2 instances that reads from an SQS queue and writes to a DynamoDB database
	Utilizing one EC2 micro instance, can pull and delete from queue as well as write to the database ~70k messages per minute

	o	DatabaseWriter: Extends DatabaseInteractor and supports all write operations to the database used for POST requests
	o	MessageProcessor: Implements Runnable to be used by a thread as the main logic for processing messages pulled from the SQS queue
			Handles batches of messages of any types
	o	MessageStorage: Short-term local cache that ensures no duplicate messages are written to the database
	o	ReceiverMain: Contains the main function which polls the SQS queue identified from the context of the instance it is being run on and instantiates threads for each batch of messages returned
	o	SqsDelete: Extends SqsInteractor to delete messages from the queue after they've been successfully written to the database
	o	SqsReceive: Logic for handling Skiers POST and GET requests
	o	ThreadPool: Class to implement a fixed thread pool for MessageProcessor instances to be run on

Server Design:

1.	When the servers start up, they receive regular health checks into the HealthServlet from the AWS load balancer's "Target Group" virtual grouping mechanism.

2.	All client requests are submitted to an AWS Application Load Balancer (ALB)
	a.	An Application rather than the Network Load Balancer (NLB) was utilized for two reasons: connection stability and lack of performance differentiation requirement.
	b.	Connection Stability:
		i.	An ALB is able to handle generic HTTP requests across a variety of connection types rather than NLB's stricter TCP or UDP connection requirements
			1.	UDP is not guaranteed to deliver the message
			2.	TCP connections are forcibly reset by the NLB after 10k requests by design, this is not configurable so catching the reset HTTP client connections and remaking them were unnecessary stresses on the load-testing client side
	d.	Performance Differentiation:
		i.	While the NLB is more performant than the ALB in terms of throughput, the limitations imposed on EC2 instances rendered the minor gains irrelevant and possibly detrimental as the traffic would be pushed through to the servers that much faster potentially resulting in longer tomcat queues.

3.	The ALB splits the requests via a round-robin policy between server instances

4.	A Server receives the request into the servlet classes via Tomcat

5.	The respective servlet will first utilize the relevant ContentValidationUtility methods to validate the client's request
		
6.	Dependent on the validity of the request, the servlet will call the appropriate response method in the ReadWriteUtility class

7.	The Server will do one of two actions depending on a POST vs GET request: 
	a. For a successful POST requests it will use the ReadWriteUtility and SqsSend classes to send the request to the SQS queue
		i.	Using SQS was necessary with the lack of instances to host alternate queueing technologies
		ii.	However, even without the constraints SQS would likely be chosen due to its implementation simplicity and reliability
		iii.	SQS stores messages across multiple distributed caches enabling asynchronous data storage resulting in lower latencies for clients.
	b. For a successful GET request the server will use the ReadWriteUtility and DatabaseReader to query the DynamoDB database directly

8.	Once a successful receipt response is returned from the queue or the database, the ReadWriteUtility is able to send a successful response to the client

Consumer Design:

1.	The consumer takes messages from the queue in batches of ten (max allowed by SQS) using long polling
	a.	Long polling waits for messages to be available or for a timeout (20 seconds in this implementation) before returning
	b.	Short polling immediately returns regardless of the number of messages in the queue making it untenable from a cost perspective as well as unnecessary from a design perspective for an async storage system

2.	The consumer keeps an internal cache to prevent duplicates, stores the data by writing it to a file, then sends a delete request to the queue
	a.	The consumer uses a bounded queue for its fixed thread pool to prevent unnecessary backup of batch receives.
	b.	The "internal cache" is a concurrent HashMap and queue system that ensures no more than the given number of messages will be received at the same time.
	c.	The cache is set to the max possible count of messages that can be held in the ThreadPool's  bounded queue, which for the included testing was 500 (bounded queue max was 50 Message lists since each list has at most 10 messages).
	d.	This works because SQS will make a make a message "Not Visible" for a configurable period of time once it has served it via a receive request, so the only duplicates happen right away coming from receive requests finding a distributed copy of a message that hasn't made it to the "Not Visible" state yet

Database Overview:
DynamoDB was chosen for its ability to scale and read quickly as well as integration with other AWS technologies
This system utilizes a single DynamoDB table for data storage.
	- It uses a skierID as the partition key and a timestamp as the sort key, to guarantee uniqueness for the total composite key.
	- It has a global index with resortID as the primary key and again a timestamp as the sort keyto enable effecient searching by either skier or resort IDs
	- It has two local indices for the skierID partition key:
		- The first with resortID as the sort key and includes the seasonID, timeID, dayID, and lift ID fields
		- The second with the seasonID as the sort key to then include day, time, and lift IDs. The intent was to cause bucketization of the data so that it was easer to find a given skiers results for a particular resort or particular season respectively.

