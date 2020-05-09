name := "DroneCop"
organization := "droneCop"

version := "0.1"

val sparkVersion = "2.4.2"
val jacksonVersion = "2.6.5"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % jacksonVersion

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.kafka" % "kafka_2.11" % "1.1.1",
  "com.github.jurajburian" %% "mailer" % "1.2.3"
)
