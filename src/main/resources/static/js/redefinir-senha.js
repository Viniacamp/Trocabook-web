import { auth } from "./firebase.js";

import {
    verifyPasswordResetCode,
    confirmPasswordReset
} from "https://www.gstatic.com/firebasejs/12.18.0/firebase-auth.js";


const parametros = new URLSearchParams(
    window.location.search
);

const oobCode = parametros.get("oobCode");

const formulario = document.getElementById(
    "formRedefinirSenha"
);

const mensagem = document.getElementById(
    "mensagem"
);

const botaoRedefinir = document.getElementById(
    "botaoRedefinir"
);


function mostrarMensagem(texto, tipo) {

    mensagem.textContent = texto;

    mensagem.className = `alert alert-${tipo}`;
}


async function verificarCodigo() {

    if (!oobCode) {

        formulario.style.display = "none";

        mostrarMensagem(
            "Link de redefinição inválido.",
            "danger"
        );

        return;
    }

    try {

        await verifyPasswordResetCode(
            auth,
            oobCode
        );

    } catch (erro) {

        console.error(
            "Erro ao verificar código:",
            erro
        );

        formulario.style.display = "none";

        mostrarMensagem(
            "Este link é inválido ou já expirou.",
            "danger"
        );
    }
}


formulario.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        const senha = document.getElementById(
            "senha"
        ).value;

        const confirmaSenha = document.getElementById(
            "confirmaSenha"
        ).value;


        if (senha !== confirmaSenha) {

            mostrarMensagem(
                "As senhas não coincidem.",
                "danger"
            );

            return;
        }


        if (senha.length < 6) {

            mostrarMensagem(
                "A senha deve possuir pelo menos 6 caracteres.",
                "danger"
            );

            return;
        }


        botaoRedefinir.disabled = true;


        try {

            await confirmPasswordReset(
                auth,
                oobCode,
                senha
            );


            mostrarMensagem(
                "Senha redefinida com sucesso! Você será redirecionado para o login.",
                "success"
            );


            formulario.style.display = "none";


            setTimeout(function () {

                window.location.href = "/loginNovo";

            }, 3000);


        } catch (erro) {

            console.error(
                "Erro ao redefinir senha:",
                erro
            );


            let textoErro =
                "Não foi possível redefinir a senha.";


            if (erro.code === "auth/expired-action-code") {

                textoErro =
                    "Este link de redefinição expirou.";

            } else if (erro.code === "auth/invalid-action-code") {

                textoErro =
                    "Este link de redefinição é inválido ou já foi utilizado.";

            } else if (erro.code === "auth/weak-password") {

                textoErro =
                    "A senha informada é muito fraca.";
            }


            mostrarMensagem(
                textoErro,
                "danger"
            );

            botaoRedefinir.disabled = false;
        }
    }
);


verificarCodigo();