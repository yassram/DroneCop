//import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import org.json4s.jackson.JsonMethods._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.streaming.Trigger._

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
      "local[1]"
    )
    

val spark = SparkSession
        .builder()
        //.config("spark.executor.heartbeatInterval", "10000ms")
        //.config("spark.master", "local")
        .config(conf)
//        .config("spark.sql.streaming.checkpointLocation", "/tmp/rayoutropbete")
        .getOrCreate()
import spark.implicits._

def main(args: Array[String]): Unit = {
    val schemastruct = new StructType().add("lat","double").add("long", "double")
    val schemaforfile = new StructType().add("droneId","integer")
            .add("alert","integer").add("timestamp","integer").add("altitude","double")
            .add("location", schemastruct).add("speed", "double").add("temperature", "double")
            .add("battery", "double").add("violationCode", "integer")

    val sdfToHdfs = spark.readStream.format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "DroneStream")
                .load()
                //.select(from_json(col("value").cast("string"), schemaforfile))
                //.alias("csv").select("csv.*")

    sdfToHdfs.writeStream.outputMode("append")
        .format("text")
        .option("path", "./test")//"hdfs://localhost:9000/Drones/Messages")
        .option("checkpointLocation", "/tmp/test")//"hdfs://localhost:9000/tmp/Messages_checkpoints")
        //.partitionBy("window")
        //.option("truncate", False) 
        .trigger(ProcessingTime("120 seconds"))
        .start().awaitTermination()

    }
}