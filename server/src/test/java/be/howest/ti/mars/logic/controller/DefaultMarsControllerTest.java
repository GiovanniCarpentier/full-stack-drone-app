package be.howest.ti.mars.logic.controller;

import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultMarsControllerTest {

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

    @Test
    void getDrones() {
        MarsController sut = new DefaultMarsController();
        sut.getAllDrones().forEach(drone -> assertEquals(Drone.class, drone.getClass()));
        Assertions.assertNotEquals(sut.getAllDrones().get(0), sut.getAllDrones().get(1));
        Assertions.assertEquals(sut.getAllDrones().get(0), sut.getAllDrones().get(0));
    }

    @Test
    void getDroneLocationHistory() {
        MarsController sut = new DefaultMarsController();
        sut.getDroneLocationHistory(1, 50).forEach(location -> assertEquals(Location.class, location.getClass()));
    }

    @Test
    void getDroneCurrentDroneLocation() {
        MarsController sut = new DefaultMarsController();
        assertEquals(Location.class, sut.getCurrentDroneLocation(1).getClass());
    }

    @Test
    void getRides() {
        MarsController sut = new DefaultMarsController();
        Assertions.assertEquals(SeperatedRides.class, sut.getAllRides().getClass());
        Assertions.assertNotEquals(sut.getAllRides().getActiveRides().get(0), sut.getAllRides().getCompletedRides().get(0));
    }

    @Test
    void getRide() {
        MarsController sut = new DefaultMarsController();
        Assertions.assertEquals(Ride.class, sut.getRide(1).getClass());
    }

    @Test
    void createRide() {
        MarsController sut = new DefaultMarsController();
        Ride ride = sut.createRide(1, new ArrayList<>(List.of(358069.108243726, 6650245.066365996)), new ArrayList<>(List.of(365268.45397380774, 6659822.573579948)));
        assertNotNull(ride);
    }

    @Test
    void getBookings() {
        MarsController sut = new DefaultMarsController();
        Assertions.assertEquals(SeparatedBookings.class, sut.getBookings(1).getClass());
    }

    @Test
    void createBooking() {
        MarsController sut = new DefaultMarsController();
        Booking booking = sut.createBooking(1, "2052-05-23 14:25:10", new ArrayList<>(List.of(358069.108243726, 6650245.066365996)), new ArrayList<>(List.of(365268.45397380774, 6659822.573579948)));
        assertNotNull(booking);
    }

    @Test
    void deleteBooking() {
        MarsController sut = new DefaultMarsController();
        int lengthOfBookings = sut.getAllBookings().getFutureBookings().size();
        sut.deleteBooking(1);
        assertEquals(lengthOfBookings - 1, sut.getAllBookings().getFutureBookings().size());
    }
}
