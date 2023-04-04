package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.domain.*;
import be.howest.ti.mars.logic.exceptions.MarsResourceNotFoundException;
import be.howest.ti.mars.logic.exceptions.RepositoryException;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2.tools.Server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/*
This is only a starter class to use an H2 database.
In this start-project there was no need for an Interface MarsRepository.
Please always use interfaces when needed.
To make this class useful, please complete it with the topics seen in the module OOA & SD
- Make sure the conf/config.json (local & production) properties are correct.
- The h2 web console is available at http://localhost:9000
- The h2 database file is located at ~/mars-db
- Don't create tables manually but create sql files in the folder resources.
  - With each deploy create the db structure from scratch (it's just a poc).
  - Two starter sql script are already given.
- Hint:
  - Mocking this repository for testing purposes is not needed.
    Create database creating and population scripts in plain SQL (resources folder).
    Use the @Before or @BeforeEach (depending on the type of test) to quickly create a populated database.
 */

public class MarsH2Repository {
    private static final Logger LOGGER = Logger.getLogger(MarsH2Repository.class.getName());

    private static final String SQL_GET_DRONES = "select * from drones;";
    private static final String SQL_GET_DRONE_BY_ID = "select * from drones where id = ?;";
    private static final String SQL_GET_LOCATION_HISTORY = "select * from location_history where drone_id = ? order by datetime desc limit ?;";

    private static final String SQL_GET_RIDE = "select * from rides where id = ?;";
    private static final String SQL_GET_RIDES = "select * from rides";
    private static final String SQL_INSERT_RIDE = "insert into rides (mars_id, drone_id, pickup_location, destination) VALUES (?, ?, ?, ?);";
    private static final String SQL_GET_RIDE_BY_ID = "select * from rides where id = ?";


    private static final String SQL_GET_ALL_BOOKINGS = "select * from bookings order by datetime desc;";
    private static final String SQL_GET_BOOKINGS = "select * from bookings where mars_id = ?;";
    private static final String SQL_DELETE_BOOKING = "delete from bookings where id = ?;";
    private static final String SQL_GET_BOOKING_BY_ID = "select * from bookings where id = ?;";
    private static final String SQL_INSERT_BOOKING = "insert into bookings (mars_id, datetime, pickup_location, destination) VALUES (?, ?, ?, ?);";

    private static final int DEFAULT_DRONE_ID = 1;
    private static final String SPEC_DRONE_ID = "drone_id";
    private static final String SPEC_NO_DRONE_FOUND = "No drone with given id was found";

    private final Server dbWebConsole;
    private final String username;
    private final String password;
    private final String url;

    public MarsH2Repository(String url, String username, String password, int console) {
        try {
            this.username = username;
            this.password = password;
            this.url = url;
            this.dbWebConsole = Server.createWebServer(
                    "-ifNotExists",
                    "-webPort", String.valueOf(console)).start();
            LOGGER.log(Level.INFO, "Database web console started on port: {0}", console);
            this.generateData();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "DB configuration failed", ex);
            throw new RepositoryException("Could not configure MarsH2repository");
        }
    }

    public List<Drone> getAllDrones() {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_GET_DRONES)
        ) {
            List<Drone> drones = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Drone drone = new Drone(rs.getInt("id"),
                            rs.getString("name"),
                            rs.getBoolean("on_ride"));
                    drones.add(drone);
                }
            }
            return drones;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get drones", ex);
            throw new RepositoryException("Could not get drones");
        }
    }

    public List<Location> getDroneLocationHistory(int droneId, int depth) {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_GET_LOCATION_HISTORY)
        ) {
            stmt.setInt(1, droneId);
            stmt.setInt(2, depth);
            List<Location> locationHistory = new ArrayList<>();

            ensureExistence(droneId, SQL_GET_DRONE_BY_ID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    locationHistory.add(getLocationFromResultSet(rs));
                }
                return locationHistory;
            }
        } catch (JdbcSQLNonTransientException ex) {
            LOGGER.log(Level.SEVERE, SPEC_NO_DRONE_FOUND, ex);
            throw new MarsResourceNotFoundException(SPEC_NO_DRONE_FOUND);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get location", ex);
            throw new RepositoryException("Could not get location");
        }
    }

    public Location getCurrentDroneLocation(int droneId) {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_GET_LOCATION_HISTORY)
        ) {
            stmt.setInt(1, droneId);
            stmt.setInt(2, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return getLocationFromResultSet(rs);
            }
        } catch (JdbcSQLNonTransientException ex) {
            LOGGER.log(Level.SEVERE, SPEC_NO_DRONE_FOUND, ex);
            throw new MarsResourceNotFoundException(SPEC_NO_DRONE_FOUND);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get location", ex);
            throw new RepositoryException("Could not get location");
        }
    }

    public List<Ride> getAllRides() {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_GET_RIDES)
        ) {
            List<Ride> rides = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rides.add(getRideFromResultSet(rs));
                }
            }
            return rides;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to rides", ex);
            throw new RepositoryException("Could not get rides");
        }
    }

    public Ride getRide(int rideId) {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_GET_RIDE)
        ) {
            stmt.setInt(1, rideId);

            ensureExistence(rideId, SQL_GET_RIDE_BY_ID);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return getRideFromResultSet(rs);
            }
        } catch (JdbcSQLNonTransientException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get ride with given rideId", ex);
            throw new MarsResourceNotFoundException("Could not get ride with given rideId");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get ride", ex);
            throw new RepositoryException("Could not get ride");
        }
    }

    public Ride insertRide(int marsId, List<Double> pickupLocation, List<Double> destination) {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_RIDE, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setInt(1, marsId);
            stmt.setInt(2, DEFAULT_DRONE_ID);
            stmt.setString(3, stringifyAngleList(pickupLocation));
            stmt.setString(4, stringifyAngleList(destination));

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Creating ride failed (no rows affected)");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int rideId = generatedKeys.getInt(1);
                    return getRide(rideId);
                } else {
                    throw new SQLException("Creating ride failed (no id obtained)");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create ride", ex);
            throw new RepositoryException("Could not create ride");
        }
    }

    public List<Booking> getAllBookings() {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_GET_ALL_BOOKINGS)
        ) {
            List<Booking> bookings = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(getBookingFromResultSet(rs));
                }
            }
            return bookings;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get bookings", ex);
            throw new RepositoryException("Could not get bookings");
        }
    }

    public List<Booking> getBookings(int marsId) {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_GET_BOOKINGS)
        ) {
            List<Booking> bookings = new ArrayList<>();
            stmt.setInt(1, marsId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(getBookingFromResultSet(rs));
                }
                return bookings;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get bookings with marsId", ex);
            throw new RepositoryException("Could not get bookings with marsId");
        }
    }

    public Booking insertBooking(int marsId, String datetime, List<Double> pickupLocation, List<Double> destination) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_BOOKING, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, marsId);
            stmt.setString(2, datetime);
            stmt.setString(3, stringifyAngleList(pickupLocation));
            stmt.setString(4, stringifyAngleList(destination));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating booking failed (no rows affected)");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int bookingId = generatedKeys.getInt(1);
                    Coordinate pickupLocationCoords = new Coordinate(pickupLocation.get(0), pickupLocation.get(1));
                    Coordinate destinationCoords = new Coordinate(destination.get(0), destination.get(1));
                    return new Booking(bookingId, marsId, datetime, new Route(pickupLocationCoords, destinationCoords));
                } else {
                    throw new SQLException("Creating booking failed (no id obtained)");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create booking", ex);
            throw new RepositoryException("Could not create booking");
        }
    }

    public void deleteBooking(int bookingId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BOOKING)
        ) {
            ensureExistence(bookingId, SQL_GET_BOOKING_BY_ID);

            stmt.setInt(1, bookingId);
            stmt.execute();
        } catch (JdbcSQLNonTransientException ex) {
            LOGGER.log(Level.SEVERE, "No booking with given id was found", ex);
            throw new MarsResourceNotFoundException("No booking with given id was found");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to delete booking", ex);
            throw new RepositoryException("Could not delete booking");
        }
    }

    private Location getLocationFromResultSet(ResultSet rs) throws SQLException {
        return new Location(rs.getInt(SPEC_DRONE_ID),
                parseStringedCoordinate(rs.getString("coordinate")),
                rs.getString("datetime"));
    }

    private Ride getRideFromResultSet(ResultSet rs) throws SQLException {
        Coordinate pickupLocation = parseStringedCoordinate(rs.getString("pickup_location"));
        Coordinate destination = parseStringedCoordinate(rs.getString("destination"));

        return new Ride(rs.getInt("id"),
                rs.getInt("mars_id"),
                rs.getInt(SPEC_DRONE_ID),
                new Route(pickupLocation, destination),
                rs.getDouble("fare"),
                rs.getBoolean("is_completed"),
                rs.getString("completion_date"));
    }

    private Booking getBookingFromResultSet(ResultSet rs) throws SQLException {
        Coordinate pickupLocation = parseStringedCoordinate(rs.getString("pickup_location"));
        Coordinate destination = parseStringedCoordinate(rs.getString("destination"));

        return new Booking(rs.getInt("id"),
                rs.getInt("mars_id"),
                rs.getString("datetime"),
                new Route(pickupLocation, destination));
    }

    private Coordinate parseStringedCoordinate(String stringedCoordinate) {
        String[] splitCoordinate = stringedCoordinate.split(",");
        List<Double> coordinateList = Arrays.stream(splitCoordinate).map(Double::parseDouble).collect(Collectors.toList());
        return new Coordinate(coordinateList.get(0), coordinateList.get(1));
    }

    private String stringifyAngleList(List<Double> angleList) {
        return angleList.get(0).toString() + ", " + angleList.get(1).toString();
    }

    private void ensureExistence(int id, String sqlStmt) throws SQLException {
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlStmt)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                rs.getInt("id");
            }
        }
    }

    public void cleanUp() {
        if (dbWebConsole != null && dbWebConsole.isRunning(false))
            dbWebConsole.stop();
    }

    public void generateData() {
        try {
            executeScript("db-create.sql");
            executeScript("db-populate.sql");
        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Execution of database scripts failed.", ex);
        }
    }

    private void executeScript(String fileName) throws IOException, SQLException {
        String createDbSql = readFile(fileName);
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(createDbSql)
        ) {
            stmt.executeUpdate();
        }
    }

    private String readFile(String fileName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null)
            throw new RepositoryException("Could not read file: " + fileName);

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}