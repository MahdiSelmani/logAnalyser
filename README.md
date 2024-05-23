LOG ANALYSIS IN REAL TIME
===========================
Project built using Apache Spark, Kafka, Scala Programming Language

PREREQUISITES
===========================
Before you begin, make sure you have the following installed on your system:

Java Development Kit (JDK) 8 or later
Scala Build Tool (SBT)
Apache Kafka
Apache Spark
SETUP

Clone the project repository:

```
git clone https://github.com/MahdiSelmani/logAnalyser.git
```

Before running the Log Analyzer project, make sure Kafka and Zookeeper are running. Set up the URL to localhost:9092 and the topic to 'log'. Navigate to the project directory:
```
cd logAnalyser
```
Running the project : 
===========================

Run SBT:
```
sbt
```

RUNNING THE CONSUMER

Switch to the consumer project:

```
project consumer

```
Compile the project:

```
compile

```

Run the Consumer module:

```
run

```

Alternatively, you can build a JAR file to be used with the spark-submit command:

```
package

```

SAME THING FOR THE PRODUCER

Switch to the producer project:

```
project producer

```

Compile the project:

```
compile

```

Run the producer module:

```
run

```
