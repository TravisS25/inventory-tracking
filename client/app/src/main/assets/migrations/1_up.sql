
CREATE TABLE IF NOT EXISTS building(
    _id INTEGER PRIMARY KEY,
    building_name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS building_floor(
    _id INTEGER PRIMARY KEY,
    building_id INT,
    floor_name VARCHAR(20),
    FOREIGN KEY(building_id) REFERENCES building(_id)
);

CREATE TABLE IF NOT EXISTS department(
    _id INTEGER PRIMARY KEY,
    building_floor_id INT,
    department_name VARCHAR(50),
    FOREIGN KEY(building_floor_id) REFERENCES building_floor(_id)
);

CREATE TABLE IF NOT EXISTS room(
    _id INTEGER PRIMARY KEY,
    department_id INT,
    room_name VARCHAR(50),
    FOREIGN KEY(department_id) REFERENCES department(_id)
);

CREATE TABLE IF NOT EXISTS machine_status(
    _id INTEGER PRIMARY KEY,
    status_name VARCHAR(20) UNIQUE
);

CREATE TABLE IF NOT EXISTS machine(
    _id INTEGER PRIMARY KEY,
    room_id INT,
    machine_status_id INT,
    asset_tag VARCHAR(30) UNIQUE,
    scanned_time DATETIME,
    FOREIGN KEY(room_id) REFERENCES room(_id),
    FOREIGN KEY(machine_status_id) REFERENCES machine_status(_id)
);

INSERT OR IGNORE INTO building VALUES
(1, 'Mercy Boardman'),
(2, 'Mercy Downtown'),
(3, 'Mercy Warren');

INSERT OR IGNORE INTO building_floor VALUES
(1, 1, '1 B1'),
(2, 1, '2 B1'),
(3, 1, '3 B1'),
(4, 2, '1 B2'),
(5, 2, '2 B2'),
(6, 2, '3 B2'),
(7, 3, '1 B3'),
(8, 3, '2 B3'),
(9, 3, '3 B3');

INSERT OR IGNORE INTO department VALUES
(1, 1, 'Accounting1'),
(2, 2, 'Human Resources1'),
(3, 3, 'Surgery1'),
(4, 4, 'Accounting2'),
(5, 5, 'Human Resources2'),
(6, 6, 'Surgery2'),
(7, 7, 'Accounting3'),
(8, 8, 'Human Resources3'),
(9, 9, 'Surgery3');

INSERT OR IGNORE INTO room VALUES
(1, 1, 'room1'),
(2, 1, 'room2'),
(3, 2, 'room3'),
(4, 2, 'room4'),
(5, 3, 'room5'),
(6, 3, 'room6'),
(7, 4, 'room7'),
(8, 4, 'room8'),
(9, 5, 'room9'),
(10, 5, 'room10'),
(11, 6, 'room11'),
(12, 6, 'room12'),
(13, 7, 'room13'),
(14, 7, 'room14'),
(15, 8, 'room15'),
(16, 8, 'room16'),
(17, 9, 'room17'),
(18, 9, 'room18');


INSERT OR IGNORE INTO machine_status VALUES
(1, 'In Service'),
(2, 'Retired'),
(3, 'Broke'),
(4, 'Recycled');