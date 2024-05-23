Log Analyser
Prerequisites
Before you begin, make sure you have the following installed on your system:

Java Development Kit (JDK) 8 or later
Scala Build Tool (SBT)
Apache Kafka
Apache Spark

Setup
Clone the project repository:
git clone https://github.com/MahdiSelmani/logAnalyser.git

Before running the Log analyser project, make sure the kafka and zookeeper are running. Set up the url to localhost:9092, the topic to 'log'.
Navigate to the project directory:
cd logAnalyser

Run SBT:
sbt

Running the Consumer
Switch to the consumer project:
project consumer

Compile the project:
compile

Run the Consumer module :
run

Or you can build a jar file, to be used with the spark-submit command:
package

Same thing for the producer:
Switch to the producer project:
project producer

Compile the project:
producer

Run the producer module :
run
