package be.howest.ti.mars.web;

import be.howest.ti.mars.logic.controller.MockMarsController;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.web.bridge.MarsOpenApiBridge;
import be.howest.ti.mars.web.bridge.MarsRtcBridge;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.AvoidDuplicateLiterals"})
        /*
         * PMD.JUnitTestsShouldIncludeAssert: VertxExtension style asserts are marked as false positives.
         * PMD.AvoidDuplicateLiterals: Should all be part of the spec (e.g., urls and names of req/res body properties, ...)
         */
class OpenAPITest {

    public static final String MSG_200_EXPECTED = "If all goes right, we expect a 200 status";
    public static final String MSG_201_EXPECTED = "If a resource is successfully created.";
    public static final String MSG_204_EXPECTED = "If a resource is successfully deleted";
    private static final int PORT = 8080;
    private static final String HOST = "localhost";
    private Vertx vertx;
    private WebClient webClient;

    @BeforeEach
    void deploy(final VertxTestContext testContext) {
        Repositories.shutdown();
        vertx = Vertx.vertx();

        WebServer webServer = new WebServer(new MarsOpenApiBridge(new MockMarsController()), new MarsRtcBridge());
        vertx.deployVerticle(
                webServer,
                testContext.succeedingThenComplete()
        );
        webClient = WebClient.create(vertx);
    }

    @AfterEach
    void close(final VertxTestContext testContext) {
        vertx.close(testContext.succeedingThenComplete());
        webClient.close();
        Repositories.shutdown();
    }

    @Test
    void getDrones(final VertxTestContext testContext) {
        webClient.get(PORT, HOST, "/api/drones").send()
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    System.out.println(response.bodyAsJsonObject());
                    assertEquals(200, response.statusCode(), MSG_200_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void getLocation(final VertxTestContext testContext) {
        webClient.get(PORT, HOST, "/api/drones/1/location").send()
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    System.out.println(response.bodyAsJsonObject());
                    assertEquals(200, response.statusCode(), MSG_200_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void getLocationHistory(final VertxTestContext testContext) {
        webClient.get(PORT, HOST, "/api/drones/1/history").send()
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    System.out.println(response.bodyAsJsonObject());
                    assertEquals(200, response.statusCode(), MSG_200_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void getRides(final VertxTestContext testContext) {
        webClient.get(PORT, HOST, "/api/rides").send()
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    System.out.println(response.bodyAsJsonObject());
                    assertEquals(200, response.statusCode(), MSG_200_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void getRide(final VertxTestContext testContext) {
        webClient.get(PORT, HOST, "/api/rides/1").send()
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    System.out.println(response.bodyAsJsonObject());
                    assertEquals(200, response.statusCode(), MSG_200_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void createRide(final VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject()
                .put("pickupLocation", new ArrayList<>(List.of(6604062.910458012, 393192.9024483985)))
                .put("destination", new ArrayList<>(List.of(6604062.910458012, 393192.9024483985)));
        webClient.post(PORT, HOST, "/api/rides/5").sendJsonObject(jsonObject)
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(201, response.statusCode(), MSG_201_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void getBookings(final VertxTestContext testContext) {
        webClient.get(PORT, HOST, "/api/bookings").send()
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    System.out.println(response.bodyAsJsonObject());
                    assertEquals(200, response.statusCode(), MSG_200_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void getBooking(final VertxTestContext testContext) {
        webClient.get(PORT, HOST, "/api/bookings/1").send()
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    System.out.println(response.bodyAsJsonObject());
                    assertEquals(200, response.statusCode(), MSG_200_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void createBooking(final VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject()
                .put("datetime", "2052-05-23 14:25:10")
                .put("pickupLocation", new ArrayList<>(List.of(6604062.910458012, 393192.9024483985)))
                .put("destination", new ArrayList<>(List.of(6604062.910458012, 393192.9024483985)));
        webClient.post(PORT, HOST, "/api/bookings/5").sendJsonObject(jsonObject)
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(201, response.statusCode(), MSG_201_EXPECTED);
                    testContext.completeNow();
                }));
    }

    @Test
    void deleteBooking(final VertxTestContext testContext) {
        webClient.delete(PORT, HOST, "/api/bookings/5").send()
                .onFailure(testContext::failNow)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(204, response.statusCode(), MSG_204_EXPECTED);
                    testContext.completeNow();
                }));
    }
}