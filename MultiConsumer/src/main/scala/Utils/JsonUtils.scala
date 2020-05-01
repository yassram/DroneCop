
package droneCop.Utils
import org.json4s.jackson.JsonMethods._

case class jsonUtils() {
def jsonStrToMap(jsonStr: String): Map[String, Any] = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).extract[Map[String, Any]]
  }
}