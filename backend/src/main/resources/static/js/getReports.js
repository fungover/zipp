const mapButton = document.querySelector(".map__button");

if (mapButton) {

mapButton.addEventListener("click", async function (event) {

  try {
    const response = await fetch("/api/reports")

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    const data = await response.json()

    const mapElement = document.getElementById("map");

    const map = new google.maps.Map(mapElement, {
      zoom: 12
    });

    const bounds = new google.maps.LatLngBounds();

    data.forEach((res) => {
      const {latitude, longitude, eventType} = res;
      const event = eventType;
      const lat = parseFloat(latitude);
      const lng = parseFloat(longitude);

      const position = {lat: lat, lng: lng};

      new google.maps.Marker({
        position: position,
        map: map,
        title: event
      });

      bounds.extend(position);
    })

    if (data.length > 0) {
      map.fitBounds(bounds);
    }

    mapElement.scrollIntoView({
      behavior: "smooth",
      block: "center",
    })

  } catch (error) {
    console.error(error);
    alert("Couldn't retrieve any reports");
  }
})
}
