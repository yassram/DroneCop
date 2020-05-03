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
import droneCop.Drone.DroneJson

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

  val alertProd = ProducerManager("AlertStream")
  val storageProd = ProducerManager("AllStream")

  while (true) {
    val records = mainConsumer.consumer.poll(100)
    records.asScala.foreach { d =>
      val drone : DroneJson = jsonUtils.json2Drone(d.value())
      println("New message received from drone number " + drone.droneId)
      if (drone.battery == 1) {
        println("> " + drone.droneId + ": This is an alert!")
        print("> " + drone.droneId + ": Sendind to alert...")
        alertProd.send(d.value())
        println("> " + drone.droneId + ": Sendind to storage...")
        storageProd.send(d.value())
        println("---")
      } else {
        println("> " + drone.droneId + ": Normal message")
        println("> " + drone.droneId + ": Sendind to storage...")
        storageProd.send(d.value())
        println("---")
      }
    }
  }
  mainConsumer.consumer.close()

}

