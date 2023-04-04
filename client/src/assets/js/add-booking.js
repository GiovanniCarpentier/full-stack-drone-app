"use strict";

let config, api;

document.addEventListener("DOMContentLoaded", init);

async function init() {
    config = await loadConfig();
    api = `${config.host ? config.host + '/' : ''}${config.group ? config.group + '/' : ''}api/`;

    initializeNavigation();
    initializeBackButton();
    simulateData();

    document.querySelector("#add-booking-form").addEventListener("submit", addBooking);
    document.querySelectorAll("#pickup-location, #destination").forEach(el => el.addEventListener("change", checkFilledLocations));
}

function simulateData() {
    const geolocation = new ol.Geolocation();
    geolocation.setTracking(true);
    geolocation.on('change:position', () => document.querySelector("#pickup-location :first-child").value = ol.proj.transform(geolocation.getPosition(), "EPSG:4326", "EPSG:3857"));
}

function checkFilledLocations() {
    const pickup = document.querySelector("#pickup-location").value;
    const destination = document.querySelector("#destination").value;

    if (pickup !== "" && destination !== "") {
        const pickupCoords = pickup.split(",").map(parseFloat);
        const destinationCoords = destination.split(",").map(parseFloat);
        document.querySelector("#fare").innerText = calculateEstimatedFare(pickupCoords, destinationCoords);
        fillFareCalculation(pickupCoords, destinationCoords);
    }
}

function addBooking(e) {
    e.preventDefault();
    const $form = e.currentTarget;
    const pickupLocation = $form.querySelector("#pickup-location").value;
    const destination = $form.querySelector("#destination").value;
    const datetime = $form.querySelector("#datetime").value;
    const pickupLocationCoordinates = pickupLocation.split(",").map(parseFloat);
    const destinationCoordinates = destination.split(",").map(parseFloat);

    if (pickupLocation !== "" && destination !== "" && datetime !== "") {
        const requestBody = {
            "datetime": datetime.replace("T", " ") + ":00",
            "pickupLocation": pickupLocationCoordinates,
            "destination": destinationCoordinates
        };
        post(`bookings/${SIMULATED_ID}`, requestBody);
        location.href = "booking.html";
    }
}
