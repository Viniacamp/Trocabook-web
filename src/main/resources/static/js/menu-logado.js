function toggleMenu() {
  document.getElementById("userDropdown").classList.toggle("show");
}

// Fecha o menu caso o usuário clique fora da caixa do usuário
window.onclick = function(event) {
  if (!event.target.closest('.user-menu-container')) {
    var dropdowns = document.getElementsByClassName("dropdown-content");
    for (var i = 0; i < dropdowns.length; i++) {
      var openDropdown = dropdowns[i];
      if (openDropdown.classList.contains('show')) {
        openDropdown.classList.remove('show');
      }
    }
  }
}