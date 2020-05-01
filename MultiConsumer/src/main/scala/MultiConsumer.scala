package droneCop

import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConverters._
import java.util
import java.util.Properties
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.concurrent.{Await, Future, Promise}
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import droneCop.Managers.ConsumerManager
import droneCop.Managers.ProducerManager
import droneCop.Utils.jsonUtils

class AlertThread extends Runnable {
  override def run() {
    val alertTopic = "AlertStream"
    val alertConsumer = ConsumerManager(alertTopic)
    val jsonUtils = new jsonUtils()
    while (true) {
      val records = alertConsumer.consumer.poll(500)
      records.asScala.foreach { drone =>
        val md = jsonUtils.jsonStrToMap(drone.value())
        println("Alert Stream Received :", drone.value())
      }
    }
    alertConsumer.consumer.close()
  }
}

class AllThread extends Runnable {
  override def run() {
    val allTopic = "AllStream"
    val allConsumer = ConsumerManager(allTopic)
    val jsonUtils = new jsonUtils()
    while (true) {
      val records = allConsumer.consumer.poll(500)
      records.asScala.foreach { drone =>
        val md = jsonUtils.jsonStrToMap(drone.value())
        println("All Stream Received :", drone.value())
      }
    }
    allConsumer.consumer.close()
  }
}

class DroneThread extends Runnable {
  override def run() {
    val droneTopic = "DroneAlert"
    val droneConsumer = ConsumerManager(droneTopic)
    val jsonUtils = new jsonUtils()
    val alertProd = ProducerManager("AlertStream")
    val storageProd = ProducerManager("AllStream")
    while (true) {
      val records = droneConsumer.consumer.poll(500)
      records.asScala.foreach { drone =>
        val md = jsonUtils.jsonStrToMap(drone.value())
        println("New message received from drone number " + md("Alert"))
        if (md("Alert") == 1) {
          println("> " + md("Alert") + ": This is an alert!")
          alertProd.send("key", drone.value())
          storageProd.send("key", drone.value())
        } else {
          println("> " + md("Alert") + ": Normal message")
          println("> " + md("Alert") + ": Sent to storage")
          storageProd.send("key", drone.value())
        }
      }
    }
    droneConsumer.consumer.close()
  }
}

object MultiConsumer extends App {

  val sparkConf = new SparkConf()
    .setAppName("Spark")
    .setMaster("local[*]")

  val sparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

  var alertThread = new Thread(new AlertThread())
  var droneThread = new Thread(new DroneThread())
  var allThread = new Thread(new AllThread())
  alertThread.start()
  droneThread.start()
  allThread.start()

}
