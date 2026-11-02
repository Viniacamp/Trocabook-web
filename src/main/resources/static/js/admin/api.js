import { auth } from "/js/firebase.js";

const PRIMARY_SERVICE_URL = "https://trocabook-admin-service.azurewebsites.net";
const FALLBACK_SERVICE_URL = "http://localhost:8181";

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
                reject(new Error("Usuário não autenticado no Firebase."));
            }
        });
    });
}

/**
 * Executa o fetch enviando a URL base especificada.
 */
async function realizarFetch(baseUrl, endpoint, options, idToken) {
    const headers = new Headers(options.headers || {});
    headers.set("Authorization", `Bearer ${idToken}`);

    if (options.body && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }

    // Garante a formatação correta das barras de URL
    const urlFinal = `${baseUrl.replace(/\/$/, "")}/${endpoint.replace(/^\//, "")}`;

    const response = await fetch(urlFinal, {
        ...options,
        headers
    });

    if (!response.ok) {
        throw new Error(`HTTP_${response.status}`);
    }

    return response;
}

export async function adminFetch(endpoint, options = {}) {
    const user = await obterUsuarioFirebase();
    const idToken = await user.getIdToken();

    try {
        // 1ª Tentativa: Azure
        return await realizarFetch(PRIMARY_SERVICE_URL, endpoint, options, idToken);
    } catch (error) {
        // Identifica falha de rede (servidor desligado/offline) ou indisponibilidade (502/503/504)
        const ehFalhaConexao = error.name === "TypeError" ||
            error.message.includes("HTTP_502") ||
            error.message.includes("HTTP_503") ||
            error.message.includes("HTTP_504");

        if (ehFalhaConexao) {
            console.warn(`[adminFetch] Falha na Azure (${error.message}). Redirecionando para o localhost...`);

            // 2ª Tentativa: Localhost
            return await realizarFetch(FALLBACK_SERVICE_URL, endpoint, options, idToken);
        }

        // Se for um erro do cliente (ex: 401, 403, 404), relança o erro sem tentar o fallback
        throw error;
    }
}