const MARKERS = {
    MY_LOCATION: "assets/images/my-location.svg",
    PICKUP_LOCATION: "assets/images/pickup-marker.svg",
    DESTINATION: "assets/images/destination-marker.svg",
    DRONE: "assets/images/taxi.svg",
    EVENT: "assets/images/map-marker.svg"
};
const SIMULATED_ID = 5;

async function loadConfig() {
    const response = await fetch("config.json");
    return response.json();
}

function initializeNavigation() {
    const navigationMenuContent = `<nav>
                                     <ul>
                                       <li><a href="index.html">Homepage</a></li>
                                       <li><a href="booking.html">Bookings</a></li>
                                       <li><a href="order.html">Get a ride</a></li>
                                     </ul>
                                   </nav>
                                   <div>
                                     <h2>Need help or have questions?</h2>
                                     <a href="tel:#">Call us</a>
                                   </div>`;
    document.querySelector("body > header").insertAdjacentHTML("beforeend", navigationMenuContent);
    document.querySelector("#menu-btn").addEventListener("click", expandNav, {once: true});
}

function initializeBackButton() {
    document.querySelector("#back-btn").addEventListener("click", () => history.back());
}

function expandNav(e) {
    const $header = e.currentTarget.closest("header");
    $header.classList.add("expanded");
    $header.querySelector("#menu-btn").innerText = "close";
    e.currentTarget.addEventListener("click", condenseNav, {once: true});
}

function condenseNav(e) {
    const $header = e.currentTarget.closest("header");
    $header.classList.remove("expanded");
    $header.querySelector("#menu-btn").innerText = "menu";
    e.currentTarget.addEventListener("click", expandNav, {once: true});
}

function expandFare(e) {
    const $fare = e.currentTarget.closest(".fare");
    $fare.classList.add("expanded");
    $fare.querySelector(".material-icons-round").innerText = "expand_more";
    e.currentTarget.addEventListener("click", condenseFare, {once: true});
}

function condenseFare(e) {
    const $fare = e.currentTarget.closest(".fare");
    $fare.classList.remove("expanded");
    $fare.querySelector(".material-icons-round").innerText = "chevron_right";
    e.currentTarget.addEventListener("click", expandFare, {once: true});
}

async function reverseGeocode(coordinate) {
    const location = ol.proj.toLonLat(coordinate);
    return fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lon=${location[0]}&lat=${location[1]}`)
        .then((response) => response.json())
        .then((json) => {
            let res;
            const address = json.address;
            if ("street" in address) {
                res = `${address.street}, ${getCityOrEquivalent(address)}`;
            } else if ("road" in address) {
                res = `${address.road}, ${getCityOrEquivalent(address)}`;
            } else {
                res = getCityOrEquivalent(address);
            }
            return res;
        });
}

function getCityOrEquivalent(address) {
    if ("town" in address) {
        return address.town;
    }
    if ("village" in address) {
        return address.village;
    }
    if ("city" in address) {
        return address.city;
    }
    if ("county" in address) {
        return address.county;
    }
    return null;
}

function limitStringLength(string, length) {
    string = string.toString();
    return string.length > length ? `${string.substr(0, length)}...` : string;
}

function createDefaultMap(target, center = [3.214292161988944, 51.19184282497699]) {
    return new ol.Map({
        target: target,
        layers: [
            new ol.layer.Tile({
                source: new ol.source.OSM()
            })
        ],
        view: new ol.View({
            center: ol.proj.fromLonLat(center),
            zoom: 12.5
        }),
        controls: [
            new ol.control.Rotate()
        ]
    });
}

function createMarkerOnCoordinate(coordinate, marker) {
    return new ol.layer.Vector({
        source: new ol.source.Vector({
            features: [
                new ol.Feature({
                    type: 'marker',
                    geometry: new ol.geom.Point(ol.proj.fromLonLat(coordinate))
                })
            ]
        }),
        style: new ol.style.Style({
            image: new ol.style.Icon({
                src: marker,
                anchor: [0.5, 0.5]
            })
        })
    });
}

function transformAndCreateMarker(coordinate, map, icon) {
    const transformerCoordinate = ol.proj.transform(coordinate, "EPSG:3857", "EPSG:4326");
    const marker = createMarkerOnCoordinate(transformerCoordinate, icon);
    map.addLayer(marker);
    return marker;
}

function askToCancelOrder(e) {
    e.preventDefault();
    const title = "Are you sure you'd like to cancel your order?";
    const action = "You will be taken back to the home screen";
    showModal(title, action, "order", cancelOrder);
}

function showModal(title, action, cancelWord, confirmFunc) {
    const modal = `<section class="modal">
                     <div>
                       <h2>${title}</h2>
                       <small>${action}</small>
                       <button id="modal-confirm">Yes, cancel my ${cancelWord}</button>
                       <button id="modal-cancel" class="text-btn">No, take me back</button>
                     </div>
                   </section>`;
    document.body.insertAdjacentHTML("afterbegin", modal);
    document.querySelector("button#modal-confirm").addEventListener("click", () => {
        closeModal();
        confirmFunc();
    });
    document.querySelector("button#modal-cancel").addEventListener("click", closeModal);
}

function closeModal() {
    const $modal = document.querySelector("section.modal");
    if ($modal !== null) {
        $modal.remove();
    }
}

function cancelOrder() {
    clearOrderLocalStorage();
    clearActiveRide();
    location.href = "index.html";
}

function clearOrderLocalStorage() {
    window.localStorage.removeItem("destination");
    window.localStorage.removeItem("pickupLocation");
}

function clearOrderPickup() {
    localStorage.removeItem("pickupLocation");
}

function clearOrderDest() {
    localStorage.removeItem("destination");
}

function clearActiveRide() {
    localStorage.removeItem("rideId");
}

function setOrderPickup(coordinate) {
    localStorage.setItem("pickupLocation", JSON.stringify(coordinate));
}

function setActiveRide(rideId) {
    localStorage.setItem("rideId", rideId);
}

function getOrderPickup() {
    return JSON.parse(localStorage.getItem("pickupLocation"));
}

function getOrderDest() {
    return JSON.parse(localStorage.getItem("destination"));
}

function getActiveRide() {
    return localStorage.getItem("rideId");
}

function hasStoredOrder() {
    return "pickupLocation" in localStorage && "destination" in localStorage;
}

function hasActiveRide() {
    return "rideId" in localStorage;
}

function calculateEstimatedFare(pickup = getOrderPickup(), destination = getOrderDest()) {
    const distanceInMeters = calculateDistanceBetweenLocations(pickup, destination);
    const distanceInFlooredKilometres = Math.floor(distanceInMeters / 1000);
    const estDuration = calculateEstimatedDuration(distanceInFlooredKilometres); // Base of 5 min. + 30 secs/km
    const estFare = 3.00 + (1.50 * distanceInFlooredKilometres) + (0.05 * estDuration); // Drop charge + 1.50/km + 0.05/min
    return estFare.toFixed(2);
}

function calculateDistanceBetweenLocations(selectedPickupCoordinate, selectedDestinationCoordinate) {
    const distance = new ol.geom.LineString([selectedPickupCoordinate, selectedDestinationCoordinate]);
    return distance.getLength().toFixed(2); // Distance in meters (two decimals)
}

function calculateEstimatedDuration(distanceInFlooredKilometres) {
    return Math.ceil(5 + (distanceInFlooredKilometres / 2));
}

function fillFareCalculation(pickup = getOrderPickup(), destination = getOrderDest()) {
    const distanceInMeters = calculateDistanceBetweenLocations(pickup, destination);
    const distanceInKilometres = (distanceInMeters / 1000).toFixed(2);
    const distanceInFlooredKilometres = Math.floor(distanceInMeters / 1000);
    const distanceCharge = (distanceInFlooredKilometres * 1.5).toFixed(2);

    const estDuration = calculateEstimatedDuration(distanceInFlooredKilometres);
    const durationCharge = (estDuration * 0.05).toFixed(2);

    document.querySelector("#distance-charge").innerText = distanceCharge;
    document.querySelector("#duration-charge").innerText = durationCharge;

    document.querySelector("#distance").innerText = distanceInKilometres;
    document.querySelector("#duration").innerText = estDuration.toString();
}
