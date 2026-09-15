import { onAuthStateChanged } from
        "https://www.gstatic.com/firebasejs/12.18.0/firebase-auth.js";

import { auth } from "./firebase.js";

/**
 * Verifica se o usuário está autenticado.
 *
 * Se não estiver autenticado, redireciona para o login.
 */
export function exigirAutenticado() {

    return new Promise((resolve) => {

        const unsubscribe = onAuthStateChanged(auth, (user) => {

            unsubscribe();

            if (!user) {
                window.location.href = "/login";
                return;
            }

            resolve(user);
        });

    });
}

/**
 * Verifica se o usuário NÃO está autenticado.
 *
 * Se já estiver autenticado, redireciona para a página inicial.
 */
export function exigirNaoAutenticado() {

    return new Promise((resolve) => {

        const unsubscribe = onAuthStateChanged(auth, (user) => {

            unsubscribe();

            if (user) {
                window.location.href = "/";
                return;
            }

            resolve(null);
        });

    });
}