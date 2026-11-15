importScripts("https://www.gstatic.com/firebasejs/12.18.0/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/12.18.0/firebase-messaging-compat.js");


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


messaging.onBackgroundMessage((payload) => {
    console.log("[firebase-messaging-sw.js] Notificação recebida em segundo plano:", payload);


    const dados = payload.data || {};

    const notificationTitle = dados.titulo || "Trocabook";
    const notificationOptions = {
        body: dados.corpo || "Você tem uma nova mensagem.",
        icon: "/img/Design sem nome (7).svg",
        badge: "/img/Design sem nome (7).svg",
        data: dados
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});


self.addEventListener("notificationclick", (event) => {
    event.notification.close();

    const dados = event.notification.data || {};
    let urlDestino = "/chat";


    if (dados.uidAnuncio && dados.uidRemetente) {
        urlDestino = `/chat?anuncio=${encodeURIComponent(dados.uidAnuncio)}&destinatario=${encodeURIComponent(dados.uidRemetente)}`;
    }

    event.waitUntil(
        clients.matchAll({ type: "window", includeUncontrolled: true }).then((windowClients) => {

            for (let client of windowClients) {
                if (client.url.includes("/chat") && "focus" in client) {
                    client.focus();
                    if ("navigate" in client) {
                        return client.navigate(urlDestino);
                    }
                    return;
                }
            }

            if (clients.openWindow) {
                return clients.openWindow(urlDestino);
            }
        })
    );
});