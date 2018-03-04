CREATE KEYSPACE IF NOT EXISTS project
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE project;


CREATE TABLE Room (
roomId int,
user text,
size int,
PRIMARY KEY (size, roomId)
);

CREATE TABLE Reservation (
 roomId int,
 user text,
 PRIMARY KEY (user, roomId)
);