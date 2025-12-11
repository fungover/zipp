export default function getLocation(){

  return new Promise(async(resolve, reject) => {

    navigator.geolocation.getCurrentPosition(
      position => {
        resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});
      },
      async ()=>{
        alert("GPS isn't available, attempt with IP address will be made to get estimated position");
        try{
          resolve (await getIpLocation());
        }catch(error){
          reject(new Error("Location with IP address failed: " + error.message));
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 25000,
      }
    )
  });
}

async function getIpLocation() {
 const response = await fetch('http://ip-api.com/json/?fields=61439');
 if(!response.ok){
   throw Error(response.statusText);
 }
 const data = await response.json();
 const {lat, lon} = data;
  return {latitude: lat, longitude: lon};
}
