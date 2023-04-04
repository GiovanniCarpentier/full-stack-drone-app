INSERT INTO drones(name)
VALUES ('Spark');
INSERT INTO drones(name)
VALUES ('Andromeda');
INSERT INTO drones(name)
VALUES ('Medea');

INSERT INTO location_history(drone_id, coordinate, datetime)
VALUES (1, '358069.108243726, 6650245.066365996', '2053-05-23T14:25:10');
INSERT INTO location_history(drone_id, coordinate, datetime)
VALUES (1, '358069.108243726, 6650245.066365996', '2053-05-23T14:25:20');
INSERT INTO location_history(drone_id, coordinate, datetime)
VALUES (1, '358069.108243726, 6650245.066365996', '2053-05-23T14:25:30');
INSERT INTO location_history(drone_id, coordinate, datetime)
VALUES (2, '360201.67092477204, 6649405.956528121', '2052-05-23T14:25:30');


INSERT INTO bookings(mars_id, datetime, pickup_location, destination)
VALUES (1, '2052-05-23T14:25', '357714.95014137396, 6655280.110244273', '360201.67092477204, 6649405.956528121');
INSERT INTO bookings(mars_id, datetime, pickup_location, destination)
VALUES (2, '2055-05-23T14:41', '357714.95014137396, 6655280.110244273', '361670.55712160317, 6663912.8330828175');
INSERT INTO bookings(mars_id, datetime, pickup_location, destination)
VALUES (3, '2020-05-23T14:25', '348440.9204461244, 6675973.242983814', '351443.7253177136, 6674284.165824488');
INSERT INTO bookings(mars_id, datetime, pickup_location, destination)
VALUES (4, '2016-05-23T14:41', '414342.9824709746, 6630894.0469409395', '422788.3643538901, 6630768.929317083');

INSERT INTO rides(mars_id, drone_id, pickup_location, destination, fare, is_completed, completion_date)
VALUES (1, 1, '357714.95014137396, 6655280.110244273', '360201.67092477204, 6649405.956528121', 0.00, false,
        '2016-05-23T14:41');
INSERT INTO rides(mars_id, drone_id, pickup_location, destination, fare, is_completed, completion_date)
VALUES (2, 1, '357714.95014137396, 6655280.110244273', '361670.55712160317, 6663912.8330828175', 16.50, true,
        '2016-05-23T14:41');
INSERT INTO rides(mars_id, drone_id, pickup_location, destination, fare, is_completed, completion_date)
VALUES (3, 2, '358069.108243726, 6650245.066365996', '358069.108243726, 6650245.066365996', 5.50, true,
        '2016-05-23T14:41');
