package be.howest.ti.mars.logic.controller;

import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.*;

import java.util.List;

/**
 * DefaultMarsController is the default implementation for the MarsController interface.
 * It should NOT be aware that it is used in the context of a webserver:
 * <p>
 * This class and all other classes in the logic-package (or future sub-packages)
 * should use 100% plain old Java Objects (POJOs). The use of Json, JsonObject or
 * Strings that contain encoded/json data should be avoided here.
 * Do not be afraid to create your own Java classes if needed.
 * <p>
 * Note: Json and JsonObject can (and should) be used in the web-package however.
 * <p>
 * (please update these comments in the final version)
 */

public class DefaultMarsController implements MarsController {

    @Override
    public List<Drone> getAllDrones() {
        return Repositories.getH2Repo().getAllDrones();
    }

    @Override
    public List<Location> getDroneLocationHistory(int droneId, int depth) {
        return Repositories.getH2Repo().getDroneLocationHistory(droneId, depth);
    }

    @Override
    public Location getCurrentDroneLocation(int droneId) {
        return Repositories.getH2Repo().getCurrentDroneLocation(droneId);
    }

    @Override
    public Ride getRide(int rideId) {
        return Repositories.getH2Repo().getRide(rideId);
    }

    @Override
    public SeperatedRides getAllRides() {
        List<Ride> rideList = Repositories.getH2Repo().getAllRides();
        return new SeperatedRides(rideList);
    }

    @Override
    public Ride createRide(int marsId, List<Double> pickupLocation, List<Double> destination) {
        return Repositories.getH2Repo().insertRide(marsId, pickupLocation, destination);
    }

    @Override
    public SeparatedBookings getAllBookings() {
        List<Booking> sortedBookings = Repositories.getH2Repo().getAllBookings();
        return new SeparatedBookings(sortedBookings);
    }

    @Override
    public SeparatedBookings getBookings(int marsId) {
        List<Booking> sortedBookings = Repositories.getH2Repo().getBookings(marsId);
        return new SeparatedBookings(sortedBookings);
    }

    @Override
    public Booking createBooking(int marsId, String datetime, List<Double> pickupLocation, List<Double> destination) {
        return Repositories.getH2Repo().insertBooking(marsId, datetime, pickupLocation, destination);
    }

    @Override
    public void deleteBooking(int bookingId) {
        Repositories.getH2Repo().deleteBooking(bookingId);
    }
}