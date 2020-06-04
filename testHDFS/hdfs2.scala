//import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import org.json4s.jackson.JsonMethods._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger._

object stream {
case class Location(lat: Double, long: Double)
case class DroneJson(
    droneId: Int,
    alert: Int,
    timestamp: Long,
    //position
    altitude: Double,
    lat: Double,
    long: Double,
    //drone
    speed: Double,
    temperature: Double,
    battery: Double,
    //violation
    violationCode: Int
)

def json2Drone(jsonStr: String): DroneJson = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).camelizeKeys.extract[DroneJson]
  }


val conf = new SparkConf()
    .setAppName("streams")
    .setMaster(
      "local[1]"
    )
    

val spark = SparkSession
        .builder()
        .config(conf)
        .getOrCreate()

import spark.implicits._


val stringify = udf((vs: Seq[String]) => vs match {
  case null => null
  case _    => s"""[${vs.mkString(",")}]"""
})

// df.withColumn("json", stringify($"json")).write.csv(...)

def main(args: Array[String]): Unit = {

    val schemaforfile = new StructType()
            .add("drone_id","integer")
            .add("timestamp","string")
            .add("battery", "double")
            .add("altitude","double")
            .add("temperature", "double")
            .add("speed", "double")
            .add("alert","integer")
            .add("lat","double")
            .add("long", "double")
            //.add("violationCode", "integer")

    val sdfToHdfs = spark.readStream.format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "DroneStream")
                .load()
                .select(from_json(col("value").cast("string"), schemaforfile).alias("tmp"))
                .select("tmp.*")

    sdfToHdfs.writeStream.outputMode("append")
        .format("parquet")
        .option("header", true)
        .option("path", "hdfs://localhost:9000/Drones/Messages")
        .option("checkpointLocation", "hdfs://localhost:9000/tmp/Messages_checkpoints")
        .partitionBy("window")
        .option("truncate", False) 
        .trigger(ProcessingTime("11 seconds"))
        .start().awaitTermination()

    }
}