export default async function getLocation(){

  const options = {
    enableHighAccuracy: true,
    timeout: 15000,
    maximumAge: 25000,
  }

  navigator.geolocation.getCurrentPosition(success, error, options)

  function success(position){
    const latitude = position.coords.latitude
    const longitude = position.coords.longitude
    console.log(latitude, longitude)
    return{latitude: latitude, longitude: longitude};
  }

  async function error(position) {
    alert("GPS isn't available, attempt with IP address will be made");
    return await getIpLocation();
  }

}

async function getIpLocation() {
await fetch('http://ip-api.com/json/?fields=61439')
    .then(response => response.json())
    .then(data => {
      console.log(data);
      const latitude = data.latitude;
      const longitude = data.longitude;
      return {latitude: latitude, longitude: longitude};
    })
    .catch(error => console.log(error))
}

