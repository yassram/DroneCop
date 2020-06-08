# DroneCop:
A drone service to help police systems make parking tickets using drones.

## Prerequisites:
Make sure to have :
- `sbt` and `scala` ([installation guide](https://www.scala-lang.org/download/))
- `docker` ([official website](https://www.docker.com)) 

## Installation:
run `bridgeKafka` (only once)
``` sh
sh$ chmod +x requirements/bridgeKafka.sh
sh$ ./requirements/bridgeKafka.sh
```

run `Zookeeper`
``` sh
sh$ chmod +x requirements/runZookeeper.sh
sh$ ./requirements/runZookeeper.sh
```

run `runKafka`
``` sh
sh$ chmod +x requirements/runZookeeper.sh
sh$ ./requirements/runZookeeper.sh
```

run `HDFS` server
``` sh
sh$ cd DistributedStorage/docker-hadoop2
sh$ docker-compose -d up
```

## Project architecture:
This project is performed through small apps (services) communicating with each others.

* **DroneSimulator:** This program simulates a given number of flying drones by sending drone messges, violations and alerts (with a given refresh rate). These messages are added to a `DroneMessage` stream.

* **DroneConsumer:** This program is the data's entry point. It reads drones' messages from the `DroneMessage` stream and allows us to apply any business processing. Theses processes can be defined later. Currently only alerts are sent to another stream (`AlertStream`) in order to be processed a soon as possible since the drone needs a human assistance.

* **AlertConsumer:** This program reads from the `AlertStream` and sends a mail to ask from human assistance. (It's just an example of processes that can be applied)

* **CSVProducer:** This program allows us to add old data (from police) to the new servers in order to improve statistics that can be used later to optimize the drones.. (streets with highest number of violations, time, ...). It reads each line of a given csv and process them using the same pipeline as `DroneSimulator` with `-1` as `droneId`. 

* **StreamToHdfs:** This program saves data in the `DroneMessage`stream into `parquet` files in an `HDFS` each `delta_t` time.

* **Reader:** This program generates simple analysis from data stored in the `HDFS`. This is only for demo purposes.

### Visually:

<img src="/readme_images/archi.png"></img>

## Usage:
After following installation instructions, go to the `MultiApp` directory then launch apps:

### DroneConsumer:
```sh
sh$ sbt
sbt:DroneCop> run
Multiple main classes detected, select one to run:

 [1] droneCop.AlertConsumer
 [2] droneCop.CsvProducer
 [3] droneCop.DroneConsumer
 [4] droneCop.DroneSimulator
 [5] droneCop.Reader
 [6] droneCop.StreamToHDFS
 
sbt:DroneCop> 3
```

### AlertConsumer:
```sh
sh$ sbt
sbt:DroneCop> run mail_sender mail_sender_password mail_receiver
Multiple main classes detected, select one to run:

 [1] droneCop.AlertConsumer
 [2] droneCop.CsvProducer
 [3] droneCop.DroneConsumer
 [4] droneCop.DroneSimulator
 [5] droneCop.Reader
 [6] droneCop.StreamToHDFS
 
sbt:DroneCop> 1
```
**mail_sender** is the email address used to send mail notifications.

**mail_sender_password** is the password of the email address used to send mail notifications.

**mail_receiver** is the email of notifications' receiver.

### StreamToHdfs:
```sh
sh$ sbt
sbt:DroneCop> run trigger_time
Multiple main classes detected, select one to run:

 [1] droneCop.AlertConsumer
 [2] droneCop.CsvProducer
 [3] droneCop.DroneConsumer
 [4] droneCop.DroneSimulator
 [5] droneCop.Reader
 [6] droneCop.StreamToHDFS
 
sbt:DroneCop> 6
```
**trigger_time** is the time in seconds between two successive saves. (should be large enough to avoid saving files with small amount of data)

**_Note:_** You can access to the web interfce of the `HDFS` at: http://localhost:9870/explorer.html#/

### DroneSimulator:
```sh
sh$ sbt
sbt:DroneCop> run number_of_drones drones_refresh_rate
Multiple main classes detected, select one to run:

 [1] droneCop.AlertConsumer
 [2] droneCop.CsvProducer
 [3] droneCop.DroneConsumer
 [4] droneCop.DroneSimulator
 [5] droneCop.Reader
 [6] droneCop.StreamToHDFS
 
sbt:DroneCop> 4
```
**number_of_drones** is the number of drones that will be simulated. `drone_Id`s will be set from `0 to number_of_drones - 1`

**drones_refresh_rate** is the time in ms between 2 successive messages sent from the same drone.

**_Note:_** alerts / violations are sent randomly with 1% of chances to have an alert.

### CSVProducer:
```sh
sh$ sbt
sbt:DroneCop> run csv_file_path
Multiple main classes detected, select one to run:

 [1] droneCop.AlertConsumer
 [2] droneCop.CsvProducer
 [3] droneCop.DroneConsumer
 [4] droneCop.DroneSimulator
 [5] droneCop.Reader
 [6] droneCop.StreamToHDFS
 
sbt:DroneCop> 2
```
**csv_file_path** is the path to the csv file to load.


### Reader:
```sh
sh$ sbt
sbt:DroneCop> run
Multiple main classes detected, select one to run:

 [1] droneCop.AlertConsumer
 [2] droneCop.CsvProducer
 [3] droneCop.DroneConsumer
 [4] droneCop.DroneSimulator
 [5] droneCop.Reader
 [6] droneCop.StreamToHDFS
 
sbt:DroneCop> 5
```


## Authors:
- Yassir RAMDANI: yassir.ramdani@epita.fr
- Mamoune EL HABBARI: mamoune.el-habbari@epita.fr
- Amine HEFFAR: amine.heffar@epita.fr
- Rayane AMROUCHE: rayane.amrouche@epita.fr
