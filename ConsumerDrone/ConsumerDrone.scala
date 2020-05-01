import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConverters._
import java.util
import java.util.Properties
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.json4s.jackson.JsonMethods._
import org.apache.kafka.clients.producer._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.concurrent.{Await, Future, Promise}
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class ConsumerManager(topic: String) {
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
  consumer.subscribe(util.Collections.singletonList(topic))
}

case class ProducerManager(topic: String) {
  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")

  props.put(
    "key.serializer",
    "org.apache.kafka.common.serialization.StringSerializer"
  )
  props.put(
    "value.serializer",
    "org.apache.kafka.common.serialization.StringSerializer"
  )
  val producer = new KafkaProducer[String, String](props)

  def send(key: String, value: String) = {
    val droneFt = Future[Unit] {
      val record = new ProducerRecord(topic, key, value)
      producer.send(record)
      Thread.sleep(500)
    }
    val futures = Future.sequence(Seq[Future[Unit]](droneFt))
    futures foreach { value => println("Msg sent") }
    Await.ready(futures, Duration.Inf)
  }

}

object ConsumerDroneStream extends App {
  val TOPIC = "DroneStream"
  val mainConsumer = ConsumerManager(TOPIC)

  val sparkConf = new SparkConf()
    .setAppName("Spark")
    .setMaster("local[*]")

  val sparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

  def jsonStrToMap(jsonStr: String): Map[String, Any] = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).extract[Map[String, Any]]
  }

  val alertProd = ProducerManager("AlertStream")
  val storageProd = ProducerManager("AllStream")

  while (true) {
    val records = mainConsumer.consumer.poll(500)
    records.asScala.foreach { drone =>
      val md = jsonStrToMap(drone.value())
      println("New message received from drone number " + md("DroneId"))
      if (md("Alert") == 1) {
        println("> " + md("DroneId") + ": This is an alert!")
        alertProd.send("key", drone.value())
        storageProd.send("key", drone.value())
      } else {
        println("> " + md("DroneId") + ": Normal message")
        println("> " + md("DroneId") + ": Sent to storage")
        storageProd.send("key", drone.value())
      }
    }
  }
  mainConsumer.consumer.close()

}
