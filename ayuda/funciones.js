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