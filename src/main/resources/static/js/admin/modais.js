const modalAlterar =
    document.getElementById("modalAlterarUsuario");

const modalStatus =
    document.getElementById("modalStatusUsuario");

const alterarNome =
    document.getElementById("alterarUsuarioNome");

const alterarEmail =
    document.getElementById("alterarUsuarioEmail");

const alterarAvaliacao =
    document.getElementById("alterarUsuarioAvaliacao");

const alterarErro =
    document.getElementById("alterarUsuarioErro");

const tituloStatus =
    document.getElementById("tituloModalStatus");

const mensagemStatus =
    document.getElementById("mensagemModalStatus");

const avisoStatus =
    document.getElementById("avisoModalStatus");

const statusErro =
    document.getElementById("statusUsuarioErro");

const confirmarStatus =
    document.getElementById("confirmarStatusUsuario");


function abrirModal(modal) {
    modal.hidden = false;
    document.body.classList.add("admin-modal-aberto");
}


export function fecharModal(modal) {
    modal.hidden = true;

    if (
        modalAlterar.hidden &&
        modalStatus.hidden
    ) {
        document.body.classList.remove(
            "admin-modal-aberto"
        );
    }
}


export function abrirModalAlterar(usuario) {

    alterarNome.textContent = usuario.nome;
    alterarEmail.textContent = usuario.emailPrincipal;

    alterarAvaliacao.value =
        Number(usuario.avaliacao ?? 0).toFixed(1);

    alterarErro.hidden = true;
    alterarErro.textContent = "";

    abrirModal(modalAlterar);

    alterarAvaliacao.focus();
}


export function abrirModalStatus(usuario) {

    const desativar =
        usuario.status === "ATIVO";

    tituloStatus.textContent =
        desativar
            ? "Desativar usuário"
            : "Ativar usuário";

    mensagemStatus.textContent =
        desativar
            ? `Deseja realmente desativar a conta de ${usuario.nome}?`
            : `Deseja realmente ativar a conta de ${usuario.nome}?`;

    avisoStatus.textContent =
        desativar
            ? "O usuário não poderá acessar a plataforma enquanto a conta estiver desativada."
            : "O usuário poderá voltar a acessar normalmente a plataforma.";

    confirmarStatus.textContent =
        desativar
            ? "Desativar"
            : "Ativar";

    confirmarStatus.className =
        desativar
            ? "admin-btn admin-btn-desativar"
            : "admin-btn admin-btn-ativar";

    statusErro.hidden = true;
    statusErro.textContent = "";

    abrirModal(modalStatus);
}


export function obterAvaliacaoModal() {
    return Number(alterarAvaliacao.value);
}


export function mostrarErroAlteracao(mensagem) {
    alterarErro.textContent = mensagem;
    alterarErro.hidden = false;
}


export function mostrarErroStatus(mensagem) {
    statusErro.textContent = mensagem;
    statusErro.hidden = false;
}


export function setAlteracaoCarregando(carregando) {

    const botao =
        document.getElementById(
            "salvarAlteracaoUsuario"
        );

    botao.disabled = carregando;

    botao.textContent =
        carregando
            ? "Salvando..."
            : "Salvar";
}


export function setStatusCarregando(
    carregando,
    statusAtual
) {

    confirmarStatus.disabled = carregando;

    if (carregando) {
        confirmarStatus.textContent =
            "Processando...";
        return;
    }

    confirmarStatus.textContent =
        statusAtual === "ATIVO"
            ? "Desativar"
            : "Ativar";
}


document
    .querySelectorAll("[data-fechar-modal]")
    .forEach(botao => {

        botao.addEventListener(
            "click",
            () => {

                const id =
                    botao.dataset.fecharModal;

                const modal =
                    document.getElementById(id);

                fecharModal(modal);
            }
        );
    });


[modalAlterar, modalStatus]
    .forEach(modal => {

        modal.addEventListener(
            "click",
            event => {

                if (event.target === modal) {
                    fecharModal(modal);
                }
            }
        );
    });


document.addEventListener(
    "keydown",
    event => {

        if (event.key !== "Escape") {
            return;
        }

        if (!modalAlterar.hidden) {
            fecharModal(modalAlterar);
        }

        if (!modalStatus.hidden) {
            fecharModal(modalStatus);
        }
    }
);