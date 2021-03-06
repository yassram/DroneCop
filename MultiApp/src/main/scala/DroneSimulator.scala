package droneCop

import org.apache.kafka.clients.producer._
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Promise}
import droneCop.Drone._
import droneCop.Managers.ProducerManager

object DroneSimulator {

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      println(
        "Please provide the number of drones for the simulation and the refresh rate of each drone !"
      )
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
      if (args(2) == "-v") { verbose == true }
    }

    println("Simulating " + args(0) + " drones")
    println("with a log each " + args(1) + " ms")
    println("-------------------------------------------")

    // generete *numberOfDrones* drones
    val drones = (0 to numberOfDrones - 1)

    val timer = new java.util.Timer()
    val task = new java.util.TimerTask {
      def run() = {

        // log foreach drone
        drones.map { DroneMessage(_).toJsonString() }.foreach {m =>
          if (verbose) {
            println(m)
          }
          ProducerManager.send("DroneStream", m)
          }
      }
    }
    timer.schedule(task, 0, droneRefreshRate)
  }
}
