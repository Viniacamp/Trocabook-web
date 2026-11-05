import { auth, getMessagingSeguro, VAPID_KEY } from "./firebase.js";
import { getToken } from "https://www.gstatic.com/firebasejs/12.18.0/firebase-messaging.js";
import { exigirNaoAutenticado } from "./auth.js";

import {
    signInWithEmailAndPassword
} from "https://www.gstatic.com/firebasejs/12.18.0/firebase-auth.js";

/*
- Impede que um usuário já autenticado

- permaneça na página de login.

 */
exigirNaoAutenticado();

/*
- Registra o token de notificação FCM do navegador no backend.

 */
async function registrarFcmToken(idToken) {
    try {
        const messaging = await getMessagingSeguro();

        if (!messaging) {
            // Navegador não suporta FCM ou permissão bloqueada, ignora sem quebrar o login
            return;
        }

        const permission = await Notification.requestPermission();
        if (permission !== "granted") return;

        const fcmToken = await getToken(messaging, { vapidKey: VAPID_KEY });

        if (fcmToken) {
            await fetch("/api/fcm/token", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${idToken}`
                },
                body: JSON.stringify({ fcmToken })
            });
            console.log("Token FCM registrado com sucesso no backend.");
        }
    } catch (error) {
        console.warn("Falha ao registrar FCM Token (fluxo de login mantido):", error);
    }
}

/*
- Elementos da página

 */
const loginForm = document.getElementById("loginForm");
const loginButton = document.getElementById("loginButton");
const loginError = document.getElementById("login-error");
const togglePassword = document.getElementById("togglePassword");
const passwordField = document.getElementById("senha");

/*
- Login

 */
loginForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    loginError.style.display = "none";

    const email =
        document.getElementById("email").value.trim();

    const senha =
        passwordField.value;

    /*
    - Validação básica

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
        - Executa o reCAPTCHA

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
        - Coloca o token do reCAPTCHA no formulário.

         */
        document.getElementById(
            "g-recaptcha-response"
        ).value = tokenRecaptcha;


        /*
        - Autentica o usuário no Firebase.

         */
        await signInWithEmailAndPassword(
            auth,
            email,
            senha
        );


        /*
        - Obtém o usuário autenticado.

         */
        const user = auth.currentUser;

        if (!user) {
            throw new Error(
                "Usuário não encontrado após autenticação."
            );
        }


        /*
        - Obtém o Firebase ID Token.

         */
        const idToken =
            await user.getIdToken();


        /*
        - Envia o token para o backend.

         *
        - O FirebaseAuthenticationFilter irá:

         *
        - receber o Bearer Token;

        - 2. validar o token no Firebase;

        - 3. criar a Authentication;

        - 4. disponibilizar o FirebaseToken

        -    no SecurityContext.

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
        - Verifica se o backend aceitou

        - a autenticação.

         */
        if (!response.ok) {

            throw new Error(
                "O backend não conseguiu autenticar o usuário."
            );
        }


        /*
        - Registra o token FCM em segundo plano (sem travar o login)

         */
        registrarFcmToken(idToken);


        const destinoResponse = await fetch(
            "/api/auth/destino"
        );

        if (!destinoResponse.ok) {
            throw new Error(
                "Não foi possível determinar o destino do usuário."
            );
        }

        const destino = await destinoResponse.text();

        window.location.href = destino;


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
- Mostrar / ocultar senha

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