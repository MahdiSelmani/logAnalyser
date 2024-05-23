LOG ANALYSER

PREREQUISITES
Before you begin, make sure you have the following installed on your system:

Java Development Kit (JDK) 8 or later\n
Scala Build Tool (SBT)\n
Apache Kafka\n
Apache Spark\n

SETUP
Clone the project repository:\n
git clone https://github.com/MahdiSelmani/logAnalyser.git\n

Before running the Log analyser project, make sure the kafka and zookeeper are running. Set up the url to localhost:9092, the topic to 'log'.\n
Navigate to the project directory:\n
cd logAnalyser\n

Run SBT:\n
sbt\n

RUNNING THE CONSUMER
Switch to the consumer project:\n
project consumer\n

Compile the project:\n
compile\n

Run the Consumer module :\n
run\n

Or you can build a jar file, to be used with the spark-submit command:\n
package\n

SAME THING FOR THE PRODUCER
Switch to the producer project:\n
project producer\n

Compile the project:\n
producer\n

Run the producer module :\n
run\n
