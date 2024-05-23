import java.sql.{Connection, DriverManager, PreparedStatement, Timestamp, Date}
import java.util.Properties
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object Consumer {
  @volatile var prevTotalCount = 0
  @volatile var prevErrorCount = 0
  @volatile var prevUserCount = 0
  @volatile var prevDisconnectCount = 0

  def main(args: Array[String]): Unit = {
    // Spark Configuration
    val conf = new SparkConf().setAppName("LogAnalysis").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(3))
    Logger.getRootLogger.setLevel(Level.ERROR)

    // Kafka Parameters
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "my_group_id",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("log")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val logs = stream.map(record => record.value)

    logs.foreachRDD { rdd =>
      if (!rdd.isEmpty) {
        val logDetails = rdd.collect().map { log =>
          val Array(hostname, ipAddress, logMessage) = log.split("\\|")
          (hostname, ipAddress, logMessage, containsError(log), isUserConnected(log), isUserDisconnected(log))
        }

        // Batch Processing
        batchProcessLogs(logDetails)

        // Print summary
        val currentTime = java.time.LocalDateTime.now.toString
        val logCounts = logDetails.map { case (_, _, _, error, userConnected, userDisconnected) =>
          (1, if (error) 1 else 0, if (userConnected) 1 else 0, if (userDisconnected) 1 else 0)
        }.reduce((a, b) => (a._1 + b._1, a._2 + b._2, a._3 + b._3, a._4 + b._4))

        // Update previous counts and print the summary table
        prevTotalCount += logCounts._1
        prevErrorCount += logCounts._2
        prevUserCount += logCounts._3
        prevDisconnectCount += logCounts._4
        println("|----------------------------------------------------------------------------------------------------------|")
        println(s"|${align("Timestamp", 25)}|${align("Total analysed logs", 20)}|${align("Total error logs", 25)}|${align("Connections", 15)}|${align("Disconnections", 17)}|")
        println("|----------------------------------------------------------------------------------------------------------|")
        println(s"|${align(currentTime, 25)}|${align(prevTotalCount, 20)}|${align(prevErrorCount, 25)}|${align(prevUserCount, 15)}|${align(prevDisconnectCount, 17)}|")
        println("|----------------------------------------------------------------------------------------------------------|")
     
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }

  // Function to process logs in batch and save to MySQL
  def batchProcessLogs(logDetails: Array[(String, String, String, Boolean, Boolean, Boolean)]): Unit = {
    val url = "jdbc:mysql://localhost:3306/log"
    val username = "root"
    val password = "The13001987-"
    var connection: Connection = null

    try {
      connection = DriverManager.getConnection(url, username, password)

      logDetails.foreach { case (hostname, ipAddress, logMessage, isError, isUserConnected, isUserDisconnected) =>
        if (isError) {
          saveToTable(connection, "errors", hostname, ipAddress, logMessage)
          println("\u001b[31mNew Error detected: \u001b[0m" + logMessage) // Red color for errors
          println("Source: " + hostname+" | "+ipAddress)
        }
        if (isUserConnected) {
          saveToTable(connection, "connection", hostname, ipAddress, logMessage)
          incrementConnectionCount(connection, ipAddress)
          println("\u001b[32mNew Connection detected: \u001b[0m" + logMessage) // Green color for connections
          println("Source: " + hostname+" | "+ipAddress)
        }
        if (isUserDisconnected) {
          saveToTable(connection, "disconnection", hostname, ipAddress, logMessage)
          decrementConnectionCount(connection, ipAddress)
          println("\u001b[36mNew disconnection detected: \u001b[0m" + logMessage)
          println("Source: " + hostname+" | "+ipAddress) 
        }
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (connection != null) connection.close()
    }
  }

  // Function to check if log contains an error pattern
  def containsError(log: String): Boolean = {
    val errorPatterns = List(
      "error", "failed", "exception", "fatal", "segfault",
      "authentication failure", "intrusion detected", "buffer overflow",
      "security breach", "access denied", "SQL injection attempt",
      "brute force attack", "virus detected", "malware infection",
      "unauthorized access", "certificate expired", "data breach",
      "denial of service", "permission denied", "invalid credentials",
      "server overload"
      // Add more patterns as needed
    )
    errorPatterns.exists(pattern => log.toLowerCase.contains(pattern))
  }

  // Function to check if log indicates a new user
  def isUserConnected(log: String): Boolean = {
    // Add patterns to detect new user events
    val newUserPatterns = List(
      "new session", "user logged in", "session opened", "login successful",
      "new user session", "session started", "authentication successful",
      "remote login", "user login", "successful login attempt",
      "session established", "client connected"
      // Add more patterns as needed
    )
    newUserPatterns.exists(pattern => log.toLowerCase.contains(pattern))
  }

  // Function to check if log indicates user disconnect
  def isUserDisconnected(log: String): Boolean = {
    // Add patterns to detect user disconnect events
    val disconnectPatterns = List(
      "disconnected", "disconnect", "session closed", "logout",
      "session ended", "user disconnected", "disconnect request",
      "logout successful", "session terminated", "connection closed",
      "client disconnected", "remote session ended", "session timeout",
      "connection terminated"
      // Add more patterns as needed
    )
    disconnectPatterns.exists(pattern => log.toLowerCase.contains(pattern))
  }

  // Function to align content within the table
  def align(content: Any, width: Int): String = {
    val str = content.toString
    if (str.length >= width) str
    else " " * (width - str.length) + str
  }

  // Function to save log to MySQL table with current timestamp
  def saveToTable(connection: Connection, tableName: String, hostname: String, ipAddress: String, logMessage: String): Unit = {
    val query = s"INSERT INTO $tableName (timestamp, hostname, ip_address, log, date) VALUES (?, ?, ?, ?, ?)"
    val preparedStatement = connection.prepareStatement(query)
    preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()))
    preparedStatement.setString(2, hostname)
    preparedStatement.setString(3, ipAddress)
    preparedStatement.setString(4, logMessage)
    preparedStatement.setDate(5, new Date(System.currentTimeMillis()))
    preparedStatement.executeUpdate()
    preparedStatement.close()
  }

  // Function to increment the current connection count for a given host
  def incrementConnectionCount(connection: Connection, ipAddress: String): Unit = {
    val query = "INSERT INTO host (ip, currentConnection) VALUES (?, 1) ON DUPLICATE KEY UPDATE currentConnection = currentConnection + 1"
    val preparedStatement = connection.prepareStatement(query)
    preparedStatement.setString(1, ipAddress)
    preparedStatement.executeUpdate()
    preparedStatement.close()
  }

  // Function to decrement the current connection count for a given host
  def decrementConnectionCount(connection: Connection, ipAddress: String): Unit = {
    val query = "UPDATE host SET currentConnection = currentConnection - 1 WHERE ip = ? AND currentConnection > 0"
    val preparedStatement = connection.prepareStatement(query)
    preparedStatement.setString(1, ipAddress)
    preparedStatement.executeUpdate()
    preparedStatement.close()
  }
}
