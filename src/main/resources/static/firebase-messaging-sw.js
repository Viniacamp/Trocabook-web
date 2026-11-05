importScripts("https://www.gstatic.com/firebasejs/12.18.0/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/12.18.0/firebase-messaging-compat.js");

// As mesmas credenciais do seu firebase.js
firebase.initializeApp({
    apiKey: "AIzaSyClRyuzZ3VFTlHwZBTrt7Z-I0vSldRe9U8",
    authDomain: "trocabook-92297.firebaseapp.com",
    projectId: "trocabook-92297",
    storageBucket: "trocabook-92297.firebasestorage.app",
    messagingSenderId: "141097973066",
    appId: "1:141097973066:web:19167c8bececc72f8c3db3",
    measurementId: "G-HQG9DFF52L"
});

const messaging = firebase.messaging();

// Trata notificações recebidas com o navegador em segundo plano ou fechado
messaging.onBackgroundMessage((payload) => {
    console.log("[firebase-messaging-sw.js] Notificação recebida em segundo plano:", payload);

    const notificationTitle = payload.notification?.title || "Trocabook";
    const notificationOptions = {
        body: payload.notification?.body || "Você tem uma nova mensagem.",
        icon: "/img/Design sem nome (7).svg",
        badge: "/img/Design sem nome (7).svg",
        data: payload.data || {}
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});

// Ação ao clicar na notificação push (Abre ou foca a aba do chat)
self.addEventListener("notificationclick", (event) => {
    event.notification.close();

    const dados = event.notification.data;
    let urlDestino = "/chat";

    if (dados && dados.anuncio && dados.destinatario) {
        urlDestino = `/chat?anuncio=${encodeURIComponent(dados.anuncio)}&destinatario=${encodeURIComponent(dados.destinatario)}`;
    }

    event.waitUntil(
        clients.matchAll({ type: "window", includeUncontrolled: true }).then((windowClients) => {
            // Se o chat já estiver aberto em alguma aba, foca nela
            for (let client of windowClients) {
                if (client.url.includes("/chat") && "focus" in client) {
                    return client.focus();
                }
            }
            // Caso contrário, abre uma nova janela
            if (clients.openWindow) {
                return clients.openWindow(urlDestino);
            }
        })
    );
});