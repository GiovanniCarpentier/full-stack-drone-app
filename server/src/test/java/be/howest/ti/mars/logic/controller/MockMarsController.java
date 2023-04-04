package be.howest.ti.mars.logic.controller;

import be.howest.ti.mars.logic.domain.*;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;

public class MockMarsController implements MarsController {
    private static final String SOME_DATETIME_1 = "2053-05-23 14:25:20";
    private static final String SOME_DATETIME_2 = "2053-05-23 14:25:10";
    private static final String SOME_DATETIME_3 = "2053-05-23 14:25:00";
    private static final Coordinate SOME_COORDINATE = new Coordinate(358069.108243726, 6650245.066365996);

    private static final Drone SOME_DRONE_1 = new Drone(1, "Spark", false);
    private static final Drone SOME_DRONE_2 = new Drone(2, "Andromeda", false);
    private static final Drone SOME_DRONE_3 = new Drone(3, "Bob", false);
    private static final List<Drone> SOME_DRONE_LIST = new ArrayList<>(List.of(SOME_DRONE_1, SOME_DRONE_2, SOME_DRONE_3));

    private static final Booking SOME_BOOKING_1 = new Booking(1, 1, SOME_DATETIME_1, new Route(SOME_COORDINATE, SOME_COORDINATE));
    private static final Booking SOME_BOOKING_2 = new Booking(2, 1, SOME_DATETIME_1, new Route(SOME_COORDINATE, SOME_COORDINATE));
    private static final Booking SOME_BOOKING_3 = new Booking(3, 1, SOME_DATETIME_1, new Route(SOME_COORDINATE, SOME_COORDINATE));
    private static final List<Booking> SOME_BOOKING_LIST = new ArrayList<>(List.of(SOME_BOOKING_1, SOME_BOOKING_2, SOME_BOOKING_3));

    private static final Location SOME_LOCATION_1 = new Location(1, SOME_COORDINATE, SOME_DATETIME_1);
    private static final Location SOME_LOCATION_2 = new Location(1, SOME_COORDINATE, SOME_DATETIME_2);
    private static final Location SOME_LOCATION_3 = new Location(1, SOME_COORDINATE, SOME_DATETIME_3);
    private static final List<Location> SOME_LOCATION_HISTORY_LIST = new ArrayList<>(List.of(SOME_LOCATION_1, SOME_LOCATION_2, SOME_LOCATION_3));

    @Override
    public List<Drone> getAllDrones() {
        return SOME_DRONE_LIST;
    }

    @Override
    public SeparatedBookings getAllBookings() {
        return new SeparatedBookings(SOME_BOOKING_LIST);
    }

    @Override
    public List<Location> getDroneLocationHistory(int droneId, int depth) {
        int limit = Math.min(depth, SOME_LOCATION_HISTORY_LIST.size());
        return SOME_LOCATION_HISTORY_LIST.subList(0, limit);
    }

    @Override
    public Location getCurrentDroneLocation(int droneId) {
        return new Location(droneId, SOME_COORDINATE, SOME_DATETIME_1);
    }

    @Override
    public Ride getRide(int rideId) {
        Coordinate pickupLocation = new Coordinate(358069.108243726, 6650245.066365996);
        Coordinate destination = new Coordinate(365268.45397380774, 6659822.573579948);


        return new Ride(rideId, 1, 1, new Route(
                pickupLocation, destination), 50.00, true, "2053-05-23 14:25:10");
    }

    @Override
    public SeperatedRides getAllRides() {
        List<Ride> rideList = new ArrayList<>();

        Coordinate pickupLocation = new Coordinate(358069.108243726, 6650245.066365996);
        Coordinate destination = new Coordinate(365268.45397380774, 6659822.573579948);

        rideList.add(new Ride(1, 1, 1, new Route(pickupLocation, destination), 0.00, false, "2053-05-23 14:25:10"));
        rideList.add(new Ride(2, 2, 2, new Route(pickupLocation, destination), 5.70, true, "2053-05-23 14:25:10"));
        rideList.add(new Ride(3, 2, 3, new Route(pickupLocation, destination), 74.30, true, "2053-05-23 14:25:10"));

        return new SeperatedRides(rideList);
    }

    @Override
    public Ride createRide(int marsId, List<Double> pickupLocation, List<Double> destination) {
        return new Ride(1, marsId, 1, new Route(
                new Coordinate(pickupLocation.get(0), pickupLocation.get(1)),
                new Coordinate(destination.get(0), destination.get(1))), 50.00, true, "2053-05-23 14:25:10");
    }

    @Override
    public SeparatedBookings getBookings(int marsId) {
        List<Booking> bookingList = new ArrayList<>();

        Coordinate pickupLocation = new Coordinate(358069.108243726, 6650245.066365996);
        Coordinate destination = new Coordinate(365268.45397380774, 6659822.573579948);

        bookingList.add(new Booking(1, 1, "2053-05-23 14:25:10", new Route(pickupLocation, destination)));
        bookingList.add(new Booking(2, 1, "2053-05-23 14:25:10", new Route(pickupLocation, destination)));

        return new SeparatedBookings(bookingList);
    }

    @Override
    public Booking createBooking(int marsId, String datetime, List<Double> pickupLocation, List<Double> destination) {
        return new Booking(10, marsId, datetime, new Route(new Coordinate(pickupLocation.get(0), pickupLocation.get(1)), new Coordinate(destination.get(0), destination.get(1))));
    }


    @Override
    public void deleteBooking(int bookingId) {
    }
}
