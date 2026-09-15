import { signOut } from
        "https://www.gstatic.com/firebasejs/12.18.0/firebase-auth.js";

import { auth } from "./firebase.js";

async function deslogar() {
    try {
        await signOut(auth);

        await fetch("/deslogar", {
            method: "POST"
        });

        window.location.href = "/login";
    } catch (erro) {
        console.error("Erro ao realizar logout:", erro);
    }
}

document.getElementById("deslogar").addEventListener("click", deslogar);