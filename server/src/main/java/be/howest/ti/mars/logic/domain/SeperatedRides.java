package be.howest.ti.mars.logic.domain;

import java.util.List;
import java.util.stream.Collectors;

public class SeperatedRides {
    private final List<Ride> activeRides;
    private final List<Ride> completedRides;

    public SeperatedRides(List<Ride> rides) {
        this.activeRides = rides.stream()
                .filter(ride -> !(ride.isCompleted()))
                .collect(Collectors.toList());
        this.completedRides = rides.stream()
                .filter(Ride::isCompleted)
                .collect(Collectors.toList());
    }

    public List<Ride> getActiveRides() {
        return activeRides;
    }

    public List<Ride> getCompletedRides() {
        return completedRides;
    }
}
