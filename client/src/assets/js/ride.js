"use strict";

let map, sendToServer, canVibrate;
let hasSubmittedEvent = false;
let counter = 0;
const $FORM = document.querySelector("#ride-form");
const $FORM_CONTAINER = $FORM.querySelector(".form-container");

document.addEventListener("DOMContentLoaded", init);

function init() {
    $FORM.addEventListener("submit", askToCancelOrder);
    $FORM.querySelector("button#vibration-mock-button").addEventListener("click", enableVibration);

    checkRideStatus();
    initializeMap();
    initializeNavigation();
    sendToServer = openSocket();
    window.setTimeout(simulateDroneArriving, 5000);
}

function checkRideStatus() {
    if (!hasActiveRide() || !hasStoredOrder()) {
        location.href = "order.html";
    }
}

function initializeMap() {
    map = createDefaultMap("ride-map", ol.proj.transform(getOrderPickup(), "EPSG:3857", "EPSG:4326"));

    simulateMarkers(500);
}

function enableVibration(e) {
    e.preventDefault();
    canVibrate = true;
}

function displayEventMaker(e) {
    e.preventDefault();
    document.body.insertAdjacentHTML("afterbegin", createEventMakerModal());
    startVideoFeed();
    document.querySelector("section#event-maker span").addEventListener("click", closeModal);
    document.querySelector("section#event-maker form").addEventListener("submit", submitEvent);
}

function createEventMakerModal() {
    return `<section class="modal" id="event-maker">
              <div>
                <header>
                  <h2>Create new event</h2>
                  <span class="material-icons-round">close</span>
                </header>
                <form action="#">
                  <label class="hidden" for="event"></label>
                  <select id="event" name="event">
                    <option value="accident">Accident</option>
                    <option value="event">Event</option>
                    <option value="scenery">Scenery</option>
                    <option value="other">Other</option>
                  </select>
                  <label class="hidden" for="description"></label>
                  <input id="description" placeholder="Description" type="text">
                  <input type="submit">
                </form>
              </div>
            </section>`;
}

function submitEvent(e) {
    e.preventDefault();
    closeModal();
    hasSubmittedEvent = true;
}

function simulateMarkers(distance) {
    const modifiedCoordinate = getOrderPickup();
    modifiedCoordinate[0] += distance;
    modifiedCoordinate[1] -= distance;

    const locations = [getOrderPickup(), modifiedCoordinate];

    removeMarkers();
    map.addLayer(createLineBetweenLocations(locations));
    transformAndCreateMarker(locations[0], map, MARKERS.PICKUP_LOCATION);
    transformAndCreateMarker(locations[1], map, MARKERS.DRONE);
}

function simulateDroneArriving() {
    vibrate();
    simulateMarkers(50);
    $FORM_CONTAINER.innerHTML = `<h2>Your drone has arrived!</h2>
                                 <small>The drone will leave after 10 minutes of waiting</small>
                                 <p>Please enter the drone at the location on the map</p>
                                 <p>Click the button below if everyone is aboard and ready to start the trip</p>
                                 <button class="text-btn cancel-btn">Cancel order</button>`;

    $FORM.removeEventListener("submit", askToCancelOrder);
    $FORM.addEventListener("submit", simulateTripStart);
    $FORM.querySelector("button.cancel-btn").addEventListener("click", askToCancelOrder);
    $FORM.querySelector("input[type=submit]").value = "Start trip";
}

function simulateTripStart(e) {
    e.preventDefault();
    removeMarkers();

    transformAndCreateMarker(getOrderDest(), map, MARKERS.DESTINATION);
    transformAndCreateMarker(getOrderPickup(), map, MARKERS.DRONE);
    map.addLayer(createLineBetweenLocations([getOrderDest(), getOrderPickup()]));

    $FORM_CONTAINER.innerHTML = `<h2>En route</h2>
                                 <small>You can view the progress of your journey here</small>
                                 <p>ETA: < 1 min</p>`;

    $FORM.querySelector("input[type=submit]").remove();
    $FORM.insertAdjacentHTML("afterbegin", `<button class="icon-btn" id="comm-aspect-button"><span class="material-icons-round">add_a_photo</span></button>`);
    $FORM.querySelector("button#comm-aspect-button").addEventListener("click", displayEventMaker);
    sendTripStarted();
    simulateDroneTracking();
}

function sendTripStarted() {
    const start = getOrderPickup();
    const end = getOrderDest();
    const data = {type: 'message', startLon: start[0], startLat: start[1], endLon: end[0], endLat: end[1]};
    sendToServer(data);
}

function simulateDroneTracking(error, message) {
    if (message !== undefined) {
        moveMarker(message.body);
    }

    function moveMarker(coords) {
        removeMarkers();
        transformAndCreateMarker(getOrderDest(), map, MARKERS.DESTINATION);
        transformAndCreateMarker(coords, map, MARKERS.DRONE);
        map.addLayer(createLineBetweenLocations([getOrderDest(), coords]));
        map.getView().centerOn(coords, map.getSize(), [map.getSize()[0] / 2, map.getSize()[1] / 2]);

        counter++;
        if (counter === 5) {
            counter = 0;
            simulateTripEnding();
        }
    }
}

function simulateTripEnding() {
    vibrate();
    $FORM_CONTAINER.innerHTML = `<h2>You've arrived!</h2>
                                 <small>We hope you've enjoyed using our services</small>
                                 <small>Payment will be requested shortly</small>
                                 <p>Please exit the drone</p>`;

    $FORM.querySelector("div.form-container").insertAdjacentHTML("beforebegin", createFareElement());
    $FORM.querySelector("#final-fare p strong:first-child").addEventListener("click", condenseFare, {once: true});
    $FORM.querySelector("button#comm-aspect-button").remove();
    closeModal();

    if (hasSubmittedEvent) {
        transformAndCreateMarker(getOrderPickup(), map, MARKERS.EVENT);
    }
    fillFareCalculation();
    clearActiveRide();
}

function createFareElement() {
    return `<div class="form-container fare expanded" id="final-fare">
              <p>
                <strong><span class="material-icons-round">expand_more</span><span>Fare</span></strong>
                <strong>
                  <small>MC</small>
                  <span id="fare">${calculateEstimatedFare()}</span>
                </strong>
              </p>
              <ul class="fare-calculation">
                <li><span>Drop charge</span><span>3.00</span></li>
                <li><span>Distance (est. <span id="distance">0.0</span> km)</span><span id="distance-charge">0.00</span></li>
                <li><span>Duration (est. <span id="duration">0.0</span> min)</span><span id="duration-charge">0.00</span></li>
              </ul>
            </div>`;
}

function removeMarkers() {
    const layers = [...map.getLayers().getArray()];
    map.removeLayer(layers[1]);
    map.removeLayer(layers[2]);
    map.removeLayer(layers[3]);
}

function createLineBetweenLocations(locations) {
    const featureLine = new ol.Feature({
        geometry: new ol.geom.LineString(locations)
    });

    const vectorLine = new ol.source.Vector({});
    vectorLine.addFeature(featureLine);

    return new ol.layer.Vector({
        source: vectorLine,
        style: new ol.style.Style({
            stroke: new ol.style.Stroke({color: '#FFFFFF', width: 3})
        })
    });
}

function vibrate() {
    if (canVibrate) {
        window.navigator.vibrate([50, 0, 50]);
    }
}

