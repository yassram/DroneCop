//import org.apache.spark.sql.functions._
package droneCop
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import org.json4s.jackson.JsonMethods._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger._

object StreamToHDFS {

val conf = new SparkConf()
    .setAppName("StreamToHDFS")
    .setMaster(
      "local[*]"
    )
    
val spark = SparkSession
        .builder()
        .config(conf)
        .getOrCreate()

import spark.implicits._

def main(args: Array[String]): Unit = {

    if (args.length < 1) {
        println("Please provide the triggerTime between 2 succesive saves to the HDFS.")
    }

    val triggerTime = args(0)

    val schemaforfile = new StructType()
            .add("drone_id", "integer")
            .add("timestamp", "string")
            .add("battery", "double")
            .add("altitude", "double")
            .add("temperature", "double")
            .add("speed", "double")
            .add("alert", "integer")
            .add("lat", "double")
            .add("long", "double")
            .add("violation_code", "integer")
            .add("plateState", "string")
            .add("plateId", "string")
            .add("plateType", "string")
            .add("vehicleColor", "string")
            .add("vehicleYear", "string")
            .add("vehicleMake", "string")
            .add("vehicleBody", "string")

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
        .option("parquet.block.size", 10240)
        // .partitionBy("window")
        // .option("truncate", False) 
        .trigger(ProcessingTime(s"$triggerTime seconds"))
        .start()
        .awaitTermination()

    }
}