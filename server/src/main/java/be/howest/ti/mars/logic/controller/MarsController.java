package be.howest.ti.mars.logic.controller;

import be.howest.ti.mars.logic.domain.*;

import java.util.List;

public interface MarsController {

    List<Drone> getAllDrones();

    List<Location> getDroneLocationHistory(int droneId, int depth);

    Location getCurrentDroneLocation(int droneId);

    SeperatedRides getAllRides();

    Ride getRide(int rideId);

    Ride createRide(int marsId, List<Double> pickupLocation, List<Double> destination);

    SeparatedBookings getAllBookings();

    SeparatedBookings getBookings(int marsId);

    Booking createBooking(int marsId, String datetime, List<Double> pickupLocation, List<Double> destination);

    void deleteBooking(int bookingId);
}
