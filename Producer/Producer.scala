import org.apache.kafka.clients.producer._
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Promise}

object Producer {
  
  case class ProducerManager(topic: String) {
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

    def send(value: String) = {
		val record = new ProducerRecord(topic, "DroneLog", value)
	    val p = Promise[(RecordMetadata, Exception)]()
	    producer.send(record, new Callback {
	     	override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
	      		p.success((metadata, exception))
	      	}
	    })
    }
  }


  def main(args: Array[String]): Unit = {

    
    val producer = new KafkaProducer[String, String](props)

    val numberOfDrones = args[1]
    val droneRefreshRate = args[2]

    // generete *numberOfDrones* drones
    val drones = (0 to numberOfDrones).toList.map { new Drone(_) }

    val producerManager = ProducerManager()


	val timer = new java.util.Timer()
    val task = new java.util.TimerTask {
      def run() = {

      	// log foreach drone
  		drones.foreach { producerManger.send(_.toJsonString()) }

  		// update drones
  		drones.foreach { _.update(droneRefreshRate) }

      }
  	}



	timer.schedule(task, 0, droneRefreshRate)
    producer.close()

  }
}
