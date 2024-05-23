package example
import java.io.RandomAccessFile
import java.net.{InetAddress, NetworkInterface}
import java.nio.file.{Paths, StandardWatchEventKinds, WatchService, WatchKey, WatchEvent}
import java.util.Properties
import scala.collection.JavaConverters._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Producer {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Usage: LogFileMonitor <log_file_path>")
      System.exit(1)
    }

    val logFilePath = args(0)
    println(s"Monitoring log file: $logFilePath")

    // Get hostname and IP address
    val inetAddresses = NetworkInterface.getNetworkInterfaces.asScala
      .filter(_.isUp)
      .flatMap(_.getInetAddresses.asScala)
      .filter(!_.isLoopbackAddress)
      .filter(_.getHostAddress.contains("."))
      .map(_.getHostAddress)
      .toList

    val ipAddress = if (inetAddresses.nonEmpty) inetAddresses.head else "Unknown"

    // Get hostname
    val hostname = InetAddress.getLocalHost.getHostName

    // Kafka Configuration
    val BROKER = "localhost:9092"
    val TOPIC = "log"

    // Kafka Producer Properties
    val props = new Properties()
    props.put("bootstrap.servers", BROKER)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    // Initialize Kafka Producer
    val producer = new KafkaProducer[String, String](props)

    // Function to send log message to Kafka
    def sendToKafka(message: String): Unit = {
      val logWithHostnameAndIP = s"$hostname | $ipAddress | $message"
      producer.send(new ProducerRecord[String, String](TOPIC, logWithHostnameAndIP))
      producer.flush()
    }

    // Function to tail the log file and send new lines to Kafka
    def tailLogFile(logFilePath: String): Unit = {
      val LOG_FILE_PATH = Paths.get(logFilePath)
      val raf = new RandomAccessFile(LOG_FILE_PATH.toFile, "r")
      var lastPointer = raf.length()

      while (true) {
        Thread.sleep(1000) // Polling interval
        val newPointer = raf.length()
        if (newPointer > lastPointer) {
          raf.seek(lastPointer)
          var line = raf.readLine()
          while (line != null) {
            sendToKafka(line.trim)
            line = raf.readLine()
          }
          lastPointer = newPointer
        }
      }
    }

    // Start tailing the log file
    tailLogFile(logFilePath)
  }
}
