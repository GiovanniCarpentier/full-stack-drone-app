package be.howest.ti.mars.web.bridge;

import be.howest.ti.mars.logic.domain.Booking;
import be.howest.ti.mars.logic.domain.Drone;
import be.howest.ti.mars.logic.domain.Location;
import be.howest.ti.mars.logic.domain.Ride;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The Response class is responsible for translating the result of the controller into
 * JSON responses with an appropriate HTTP code.
 */
public class Response {

    public static final String SPEC_MARS_ID = "marsId";
    public static final String SPEC_DATETIME = "datetime";
    public static final String SPEC_ROUTE = "route";
    public static final String SPEC_DRONE_ID = "droneId";
    public static final String SPEC_PICKUP_LOCATION = "pickupLocation";
    public static final String SPEC_DESTINATION = "destination";

    private Response() {
    }

    public static void sendDrones(RoutingContext ctx, List<Drone> drones) {
        sendJsonResponse(ctx, 200, new JsonObject().put("drones", drones));
    }

    public static void getDroneLocationHistory(RoutingContext ctx, int droneId, List<Location> locationHistory) {
        sendJsonResponse(ctx, 200, new JsonObject()
                .put(SPEC_DRONE_ID, droneId)
                .put("locationHistory", createLocationObjects(locationHistory)));
    }

    public static void sendCurrentDroneLocation(RoutingContext ctx, Location currentLocation) {
        sendJsonResponse(ctx, 200, new JsonObject()
                .put(SPEC_DRONE_ID, currentLocation.getDroneId())
                .put("coordinate", currentLocation.getCoordinate().getCoordinateList())
                .put(SPEC_DATETIME, currentLocation.getDatetime()));
    }

    public static void sendAllBookings(RoutingContext ctx, List<Booking> pastBookings, List<Booking> futureBookings) {
        sendJsonResponse(ctx, 200, new JsonObject()
                .put("futureBookings", createBookingObjects(futureBookings))
                .put("pastBookings", createBookingObjects(pastBookings)));
    }

    private static List<JsonObject> createBookingObjects(List<Booking> bookings) {
        return bookings.stream().map(booking -> new JsonObject()
                .put("bookingId", booking.getBookingId())
                .put(SPEC_MARS_ID, booking.getMarsId())
                .put(SPEC_DATETIME, booking.getDatetime())
                .put(SPEC_ROUTE, new JsonObject()
                        .put(SPEC_PICKUP_LOCATION, booking.getRoute().getPickupLocation().getCoordinateList())
                        .put(SPEC_DESTINATION, booking.getRoute().getDestination().getCoordinateList())
                )).collect(Collectors.toList());
    }

    private static List<JsonObject> createLocationObjects(List<Location> locationHistory) {
        return locationHistory.stream().map(location -> new JsonObject()
                .put("coordinate", location.getCoordinate().getCoordinateList())
                .put(SPEC_DATETIME, location.getDatetime()
                )).collect(Collectors.toList());
    }

    private static JsonObject createRideObject(Ride ride) {
        return new JsonObject()
                .put("rideId", ride.getRideId())
                .put(SPEC_MARS_ID, ride.getMarsId())
                .put(SPEC_DRONE_ID, ride.getDroneId())
                .put(SPEC_ROUTE, new JsonObject()
                        .put(SPEC_PICKUP_LOCATION, ride.getRoute().getPickupLocation().getCoordinateList())
                        .put(SPEC_DESTINATION, ride.getRoute().getDestination().getCoordinateList()))
                .put("fare", ride.getFare())
                .put("isCompleted", ride.isCompleted())
                .put("completionDate", ride.getCompletionDate());
    }

    public static void sendRide(RoutingContext ctx, Ride ride) {
        sendJsonResponse(ctx, 200, createRideObject(ride));
    }

    public static void sendRides(RoutingContext ctx, List<Ride> activeRides, List<Ride> completedRides) {
        sendJsonResponse(ctx, 200, new JsonObject()
                .put("activeRides", activeRides)
                .put("completedRides", completedRides));
    }

    public static void sendRideCreated(RoutingContext ctx, Ride ride) {
        sendJsonResponse(ctx, 201, createRideObject(ride));
    }

    public static void sendBookingCreated(RoutingContext ctx, Booking booking) {
        sendJsonResponse(ctx, 201, new JsonObject()
                .put("bookingId", booking.getBookingId())
                .put(SPEC_MARS_ID, booking.getMarsId())
                .put(SPEC_DATETIME, booking.getDatetime())
                .put(SPEC_ROUTE, new JsonObject()
                        .put(SPEC_PICKUP_LOCATION, booking.getRoute().getPickupLocation().getCoordinateList())
                        .put(SPEC_DESTINATION, booking.getRoute().getDestination().getCoordinateList())));
    }

    public static void sendBookingDeleted(RoutingContext ctx) {
        sendEmptyResponse(ctx);
    }

    public static void sendBookings(RoutingContext ctx, List<Booking> pastBookings, List<Booking> futureBookings, int marsId) {
        sendJsonResponse(ctx, 200, new JsonObject()
                .put(SPEC_MARS_ID, marsId)
                .put("futureBookings", createBookingObjects(futureBookings))
                .put("pastBookings", createBookingObjects(pastBookings)));
    }

    private static void sendEmptyResponse(RoutingContext ctx) {
        ctx.response()
                .setStatusCode(204)
                .end();
    }

    private static void sendJsonResponse(RoutingContext ctx, int statusCode, Object response) {
        ctx.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setStatusCode(statusCode)
                .end(Json.encodePrettily(response));
    }

    public static void sendFailure(RoutingContext ctx, int code, String quote) {
        sendJsonResponse(ctx, code, new JsonObject()
                .put("failure", code)
                .put("cause", quote));
    }
}
