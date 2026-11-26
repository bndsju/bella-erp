// Aplica o tema salvo antes de a tela aparecer, para não piscar o claro no Dark. Sem escolha salva, o padrão é o claro.
// Fica em arquivo próprio (e não inline no HTML) para a política de segurança de conteúdo poder proibir scripts inline.
(function () {
  try {
    var salvo = localStorage.getItem("bella-tema");
    var escuro =
      salvo === "dark" ||
      (salvo === "sistema" && window.matchMedia("(prefers-color-scheme: dark)").matches);
    if (escuro) document.documentElement.setAttribute("data-tema", "dark");
  } catch (e) {}
})();
