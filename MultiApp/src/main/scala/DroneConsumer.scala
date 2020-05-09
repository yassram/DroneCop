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

object DroneConsumer extends App {

  val TOPIC = "DroneStream"

  val consumerManager = new ConsumerManager(TOPIC)

  val jsonUtils = new jsonUtils()

  val alertProd = ProducerManager("AlertStream")
  val storageProd = ProducerManager("AllStream")

  def msgFromDrone(droneId: Int, msg: String) {
    println("> " + droneId.toString() + ": " + msg)
  }

  while (true) {
    val records = consumerManager.poll(100)
    records.asScala.foreach { d =>
      val drone: DroneJson = jsonUtils.json2Drone(d.value())
      println("New message received from drone number " + drone.droneId)
      msgFromDrone(
        drone.droneId,
        "📍 - lat:" + drone.location.lat + ", long:" + drone.location.long
      )
      if (drone.alert == 1) {
        msgFromDrone(drone.droneId, "Alert!!!")
        msgFromDrone(drone.droneId, "Alert redirected to alert stream...")
        alertProd.send(d.value())
        msgFromDrone(drone.droneId, "Alert redirected to storage stream...")
        storageProd.send(d.value())
        println("---")
      } else {
        msgFromDrone(drone.droneId, "Normal Message.")
        msgFromDrone(
          drone.droneId,
          "Normal message redirected to storage stream..."
        )
        storageProd.send(d.value())
        println("---")
      }
    }
  }

  consumerManager.close()
}
