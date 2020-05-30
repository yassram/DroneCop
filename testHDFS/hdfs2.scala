//import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import org.json4s.jackson.JsonMethods._
import org.apache.spark.sql.types.StructType


object stream {
case class Location(lat: Double, long: Double)
case class DroneJson(
    droneId: Int,
    alert: Int,
    timestamp: Long,
    //position
    altitude: Double,
    location: Location,
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
      "local[*]"
    )
    

val spark = SparkSession
        .builder()
        //.config("spark.executor.heartbeatInterval", "10000ms")
        //.config("spark.master", "local")
        .config(conf)
        .config("spark.sql.streaming.checkpointLocation", "/tmp/rayoutropbete")
        .getOrCreate()
import spark.implicits._

def main(args: Array[String]): Unit = {

    // spark
    //     .readStream
    //     .format("kafka")
    //     .option("kafka.bootstrap.servers", "localhost:9092")
    //     .option("subscribe", "DroneStream")
    //     //.option("startingOffsets", "earliest")
    //     .load()
    //     .selectExpr("CAST(value AS STRING)")
    //          .as[String]
    //     // .writeStream
    //     // .format("console")
    //     // .start()
    //     .writeStream
    //     .outputMode("append")
    //     .format("text")        // can be "orc", "json", "csv", etc.
    //     //.option("checkpointLocation", "./rayoutropbete")
    //     .option("path", "./rayoutropbete")
    //     .start()
        
    // spark.streams.awaitAnyTermination()
 

    // val kafkaStreamDF = spark.readStream.format("kafka")
    //             .option("sep", ",")
    //             .option("kafka.bootstrap.servers", "localhost:9092")
    //             .option("subscribe", "DroneStream")
    //             .option("startingOffsets", "earliest")
    //             .load()
    //             .selectExpr("CAST(value AS STRING)")
    //               .as[String]

    // val schemastruct = new StructType().add("lat","double").add("long", "double")
    // val schemaforfile = new StructType().add("droneId","integer")
    //         .add("alert","integer").add("timestamp","integer").add("altitude","double")
    //         .add("location", schemastruct).add("speed", "double").add("temperature", "double")
    //         .add("battery", "double").add("violationCode", "integer")

    val sdfToHdfs = spark.readStream.format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "DroneStream")
                .option("startingOffsets", "latest")
                .load()
                .selectExpr("CAST(value AS STRING)")

    sdfToHdfs.writeStream.outputMode("append")
        .format("parquet")
        .option("")
        .option("parquet.block.size", 10240)
        .option("path", "hdfs://localhost:9000/Drones/Messages")
        .option("checkpointLocation", "hdfs://localhost:9000/tmp/Messages_checkpoints")
        .partitionBy("window")
        //.option("truncate", False) 
        .trigger(ProcessingTime("120 seconds"))
        

    sdfToHdfs.start()
    sdfToHdfs.awaitTermination()


    }
}