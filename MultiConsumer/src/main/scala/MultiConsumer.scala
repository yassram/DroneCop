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

class DroneThread extends Runnable {
  override def run() {}
}

object MultiConsumer extends App {

  val sparkConf = new SparkConf()
    .setAppName("Spark")
    .setMaster("local[*]")

  val sparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

  val jsonUtils = new jsonUtils()

  val droneTopic = "DroneAlert"
  val alertTopic = "AlertStream"
  val allTopic = "AllStream"

  val droneConsumer = ConsumerManager(droneTopic)
  val alertConsumer = ConsumerManager(alertTopic)
  val allConsumer = ConsumerManager(allTopic)

  val alertProd = ProducerManager("AlertStream")
  val storageProd = ProducerManager("AllStream")

  val alertFuture = Future[Unit] {
      val records = alertConsumer.consumer.poll(500)
      records.asScala.foreach { drone =>
        val md = jsonUtils.jsonStrToMap(drone.value())
        println("Alert Stream Received :", drone.value())
    }
  }

  val allFuture = Future[Unit] {
      val records = allConsumer.consumer.poll(500)
      records.asScala.foreach { drone =>
        val md = jsonUtils.jsonStrToMap(drone.value())
        println("New message received from the drone stream")
        println("> drone : " + md("DroneId") + " message's is now stored")
        println("---")
    }
  }

  val droneFuture = Future[Unit] {
      val records = droneConsumer.consumer.poll(500)
      records.asScala.foreach { drone =>
        val md = jsonUtils.jsonStrToMap(drone.value())
        println("New message received from drone number " + md("DroneId"))
        if (md("Alert") == 1) {
          println("> " + md("DroneId") + ": This is an alert!")
          print("> " + md("DroneId") + ": Sendind to alert...")
          alertProd.send("key", drone.value())
          println("> " + md("DroneId") + ": Sendind to storage...")
          storageProd.send("key", drone.value())
          println("---")
        } else {
          println("> " + md("DroneId") + ": Normal message")
          println("> " + md("DroneId") + ": Sendind to storage...")
          storageProd.send("key", drone.value())
          println("---")
        }
    }
  }

  val futures = Future.sequence(Seq[Future[Unit]](droneFuture))

  Await.ready(futures, Duration.Inf)

  alertConsumer.consumer.close()
  allConsumer.consumer.close()
  droneConsumer.consumer.close()

}
