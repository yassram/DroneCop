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

case class DroneMessage(val id: Int) {

  val r = scala.util.Random

  val droneId: Int = id

  // time
  val timestamp: Long = 0

  // altitude in meters
  val altitude: Double = 2 + r.nextDouble() * 4

  // temperature in degrees Celsius
  val temperature: Double = 25 +pow(-1, r.nextInt(2)) * r.nextDouble() * 6

  // Speed in m/s
  val speed: Double = 2 + r.nextDouble() * 11

  // battery in percentage
  val battery: Double = 0 + r.nextDouble() * 6

  //alert
  val alert: Int = if (r.nextInt(60000) == 0) 1 else 0

  //violation
  val violationCode: Int = if (alert == 1) 100 else (if (r.nextInt(600) == 0) r.nextInt(100) else -1)

  // Coordinates
  val x = 100 + pow(-1, r.nextInt(2)) * r.nextDouble() * 10
  val y = 100 + pow(-1, r.nextInt(2)) * r.nextDouble() * 10
  case class Location(x: Double, y: Double)
  val location = Location(x, y)

  def plateGen(): String = {

    val c0 = ('A'.toInt + r.nextInt('Z'.toInt - 'A'.toInt)).toChar
    val c1 = ('A'.toInt + r.nextInt('Z'.toInt - 'A'.toInt)).toChar

    return s"${c0}${c1}${100000 + r.nextInt(899999)}"
  }


    //plate
  val  plateState: String =  ("NY", "NJ", "PA", "CT", "FL", "MA", "IN", "VA", "MD", 
    "NC", "99", "IL", "GA", "TX", "AZ", "ME", "OH", "CA", "OK", "SC", "TN", "MI", 
    "DE", "MN", "RI", "NH", "AL", "WA", "VT", "OR", "ON", "QB", "WI", "ID", "KY", 
    "IA", "DC", "MS", "DP", "CO", "MO", "NM", "AR", "LA", "WV", "NV", "SD", "NE", 
    "UT", "KS", "NS", "GV", "AK", "MT", "ND", "HI", "WY", "AB", "PR", "BC", "NB", 
    "PE", "MB", "SK", "FO", "MX", "YT", "NT")(r.nextInt(68))
  val  plateId: String = plateGen()
  val  plateType: String = List("0", "1", "2", "3", "4")(r.nextInt(5))

  //vehicle
  val  vehicleColor: String = s"${r.nextInt(30)}"
  val  vehicleYear: String = s"${1960 + r.nextInt(60)}"
  val  vehicleMake: String = s"${r.nextInt(30)}"
  val  vehicleBody: String = s"${r.nextInt(30)}"
  val  vehicleDate: String = s"${r.nextInt(1220)}"

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

}
