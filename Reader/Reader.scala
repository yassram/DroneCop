package com.tp.spark.core
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.log4j.{Level, Logger}

object Reader extends App {



    val pathToFile = "hdfs://localhost:9000/Drones/Messages"

	val conf = new SparkConf()
                        .setAppName("Stats")
                        .setMaster("local[*]") // here local mode. And * means you will use as much as you have cores.
    
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    val ss = SparkSession.builder()
        .config(conf)
        .getOrCreate()

    val df = ss.read.format("parquet").load(pathToFile + "/*.parquet")
}