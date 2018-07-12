
CREATE TABLE IF NOT EXISTS building(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) not null
);

CREATE TABLE IF NOT EXISTS building_floor(
    id SERIAL PRIMARY KEY,
    building_id INT not null,
    name VARCHAR(20) not null,
    FOREIGN KEY(building_id) REFERENCES building(id)
);

CREATE TABLE IF NOT EXISTS department(
    id SERIAL PRIMARY KEY,
    building_floor_id INT not null,
    name VARCHAR(50) not null,
    FOREIGN KEY(building_floor_id) REFERENCES building_floor(id)
);

CREATE TABLE IF NOT EXISTS room(
    id SERIAL PRIMARY KEY,
    department_id INT not null,
    name VARCHAR(50) not null,
    FOREIGN KEY(department_id) REFERENCES department(id)
);

CREATE TABLE IF NOT EXISTS machine_status(
    id SERIAL PRIMARY KEY,
    status VARCHAR(20) not null UNIQUE
);

CREATE TABLE IF NOT EXISTS machine(
    id SERIAL PRIMARY KEY,
    room_id INT not null,
    machine_status_id INT not null,
    machine_name VARCHAR(30) not null UNIQUE,
    scanned_time timestamp not null DEFAULT now(),
    FOREIGN KEY(room_id) REFERENCES room(id),
    FOREIGN KEY(machine_status_id) REFERENCES machine_status(id)
);

CREATE TABLE IF NOT EXISTS user_group(
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) not null UNIQUE
);

CREATE TABLE IF NOT EXISTS title(
    id SERIAL PRIMARY KEY,
    title VARCHAR(20) not null UNIQUE,
    date_created TIMESTAMP not null DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_profile(
    id SERIAL PRIMARY KEY,
    title_id int not null,
    email VARCHAR(200) not null UNIQUE,
    password VARCHAR(1024) not null,
    first_name VARCHAR(50) not null,
    last_name VARCHAR(50) not null,
    is_active boolean not null,
    date_joined TIMESTAMP not null DEFAULT now(),
    FOREIGN KEY(title_id) REFERENCES title(id)
);

CREATE TABLE IF NOT EXISTS logging_history (
    id             uuid primary key not null DEFAULT uuid_generate_v4(),
    date_entered   timestamp DEFAULT now(),
    url            text not null,
    operation      text not null,
    value          json,
    entered_by_id  int REFERENCES user_profile(id)
);

CREATE TABLE IF NOT EXISTS user_group_join(
    id SERIAL PRIMARY KEY,
    user_profile_id INT not null,
    user_group_id INT not null,
    FOREIGN KEY(user_profile_id) REFERENCES user_profile(id),
    FOREIGN KEY(user_group_id) REFERENCES user_group(id)
);

INSERT INTO building VALUES
(DEFAULT, 'Building One'),
(DEFAULT, 'Building Two'),
(DEFAULT, 'Building Three');

INSERT INTO building_floor VALUES
(DEFAULT, 1, '1 B1'),
(DEFAULT, 1, '2 B1'),
(DEFAULT, 1, '3 B1'),
(DEFAULT, 2, '1 B2'),
(DEFAULT, 2, '2 B2'),
(DEFAULT, 2, '3 B2'),
(DEFAULT, 3, '1 B3'),
(DEFAULT, 3, '2 B3'),
(DEFAULT, 3, '3 B3');

INSERT INTO department VALUES
(DEFAULT, 1, 'Accounting1'),
(DEFAULT, 1, 'Human Resources1'),
(DEFAULT, 1, 'Engineering1'),
(DEFAULT, 2, 'Accounting2'),
(DEFAULT, 2, 'Human Resources2'),
(DEFAULT, 2, 'Engineering'),
(DEFAULT, 3, 'Accounting3'),
(DEFAULT, 3, 'Human Resources3'),
(DEFAULT, 3, 'Engineering3');

INSERT INTO room VALUES
(DEFAULT, 1, 'room1'),
(DEFAULT, 1, 'room2'),
(DEFAULT, 2, 'room3'),
(DEFAULT, 2, 'room4'),
(DEFAULT, 3, 'room5'),
(DEFAULT, 3, 'room6'),
(DEFAULT, 4, 'room7'),
(DEFAULT, 4, 'room8'),
(DEFAULT, 5, 'room9'),
(DEFAULT, 5, 'room10'),
(DEFAULT, 6, 'room11'),
(DEFAULT, 6, 'room12'),
(DEFAULT, 7, 'room13'),
(DEFAULT, 7, 'room14'),
(DEFAULT, 8, 'room15'),
(DEFAULT, 8, 'room16'),
(DEFAULT, 9, 'room17'),
(DEFAULT, 9, 'room18');

INSERT INTO machine_status VALUES
(DEFAULT, 'In Service'),
(DEFAULT, 'Retired'),
(DEFAULT, 'Broke'),
(DEFAULT, 'Recycled');

INSERT INTO title VALUES
(DEFAULT, 'Manager', '2017-01-02 15:10:00'),
(DEFAULT, 'Worker', '2017-01-02 15:10:00'),
(DEFAULT, 'Client', '2017-01-02 15:10:00');

INSERT INTO user_group VALUES
(DEFAULT, 'Any'),
(DEFAULT, 'User'),
(DEFAULT, 'Admin');

INSERT INTO user_profile VALUES
(DEFAULT, 2, 'worker@email.com', '$2a$10$bi8mFKrlUfYlXgeIJj6buucEgT0scC./LaMAqOfnAAHMEcTPaXqy2', 'Worker', 'Last', TRUE, '2018-05-10 10:00:00');

INSERT INTO user_group_join VALUES
(DEFAULT, 1, 2);