package droneCop.Drone
import scala.math._

case class Location(lat: Double, long: Double)
case class DroneJson(
    droneId: Int,
    alert: Int,
    timestamp: Long,
    //position
    altitude: Double,
    location: Location,
    //drone
    speed: Double,
    temperature: Double,
    battery: Double,
    //violation
    violationCode: Int
)

case class DroneViolationJson(
    droneId: Int,
    alert: Int,
    timestamp: Long,
    //position
    altitude: Double,
    location: Location,
    //drone
    speed: Double,
    temperature: Double,
    battery: Double,
    //violation
    violationCode: Int,
    //plate
    plateState: String,
    plateId: String,
    plateType: String,
    //vehicle
    vehicleColor: String,
    vehicleYear: String,
    vehicleMake: String,
    vehicleBody: String,
    vehicleDate: String
)

case class Drone(val id: Int) {
  var droneId: Int = id

  // time
  var timestamp: Long = 0

  // altitude in meters
  var altitude: Double = 0

  // temperature in degrees Celsius
  var temperature: Double = 25

  // Speed in m/s
  var speed: Double = 0

  // battery in percentage
  var battery: Double = 100

  // random
  val r1 = scala.util.Random

  //alert
  var alert: Int = if (r1.nextInt(100) == 0) 1 else 0

  //violation
  var violationCode: Int = r1.nextInt(100)

  // Coordinates
  case class Location(x: Double, y: Double)
  var location = Location(0, 0)

    //plate
  var  plateState: String = "0"
  var  plateId: String = "1"
  var  plateType: String = "2"

  //vehicle
  var  vehicleColor: String = "3"
  var  vehicleYear: String = "4"
  var  vehicleMake: String = "5"
  var  vehicleBody: String = "6"
  var  vehicleDate: String = "7"

  def toJsonString(): String = {
    s"""{
    "drone_id" : ${droneId},
    "timestamp" : ${timestamp},
    "battery" : ${battery},
    "altitude" : ${altitude},
    "temperature" : ${temperature},
    "speed" : ${speed},
    "alert" : ${alert},
    "violation_code" : ${violationCode},
    "location" : { 
      "lat" : ${location.x}, 
      "long" : ${location.y}
    },
    "plateState" : "${plateState}",
    "plateId" :  "${plateId}",
    "plateType" :  "${plateType}",
    "vehicleColor" :  "${vehicleColor}",
    "vehicleYear" :  "${vehicleYear}",
    "vehicleMake" :  "${vehicleMake}",
    "vehicleBody" :  "${vehicleBody}",
    "vehicleDate" :  "${vehicleDate}"
    }"""
  }

  // deltaTime in ms
  def update(deltaTime: Long) {
    val r = scala.util.Random

    // update battery
    battery = battery - (deltaTime * 100) / (30 * 60 * 1000)

    //alert random
    alert = if (r.nextInt(100) == 0) 1 else 0

    var newAltitude = altitude + pow(-1, r.nextInt(2)) * r.nextDouble() * 5
    if (newAltitude > 10) { newAltitude = 6 }
    if (newAltitude < 3) { newAltitude = 5 }

    var newX = location.x + pow(-1, r.nextInt(2)) * r.nextDouble() * 10
    var newY = location.y + pow(-1, r.nextInt(2)) * r.nextDouble() * 10

    // compute speed
    speed = sqrt(
      pow(newX - location.x, 2) + pow(newY - location.y, 2) + pow(
        newAltitude - altitude,
        2
      )
    ) / deltaTime * 1000

    // update altitude
    altitude = newAltitude

    // update location
    location = Location(newX, newY)

    // update temperature
    temperature = temperature + +pow(-1, r.nextInt(2)) * r.nextDouble() * 0.1

    // update timestamp
    timestamp = timestamp + deltaTime
  }

}
