const button = document.querySelectorAll(".report__button");

button.forEach(button => {
  button.addEventListener("click", (e) => {
    e.preventDefault();
    const report = e.currentTarget.value;
    console.log(report);
  })
})

