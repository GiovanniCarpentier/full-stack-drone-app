"use strict";

let imageCapture;

function startVideoFeed() {
    navigator.mediaDevices.getUserMedia({video: true})
        .then(mediaStream => {
            document.querySelector("section#event-maker input[type=submit]").insertAdjacentHTML("beforebegin", `<video autoplay></video>
                                                                                                                                   <input type="button" value="Take picture">`);
            document.querySelector("video").srcObject = mediaStream;

            const track = mediaStream.getVideoTracks()[0];
            imageCapture = new ImageCapture(track);
            document.querySelector("#event-maker input[type=button]").addEventListener("click", takePicture);
        })
        .catch(error => console.log(error));
}

function takePicture() {
    imageCapture.takePhoto()
        .then(blob => createImageBitmap(blob))
        .then(imageBitmap => {
            drawCanvas(imageBitmap);
        })
        .catch(error => console.log(error));
}

function drawCanvas(img) {
    const $canvas = document.createElement("canvas");
    const $video = document.querySelector("video");

    $canvas.height = $video.offsetHeight;
    $canvas.width = $video.offsetWidth;
    $video.replaceWith($canvas);

    const ratio = Math.min($canvas.width / img.width, $canvas.height / img.height);
    const x = ($canvas.width - img.width * ratio) / 2;
    const y = ($canvas.height - img.height * ratio) / 2;
    $canvas.getContext('2d').clearRect(0, 0, $canvas.width, $canvas.height);
    $canvas.getContext('2d').drawImage(img, 0, 0, img.width, img.height,
        x, y, img.width * ratio, img.height * ratio);
}
