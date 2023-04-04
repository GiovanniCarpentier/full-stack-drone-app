"use strict";

let map, pickupMarker, userLocationMarker, destinationMarker;

document.addEventListener("DOMContentLoaded", init);

function init() {
    document.querySelector("#order-form").addEventListener("submit", submitOrder);
    document.querySelectorAll("#order-form input.location").forEach(el => el.addEventListener("click", switchFocus));
    document.querySelectorAll("#order-form input.location").forEach(el => el.addEventListener("search", clearStorageIfCleared));
    document.querySelector("#estimated-fare p strong:first-child").addEventListener("click", expandFare, {once: true});

    initializeMap();
    initializeNavigation();
}

function initializeMap() {
    if (hasStoredOrder()) {
        useStoredLocations().then(() => {
            map.addEventListener("click", clickMap);
        });
    } else if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition(useCurrentLocation, () => console.log("Location access was denied"));
    } else {
        console.log("Browser doesn't support geolocation");
        map = createDefaultMap("order-map");
        map.addEventListener("click", clickMap);
    }
}

async function useStoredLocations() {
    document.querySelector("#fare").innerText = calculateEstimatedFare();
    fillFareCalculation();

    map = createDefaultMap("order-map", ol.proj.transform(getOrderPickup(), "EPSG:3857", "EPSG:4326"));

    document.querySelector("#pickup-location").value = await reverseGeocode(getOrderPickup());
    document.querySelector("#destination").value = await reverseGeocode(getOrderDest());
    pickupMarker = transformAndCreateMarker(getOrderPickup(), map, MARKERS.PICKUP_LOCATION);
    destinationMarker = transformAndCreateMarker(getOrderDest(), map, MARKERS.DESTINATION);
}

function useCurrentLocation(geolocation) {
    clearOrderLocalStorage();

    const currentCoords = [geolocation.coords.longitude, geolocation.coords.latitude];
    map = createDefaultMap("order-map", currentCoords);

    setOrderPickup(ol.proj.transform(currentCoords, "EPSG:4326", "EPSG:3857"));
    document.querySelector("#pickup-location").value = "My Location";
    pickupMarker = createMarkerOnCoordinate(currentCoords, MARKERS.MY_LOCATION);
    map.addLayer(pickupMarker);

    map.addEventListener("click", clickMap);
}

async function clickMap(e) {
    const activeInput = document.querySelector("form input.active").id;
    const isPickup = activeInput === "pickup-location";

    map.removeLayer(isPickup ? pickupMarker : destinationMarker); // Remove previous marker

    if (isPickup) {
        map.removeLayer(userLocationMarker);
        pickupMarker = transformAndCreateMarker(e.coordinate, map, MARKERS.PICKUP_LOCATION);
    } else {
        destinationMarker = transformAndCreateMarker(e.coordinate, map, MARKERS.DESTINATION);
    }
    localStorage.setItem(isPickup ? "pickupLocation" : "destination", JSON.stringify(e.coordinate));

    document.querySelector("form input.active").value = await reverseGeocode(e.coordinate);
    if (hasStoredOrder()) {
        document.querySelector("#fare").innerText = calculateEstimatedFare();
        fillFareCalculation();
    }
}

function submitOrder(e) {
    e.preventDefault();

    if (hasStoredOrder()) {
        location.href = "order-confirmation.html";
    }
}

function switchFocus(e) {
    document.querySelectorAll("form input").forEach(el => el.classList.remove("active"));
    e.target.classList.add("active");
}

function clearStorageIfCleared(e) {
    e.preventDefault();
    const currId = e.currentTarget.id;
    if (e.currentTarget.value === "") {
        currId === "destination" ? clearOrderDest() : clearOrderPickup();
        map.removeLayer(currId === "destination" ? destinationMarker : pickupMarker);
    }
}
