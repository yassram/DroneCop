package droneCop

import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.log4j.{Level, Logger}

import droneCop.Utils.JsonUtils

import droneCop.Managers.ConsumerManager
import droneCop.Managers.ProducerManager
import droneCop.Drone.{DroneJson, DroneViolationJson}

object CsvProducer extends App {

  val pathToFile =
    "/Users/yassram/Desktop/Parking_Violations_Issued_-_Fiscal_Year_2016.csv"

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
    // "Latitude",                   //  0
    // "Longitude",                  //  1
    "Violation Code",             //  2
    "Registration State",         //  3
    "Plate ID",                   //  4
    "Plate Type",                 //  5
    "Vehicle Color",              //  6
    "Vehicle Make",               //  7
    "Vehicle Body Type",          //  8
    "Vehicle Year"                //  9
  )   

  fakeDrone.foreach { row =>
    val drone = DroneViolationJson(
    -1,                           // drone_id
    -1,                           // alert
    "-1",                         // timestamp
    -1,                           // altitude
    0,0,                          // lat, long
    -1,                           // speed
    -1,                           // temperature
    -1,                           // battery
    Option(row(0)).map(_.toString).getOrElse("-1").toInt,     // violation code
    Option(row(1)).map(_.toString).getOrElse(""),            // plate state
    Option(row(2)).map(_.toString).getOrElse(""),            // plate id
    Option(row(3)).map(_.toString).getOrElse(""),            // plate type
    Option(row(7)).map(_.toString).getOrElse(""),            // plate year
    Option(row(4)).map(_.toString).getOrElse(""),            // vehicule color
    Option(row(5)).map(_.toString).getOrElse(""),            // vehicule make
    Option(row(6)).map(_.toString).getOrElse("")             // vehicule body
    )
    println(drone)
    producerManager.send(jsonUtils.drone2Json(drone))
    Thread.sleep(500)
  }


  producerManager.close()
}
