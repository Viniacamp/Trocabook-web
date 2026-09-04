import { auth } from "./firebase.js";
import { exigirNaoAutenticado } from "./auth.js";

import {
    signInWithEmailAndPassword
} from "https://www.gstatic.com/firebasejs/12.18.0/firebase-auth.js";

/*
 * Impede que um usuário já autenticado
 * permaneça na página de login.
 */
exigirNaoAutenticado();

/*
 * Elementos da página
 */
const loginForm = document.getElementById("loginForm");
const loginButton = document.getElementById("loginButton");
const loginError = document.getElementById("login-error");
const togglePassword = document.getElementById("togglePassword");
const passwordField = document.getElementById("senha");

/*
 * Login
 */
loginForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    loginError.style.display = "none";

    const email =
        document.getElementById("email").value.trim();

    const senha =
        passwordField.value;

    /*
     * Validação básica
     */
    if (!email || !senha) {

        loginError.textContent =
            "Preencha o e-mail e a senha.";

        loginError.style.display = "block";

        return;
    }

    loginButton.disabled = true;
    loginButton.textContent = "Entrando...";

    try {

        /*
         * Executa o reCAPTCHA
         */
        const recaptchaSiteKey =
            document.getElementById("recaptchaSiteKey").value;

        const tokenRecaptcha =
            await new Promise((resolve, reject) => {

                grecaptcha.ready(function () {

                    grecaptcha.execute(
                        recaptchaSiteKey,
                        { action: "login" }
                    )
                    .then(resolve)
                    .catch(reject);

                });

            });

        /*
         * Coloca o token do reCAPTCHA no formulário.
         */
        document.getElementById(
            "g-recaptcha-response"
        ).value = tokenRecaptcha;


        /*
         * Autentica o usuário no Firebase.
         */
        await signInWithEmailAndPassword(
            auth,
            email,
            senha
        );


        /*
         * Obtém o usuário autenticado.
         */
        const user = auth.currentUser;

        if (!user) {
            throw new Error(
                "Usuário não encontrado após autenticação."
            );
        }


        /*
         * Obtém o Firebase ID Token.
         */
        const idToken =
            await user.getIdToken();


        /*
         * Envia o token para o backend.
         *
         * O FirebaseAuthenticationFilter irá:
         *
         * 1. receber o Bearer Token;
         * 2. validar o token no Firebase;
         * 3. criar a Authentication;
         * 4. disponibilizar o FirebaseToken
         *    no SecurityContext.
         */
        const response = await fetch(
            "/api/auth/autenticacao",
            {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${idToken}`
                }
            }
        );


        /*
         * Verifica se o backend aceitou
         * a autenticação.
         */
        if (!response.ok) {

            throw new Error(
                "O backend não conseguiu autenticar o usuário."
            );
        }


        window.location.href = "/";


    } catch (error) {

        console.error(
            "Erro ao realizar login:",
            error
        );

        loginError.textContent =
            "Usuário ou senha inválidos.";

        loginError.style.display =
            "block";

    } finally {

        loginButton.disabled = false;

        loginButton.textContent =
            "Fazer login";
    }

});


/*
 * Mostrar / ocultar senha
 */
togglePassword.addEventListener("click", function () {

    const tipoAtual = passwordField.getAttribute("type");

    const icone = togglePassword.querySelector("i");

    if (tipoAtual === "password") {

        passwordField.setAttribute(
            "type",
            "text"
        );

        icone.classList.remove("fa-eye");
        icone.classList.add("fa-eye-slash");

        togglePassword.setAttribute(
            "aria-label",
            "Ocultar senha"
        );

    } else {

        passwordField.setAttribute(
            "type",
            "password"
        );

        icone.classList.remove("fa-eye-slash");
        icone.classList.add("fa-eye");

        togglePassword.setAttribute(
            "aria-label",
            "Mostrar senha"
        );
    }

});