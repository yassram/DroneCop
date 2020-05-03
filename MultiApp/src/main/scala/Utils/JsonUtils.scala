
package droneCop.Utils
import org.json4s.jackson.JsonMethods._
import droneCop.Drone.DroneJson
case class jsonUtils() {
def json2Drone(jsonStr: String): DroneJson = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).camelizeKeys.extract[DroneJson]
  }
}