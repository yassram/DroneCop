import org.apache.kafka.clients.producer._
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Promise}
import Drone._

object Producer {
  
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
    props.put("acks", "1")
    props.put("producer.type", "async")
    props.put("retries", "3")
    props.put("linger.ms", "5")

    val producer = new KafkaProducer[String, String](props)

    def send(value: String) = {
		  val record = new ProducerRecord(topic, "DroneLog", value)
	    val p = Promise[(RecordMetadata, Exception)]()
	    
      producer.send(record, new Callback {
	     	override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
	      		p.success((metadata, exception))
            println(metadata)
	      	}
	    })
    }

    def close():Unit = producer.close()
  }


  def main(args: Array[String]): Unit = {

    if (args.length < 2) { 
        println("Please provide the number of drones for the simulation and the refresh rate of each drone !")
        println("Usage :")
        println("        run numberOfDrones droneRefreshRate [-v]\n")
        println("   -v :  show sent logs")
        System.exit(1)
    }

    val numberOfDrones = args(0).toInt
    // in ms
    val droneRefreshRate = args(1).toInt
    var verbose = false
    if (args.length == 3) {
        if (args(2) == "-v") { verbose == true} 
    }


    println("Simulating " + args(0) + " drones")
    println("with a log each " + args(1) + " ms")
    println("-------------------------------------------")

    // generete *numberOfDrones* drones
    val drones = (0 to numberOfDrones - 1).toList.map { new Drone(_) }
    val producerManager = ProducerManager("DroneStream")


	val timer = new java.util.Timer()
    val task = new java.util.TimerTask {
      def run() = {

      // log foreach drone
  		drones.map { _.toJsonString() }.foreach { producerManager.send(_) }

  		// update drones
  		drones.foreach { _.update(droneRefreshRate) }

      }
  	}



	 timer.schedule(task, 0, droneRefreshRate)
  }
}
