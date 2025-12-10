const button = document.querySelectorAll(".report__button");

button.forEach(button => {
  button.addEventListener("click", (e) => {
    e.preventDefault();
    const report = e.currentTarget.value;
    console.log(report);

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
