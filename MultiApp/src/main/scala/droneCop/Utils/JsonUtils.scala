package droneCop.Utils
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import droneCop.Drone.DroneViolationJson

case class JsonUtils() {
  def json2Drone(jsonStr: String): DroneViolationJson = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).camelizeKeys.extract[DroneViolationJson]
  }

  def drone2Json(jsonStr: DroneViolationJson): String = {
    implicit val formats = org.json4s.DefaultFormats
    write(jsonStr)
  }
}

