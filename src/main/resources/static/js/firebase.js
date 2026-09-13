import { initializeApp } from
        "https://www.gstatic.com/firebasejs/12.18.0/firebase-app.js";

import { getAuth } from
        "https://www.gstatic.com/firebasejs/12.18.0/firebase-auth.js";

const firebaseConfig = {
    apiKey: "AIzaSyClRyuzZ3VFTlHwZBTrt7Z-I0vSldRe9U8",
    authDomain: "trocabook-92297.firebaseapp.com",
    projectId: "trocabook-92297",
    storageBucket: "trocabook-92297.firebasestorage.app",
    messagingSenderId: "141097973066",
    appId: "1:141097973066:web:19167c8bececc72f8c3db3",
    measurementId: "G-HQG9DFF52L"
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);