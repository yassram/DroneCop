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

import javax.mail.internet.InternetAddress
import com.github.jurajburian.mailer._

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
  kafkaProps.put(
    "group.id",
    "drone"
  )
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

object ConsumerAlertStream extends App {
  //setup mailer
  val session = (SmtpAddress("smtp.gmail.com", 587) :: 
        Property("mail.smtp.auth", "true") ::
        Property("mail.smtp.starttls.enable", "true") ::
        Property("mail.smtp.host", "smtp.gmail.com") ::
        Property("mail.smtp.port", "587") ::
        SessionFactory()).session(Some("scalayarm@gmail.com" -> "Banane94"))
  val mailer = Mailer(session)
  



  val TOPIC = "AlertStream"
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


  while (true) {
    val records = mainConsumer.consumer.poll(500)
    records.asScala.foreach { drone =>
      val md = jsonStrToMap(drone.value())
      println("Alert received from drone number " + md("DroneId"))
      val pos : Map[String,Any] = md("Position").asInstanceOf[Map[String,Any]]
      val content: Content = new Content().text("Hi\n").html(s"<html><body>" + "<img width='600' src='https://maps.googleapis.com/maps/api/staticmap?center=" + pos("Latitude") + "," + pos("Longitude") + "&zoom=16&scale=1&size=600x300&maptype=roadmap&key=AIzaSyDSIp9pblkkr5nxfRhujeBvVe27JzwHlTM&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C" + pos("Latitude") + "," + pos("Longitude") + "'>" + "</body></html>")
      val msg = Message(
        from = new InternetAddress("scalayarm@gmail.com"),
        subject = "my subject",
        content = content,
        to = Seq(new InternetAddress("rayane.amrouche@epita.fr")))

      try {
        mailer.send(msg)
      }
      catch {
        case e: javax.mail.MessagingException => println("PB! ", e)
      }
      mailer.close()
    }
  }
  mainConsumer.consumer.close()
}