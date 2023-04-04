package be.howest.ti.mars.web.bridge;

import be.howest.ti.mars.logic.controller.DefaultMarsController;
import be.howest.ti.mars.logic.controller.MarsController;
import be.howest.ti.mars.logic.domain.*;
import be.howest.ti.mars.logic.exceptions.MarsResourceNotFoundException;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.openapi.RouterBuilder;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * In the MarsOpenApiBridge class you will find one handler-method per API operation.
 * The job of the "bridge" is to bridge between JSON (request and response) and Java (the controller).
 * <p>
 * For each API operation you should get the required data from the `Request` class,
 * pass it to the controller and use its result to generate a response in the `Response` class.
 */

public class MarsOpenApiBridge {
    private static final Logger LOGGER = Logger.getLogger(MarsOpenApiBridge.class.getName());
    private final MarsController controller;

    public MarsOpenApiBridge() {
        this.controller = new DefaultMarsController();
    }

    public MarsOpenApiBridge(MarsController controller) {
        this.controller = controller;
    }

    /**
     * GET /api/drones
     */
    public void getAllDrones(RoutingContext ctx) {
        Response.sendDrones(ctx, controller.getAllDrones());
    }

    /**
     * GET /api/drones/{droneId}/history
     */
    public void getDroneLocationHistory(RoutingContext ctx) {
        int droneId = Request.from(ctx).getDroneId();
        int depth = Request.from(ctx).getDepth();

        List<Location> locationHistory = controller.getDroneLocationHistory(droneId, depth);

        Response.getDroneLocationHistory(ctx, droneId, locationHistory);
    }

    /**
     * GET /api/drones/{droneId}/location
     */
    public void getCurrentDroneLocation(RoutingContext ctx) {
        int droneId = Request.from(ctx).getDroneId();

        Location currentLocation = controller.getCurrentDroneLocation(droneId);

        Response.sendCurrentDroneLocation(ctx, currentLocation);
    }

    /**
     * GET /api/rides
     */
    private void getAllRides(RoutingContext ctx) {
        SeperatedRides seperatedRides = controller.getAllRides();

        Response.sendRides(ctx, seperatedRides.getActiveRides(), seperatedRides.getCompletedRides());
    }

    /**
     * GET /api/rides/{rideId}
     */
    private void getRide(RoutingContext ctx) {
        int rideId = Request.from(ctx).getRideId();

        Ride ride = controller.getRide(rideId);

        Response.sendRide(ctx, ride);
    }

    /**
     * POST /api/rides/{marsId}
     */
    private void createRide(RoutingContext ctx) {
        int marsId = Request.from(ctx).getMarsId();
        List<Double> pickupLocation = Request.from(ctx).getCoordinate("pickupLocation");
        List<Double> destination = Request.from(ctx).getCoordinate("destination");

        Ride ride = controller.createRide(marsId, pickupLocation, destination);

        Response.sendRideCreated(ctx, ride);
    }


    /**
     * GET /api/bookings
     */
    public void getAllBookings(RoutingContext ctx) {
        SeparatedBookings separatedBookings = controller.getAllBookings();

        Response.sendAllBookings(ctx, separatedBookings.getPastBookings(), separatedBookings.getFutureBookings());
    }

    /**
     * GET /api/bookings/{marsId}
     */
    public void getBookings(RoutingContext ctx) {
        int marsId = Request.from(ctx).getMarsId();

        SeparatedBookings separatedBookings = controller.getBookings(marsId);

        Response.sendBookings(ctx, separatedBookings.getPastBookings(), separatedBookings.getFutureBookings(), marsId);
    }

    /**
     * POST /api/bookings/{marsId}
     */
    public void createBooking(RoutingContext ctx) {
        int marsId = Request.from(ctx).getMarsId();
        String datetime = Request.from(ctx).getDatetime();
        List<Double> pickupLocation = Request.from(ctx).getCoordinate("pickupLocation");
        List<Double> destination = Request.from(ctx).getCoordinate("destination");

        Booking booking = controller.createBooking(marsId, datetime, pickupLocation, destination);

        Response.sendBookingCreated(ctx, booking);
    }

    /**
     * DELETE /api/bookings/{bookingId}
     */
    public void deleteBooking(RoutingContext ctx) {
        int bookingId = Request.from(ctx).getBookingId();

        controller.deleteBooking(bookingId);

        Response.sendBookingDeleted(ctx);
    }

    public Router buildRouter(RouterBuilder routerBuilder) {
        LOGGER.log(Level.INFO, "Installing cors handlers");
        routerBuilder.rootHandler(createCorsHandler());

        LOGGER.log(Level.INFO, "Installing failure handlers for all operations");
        routerBuilder.operations().forEach(op -> op.failureHandler(this::onFailedRequest));


        LOGGER.log(Level.INFO, "Installing handler for: getAllDrones");
        routerBuilder.operation("getAllDrones").handler(this::getAllDrones);

        LOGGER.log(Level.INFO, "Installing handler for: getDroneLocationHistory");
        routerBuilder.operation("getDroneLocationHistory").handler(this::getDroneLocationHistory);

        LOGGER.log(Level.INFO, "Installing handler for: getDroneLocation");
        routerBuilder.operation("getCurrentDroneLocation").handler(this::getCurrentDroneLocation);


        LOGGER.log(Level.INFO, "Installing handler for: getAllRides");
        routerBuilder.operation("getAllRides").handler(this::getAllRides);

        LOGGER.log(Level.INFO, "Installing handler for: getRide");
        routerBuilder.operation("getRide").handler(this::getRide);

        LOGGER.log(Level.INFO, "Installing handler for: createRide");
        routerBuilder.operation("createRide").handler(this::createRide);


        LOGGER.log(Level.INFO, "Installing handler for: getAllBookings");
        routerBuilder.operation("getAllBookings").handler(this::getAllBookings);

        LOGGER.log(Level.INFO, "Installing handler for: getBookings");
        routerBuilder.operation("getBookings").handler(this::getBookings);

        LOGGER.log(Level.INFO, "Installing handler for: createBooking");
        routerBuilder.operation("createBooking").handler(this::createBooking);

        LOGGER.log(Level.INFO, "Installing handler for: deleteBooking");
        routerBuilder.operation("deleteBooking").handler(this::deleteBooking);


        LOGGER.log(Level.INFO, "All handlers are installed, creating router.");
        return routerBuilder.createRouter();
    }

    private void onFailedRequest(RoutingContext ctx) {
        Throwable cause = ctx.failure();
        int code = ctx.statusCode();
        String quote = Objects.isNull(cause) ? "" + code : cause.getMessage();

        // Map custom runtime exceptions to a HTTP status code.
        if (cause instanceof MarsResourceNotFoundException) {
            code = 404;
        } else if (cause instanceof IllegalArgumentException) {
            code = 400;
        } else {
            LOGGER.log(Level.WARNING, "Failed request", cause);
        }

        Response.sendFailure(ctx, code, quote);
    }

    private CorsHandler createCorsHandler() {
        return CorsHandler.create(".*.")
                .allowedHeader("x-requested-with")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("origin")
                .allowedHeader("Content-Type")
                .allowedHeader("accept")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedMethod(HttpMethod.PATCH)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.PUT);
    }
}
