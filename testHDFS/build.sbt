name := "HDFS"

version := "0.1"

val sparkVersion = "2.4.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.kafka" % "kafka_2.11" % "1.1.1"
)
// name := "HDFS"

// version := "0.1.0"


// val sparkVersion = "2.4.2"

// libraryDependencies ++= Seq(
//   "org.apache.spark" %% "spark-core" % sparkVersion,
//   "org.apache.spark" %% "spark-sql" % sparkVersion,
//   "org.apache.spark" %% "spark-streaming" % sparkVersion,
//   "org.apache.kafka" %% "kafka_2.11" % "1.1.1",
//   "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion

// )