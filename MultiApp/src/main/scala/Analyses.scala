package droneCop
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
    
    val countItems = df.count()
    val countAlerts = df.filter("alert != 0").count()
    
    val topState = df.filter("violation_code != -1").groupBy("plateState").count().withColumnRenamed("count","Number Violations").sort(desc("Number Violations")).limit(10)
    val topDrone = df.filter("violation_code != -1 AND violation_code != 100").groupBy("drone_id").count().withColumnRenamed("count","Number Violations").sort(desc("Number Violations")).limit(1)
    println("////////////////////////////////////////////:")
    
    println("Number of items: " + countItems + '\n')
    println("Number of alerts: " + countAlerts + '\n')

    println("Top State(s):")
    topState.show(false)

    println("Top Drone(s):")
    topDrone.show(false)
}