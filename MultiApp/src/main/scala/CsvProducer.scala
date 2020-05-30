package droneCop

import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.log4j.{Level, Logger}

import droneCop.Utils.JsonUtils

import droneCop.Managers.ConsumerManager
import droneCop.Managers.ProducerManager
import droneCop.Drone.{DroneJson, Location, DroneViolationJson}

object CsvProducer extends App {

  val pathToFile =
    "/mnt/c/Users/XPS15/OneDrive - EPITA/Documents/Cours/Epita - Scia/Cours/Scala/Projet/Data/Parking_Violations_Issued_-_Fiscal_Year_2016.csv"

  val conf = new SparkConf()
    .setAppName("CsvProducer")
    .setMaster(
      "local[*]"
    )

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val jsonUtils = new JsonUtils()

  val ss = SparkSession
    .builder()
    .config(conf)
    .getOrCreate()

  val df = ss.read
    .format("csv")
    .option("sep", ",")
    .option("inferSchema", "true")
    .option("header", "true")
    .load(pathToFile)

  val producerManager = new ProducerManager("DroneStream")

  val fakeDrone = df.select(
    "Latitude",
    "Longitude",
    "Violation Code",
    "Registration State",
    "Plate ID",
    "Plate Type",
    "Vehicle Color",
    "Vehicle Year",
    "Vehicle Make",
    "Vehicle Body Type",
    "Vehicle Expiration Date",
    "Summons Number",
    "Issue Date"
  )

  fakeDrone.foreach { row =>
    val position = Location(row(0).asInstanceOf[Int], row(1).asInstanceOf[Int])
    val alt = 1.0
    val speed = 1.0
    val temperature = 1.0
    val battery = 1.0
    val drone = DroneViolationJson(
    0,//row(11).asInstanceOf[Int],
    0,
    0, //row(12).asInstanceOf[Long],
    alt, //random environ 4-5
    position,
    speed, //random speed
    temperature, //random temperature
    battery, //random entre 0 et 100
    row(2).asInstanceOf[Int],
    row(3).toString(),
    row(4).toString(),
    row(5).toString(),
    row(6).toString(),
    row(7).toString(),
    row(8).toString(),
    row(9).toString(),
    row(10).toString()
    )
    println(drone)
    producerManager.send(jsonUtils.drone2Json(drone))
    Thread.sleep(500)
  }


  producerManager.close()
}
