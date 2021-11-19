
THIS PROJECT IS CURRENTLY UNDER CONSTRUCTION AND DOES NOT REPRESENT THE FINAL PRODUCT.

Many pieces of this project are hosted on AWS this is the code that either runs on the instances in the cloud or interacts with the system there.

Terms to know:
Simple Queueing Service (SQS): A distributed queue offered by AWS
Elastic Compute Cloud (EC2): Scalable compute resources hosted on AWS
Application Load Balancer: A switch that is configured to send incoming data to multiple other AWS technologies

CloudServices Packages and Classes:


There are currently two modules in Cloud Services: Server and ReceivingProgram.
	
•	SharedLibrary Classes:Both the Server and the ReceivingProgram utilize a package called SharedLibrary
	o	AbstractSqsInteractor: Abstract class that all implementations of SQS clients extend
			SQS clients are used to interface with a given SQS queue
	o	InterfaceSkierDataObject: Contains required methods for all DAOs, currently isValid() is the only requirement
	o	DataAccessObjects: Multiple DAO classes for various data entries to facilitate encoding and decoding of json via Gson
•	Server: Contains the servlets and supporting utility classes to validate requests according to the below Swagger doc and then send them onwards to an SQS queue
			(https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.0.2#/SeasonsList) 
	o	ResortsServlet: Logic for handling Resorts POST and GET requests
	o	SkiersServlet: Logic for handling Skiers POST and GET requests
	o	HealthServlet: Returns success to any POST or GET request
			Used for health checks from the AWS Target Group virtual grouping mechanism the load balancer is connected to
	o	ContentValidationUtility: A variety of methods to validate requests and their content
	o	ErrorBuilder: Simple error building class
			Created for future standardization of errors and logging but has little current functionality
	o	ReadWriteUtility: Contains all methods pertaining to reading from and writing to the client
	o	SqsSend: Extends AbstractSqsInteractor to provide all logic for sending a client request to an SQS queue and receiving confirmation of receipt in return 
			Implemented using a singleton design pattern as queue connections are high-cost
	o	StatusCodes: Static classes to provide HTTP response codes for responding to client requests
•	ReceivingProgram: TODO 

System Design:

1.	When the servers start up, they receive regular health checks into the HealthServlet from the AWS load balancer's "Target Group" virtual grouping mechanism.
2.	All client requests are submitted to an AWS Application Load Balancer (ALB)
	a.	The client from A1 can now use a capped exponential backoff but does not have to
		i.	This is covered in greater depth in the data analysis section
	b.	I used the Application rather than the Network Load Balancer (NLB) for two reasons connection stability and lack of performance differentiation requirement.
	c.	Connection Stability:
		i.	An ALB is able to handle generic HTTP requests across a variety of connection types rather than NLB's stricter TCP or UDP connection requirements
			1.	UDP is not guaranteed to deliver the message
			2.	TCP connections are forcibly reset by the NLB after 10k requests by design, this is not configurable so catching the reset HTTPclient connections and remaking them were unnecessary stresses on the client side
	d.	Performance Differentiation:
		i.	While the NLB is more performant than the ALB in terms of throughput, the limitations on EC2 instances rendered the minor gains irrelevant and possibly detrimental as the traffic would be pushed through to the servers that much faster potentially resulting in longer tomcat queues.
3.	The ALB splits the requests via a round-robin policy between two server instances
	a.	As mentioned above, the AWS Educate account used for these tests is not allowed to have more than three total EC2 instances
4.	A Server receives the request into either SkiersServlet or ResortsServlet via Tomcat
5.	The respective servlet will first utilize the relevant ContentValidationUtility methods to validate the client's request		
6.	Dependent on the validity of the request, the servlet will call the appropriate response method in the ReadWriteUtility class
7.	The ReadWriteUtility class, for successful requests will use the SqsSend class to send the data to the SQS queue
	a.	Using SQS was necessary with the lack of instances to host alternate queueing technologies
	b.	However, even without the constraints I would likely choose SQS due to its implementation simplicity and reliability
8.	Once a successful receipt response is returned from the queue the ReadWriteUtility is able to send its own successful response to the client
	a.	SQS stores messages across multiple distributed caches and doesn't remove them until they're deleted by the consumer
		i.	This enables asynchronous data storage resulting in lower latencies for clients.
	b.	There are other AWS solutions for two-way communication between server and consumer that I will implement in the future since SQS won't work as well for GET requests.
9.	The consumer takes messages from the queue in batches of ten (max allowed by SQS) using long polling
	a.	Long polling waits for messages to be available or for a timeout (20 seconds in this implementation) before returning
	b.	Short polling immediately returns regardless of the number of messages in the queue making it untenable from a cost perspective as well as unnecessary from a design perspective for an async storage system
10.	The consumer keeps an internal cache to prevent duplicates, stores the data by writing it to a file, then sends a delete request to the queue
	a.	The consumer uses a bounded queue for its fixed thread pool to prevent unnecessary backup of batch receives.
	b.	The "internal cache" is a concurrent HashMap and queue system that ensures no more than the given number of messages will be received at the same time.
	c.	The cache is set to the max possible count of messages that can be held in the ThreadPool's  bounded queue, which for the included testing was 500 (bounded queue max was 50 Message lists since each list has at most 10 messages).
	d.	This works because SQS will make a make a message "Not Visible" for a configurable period of time once it has served it via a receive request, so the only duplicates happen right away coming from receive requests finding a distributed copy of a message that hasn't made it to the "Not Visible" state yet

