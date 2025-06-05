function toggleSidebar() {
  const sidebar = document.getElementById("mobileSidebar");
  const checkbox = document.getElementById("lanzador");
  sidebar.classList.toggle("show");

  if (sidebar.classList.contains("show")) {
    checkbox.checked = true;
  } else {
    checkbox.checked = false;
  }
}

document.querySelectorAll(".mobile-sidebar a").forEach(link => {
  link.addEventListener("click", () => {
    document.getElementById("mobileSidebar").classList.remove("show");
    document.getElementById("lanzador").checked = false;
  });
});

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
  anchor.addEventListener("click", function (e) {
    e.preventDefault();
    const target = document.querySelector(this.getAttribute("href"));
    if (target) {
      target.scrollIntoView({ behavior: "smooth" });
    }
  });
});
