package be.howest.ti.mars.logic.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Booking {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int bookingId;
    private final int marsId;
    private final LocalDateTime datetime;
    private final Route route;

    public Booking(int bookingId, int marsId, String datetime, Route route) {
        this.bookingId = bookingId;
        this.marsId = marsId;
        this.datetime = LocalDateTime.parse(datetime, FORMATTER);
        this.route = route;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getMarsId() {
        return marsId;
    }

    public String getDatetime() {
        return datetime.format(FORMATTER);
    }

    public Route getRoute() {
        return route;
    }

    public LocalDateTime retrieveDatetime() {
        return datetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return bookingId == booking.bookingId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }
}
