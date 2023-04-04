package be.howest.ti.mars.logic.domain;

public class Route {
    private final Coordinate pickupLocation;
    private final Coordinate destination;

    public Route(Coordinate pickupLocation, Coordinate destination) {
        this.pickupLocation = pickupLocation;
        this.destination = destination;
    }

    public Coordinate getPickupLocation() {
        return pickupLocation;
    }

    public Coordinate getDestination() {
        return destination;
    }
}
