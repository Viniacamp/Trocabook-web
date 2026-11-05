import { initializeApp } from "https://www.gstatic.com/firebasejs/12.18.0/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/12.18.0/firebase-auth.js";
import {
    getMessaging,
    isSupported
} from "https://www.gstatic.com/firebasejs/12.18.0/firebase-messaging.js";

const firebaseConfig = {
    apiKey: "AIzaSyClRyuzZ3VFTlHwZBTrt7Z-I0vSldRe9U8",
    authDomain: "trocabook-92297.firebaseapp.com",
    projectId: "trocabook-92297",
    storageBucket: "trocabook-92297.firebasestorage.app",
    messagingSenderId: "141097973066",
    appId: "1:141097973066:web:19167c8bececc72f8c3db3",
    measurementId: "G-HQG9DFF52L"
};

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);

/*
 - Inicialização segura do FCM
 - Garante que se o navegador/contexto não suportar Web Push,
 - o restante da aplicação (Auth, Login) continue funcionando perfeitamente.
 */
let messagingInstance = null;

export async function getMessagingSeguro() {
    if (messagingInstance) {
        return messagingInstance;
    }

    try {
        const suportado = await isSupported();
        if (suportado) {
            messagingInstance = getMessaging(app);
            return messagingInstance;
        } else {
            console.warn("FCM Messaging não é suportado neste ambiente/navegador.");
            return null;
        }
    } catch (error) {
        console.warn("Erro ao checar suporte do Firebase Messaging:", error);
        return null;
    }
}

export const VAPID_KEY = "BAjp-QofrMOCva5RyvBfjEXPWRjkrrEPjUjod7cXASueeifX5GsTiXaACwAwm5_o5PIpzYsX_HFwSpcGrxOy8Ao";