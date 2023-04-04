const CHNL_TO_SERVER = "events.to.server";
const EVENTBUS_PATH = "https://project-ii.ti.howest.be/mars-03/events";
const CHNL_TO_CLIENTS_BROADCAST = "events.to.clients";
const CHNL_TO_CLIENT_UNICAST = "events.to.client.";

function openSocket() {
    const eb = new EventBus(EVENTBUS_PATH);

    let clientId = localStorage.getItem("clientId");

    if (localStorage.getItem('clientId') === null) {
        clientId = uuidv4();
        localStorage.setItem('clientId', clientId);
    }

    function sendToServer(message) {
        message.clientId = clientId;
        eb.send(CHNL_TO_SERVER, message);
    }

    eb.onopen = function () {
        eb.registerHandler(CHNL_TO_CLIENTS_BROADCAST, simulateDroneTracking);
        eb.registerHandler(CHNL_TO_CLIENT_UNICAST + clientId, simulateDroneTracking);
    };
    return sendToServer;
}

function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}
