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

  

  override def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Please provide the CSV file's path.")
      System.exit(1)
    }

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    val pathToFile = args(0)
    val jsonUtils = new JsonUtils()

    val conf = new SparkConf()
          .setAppName("CsvProducer")
          .setMaster(
             "local[*]"
          )

    val ss = SparkSession
        .builder()
        .config(conf)
        .getOrCreate()


    println("[info] Reading " + pathToFile + "...")

    val df = ss.read
      .format("csv")
      .option("sep", ",")
      .option("inferSchema", "true")
      .option("header", "true")
      .load(pathToFile)

    val fakeDrone = df.select(
          "Violation Code",             //  0
          "Registration State",         //  1
          "Plate ID",                   //  2
          "Plate Type",                 //  3
          "Vehicle Color",              //  4
          "Vehicle Make",               //  5
          "Vehicle Body Type",          //  6
          "Vehicle Year"                //  7
      )   

    fakeDrone.foreach { row =>
      val drone = DroneViolationJson(
      -1,                           // drone_id
      0,                           // alert
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
      println(drone.toJsonString())
      ProducerManager.send("DroneStream", drone.toJsonString())
      Thread.sleep(500)
    }

      ProducerManager.close()

  }
}
