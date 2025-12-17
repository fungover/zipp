const mapButton = document.querySelector(".map__button");

if (mapButton) {

mapButton.addEventListener("click", async function (event) {

  try {
    const response = await fetch("/api/reports")

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    const data = await response.json()

    data.forEach((res) => {
      const {latitude, longitude, eventType} = res;
      const event = eventType;
      const lat = parseFloat(latitude);
      const lng = parseFloat(longitude);
      loadMap(lat, lng, event);
    })

    const map = document.getElementById("map");
    map.scrollIntoView({
      behavior: "smooth",
      block: "center",
    })

  } catch (error) {
    console.error(error);
    alert("Couldn't retrieve any reports");
  }
})

function loadMap(lat, lng, event) {

  const map = new google.maps.Map(document.getElementById("map"), {
    center: {lat, lng},
    zoom: 12
  });

  new google.maps.Marker({
    position: {lat, lng},
    map: map,
    title: event
  });
}
}
