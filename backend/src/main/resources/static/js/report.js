function getLocation(){
  const options = {
    enableHighAccuracy: true,
    timeout: 5000,
    maximumAge: 25000,
  }
  navigator.geolocation.getCurrentPosition(success, error, options)

  function success(position){
    const latitude = position.coords.latitude
    const longitude = position.coords.longitude
    console.log(latitude, longitude)
  }

  function error(position){
    alert("Error: "+position);
  }

}

function getUserLocation(){
  fetch('http://ip-api.com/json/?fields=61439')
    .then(response => response.json())
    .then(data => console.log(data))
}

const button = document.querySelectorAll(".report__button");

button.forEach(button => {
  button.addEventListener("click", (e) => {
    e.preventDefault();
    const report = e.currentTarget.value;

    getLocation();
    getUserLocation()

    fetch("/api/reports", {
      method: "POST",
      credentials: "same-origin",
      headers: {"Content-Type": "application/json"},
      body: JSON.stringify({
        submittedByUserId: 1000,
        description: "debris",
        eventType: report,
        latitude: 50.1010,
        longitude: 50.1010,
        submittedAt: Date.now(),
        status: "ACTIVE",
        image: null,
      })
    })
      .then(response => response.json())
      .then(data => {
        console.log(data);
      })
      .catch(error => console.log(error)) ;
  })
})

// return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
