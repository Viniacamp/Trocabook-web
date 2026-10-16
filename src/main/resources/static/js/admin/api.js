import { auth } from "/js/firebase.js";

const ADMIN_SERVICE_URL = "http://localhost:8181";

function obterUsuarioFirebase() {

    return new Promise((resolve, reject) => {

        if (auth.currentUser) {
            resolve(auth.currentUser);
            return;
        }

        const unsubscribe = auth.onAuthStateChanged(user => {

            unsubscribe();

            if (user) {
                resolve(user);
            } else {
                reject(
                    new Error(
                        "Usuário não autenticado no Firebase."
                    )
                );
            }
        });
    });
}


export async function adminFetch(endpoint, options = {}) {

    const user = await obterUsuarioFirebase();

    const idToken = await user.getIdToken();

    const headers = new Headers(
        options.headers || {}
    );

    headers.set(
        "Authorization",
        `Bearer ${idToken}`
    );

    if (options.body && !headers.has("Content-Type")) {
        headers.set(
            "Content-Type",
            "application/json"
        );
    }

    const response = await fetch(
        `${ADMIN_SERVICE_URL}${endpoint}`,
        {
            ...options,
            headers
        }
    );

    if (!response.ok) {
        throw new Error(
            `Erro na requisição administrativa: ${response.status}`
        );
    }

    return response;
}