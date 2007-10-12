-- MySQL dump 10.11
--
-- Host: localhost    Database: adhocrailway
-- ------------------------------------------------------
-- Server version	5.0.38-Ubuntu_0ubuntu1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `locomotive`
--

DROP TABLE IF EXISTS `locomotive`;
CREATE TABLE `locomotive` (
  `id` int(11) NOT NULL auto_increment,
  `locomotive_group_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `image` varchar(255) NOT NULL,
  `address` int(11) NOT NULL,
  `bus` int(11) NOT NULL,
  `locomotive_type_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `locomotive_locomotive_group_id` (`locomotive_group_id`),
  KEY `locomotive_locomotive_type_fk` (`locomotive_type_id`),
  CONSTRAINT `locomotive_locomotive_group_id` FOREIGN KEY (`locomotive_group_id`) REFERENCES `locomotive_group` (`id`),
  CONSTRAINT `locomotive_locomotive_type_fk` FOREIGN KEY (`locomotive_type_id`) REFERENCES `locomotive_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `locomotive`
--

LOCK TABLES `locomotive` WRITE;
/*!40000 ALTER TABLE `locomotive` DISABLE KEYS */;
INSERT INTO `locomotive` VALUES (1,2,'Bernaa','Bern desc','bern.png',1,1,2),(2,1,'ascom','asocm','ascom.png',2,1,2),(3,6,'sdasdf','sdf','dsf',12,1,3),(4,6,'213','sadsadfsads','sdsaf',3,1,3),(5,6,'asdfsdf','sadf','dsaf',21,1,3),(6,6,'sdf','sadfsadf','adfsadf',4,1,2),(7,6,'sadfasfd','sdf','Sdf',23,1,3),(8,2,'asdsafd','sadfsadf','sdfsadf',5,1,3),(9,2,'sadsdfsadf','sadfsadf','ssaf',6,1,3),(10,2,'a','a','a',7,1,3),(11,2,'asdf','sdf','sdf',8,1,3),(12,2,'213','fdsgfdg','sdfdsa',9,1,3),(13,3,'asdf','sadf','sdf',10,1,3),(14,1,'sadf','dsaf','sdf',11,1,3),(15,1,'dfsdf','sdf','sdf',12,1,2),(16,7,'dfsdf','sdf','sdf',12,1,3);
/*!40000 ALTER TABLE `locomotive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locomotive_group`
--

DROP TABLE IF EXISTS `locomotive_group`;
CREATE TABLE `locomotive_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `locomotive_group`
--

LOCK TABLES `locomotive_group` WRITE;
/*!40000 ALTER TABLE `locomotive_group` DISABLE KEYS */;
INSERT INTO `locomotive_group` VALUES (1,'Ae 6/6'),(2,'Re 460'),(3,'asdf'),(6,'123'),(7,'asdasd');
/*!40000 ALTER TABLE `locomotive_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locomotive_type`
--

DROP TABLE IF EXISTS `locomotive_type`;
CREATE TABLE `locomotive_type` (
  `id` int(11) NOT NULL auto_increment,
  `type_name` varchar(255) NOT NULL,
  `drivingSteps` int(11) NOT NULL,
  `functionCount` int(11) NOT NULL,
  `stepping` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `locomotive_type`
--

LOCK TABLES `locomotive_type` WRITE;
/*!40000 ALTER TABLE `locomotive_type` DISABLE KEYS */;
INSERT INTO `locomotive_type` VALUES (2,'Digital',28,5,4),(3,'Delta',14,1,2);
/*!40000 ALTER TABLE `locomotive_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route`
--

DROP TABLE IF EXISTS `route`;
CREATE TABLE `route` (
  `id` int(11) NOT NULL,
  `route_group_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `number` (`number`),
  KEY `route_route_group_fk` (`route_group_id`),
  CONSTRAINT `route_route_group_fk` FOREIGN KEY (`route_group_id`) REFERENCES `route_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `route`
--

LOCK TABLES `route` WRITE;
/*!40000 ALTER TABLE `route` DISABLE KEYS */;
/*!40000 ALTER TABLE `route` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route_group`
--

DROP TABLE IF EXISTS `route_group`;
CREATE TABLE `route_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `weight` int(11) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `route_group`
--

LOCK TABLES `route_group` WRITE;
/*!40000 ALTER TABLE `route_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `route_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route_item`
--

DROP TABLE IF EXISTS `route_item`;
CREATE TABLE `route_item` (
  `id` int(11) NOT NULL auto_increment,
  `route_id` int(11) NOT NULL,
  `turnout_id` int(11) NOT NULL,
  `routed_state` enum('STRAIGHT','LEFT','RIGHT') NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `route_item_route_id_fk` (`route_id`),
  KEY `route_item_turnout_id_fk` (`turnout_id`),
  CONSTRAINT `route_item_route_id_fk` FOREIGN KEY (`route_id`) REFERENCES `route` (`id`),
  CONSTRAINT `route_item_turnout_id_fk` FOREIGN KEY (`turnout_id`) REFERENCES `turnout` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `route_item`
--

LOCK TABLES `route_item` WRITE;
/*!40000 ALTER TABLE `route_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `route_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout`
--

DROP TABLE IF EXISTS `turnout`;
CREATE TABLE `turnout` (
  `id` int(11) NOT NULL auto_increment,
  `turnout_group_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `description` varchar(255) default NULL,
  `default_state` enum('STRAIGHT','LEFT','RIGHT') NOT NULL,
  `orientation` enum('NORTH','EAST','SOUTH','WEST') NOT NULL,
  `turnout_type_id` int(11) NOT NULL,
  `address1` int(11) NOT NULL,
  `address2` int(11) default NULL,
  `bus1` int(11) NOT NULL,
  `bus2` int(11) default NULL,
  `address1_switched` tinyint(4) NOT NULL,
  `address2_switched` tinyint(4) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `number` (`number`),
  KEY `turnout_turnout_group_id_fk` (`turnout_group_id`),
  KEY `turnout_turnout_type_fk` (`turnout_type_id`),
  KEY `turnout_address1_fk` (`address1`),
  KEY `turnout_address2_fk` (`address2`),
  CONSTRAINT `turnout_turnout_group_id_fk` FOREIGN KEY (`turnout_group_id`) REFERENCES `turnout_group` (`id`),
  CONSTRAINT `turnout_turnout_type_fk` FOREIGN KEY (`turnout_type_id`) REFERENCES `turnout_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `turnout`
--

LOCK TABLES `turnout` WRITE;
/*!40000 ALTER TABLE `turnout` DISABLE KEYS */;
INSERT INTO `turnout` VALUES (6,5,1,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(7,5,2,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(8,5,3,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(9,5,4,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(10,5,5,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(11,5,6,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(12,5,7,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(13,5,8,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(14,5,9,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(15,5,10,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(16,7,11,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(17,7,12,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(18,7,13,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(19,7,14,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(20,7,15,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(21,7,16,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(22,7,17,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(23,7,18,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(24,7,19,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(25,7,20,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(26,9,21,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(27,9,22,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(28,9,23,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(29,9,24,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(30,9,25,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(31,9,26,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(32,9,27,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(33,9,28,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(34,9,29,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(35,9,30,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(36,9,31,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(37,9,32,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(38,9,33,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(39,9,34,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(40,9,35,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(41,9,36,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(42,9,37,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(43,9,38,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(44,9,39,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0),(45,9,40,NULL,'STRAIGHT','EAST',6,0,0,0,0,0,0);
/*!40000 ALTER TABLE `turnout` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout_address`
--

DROP TABLE IF EXISTS `turnout_address`;
CREATE TABLE `turnout_address` (
  `id` int(11) NOT NULL auto_increment,
  `address` int(11) NOT NULL,
  `bus` int(11) NOT NULL,
  `switched` tinyint(1) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `turnout_address`
--

LOCK TABLES `turnout_address` WRITE;
/*!40000 ALTER TABLE `turnout_address` DISABLE KEYS */;
/*!40000 ALTER TABLE `turnout_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout_group`
--

DROP TABLE IF EXISTS `turnout_group`;
CREATE TABLE `turnout_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `weight` int(11) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `turnout_group`
--

LOCK TABLES `turnout_group` WRITE;
/*!40000 ALTER TABLE `turnout_group` DISABLE KEYS */;
INSERT INTO `turnout_group` VALUES (3,'gugua',1),(4,'rt',2),(5,'sdf',3),(6,'sdfsdfsdfdf',4),(7,'sdsadsdf',5),(8,'123',6),(9,'2312313123',7);
/*!40000 ALTER TABLE `turnout_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout_type`
--

DROP TABLE IF EXISTS `turnout_type`;
CREATE TABLE `turnout_type` (
  `id` int(11) NOT NULL auto_increment,
  `type_name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `turnout_type`
--

LOCK TABLES `turnout_type` WRITE;
/*!40000 ALTER TABLE `turnout_type` DISABLE KEYS */;
INSERT INTO `turnout_type` VALUES (4,'THREEWAY'),(5,'DOUBLECROSS'),(6,'DEFAULT');
/*!40000 ALTER TABLE `turnout_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2007-10-12 15:57:44
