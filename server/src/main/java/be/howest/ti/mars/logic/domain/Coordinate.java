package be.howest.ti.mars.logic.domain;

import java.util.List;

public class Coordinate {

    private final Double lon;
    private final Double lat;

    public Coordinate(Double lon, Double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public List<Double> getCoordinateList() {
        return List.of(lon, lat);
    }
}
