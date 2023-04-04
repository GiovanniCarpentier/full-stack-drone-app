package be.howest.ti.mars.logic.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SeparatedBookings {

    private final List<Booking> pastBookings;
    private final List<Booking> futureBookings;

    public SeparatedBookings(List<Booking> bookings) {
        this.pastBookings = bookings.stream()
                .filter(booking -> !(booking.retrieveDatetime().isAfter(LocalDateTime.now())))
                .collect(Collectors.toList());
        this.futureBookings = bookings.stream()
                .filter(booking -> booking.retrieveDatetime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    public List<Booking> getPastBookings() {
        return pastBookings;
    }

    public List<Booking> getFutureBookings() {
        return futureBookings;
    }
}
