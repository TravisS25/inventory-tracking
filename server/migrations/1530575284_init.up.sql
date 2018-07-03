
CREATE TABLE IF NOT EXISTS building(
    id SERIAL PRIMARY KEY,
    building_name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS building_floor(
    id SERIAL PRIMARY KEY,
    building_id INT,
    floor_name VARCHAR(20),
    FOREIGN KEY(building_id) REFERENCES building(id)
);

CREATE TABLE IF NOT EXISTS department(
    id SERIAL PRIMARY KEY,
    building_floor_id INT,
    department_name VARCHAR(50),
    FOREIGN KEY(building_floor_id) REFERENCES building_floor(id)
);

CREATE TABLE IF NOT EXISTS room(
    id SERIAL PRIMARY KEY,
    department_id INT,
    room_name VARCHAR(50),
    FOREIGN KEY(department_id) REFERENCES department(id)
);

CREATE TABLE IF NOT EXISTS machine_status(
    id SERIAL PRIMARY KEY,
    status_name VARCHAR(20) UNIQUE
);

CREATE TABLE IF NOT EXISTS machine(
    id SERIAL PRIMARY KEY,
    room_id INT,
    machine_status_id INT,
    machine_name VARCHAR(30) UNIQUE,
    scanned_time timestamp DEFAULT now(),
    FOREIGN KEY(room_id) REFERENCES room(id),
    FOREIGN KEY(machine_status_id) REFERENCES machine_status(id)
);

CREATE TABLE IF NOT EXISTS user_group(
    id SERIAL PRIMARY KEY,
    group_name VARCHAR(30) UNIQUE
);

CREATE TABLE IF NOT EXISTS title(
    id SERIAL PRIMARY KEY,
    title VARCHAR(20) UNIQUE,
    date_created TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_profile(
    id SERIAL PRIMARY KEY,
    title_id int,
    email VARCHAR(200) UNIQUE,
    password VARCHAR(1024),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    date_joined TIMESTAMP DEFAULT now(),
    FOREIGN KEY(title_id) REFERENCES title(id)
);

-- CREATE TABLE IF NOT EXISTS user_log(
--     id BIGSERIAL PRIMARY KEY,
--     user_profile_id int,
--     ip_address VARCHAR(20),
--     login_time TIMESTAMP,
--     FOREIGN KEY(user_profile_id) REFERENCES user_profile(id)
-- );

CREATE TABLE IF NOT EXISTS user_group_join(
    id SERIAL PRIMARY KEY,
    user_profile_id INT,
    user_group_id INT,
    FOREIGN KEY(user_profile_id) REFERENCES user_profile(id),
    FOREIGN KEY(user_group_id) REFERENCES user_group(id)
);

INSERT INTO building VALUES
(1, 'Mercy Boardman'),
(2, 'Mercy Downtown'),
(3, 'Mercy Warren');

INSERT INTO building_floor VALUES
(1, 1, '1 B1'),
(2, 1, '2 B1'),
(3, 1, '3 B1'),
(4, 2, '1 B2'),
(5, 2, '2 B2'),
(6, 2, '3 B2'),
(7, 3, '1 B3'),
(8, 3, '2 B3'),
(9, 3, '3 B3');

INSERT INTO department VALUES
(1, 1, 'Accounting1'),
(2, 1, 'Human Resources1'),
(3, 1, 'Surgery1'),
(4, 2, 'Accounting2'),
(5, 2, 'Human Resources2'),
(6, 2, 'Surgery2'),
(7, 3, 'Accounting3'),
(8, 3, 'Human Resources3'),
(9, 3, 'Surgery3');

INSERT INTO room VALUES
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

INSERT INTO machine_status VALUES
(1, 'In Service'),
(2, 'Retired'),
(3, 'Broke'),
(4, 'Recycled');

INSERT INTO title VALUES
(1, 'Manager', '2017-01-02 15:10:00'),
(2, 'Worker', '2017-01-02 15:10:00'),
(3, 'Client', '2017-01-02 15:10:00');

INSERT INTO user_group VALUES
(1, 'Any'),
(2, 'User'),
(3, 'Admin');