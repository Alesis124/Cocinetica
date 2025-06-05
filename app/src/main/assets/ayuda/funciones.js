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

let touchStartX = 0;
let touchEndX = 0;
const SWIPE_THRESHOLD = 50;

document.addEventListener('touchstart', function(e) {
  touchStartX = e.changedTouches[0].screenX;
}, false);

document.addEventListener('touchend', function(e) {
  touchEndX = e.changedTouches[0].screenX;
  handleSwipe();
}, false); 

function handleSwipe() {
  const sidebar = document.getElementById("mobileSidebar");
  
  if (touchEndX > touchStartX + SWIPE_THRESHOLD && 
      touchStartX < 50 &&
      !sidebar.classList.contains("show")) {
    toggleSidebar();
  }
  
  if (touchEndX < touchStartX - SWIPE_THRESHOLD && 
      sidebar.classList.contains("show")) {
    toggleSidebar();
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