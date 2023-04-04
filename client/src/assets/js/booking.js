"use strict";

let config, api;

document.addEventListener("DOMContentLoaded", init);

async function init() {
    config = await loadConfig();
    api = `${config.host ? config.host + '/' : ''}${config.group ? config.group + '/' : ''}api/`;

    initializeNavigation();
    retrieveBookings();
    document.querySelector("#bookings").addEventListener("click", clickBookingContainer);
}

function retrieveBookings() {
    get(`bookings/${SIMULATED_ID}`, handleBookings);
}

async function handleBookings(response) {
    const bookings = await response.json().then();
    bookings["futureBookings"].forEach(addBookingCard);
}

async function addBookingCard(booking) {
    const pickupLocation = await reverseGeocode(booking["route"]["pickupLocation"]);
    const destination = await reverseGeocode(booking["route"]["destination"]);
    const distanceInMeters = calculateDistanceBetweenLocations(booking["route"]["pickupLocation"], booking["route"]["destination"]);
    const distanceInFlooredKilometres = Math.floor(distanceInMeters / 1000);
    const card = `<div class="summary-card" data-id="${booking["bookingId"]}">
                    <h2>${destination} Booking</h2>
                    <ul>
                      <li><span>Date and time</span><span>${booking["datetime"]}</span></li>
                      <li><span>From</span><span></span>${pickupLocation}</li>
                      <li><span>To</span><span>${destination}</span></li>
                      <li><span>Est. travel duration</span><span>${calculateEstimatedDuration(distanceInFlooredKilometres)} min</span></li>
                      <li><span>Est. distance</span><span>${(distanceInMeters / 1000).toFixed(2)} km</span></li>
                    </ul>
                    ${createFareContainer(booking["route"])}
                    <small>Payment will be made automatically through mID at the end of the trip.</small>
                    <button class="text-btn cancel-btn">Cancel booking</button>
                  </div>`;
    document.querySelector("#bookings").insertAdjacentHTML("beforeend", card);
}

function createFareContainer(route) {
    return ` <div class="fare">
               <p>
               <strong>Estimated fare</strong>
               <strong>
                 <small>MC</small>
                 <span>${calculateEstimatedFare(route["pickupLocation"], route["destination"])}</span>
               </strong>
               </p>
             </div>`;
}

function clickBookingContainer(e) {
    e.preventDefault();
    if (e.target.classList.contains("cancel-btn")) {
        const bookingId = e.target.closest(".summary-card").getAttribute("data-id");
        askToCancelBooking(bookingId);
    }
}

function askToCancelBooking(bookingId) {
    const title = "Are you sure you'd like to cancel your booking?";
    const action = "This will permanently delete your booking";
    showModal(title, action, "booking", () => cancelBooking(bookingId));
}

function cancelBooking(bookingId) {
    remove(`bookings/${bookingId}`, () => location.reload());
}
