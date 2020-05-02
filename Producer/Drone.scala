
import scala.math._

package Drone {

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
      

		// Coordinates
		case class Location(x: Double, y: Double)
		var location = Location(0, 0)


      	def toJsonString(): String = {
	            s"""{"drone_id" : ${droneId},"timestamp" : ${timestamp},"battery" : ${battery},"altitude" : ${altitude},"temperature" : ${temperature},"speed": ${speed},"location" : {"lat" : ${location.x}, "long" : ${location.y}}}"""

      	}

      	// deltaTime in ms
     	def update(deltaTime: Long) {
      		val r = scala.util.Random
         
	        // update battery
	        battery = battery - (deltaTime * 100) / (30 * 60 * 1000)


	        var newAltitude = altitude + pow(-1, r.nextInt(2)) * r.nextDouble()*5
	        if (newAltitude > 10) { newAltitude = 6 }
	        if (newAltitude < 3) { newAltitude = 5 }

	        var newX = location.x + pow(-1, r.nextInt(2)) * r.nextDouble()*10
	        var newY = location.y + pow(-1, r.nextInt(2)) * r.nextDouble()*10

	        // compute speed
	        speed = sqrt(pow(newX - location.x, 2) + pow(newY - location.y, 2) + pow(newAltitude - altitude, 2)) / deltaTime * 1000

	        // update altitude
	        altitude = newAltitude

	        // update location
	        location = Location(newX, newY)
	       
	       	// update temperature
	        temperature = temperature + + pow(-1, r.nextInt(2)) * r.nextDouble()*0.1

	       	// update timestamp
	       	timestamp = timestamp + deltaTime 
      }

   }
}
