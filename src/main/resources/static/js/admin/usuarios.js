import { adminFetch } from "./api.js";
import {
    abrirModalAlterar,
    abrirModalStatus,
    fecharModal,
    obterAvaliacaoModal,
    mostrarErroAlteracao,
    mostrarErroStatus,
    setAlteracaoCarregando,
    setStatusCarregando
} from "./modais.js";

const tabela = document.getElementById("tabelaUsuarios");
const tabelaBody = document.getElementById("usuariosTabelaBody");

const carregando = document.getElementById("usuariosCarregando");
const erro = document.getElementById("usuariosErro");

const pesquisa = document.getElementById("pesquisaUsuario");

const modalAlterar =
    document.getElementById("modalAlterarUsuario");

const modalStatus =
    document.getElementById("modalStatusUsuario");

const salvarAlteracao =
    document.getElementById("salvarAlteracaoUsuario");

const confirmarStatus =
    document.getElementById("confirmarStatusUsuario");

let usuarioSelecionado = null;

let usuarios = [];


async function carregarUsuarios() {

    carregando.hidden = false;
    erro.hidden = true;
    tabela.hidden = true;

    try {

        const response = await adminFetch(
            "/api/admin/usuarios"
        );

        usuarios = await response.json();

        renderizarUsuarios(usuarios);

        carregando.hidden = true;
        tabela.hidden = false;

    } catch (error) {

        console.error(
            "Erro ao carregar usuários:",
            error
        );

        carregando.hidden = true;
        erro.hidden = false;
    }
}


function renderizarUsuarios(lista) {

    tabelaBody.innerHTML = "";

    lista.forEach(usuario => {

        const linha = document.createElement("tr");

        linha.innerHTML = `
            <td>${escapeHtml(usuario.nome)}</td>

            <td>${escapeHtml(usuario.emailPrincipal)}</td>

            <td>${formatarAvaliacao(usuario.avaliacao)}</td>

            <td>${escapeHtml(usuario.role)}</td>

            <td>
                <span class="
                    admin-status
                    ${usuario.status === "ATIVO"
            ? "admin-status-ativo"
            : "admin-status-inativo"}
                ">
                    ${escapeHtml(usuario.status)}
                </span>
            </td>

            <td>
                ${criarAcoesUsuario(usuario)}
            </td>
        `;

        tabelaBody.appendChild(linha);
    });
}


function criarAcoesUsuario(usuario) {

    if (usuario.role === "ADMIN") {
        return "<span>—</span>";
    }

    const botaoStatus = usuario.status === "ATIVO"
        ? `
            <button
                type="button"
                class="admin-btn admin-btn-desativar"
                data-acao="desativar"
                data-uid="${usuario.id}">
                Desativar
            </button>
          `
        : `
            <button
                type="button"
                class="admin-btn admin-btn-ativar"
                data-acao="ativar"
                data-uid="${usuario.id}">
                Ativar
            </button>
          `;

    return `
        <div class="admin-acoes-usuario">

            <button
                type="button"
                class="admin-btn admin-btn-alterar"
                data-acao="alterar"
                data-uid="${usuario.id}">
                Alterar
            </button>

            ${botaoStatus}

        </div>
    `;
}

tabelaBody.addEventListener("click", event => {

    const botao =
        event.target.closest("[data-acao]");

    if (!botao) {
        return;
    }

    const uid = botao.dataset.uid;
    const acao = botao.dataset.acao;

    const usuario =
        usuarios.find(usuario =>
            usuario.id === uid
        );

    if (!usuario) {
        return;
    }

    usuarioSelecionado = usuario;

    if (acao === "alterar") {
        abrirModalAlterar(usuario);
        return;
    }

    if (
        acao === "desativar" ||
        acao === "ativar"
    ) {
        abrirModalStatus(usuario);
    }
});

salvarAlteracao.addEventListener(
    "click",
    async () => {

        if (!usuarioSelecionado) {
            return;
        }

        const avaliacao =
            obterAvaliacaoModal();

        if (
            Number.isNaN(avaliacao) ||
            avaliacao < 0 ||
            avaliacao > 5
        ) {
            mostrarErroAlteracao(
                "A avaliação deve estar entre 0 e 5."
            );

            return;
        }

        setAlteracaoCarregando(true);

        try {

            const response = await adminFetch(
                `/api/admin/usuarios/${usuarioSelecionado.id}`,
                {
                    method: "PATCH",

                    body: JSON.stringify({
                        status:
                        usuarioSelecionado.status,

                        avaliacao:
                        avaliacao
                    })
                }
            );

            const usuarioAtualizado =
                await response.json();

            atualizarUsuarioLocal(
                usuarioAtualizado
            );

            fecharModal(modalAlterar);

        } catch (error) {

            console.error(
                "Erro ao alterar usuário:",
                error
            );

            mostrarErroAlteracao(
                "Não foi possível alterar o usuário."
            );

        } finally {

            setAlteracaoCarregando(false);
        }
    }
);

confirmarStatus.addEventListener(
    "click",
    async () => {

        if (!usuarioSelecionado) {
            return;
        }

        const statusAtual =
            usuarioSelecionado.status;

        const novoStatus =
            statusAtual === "ATIVO"
                ? "INATIVO"
                : "ATIVO";

        setStatusCarregando(
            true,
            statusAtual
        );

        try {

            const response = await adminFetch(
                `/api/admin/usuarios/${usuarioSelecionado.id}`,
                {
                    method: "PATCH",

                    body: JSON.stringify({
                        status: novoStatus,

                        avaliacao:
                        usuarioSelecionado.avaliacao
                    })
                }
            );

            const usuarioAtualizado =
                await response.json();

            atualizarUsuarioLocal(
                usuarioAtualizado
            );

            fecharModal(modalStatus);

        } catch (error) {

            console.error(
                "Erro ao alterar status do usuário:",
                error
            );

            mostrarErroStatus(
                "Não foi possível alterar o status do usuário."
            );

        } finally {

            setStatusCarregando(
                false,
                statusAtual
            );
        }
    }
);

function atualizarUsuarioLocal(
    usuarioAtualizado
) {

    const indice =
        usuarios.findIndex(
            usuario =>
                usuario.id ===
                usuarioAtualizado.id
        );

    if (indice === -1) {
        return;
    }

    usuarios[indice] =
        usuarioAtualizado;

    aplicarFiltroAtual();
}

function aplicarFiltroAtual() {

    const termo =
        pesquisa.value
            .trim()
            .toLowerCase();

    const usuariosFiltrados =
        usuarios.filter(usuario => {

            const nome =
                usuario.nome
                    ?.toLowerCase() || "";

            const email =
                usuario.emailPrincipal
                    ?.toLowerCase() || "";

            return nome.includes(termo) ||
                email.includes(termo);
        });

    renderizarUsuarios(
        usuariosFiltrados
    );
}

function formatarAvaliacao(avaliacao) {

    if (avaliacao === null || avaliacao === undefined) {
        return "0.0";
    }

    return Number(avaliacao).toFixed(1);
}


function escapeHtml(valor) {

    if (valor === null || valor === undefined) {
        return "";
    }

    const elemento = document.createElement("div");

    elemento.textContent = String(valor);

    return elemento.innerHTML;
}


pesquisa.addEventListener(
    "input",
    aplicarFiltroAtual
);


carregarUsuarios();