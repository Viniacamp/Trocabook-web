import { exigirNaoAutenticado } from "./auth.js";
import { validateForm } from "./form-validation.js";

/*
 - Impede que um usuário já autenticado permaneça na página de cadastro.
 */
exigirNaoAutenticado();

const cadastroForm = document.getElementById("cadastroForm");

/**
 * Executa o reCAPTCHA v3 e submete o formulário.
 * Possui mecanismo de retentativa para aguardar o download da biblioteca.
 */
function processarEnvioRecaptcha(tentativas = 0) {
    // Se o objeto do Google reCAPTCHA já existe e tem a função .ready
    if (typeof grecaptcha !== "undefined" && typeof grecaptcha.ready === "function") {

        const recaptchaSiteKeyInput = document.getElementById("recaptchaSiteKey");
        const recaptchaSiteKey = (recaptchaSiteKeyInput && recaptchaSiteKeyInput.value)
            ? recaptchaSiteKeyInput.value
            : "6LdYpqItAAAAACx5jkgwv3dQHPKTJxDIZoThJaef";

        grecaptcha.ready(function () {
            grecaptcha.execute(
                recaptchaSiteKey,
                { action: "cadastro" }
            ).then(function (token) {
                // Insere o token no campo oculto e submete
                document.getElementById("g-recaptcha-response").value = token;
                cadastroForm.submit();
            }).catch(function (error) {
                console.error("Erro ao gerar token reCAPTCHA:", error);
                alert("Falha na validação de segurança reCAPTCHA. Tente novamente.");
            });
        });

    } else if (tentativas < 5) {
        // Se o script do Google ainda está terminando de baixar na rede, aguarda 400ms e tenta de novo
        setTimeout(() => processarEnvioRecaptcha(tentativas + 1), 400);
    } else {
        // Se passou de 2 segundos e não carregou, avisa e faz fallback seguro ou alerta
        console.error("Não foi possível detectar a biblioteca grecaptcha global.");
        alert("O reCAPTCHA demorou a responder. Verifique sua conexão ou extensões de AdBlock e tente novamente.");
    }
}

if (cadastroForm) {
    cadastroForm.addEventListener("submit", function (event) {
        event.preventDefault();

        // Validação JavaScript local (campos obrigatórios, CPF, senha, etc.)
        if (!validateForm()) {
            return;
        }

        // Inicia o processo de envio aguardando a biblioteca do reCAPTCHA
        processarEnvioRecaptcha();
    });
}

/*
 - Mostrar / Ocultar Senha
 */
const togglePassword = document.getElementById("togglePassword");
const passwordField = document.getElementById("senha");

if (togglePassword && passwordField) {

    togglePassword.addEventListener("click", function () {

        const tipoAtual = passwordField.getAttribute("type");
        const icone = togglePassword.querySelector("i");

        if (tipoAtual === "password") {

            passwordField.setAttribute("type", "text");
            icone.classList.remove("fa-eye");
            icone.classList.add("fa-eye-slash");
            togglePassword.setAttribute("aria-label", "Ocultar senha");

        } else {

            passwordField.setAttribute("type", "password");
            icone.classList.remove("fa-eye-slash");
            icone.classList.add("fa-eye");
            togglePassword.setAttribute("aria-label", "Mostrar senha");
        }

    });

}