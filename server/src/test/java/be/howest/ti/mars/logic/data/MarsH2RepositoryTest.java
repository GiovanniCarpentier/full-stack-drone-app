package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.domain.*;
import be.howest.ti.mars.logic.exceptions.MarsResourceNotFoundException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarsH2RepositoryTest {
    private static final String URL = "jdbc:h2:~/mars-db";

    @BeforeAll
    void setupTestSuite() {
        Repositories.shutdown();
        JsonObject dbProperties = new JsonObject(Map.of("url", "jdbc:h2:~/mars-db",
                "username", "",
                "password", "",
                "webconsole.port", 9000));
        WebClient.create(Vertx.vertx());
        Repositories.configure(dbProperties);
    }

    @BeforeEach
    void setupTest() {
        Repositories.getH2Repo().generateData();
    }

    /**
     * Drones
     */
    @Test
    void getDrones() {
        List<Drone> drones = Repositories.getH2Repo().getAllDrones();
        drones.forEach(drone -> Assertions.assertEquals(Drone.class, drone.getClass()));

        Assertions.assertEquals(1, drones.get(0).getDroneId());
        Assertions.assertEquals(String.class, drones.get(0).getName().getClass());
        Assertions.assertEquals(1, drones.get(0).getDroneId());
        Assertions.assertFalse(drones.get(0).isOnRide());

        Assertions.assertNotEquals(drones.get(0), drones.get(1));
    }

    @Test
    void getDroneLocationHistory() {
        List<Location> droneLocationHistory = Repositories.getH2Repo().getDroneLocationHistory(1, 50);
        droneLocationHistory.forEach(location -> Assertions.assertEquals(Location.class, location.getClass()));

        droneLocationHistory.forEach(location -> Assertions.assertEquals(1, location.getDroneId()));
        droneLocationHistory.forEach(location -> Assertions.assertEquals(Coordinate.class, location.getCoordinate().getClass()));
        droneLocationHistory.forEach(location -> Assertions.assertEquals(String.class, location.getDatetime().getClass()));
    }

    @Test
    void getWrongIdDroneLocationHistory() {
        MarsH2Repository repo = Repositories.getH2Repo();
        Assertions.assertThrows(MarsResourceNotFoundException.class, () -> repo.getDroneLocationHistory(-1, 50));
    }

    @Test
    void getCurrentDroneLocation() {
        Location location = Repositories.getH2Repo().getCurrentDroneLocation(1);
        Assertions.assertEquals(Location.class, location.getClass());
    }

    @Test
    void getWrongIdDroneLocation() {
        MarsH2Repository repo = Repositories.getH2Repo();
        Assertions.assertThrows(MarsResourceNotFoundException.class, () -> repo.getCurrentDroneLocation(-1));
    }

    /**
     * Rides
     */
    @Test
    void getRides() {
        List<Ride> rideList = Repositories.getH2Repo().getAllRides();
        Assertions.assertNotNull(rideList);
        rideList.forEach(ride -> Assertions.assertEquals(Ride.class, ride.getClass()));

    }

    @Test
    void getRide() {
        Ride ride = Repositories.getH2Repo().getRide(1);
        Assertions.assertNotNull(ride);
        Assertions.assertEquals(1, ride.getMarsId());
        Assertions.assertEquals(1, ride.getRideId());
        Assertions.assertEquals(1, ride.getDroneId());
        Assertions.assertEquals(0.00, ride.getFare());
        Assertions.assertFalse(ride.isCompleted());
        Assertions.assertEquals(Coordinate.class, ride.getRoute().getPickupLocation().getClass());
    }

    @Test
    void getRideWrongId() {
        MarsH2Repository repo = Repositories.getH2Repo();
        Assertions.assertThrows(MarsResourceNotFoundException.class, () -> repo.getRide(-1));
    }

    @Test
    void createRide() {
        Ride ride = Repositories.getH2Repo().insertRide(1, new ArrayList<>(List.of(358069.108243726, 6650245.066365996)), new ArrayList<>(List.of(365268.45397380774, 6659822.573579948)));
        Assertions.assertNotNull(ride);
        Assertions.assertEquals(1, ride.getMarsId());
        Assertions.assertEquals(4, ride.getRideId());
        Assertions.assertEquals(1, ride.getDroneId());
        Assertions.assertEquals(0.00, ride.getFare());
        Assertions.assertFalse(ride.isCompleted());
        Assertions.assertEquals(Coordinate.class, ride.getRoute().getPickupLocation().getClass());
    }

    /**
     * Bookings
     */
    @Test
    void getBookings() {
        List<Booking> bookings = Repositories.getH2Repo().getAllBookings();
        bookings.forEach(booking -> Assertions.assertEquals(Booking.class, booking.getClass()));

        Assertions.assertEquals(2, bookings.get(0).getBookingId());
        Assertions.assertEquals(2, bookings.get(0).getMarsId());
        Assertions.assertEquals(String.class, bookings.get(0).getDatetime().getClass());
        Assertions.assertEquals(LocalDateTime.class, bookings.get(0).retrieveDatetime().getClass());

        Assertions.assertNotEquals(bookings.get(0), bookings.get(1));
        Assertions.assertEquals(bookings.get(0), bookings.get(0));
    }

    @Test
    void getBookingsByMarsId() {
        List<Booking> bookings = Repositories.getH2Repo().getBookings(1);
        bookings.forEach(booking -> Assertions.assertEquals(Booking.class, booking.getClass()));
    }

    @Test
    void createBooking() {
        Booking booking = Repositories.getH2Repo().insertBooking(1, "2052-05-23 14:25:10", new ArrayList<>(List.of(358069.108243726, 6650245.066365996)), new ArrayList<>(List.of(365268.45397380774, 6659822.573579948)));
        Assertions.assertNotNull(booking);
    }

    @Test
    void deleteBooking() {
        int id = 1;

        int length = Repositories.getH2Repo().getAllBookings().size();
        Repositories.getH2Repo().deleteBooking(id);
        Assertions.assertEquals(length - 1, Repositories.getH2Repo().getAllBookings().size());
    }

    @Test
    void deleteBookingWrongId() {
        MarsH2Repository repo = Repositories.getH2Repo();
        int length = repo.getAllBookings().size();

        Assertions.assertThrows(MarsResourceNotFoundException.class, () -> repo.deleteBooking(-1));
        Assertions.assertEquals(length, Repositories.getH2Repo().getAllBookings().size());
    }
}
