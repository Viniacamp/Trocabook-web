import { exigirNaoAutenticado } from "./auth.js";
import { validateForm } from "./form-validation.js";

/*
 * Impede que um usuário já autenticado
 * permaneça na página de cadastro.
 */
exigirNaoAutenticado();

/*
 * Formulário de cadastro
 */
const cadastroForm = document.getElementById("cadastroForm");

if (cadastroForm) {

    cadastroForm.addEventListener("submit", function (event) {

        /*
         * Impede o envio enquanto fazemos
         * as validações e o reCAPTCHA.
         */
        event.preventDefault();

        /*
         * Executa as validações do formulário.
         */
        if (!validateForm()) {
            return;
        }

        /*
         * Executa o reCAPTCHA.
         */
        const recaptchaSiteKey =
            document.getElementById("recaptchaSiteKey").value;

        grecaptcha.ready(function () {

            grecaptcha.execute(
                recaptchaSiteKey,
                { action: "cadastro" }
            ).then(function (token) {

                /*
                 * Coloca o token no formulário.
                 */
                document.getElementById(
                    "g-recaptcha-response"
                ).value = token;

                /*
                 * Envia o formulário para o backend.
                 */
                cadastroForm.submit();

            }).catch(function (error) {

                console.error(
                    "Erro ao executar reCAPTCHA:",
                    error
                );

            });

        });

    });
}