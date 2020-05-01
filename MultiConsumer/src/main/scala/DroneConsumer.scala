package droneCop

import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConverters._
import java.util
import java.util.Properties
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.json4s.jackson.JsonMethods._
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.concurrent.{Await, Future, Promise}
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import droneCop.Managers.ConsumerManager
import droneCop.Managers.ProducerManager

object ConsumerDroneStream extends App {
  val TOPIC = "DroneStream"
  val mainConsumer = ConsumerManager(TOPIC)

  val sparkConf = new SparkConf()
    .setAppName("Spark")
    .setMaster("local[*]")

  val sparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

  def jsonStrToMap(jsonStr: String): Map[String, Any] = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).extract[Map[String, Any]]
  }

  val alertProd = ProducerManager("AlertStream")
  val storageProd = ProducerManager("AllStream")

  while (true) {
    val records = mainConsumer.consumer.poll(500)
    records.asScala.foreach { drone =>
      val md = jsonStrToMap(drone.value())
      println("New message received from drone number " + md("DroneId"))
      if (md("Alert") == 1) {
        println("> " + md("DroneId") + ": This is an alert!")
        alertProd.send("key", drone.value())
        storageProd.send("key", drone.value())
      } else {
        println("> " + md("DroneId") + ": Normal message")
        println("> " + md("DroneId") + ": Sent to storage")
        storageProd.send("key", drone.value())
      }
    }
  }
  mainConsumer.consumer.close()

}
