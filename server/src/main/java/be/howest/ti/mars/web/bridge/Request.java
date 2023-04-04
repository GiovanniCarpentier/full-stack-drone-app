package be.howest.ti.mars.web.bridge;

import be.howest.ti.mars.web.exceptions.MalformedRequestException;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Request class is responsible for translating information that is part of the
 * request into Java.
 */
public class Request {
    public static final String SPEC_DRONE_ID = "droneId";
    public static final String SPEC_MARS_ID = "marsId";
    public static final String SPEC_RIDE_ID = "rideId";
    public static final String SPEC_BOOKING_ID = "bookingId";
    public static final String SPEC_DEPTH = "depth";
    public static final int SPEC_DEFAULT_DEPTH = 50;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = Logger.getLogger(Request.class.getName());
    private final RequestParameters params;

    private Request(RoutingContext ctx) {
        this.params = ctx.get(ValidationHandler.REQUEST_CONTEXT_KEY);
    }

    public static Request from(RoutingContext ctx) {
        return new Request(ctx);
    }

    public int getDroneId() {
        return params.pathParameter(SPEC_DRONE_ID).getInteger();
    }

    public int getBookingId() {
        return params.pathParameter(SPEC_BOOKING_ID).getInteger();
    }

    public int getMarsId() {
        return ensurePositiveMarsId(params.pathParameter(SPEC_MARS_ID).getInteger());
    }

    public String getDatetime() {
        return ensureValidDatetime(getString("datetime"));
    }

    public int getRideId() {
        return params.pathParameter(SPEC_RIDE_ID).getInteger();
    }

    public int getDepth() {
        return params.queryParameter(SPEC_DEPTH) == null ? SPEC_DEFAULT_DEPTH : params.queryParameter(SPEC_DEPTH).getInteger();
    }

    private int ensurePositiveMarsId(int marsId) {
        if (marsId < 0) throw new IllegalArgumentException("MarsId isn't positive");
        return marsId;
    }

    private String ensureValidDatetime(String datetime) throws IllegalArgumentException {
        try {
            LocalDateTime.parse(datetime, FORMATTER);
            return datetime;
        } catch (DateTimeParseException ex) {
            LOGGER.log(Level.SEVERE, "Datetime format does not match datetime format of yyyy-MM-dd HH:mm:ss", ex);
            throw new IllegalArgumentException("Datetime format doesn't match datetime format of yyyy-MM-dd HH:mm:ss");
        }
    }

    public List<Double> getCoordinate(String key) {
        try {
            if (params.body().isJsonObject()) {
                JsonArray array = params.body().getJsonObject().getJsonArray(key);
                if (array.size() != 2) {
                    throw new IllegalArgumentException("Array should be of length 2 ");
                }
                return new ArrayList<>(List.of(array.getDouble(0), array.getDouble(1)));
            } else {
                throw new IllegalArgumentException("Data in JSON object does not match expected values");
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.INFO, "Unable to decipher the data in the body to a coordinate", ex);
            throw new IllegalArgumentException("Unable to decipher the data in the request body to a coordinate");
        }
    }

    public String getString(String value) {
        try {
            if (params.body().isJsonObject())
                return params.body().getJsonObject().getString(value);
            return params.body().get().toString();
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.INFO, "Unable to decipher the data in the body to a string", ex);
            throw new MalformedRequestException("Unable to decipher the data in the request body to a string. See logs for details.");
        }
    }
}
