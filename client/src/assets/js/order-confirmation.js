"use strict";

let config, api;

document.addEventListener("DOMContentLoaded", init);

async function init() {
    config = await loadConfig();
    api = `${config.host ? config.host + '/' : ''}${config.group ? config.group + '/' : ''}api/`;

    checkLocalStorage();
    showSummary().then();

    initializeMap();
    initializeNavigation();
    initializeBackButton();

    document.querySelector("button.cancel-btn").addEventListener("click", askToCancelOrder);
    document.querySelector("button#order").addEventListener("click", confirmOrder);
}

function checkLocalStorage() {
    if (!("pickupLocation" in localStorage) && !("destination" in localStorage)) {
        location.href = "order.html";
    }
}

async function showSummary() {
    const $summary = document.querySelector("#order-summary");
    const $list = $summary.querySelector("ul");

    const pickupLocation = await reverseGeocode(getOrderPickup());
    const destination = await reverseGeocode(getOrderDest());

    document.querySelector("#fare").innerText = calculateEstimatedFare();

    const distanceInMeters = calculateDistanceBetweenLocations(getOrderPickup(), getOrderDest());
    const distanceInKilometres = (distanceInMeters / 1000).toFixed(2);
    const estDuration = calculateEstimatedDuration(Math.floor(parseFloat(distanceInKilometres)));

    $list.insertAdjacentHTML("beforeend", `
            <li><strong>From</strong><span></span>${limitStringLength(pickupLocation, 22)}</li>
            <li><strong>To</strong><span>${limitStringLength(destination, 22)}</span></li>
            <li><strong>Est. travel duration</strong><span>${estDuration} min</span></li>
            <li><strong>Est. distance</strong><span>${distanceInKilometres} km</span></li>`);
}

function initializeMap() {
    const pickupCoordinate = ol.proj.transform(getOrderPickup(), "EPSG:3857", "EPSG:4326");
    const destinationCoordinate = ol.proj.transform(getOrderDest(), "EPSG:3857", "EPSG:4326");

    const map = new ol.Map({
        target: 'order-summary-map',
        layers: [
            new ol.layer.Tile({
                source: new ol.source.OSM()
            })
        ],
        view: new ol.View({
            center: ol.proj.fromLonLat(destinationCoordinate),
            zoom: 10
        }),
        controls: []
    });
    map.addLayer(createMarkerOnCoordinate(pickupCoordinate, MARKERS.PICKUP_LOCATION));
    map.addLayer(createMarkerOnCoordinate(destinationCoordinate, MARKERS.DESTINATION));
}

async function confirmOrder(e) {
    e.preventDefault();

    const requestBody = {
        "pickupLocation": getOrderPickup(),
        "destination": getOrderDest()
    };
    post(`rides/${SIMULATED_ID}`, requestBody, saveNewRide);
}

function saveNewRide(response) {
    response.json().then(res => {
        setActiveRide(res["rideId"]);
        location.href = "ride.html";
    });
}
