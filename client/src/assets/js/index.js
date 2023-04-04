"use strict";

document.addEventListener("DOMContentLoaded", init);

function init() {
    if (hasActiveRide()) {
        const $largeButton = document.querySelector("a.btn:last-child");
        $largeButton.setAttribute("href", "ride.html");
        $largeButton.querySelector("p").innerText = "View current ride";
    }
}
