import org.apache.kafka.clients.producer._
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}


object Drone {
  def main(args: Array[String]): Unit = {

    val TOPIC = "DroneStream"

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

    val droneFt = Future[Unit] {
      val record = new ProducerRecord(TOPIC, "key", """{"Alert": 1}""")
      producer.send(record)
      Thread.sleep(500)
    }

    val futures = Future.sequence(Seq[Future[Unit]](droneFt))

    futures foreach{
       value => println("Msg sent")
    }

    Await.ready(futures, Duration.Inf)

    producer.close()

  }
}
