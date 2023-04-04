package be.howest.ti.mars.logic.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Location {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int droneId;
    private final Coordinate coordinate;
    private final LocalDateTime datetime;

    public Location(int droneId, Coordinate coordinate, String datetime) {
        this.droneId = droneId;
        this.coordinate = coordinate;
        this.datetime = LocalDateTime.parse(datetime, FORMATTER);
    }

    public int getDroneId() {
        return droneId;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getDatetime() {
        return datetime.format(FORMATTER);
    }
}
