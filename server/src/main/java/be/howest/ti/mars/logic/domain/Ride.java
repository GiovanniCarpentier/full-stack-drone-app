package be.howest.ti.mars.logic.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Ride {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int rideId;
    private final int marsId;
    private final int droneId;
    private final Route route;
    private final double fare;
    private final boolean isCompleted;
    private final LocalDateTime completionDate;

    public Ride(int rideId, int marsId, int droneId, Route route, double fare, boolean isCompleted, String completionDate) {
        this.rideId = rideId;
        this.marsId = marsId;
        this.droneId = droneId;
        this.route = route;
        this.fare = fare;
        this.isCompleted = isCompleted;
        this.completionDate = LocalDateTime.parse(completionDate, FORMATTER);
    }

    public int getRideId() {
        return rideId;
    }

    public int getMarsId() {
        return marsId;
    }

    public int getDroneId() {
        return droneId;
    }

    public Route getRoute() {
        return route;
    }

    public double getFare() {
        return fare;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getCompletionDate() {
        return isCompleted ? completionDate.format(FORMATTER) : "";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride ride = (Ride) o;
        return rideId == ride.rideId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rideId);
    }
}
