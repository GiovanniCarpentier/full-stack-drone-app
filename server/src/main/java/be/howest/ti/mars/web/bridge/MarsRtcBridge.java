package be.howest.ti.mars.web.bridge;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.*;

/**
 * In the MarsRtcBridge class you will find one example function which sends a message on the message bus to the client.
 * The RTC bridge is one of the class taught topics.
 * If you do not choose the RTC topic you don't have to do anything with this class.
 * Otherwise, you will need to expand this bridge with the websockets topics shown in the other modules.
 * <p>
 * Compared to the other classes only the bare minimum is given.
 * The client-side starter project does not contain any teacher code about the RTC topic.
 * The rtc bridge is already initialized and configured in the WebServer.java.
 * No need to change the WebServer.java
 * <p>
 * As a proof of concept (poc) one message to the client is sent every 30 seconds.
 * <p>
 * The job of the "bridge" is to bridge between websockets events and Java (the controller).
 * Just like in the openapi bridge, keep business logic isolated in the package logic.
 * <p>
 */
public class MarsRtcBridge {
    private static final String EB_EVENT_TO_MARTIANS = "events.to.martians";
    private SockJSHandler sockJSHandler;
    private EventBus eb;
    private Vertx vertx;

    // Vertx variables
    private static final String CHNL_TO_CLIENTS = "events.to.clients";
    private static final String CHNL_TO_SERVER = "events.to.server";
    private final List<Double> lonList = new ArrayList<>();
    private final List<Double> latList = new ArrayList<>();
    private int index = 0;
    private long timerId;

    /**
     * Example function to put a message on the event bus every 10 seconds.
     * The timer logic is only there to simulate a repetitive stream of data as an example.
     * Please remove this timer logic or move it to an appropriate place.
     * Please call the controller to get some business logic data. Afterwords publish the result to the client.
     */
    public void sendEventToClients() {
        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            public void run() {
                eb.publish(EB_EVENT_TO_MARTIANS, new JsonObject(Map.of("MyJsonProp", "some value")));
            }
        };

        timer.schedule(task, 0, 30000);
    }

    private void createSockJSHandler() {
        final PermittedOptions permittedOptions = new PermittedOptions().setAddressRegex("events\\..+");
        final SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(permittedOptions)
                .addOutboundPermitted(permittedOptions);
        sockJSHandler.bridge(options);
    }

    public SockJSHandler getSockJSHandler(Vertx vertx) {
        sockJSHandler = SockJSHandler.create(vertx);
        this.vertx = vertx;
        eb = vertx.eventBus();
        createSockJSHandler();

        // This is for demo purposes only.
        // Do not send messages in this getSockJSHandler function.

        return sockJSHandler;
    }

    // Vertx functions

    public void registerConsumers() {
        eb.consumer(CHNL_TO_SERVER, this::moveByStartCoords);
    }

    private void moveByStartCoords(Message<JsonObject> msg) {
        double startLon = Double.parseDouble(msg.body().getString("startLon"));
        double startLat = Double.parseDouble(msg.body().getString("startLat"));
        double endLon = Double.parseDouble(msg.body().getString("endLon"));
        double endLat = Double.parseDouble(msg.body().getString("endLat"));

        fillList(startLon, startLat, endLon, endLat);
        timerId = vertx.setPeriodic(2000, id -> sendCoords());
    }

    private void fillList(double startLon, double startLat, double endLon, double endLat) {
        double lonDif = (startLon - endLon) / 5;
        double latDif = (startLat - endLat) / 5;

        for (int i = 1; i < 5; i++) {
            lonList.add(startLon - i * lonDif);
            latList.add(startLat - i * latDif);
        }
        lonList.add(endLon);
        latList.add(endLat);
    }

    private void sendCoords() {
        eb.publish(CHNL_TO_CLIENTS, new JsonArray().add(lonList.get(index)).add(latList.get(index)));
        index++;
        if (index > 4) {
            index = 0;
            lonList.clear();
            latList.clear();
            vertx.cancelTimer(timerId);
        }
    }
}
