import {
    buscarAnalyticsAnuncios,
    buscarAnalyticsNegociacoes,
    buscarAnalyticsPrevisao
} from "./analyticsApi.js";

import {
    renderizarAbaAnuncios,
    renderizarAbaNegociacoes,
    renderizarAbaPrevisao
} from "./graficos.js";

// Elementos da Interface
const carregando = document.getElementById("dashboardCarregando");
const erro = document.getElementById("dashboardErro");
const botoesAbas = document.querySelectorAll("[data-aba]");

const secoesAbas = {
    anuncios: document.getElementById("abaAnuncios"),
    negociacoes: document.getElementById("abaNegociacoes"),
    previsao: document.getElementById("abaPrevisao")
};

// Cache local para evitar chamadas desnecessárias à API ao alternar abas já carregadas
const cacheDadosAnalytics = {
    anuncios: null,
    negociacoes: null,
    previsao: null
};

document.addEventListener("DOMContentLoaded", () => {
    configurarNavegacaoAbas();
    carregarConteudoAba("anuncios");
});

function configurarNavegacaoAbas() {
    botoesAbas.forEach(botao => {
        botao.addEventListener("click", () => {
            const abaSelecionada = botao.dataset.aba;

            // Atualiza classe ativa dos botões
            botoesAbas.forEach(b => b.classList.remove("admin-tab-ativa"));
            botao.classList.add("admin-tab-ativa");

            // Exibe/oculta as seções de cada aba
            Object.keys(secoesAbas).forEach(chave => {
                if (secoesAbas[chave]) {
                    secoesAbas[chave].hidden = (chave !== abaSelecionada);
                }
            });

            carregarConteudoAba(abaSelecionada);
        });
    });
}

async function carregarConteudoAba(nomeAba) {
    ocultarMensagemErro();

    // Utiliza dados do cache se já tiverem sido carregados
    if (cacheDadosAnalytics[nomeAba]) {
        return;
    }

    exibirCarregando(true);

    try {
        let dados = null;

        if (nomeAba === "anuncios") {
            dados = await buscarAnalyticsAnuncios();
            cacheDadosAnalytics.anuncios = dados;
            renderizarAbaAnuncios(dados);
        } else if (nomeAba === "negociacoes") {
            dados = await buscarAnalyticsNegociacoes();
            cacheDadosAnalytics.negociacoes = dados;
            renderizarAbaNegociacoes(dados);
        } else if (nomeAba === "previsao") {
            dados = await buscarAnalyticsPrevisao();
            cacheDadosAnalytics.previsao = dados;
            renderizarAbaPrevisao(dados);
        }

        exibirCarregando(false);

    } catch (error) {
        console.error(`Erro ao carregar analytics da aba '${nomeAba}':`, error);
        exibirCarregando(false);
        exibirMensagemErro();
    }
}

function exibirCarregando(status) {
    if (carregando) {
        carregando.hidden = !status;
    }
}

function exibirMensagemErro() {
    if (erro) {
        erro.hidden = false;
    }
}

function ocultarMensagemErro() {
    if (erro) {
        erro.hidden = true;
    }
}