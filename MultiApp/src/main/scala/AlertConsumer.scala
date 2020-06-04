package droneCop

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

import droneCop.Managers.ConsumerManager
import droneCop.Utils.JsonUtils
import droneCop.Drone.DroneViolationJson

object AlertConsumer extends App {

  override def main(args: Array[String]): Unit = {

    if (args.length < 3) {
      println(
        "Please provide your mail, your password and the alert receiver"
      )
      println("Usage :")
      println("        run mail password receiver\n")
      System.exit(1)
    }

    val session = (SmtpAddress("smtp.gmail.com", 587) ::
      Property("mail.smtp.auth", "true") ::
      Property("mail.smtp.starttls.enable", "true") ::
      Property("mail.smtp.host", "smtp.gmail.com") ::
      Property("mail.smtp.port", "587") ::
      SessionFactory()).session(Some(args(0) -> args(1)))
    val mailer = Mailer(session)

    val TOPIC = "AlertStream"
    val consumerManager = ConsumerManager(TOPIC)

    val jsonUtils = new JsonUtils()

    while (true) {
      val records = consumerManager.poll(100)
      records.asScala.foreach { d =>
        val drone : DroneViolationJson = jsonUtils.json2Drone(d.value())
        println("Alert received from drone number " + drone.droneId)
        val content: Content = new Content()
          .text("This is an alert!\n")
          .html(
            s"<html><body>" + "<img width='600' src='https://maps.googleapis.com/maps/api/staticmap?center=" + drone.lat + "," + drone.long + "&zoom=16&scale=1&size=600x300&maptype=roadmap&key=AIzaSyDSIp9pblkkr5nxfRhujeBvVe27JzwHlTM&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C" + drone.lat + "," + drone.long + "'>" + "</body></html>"
          )
        val msg = Message(
          from = new InternetAddress(args(0)),
          subject = "Alert",
          content = content,
          to = Seq(new InternetAddress(args(2)))
        )
        try {
          mailer.send(msg)
        } catch {
          case e: javax.mail.MessagingException => println("Error ", e)
        }
        mailer.close()
      }
    }
    consumerManager.close()
  }
}
