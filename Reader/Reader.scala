package com.tp.spark.core
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.log4j.{Level, Logger}

object Reader extends App {



    val pathToFile = "hdfs://localhost:9000/Drones/Messages"

	val conf = new SparkConf()
                        .setAppName("Wordcount")
                        .setMaster("local[*]") // here local mode. And * means you will use as much as you have cores.
    
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    val ss = SparkSession.builder()
        .config(conf)
        .getOrCreate()

    val df = ss.read.format("csv")
                    .option("sep", ",")
                    .option("inferSchema", "true")
                    .option("header", "true")
                    .load(pathToFile)

    //println(df.groupBy("Plate ID").count().orderBy(desc("count")).show(2))
}