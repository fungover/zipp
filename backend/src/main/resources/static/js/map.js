
  function initMap() {

  const defaultLocation = { lat: 59.3293, lng: 18.0686 }; //  (Stockholm)

  function loadMap(center) {
  const map = new google.maps.Map(document.getElementById("map"), {
  center: center,
  zoom: 12
});

  new google.maps.Marker({
  position: center,
  map: map,
  title: "You are here"
});
}

  if (navigator.geolocation) {
  navigator.geolocation.getCurrentPosition(
  (position) => {
  const userLocation = {
  lat: position.coords.latitude,
  lng: position.coords.longitude
};
  loadMap(userLocation);
},
  (error) => {
  console.warn("Location denied.Showing Stockholm instead.");
  loadMap(defaultLocation);
});
}
  else {
  console.warn("Geolocation are not approved. Showing Stockholm.");
  loadMap(defaultLocation);
}
}
