import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConverters._
import java.util
import java.util.Properties
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.json4s.jackson.JsonMethods._

object Consumer extends App {
  
  val TOPIC = "DroneStream"

  val sparkConf = new SparkConf()
    .setAppName("Spark")
    .setMaster("local[*]")

  val sparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

  val kafkaProps = new Properties()
  kafkaProps.put("bootstrap.servers", "localhost:9092")
  kafkaProps.put(
    "key.deserializer",
    "org.apache.kafka.common.serialization.StringDeserializer"
  )
  kafkaProps.put(
    "value.deserializer",
    "org.apache.kafka.common.serialization.StringDeserializer"
  )
  kafkaProps.put("group.id", "drone")

  val consumer = new KafkaConsumer[String, String](kafkaProps)

  consumer.subscribe(util.Collections.singletonList(TOPIC))

  while(true){
    val records = consumer.poll(500)
    records.asScala.foreach{drone =>
	  //val md = jsonStrToMap(drone.value())
	  println(drone)
      //if(df("Alert") == 1){
        //val record = new ProducerRecord[String, String]("alert_info", "key", r.value())
        //val record1 = new ProducerRecord[String, String]("storage_info", "key", r.value())
        //producer.send(record)
        //producer.send(record1)
        //println(df("Alert"))
      // }else{
      //   val record = new ProducerRecord[String, String]("storage_info", "key", r.value())
      //   //producer.send(record)
      //   println("Pas d'alerte")
      // }
    }
  }

  consumer.close()
}
