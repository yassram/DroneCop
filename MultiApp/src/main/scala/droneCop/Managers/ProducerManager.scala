package droneCop.Managers

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