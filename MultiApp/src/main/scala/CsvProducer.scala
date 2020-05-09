package droneCop

import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

object CsvProducer extends App {

  val pathToFile =
    "/mnt/c/Users/XPS15/OneDrive - EPITA/Documents/Cours/Epita - Scia/Cours/Scala/Projet/Data/Parking_Violations_Issued_-_Fiscal_Year_2016.csv"

  val conf = new SparkConf()
    .setAppName("CsvProducer")
    .setMaster(
      "local[*]"
    )

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

println(df.groupBy("Plate ID").count().orderBy(desc("count")).show(10))
}
