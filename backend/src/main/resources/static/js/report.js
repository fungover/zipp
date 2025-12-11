import getLocation from "./getLocation.js";

const form = document.querySelector(".report");
const button = document.querySelectorAll(".report__button");

button.forEach(button => {
  button.addEventListener("click", async (e) => {
    e.preventDefault();
    const report = e.currentTarget.value;

    if(!form.checkValidity()) {
      form.reportValidity();
      return;
    }

    const input = document.getElementById("report__description");
    const imageUrl = document.getElementById("report__url");
    const result = document.getElementById("report__result");

    try {
      const location = await getLocation();

      fetch("/api/reports", {
        method: "POST",
        credentials: "same-origin",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
          submittedByUserId: 1000,
          description: input.value,
          eventType: report,
          latitude: location.latitude,
          longitude: location.longitude,
          submittedAt: Date.now(),
          status: "ACTIVE",
          image: imageUrl.value,
        })
      })
        .then(response => response.json())
        .then(data => {
          result.innerText = "Success sending report for: "+ data.eventType;

        })
    } catch (error) {
      console.error(error);
      alert("Couldn't send report");
    }
  })
})

