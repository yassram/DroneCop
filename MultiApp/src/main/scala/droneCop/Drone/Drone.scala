package droneCop.Drone
import scala.math._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    timestamp: String,
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
)

case class DroneMessage(val id: Int) {

  val r = scala.util.Random

  val droneId: Int = id



  val format = "yyyy/MM/dd HH:mm:ss"
  val dtf = DateTimeFormatter.ofPattern(format)
  val ldt = LocalDateTime.now()

  // timestamp
  val timestamp: String = ldt.format(dtf)

  // altitude in meters
  val altitude: Double = 2 + r.nextDouble() * 4

  // temperature in degrees Celsius
  val temperature: Double = 25 +pow(-1, r.nextInt(2)) * r.nextDouble() * 6

  // Speed in m/s
  val speed: Double = 2 + r.nextDouble() * 11

  // battery in percentage
  val battery: Double = 100 - r.nextDouble() * 100

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
  val plateState: String =  List("NY", "NJ", "PA", "CT", "FL", "MA", "IN", "VA", "MD", 
    "NC", "99", "IL", "GA", "TX", "AZ", "ME", "OH", "CA", "OK", "SC", "TN", "MI", 
    "DE", "MN", "RI", "NH", "AL", "WA", "VT", "OR", "ON", "QB", "WI", "ID", "KY", 
    "IA", "DC", "MS", "DP", "CO", "MO", "NM", "AR", "LA", "WV", "NV", "SD", "NE", 
    "UT", "KS", "NS", "GV", "AK", "MT", "ND", "HI", "WY", "AB", "PR", "BC", "NB", 
    "PE", "MB", "SK", "FO", "MX", "YT", "NT")(r.nextInt(68))
  val plateId: String = plateGen()
  val plateType: String = List("0", "1", "2", "3", "4")(r.nextInt(5))

  //vehicle
  val vehicleColor: String = List("GY", "WH", "WHITE", "BK", "BLACK", "BL", "GREY", "RD", "SILVE", 
    "BROWN", "BLUE", "RED", "GR", "TN", "GREEN", "null", "OTHER", "YW", "BR", "BLK", "GRAY", "GL", 
    "TAN", "YELLO", "GOLD", "GRY", "MR", "WHT", "WT", "ORANG", "OR", "WHI", "SL", "LTG", "BRN", 
    "BLU", "SIL", "LT/", "DK/", "PURPL", "SILVR", "LTGY", "GRN", "DKG", "PR", "GN", "BN", "DKGY", 
    "DKB", "GYGY", "SILV", "RDW", "WHBL", "MAROO", "BEIGE", "DKBL", "BKGY", "BLW", "BRO", "GLD", 
    "WHB", "LTB", "WH/", "GY/", "BRWN", "LAVEN", "BLG", "W", "BG", "DKR", "WHO", "BURG", "WHGY", 
    "GD", "MAR", "YELL", "BRW", "YEL", "DKRD", "NOC", "WHG", "BLGY", "BURGU", "LTBL", "RD/", "BGE", 
    "GYGR", "GYBL", "DKGR", "SLVR", "BL/", "LTGR", "UNKNO", "SLV", "UNK", "BK/", "YL", "BKBL", "BLWH", 
    "BWN")(r.nextInt(100))
  val vehicleYear: String = s"${1960 + r.nextInt(60)}"
  val vehicleMake: String = List("FORD", "TOYOT", "HONDA", "NISSA", "CHEVR", "FRUEH", 
    "ME/BE", "DODGE", "BMW", "JEEP", "INTER", "GMC", "HYUND", "LEXUS", "ACURA", "CHRYS", 
    "VOLKS", "INFIN", "NS/OT", "SUBAR", "ISUZU", "AUDI", "MITSU", "MAZDA", "INCO", "HINO", 
    "KIA", "CADIL", "MERCU", "VOLVO", "null", "ROVER", "WORKH", "KENWO", "BUICK", "PETER", 
    "MACK", "PONTI", "MINI", "PORSC", "SATUR", "SMART", "JAGUA", "UD", "WORK", "FIAT", 
    "SAAB", "UTIL", "MI/F", "SUZUK", "STERL", "VANHO", "OLDSM", "SCION", "VESPA", "MASSA", 
    "MCI", "PLYMO", "KAWAS", "HUMME", "YAMAH", "HARLE", "UPS", "TRAIL", "IC", "TRIUM", "KW", 
    "SPRI", "FERRA", "FONTA", "GEO", "BENTL", "THOMA", "PREVO", "NAVIS", "GREAT", "DUCAT", 
    "WO/C", "VPG", "SMITH", "CHECK", "STARC", "HIN", "UTILI", "TESL", "WHITE", "STAR", "MER", 
    "EAST", "UT/M", "DORSE", "FHTL", "LND", "FRIE", "FR/LI", "LARO", "PREV", "GIDNY", "TESLA", 
    "UNIFL")(r.nextInt(100))
  val vehicleBody: String = List("SUBN","4DSD","VAN","DELV","SDN","2DSD","PICK","REFG","TRAC",
    "UTIL","TAXI","4DR","BUS","CONV","null","TK","WAGO","4D","TRLR","MCY","P-U","SW","4S",
    "FLAT","TRAI","T/CR","UT","DUMP","TR/C","TRK","4W","TRUC","VN","STAK","MP","SU","TR","TOW",
    "2DR","TRL","SEMI","PKUP","SEDN","SD","FOUR","CP","BOAT","SEDA","BS","SWT","MOTO","TANK","2D",
    "SV","PK","4H","4DO","LIM","4DS","MC","LL","I","TRT","TL","TT","S/SP","TWOD","CON","TRC","TLR",
    "CV","P/SH","ST","W/DR","STW","SUV","5D","2S","4DSE","HB","MOT","MOBL","H/WH","MCC","PV","TR/E",
    "COUP","2H","FREI","FODO","HATC","TRAV","LIMO","ES","SN/P","APUR","CMIX","AR","CN","PU")(r.nextInt(100))

  def toJsonString(): String = {
    s"""{
    "drone_id" : ${droneId},
    "timestamp" : ${timestamp},
    "battery" : ${battery},
    "altitude" : ${altitude},
    "temperature" : ${temperature},
    "speed" : ${speed},
    "alert" : ${alert},
    "location" : { 
      "lat" : ${location.x}, 
      "long" : ${location.y}
    }${
      if (violationCode != -1) {
    s""",
    "violation_code" : ${violationCode},
    "plateState" : "${plateState}",
    "plateId" :  "${plateId}",
    "plateType" :  "${plateType}",
    "vehicleColor" :  "${vehicleColor}",
    "vehicleYear" :  "${vehicleYear}",
    "vehicleMake" :  "${vehicleMake}",
    "vehicleBody" :  "${vehicleBody}",
    """
      } else {
        ""
      }
    }
    }"""
  }

}
