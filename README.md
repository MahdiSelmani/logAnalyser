##LOG ANALYSIS IN REAL TIME\n
#Project built using Apache spark, Kafka, Scala PL\n

#PREREQUISITES\n
Before you begin, make sure you have the following installed on your system:\n

Java Development Kit (JDK) 8 or later\n
Scala Build Tool (SBT)\n
Apache Kafka\n
Apache Spark\n

SETUP\n
#Clone the project repository:\n
git clone https://github.com/MahdiSelmani/logAnalyser.git\n

#Before running the Log analyser project, make sure the kafka and zookeeper are running. Set up the url to localhost:9092, the topic to 'log'.\n
Navigate to the project directory:\n
>cd logAnalyser\n

#Run SBT:\n
>sbt\n

#RUNNING THE CONSUMER\n
1-Switch to the consumer project:\n
sbt>project consumer\n

2-Compile the project:\n
sbt>compile\n

3-Run the Consumer module :\n
sbt>run\n

Or you can build a jar file, to be used with the spark-submit command:\n
sbt>package\n

SAME THING FOR THE PRODUCER\n
1-Switch to the producer project:\n
sbt>project producer\n

2-Compile the project:\n
sbt>producer\n

3-Run the producer module :\n
sbt>run\n
