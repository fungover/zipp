export default async function getLocation() {

  try {
    return await getGpsLocation();
  } catch(error) {
    console.error(error);
    alert("GPS isn't available, attempt with IP address will be made to get estimated position");
    return await getIpLocation();
  }

}

function getGpsLocation(){

  return new Promise((resolve, reject) => {

    if(!navigator.geolocation){
      reject("Geolocation is not supported!");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      position => {
        resolve({lat: position.coords.latitude, lng: position.coords.longitude});
      },
      error=>{
        console.warn(error.code);
        reject(new Error("GPS location unavailable"));
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0,
      });
  })
}

async function getIpLocation() {
 const response = await fetch('http://ip-api.com/json/?fields=61439');
 if(!response.ok){
   throw Error(response.statusText);
 }
 const data = await response.json();
 const {lat, lon} = data;
 return {lat: lat, lng: lon};
}
