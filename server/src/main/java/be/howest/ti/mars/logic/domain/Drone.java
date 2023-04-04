package be.howest.ti.mars.logic.domain;

import java.util.Objects;

public class Drone {
    private final int droneId;
    private final String name;
    private final boolean onRide;

    public Drone(int droneId, String name, boolean onRide) {
        this.droneId = droneId;
        this.name = name;
        this.onRide = onRide;
    }

    public int getDroneId() {
        return droneId;
    }

    public String getName() {
        return name;
    }

    public boolean isOnRide() {
        return onRide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drone drone = (Drone) o;
        return droneId == drone.droneId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(droneId);
    }
}
