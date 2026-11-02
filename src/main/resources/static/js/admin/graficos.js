// Armazena as instâncias ativas do Chart.js para permitir destruição limpa ao atualizar
const instanciasGraficos = {};

/**
 * Renderiza os KPIs, gráficos e insights da aba de Anúncios.
 */
export function renderizarAbaAnuncios(dados) {
    const analise = dados?.analise || {};
    const geral = analise.geral || {};

    const porTipo = analise.porTipo || [];
    const porCategoria = analise.porCategoria || [];

    // 1. Destaques para os Cards de KPI
    const modalidadeTop = porTipo[0]?.tipoNegociacao || "-";
    const categoriaTop = porCategoria[0]?.categoria || "-";

    atualizarTextoElemento("kpiTotalAnuncios", geral.totalAnuncios ?? 0);
    atualizarTextoElemento("kpiModalidadeTop", modalidadeTop);
    atualizarTextoElemento("kpiCategoriaTop", categoriaTop);

    // 2. Gráfico 1: Evolução Mensal de Anúncios (Linha)
    const temporal = analise.temporal || {};
    const seriesMensal = temporal.anunciosPorMes || [];
    renderizarGraficoChart("chartEvolucaoAnuncios", "line", {
        labels: seriesMensal.map(item => item.mesCriacao || item.mes_criacao),
        datasets: [{
            label: "Novos Anúncios",
            data: seriesMensal.map(item => item.novosAnuncios ?? item.novos_anuncios ?? 0),
            borderColor: "#2ecc71",
            backgroundColor: "rgba(46, 204, 113, 0.1)",
            fill: true,
            tension: 0.3
        }]
    });

    // 3. Gráfico 2: Distribuição por Tipo de Negociação (Rosca) - CORRIGIDO AQUI
    renderizarGraficoChart("chartTiposAnuncios", "doughnut", {
        labels: porTipo.map(item => item.tipoNegociacao || item.tipo_negociacao),
        datasets: [{
            data: porTipo.map(item => item.quantidadeAnuncios ?? item.quantidade_anuncios ?? item.quantidade ?? 0),
            backgroundColor: ["#3498db", "#e74c3c", "#f1c40f"]
        }]
    });

    // 4. Lista de Insights
    renderizarListaInsights("listaInsightsAnuncios", analise.insights || []);
}

/**
 * Renderiza os KPIs, gráficos e insights da aba de Negociações.
 */
export function renderizarAbaNegociacoes(dados) {
    const geral = dados?.geral || {};

    // 1. KPI: Total de Negociações
    const totalNegociacoes = geral.totalNegociacoes ?? geral.total_negociacoes ?? 0;
    atualizarTextoElemento("kpiTotalNegociacoes", totalNegociacoes);

    // 2. KPI: Tempo Médio de Finalização
    const tempoObj = dados.temporal?.tempoFinalizacao?.[0] || dados.temporal?.tempo_finalizacao?.[0];
    const tempoMedio = tempoObj?.tempoMedioDias ?? tempoObj?.tempo_medio_dias;
    atualizarTextoElemento(
        "kpiTempoMedio",
        tempoMedio !== undefined && tempoMedio !== null ? `${Number(tempoMedio).toFixed(1)} dias` : "0 dias"
    );

    // 3. KPI: Taxa de Conclusão (Cálculo via quantidade / total)
    const listaStatus = geral.negociacoesPorStatus || geral.negociacoes_por_status || [];
    const statusFinalizada = listaStatus.find(s => s.status === "FINALIZADA");
    const qtdFinalizadas = statusFinalizada ? (statusFinalizada.quantidade ?? 0) : 0;

    const taxaPercentual = totalNegociacoes > 0 ? ((qtdFinalizadas / totalNegociacoes) * 100) : 0;
    atualizarTextoElemento("kpiTaxaConclusao", `${taxaPercentual.toFixed(1)}%`);

    // 4. Gráfico 1: Histórico Mensal de Negociações
    const negociacoesMes = dados.temporal?.negociacoesPorMes || dados.temporal?.negociacoes_por_mes || [];
    renderizarGraficoChart("chartHistoricoNegociacoes", "bar", {
        labels: negociacoesMes.map(item => item.mesCriacao || item.mesNegociacao || item.mes_negociacao || item.mes || "N/A"),
        datasets: [{
            label: "Negociações Criadas",
            data: negociacoesMes.map(item => item.quantidade ?? item.total ?? 0),
            backgroundColor: "#34495e"
        }]
    });

    // 5. Gráfico 2: Afinidades / Combinações de Categorias
    const afinidadesRaw = dados.afinidadeCategorias || dados.afinidade_categorias || [];
    const combinacoesRaw = dados.combinacoesCategorias || dados.combinacoes_categorias || [];

    let labels = [];
    let valores = [];

    if (afinidadesRaw.length > 0) {
        labels = afinidadesRaw.map(item => item.afinidade || "N/A");
        valores = afinidadesRaw.map(item => item.totalNegociacoes ?? item.quantidade ?? 0);
    } else if (combinacoesRaw.length > 0) {
        labels = combinacoesRaw.slice(0, 5).map(item => {
            const orig = item.categoriaAnuncio || item.categoria_anuncio || "N/A";
            const dest = item.categoriaAnuncioOferecido || item.categoria_anuncio_oferecido || "N/A";
            return `${orig} ➔ ${dest}`;
        });
        valores = combinacoesRaw.slice(0, 5).map(item => item.totalNegociacoes ?? item.quantidade ?? 0);
    }

    renderizarGraficoChart("chartAfinidadeCategorias", "bar", {
        labels: labels,
        datasets: [{
            label: "Total de Trocas",
            data: valores,
            backgroundColor: "#9b59b6"
        }]
    }, { indexAxis: "y" });

    // 6. Lista de Insights
    renderizarListaInsights("listaInsightsNegociacoes", dados.insights || []);
}

/**
 * Renderiza os KPIs, gráficos e tabela da aba de Previsão de Usuários.
 */
export function renderizarAbaPrevisao(dados) {
    if (!dados) {
        atualizarTextoElemento("kpiMaeUsuarios", "N/A");
        atualizarTextoElemento("kpiRmseUsuarios", "N/A");
        atualizarTextoElemento("kpiProjecaoProximoMes", "N/A");
        return;
    }

    const avaliacao = dados.avaliacao || {};
    atualizarTextoElemento("kpiMaeUsuarios", avaliacao.mae !== undefined && avaliacao.mae !== null ? Number(avaliacao.mae).toFixed(2) : "-");
    atualizarTextoElemento("kpiRmseUsuarios", avaliacao.rmse !== undefined && avaliacao.rmse !== null ? Number(avaliacao.rmse).toFixed(2) : "-");

    const projecoes = dados.projecoes || [];
    const proximoMes = projecoes[0]?.quantidadeEstimada ?? projecoes[0]?.quantidade_estimada ?? projecoes[0]?.quantidade ?? "-";
    atualizarTextoElemento("kpiProjecaoProximoMes", proximoMes);

    // Gráfico de Linha de Projeção
    renderizarGraficoChart("chartPrevisaoUsuarios", "line", {
        labels: projecoes.map(item => item.mes || item.mesCriacao || item.mes_criacao || "N/A"),
        datasets: [{
            label: "Projeção de Novos Usuários",
            data: projecoes.map(item => item.quantidadeEstimada ?? item.quantidade_estimada ?? item.quantidade ?? 0),
            borderColor: "#e67e22",
            borderDash: [5, 5],
            backgroundColor: "rgba(230, 126, 34, 0.1)",
            fill: true
        }]
    });

    // Tabela de Horizontes - CORRIGIDA AQUI
    const tabelaBody = document.getElementById("tabelaResumoHorizontesBody");
    if (tabelaBody) {
        tabelaBody.innerHTML = "";
        (dados.resumoHorizontes || dados.resumo_horizontes || []).forEach(item => {
            const horizonte = item.horizonteMeses ?? item.horizonte_meses ?? item.horizonte ?? "-";
            const total = item.totalAcumulado ?? item.total_acumulado ?? item.quantidadeEstimada ?? item.quantidade_estimada ?? "-";
            const mediaObj = item.mediaMensal ?? item.media_mensal;
            const media = mediaObj !== undefined && mediaObj !== null ? Number(mediaObj).toFixed(1) : "-";

            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${horizonte} meses</td>
                <td>${total}</td>
                <td>${media}</td>
            `;
            tabelaBody.appendChild(tr);
        });
    }
}

// =========================================================================
// FUNÇÕES AUXILIARES INTERNAS DE INTERFACE
// =========================================================================

function renderizarGraficoChart(canvasId, tipo, data, options = {}) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;

    if (instanciasGraficos[canvasId]) {
        instanciasGraficos[canvasId].destroy();
    }

    const ctx = canvas.getContext("2d");
    instanciasGraficos[canvasId] = new Chart(ctx, {
        type: tipo,
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { position: "bottom" }
            },
            ...options
        }
    });
}

function renderizarListaInsights(elementId, insights) {
    const ul = document.getElementById(elementId);
    if (!ul) return;

    ul.innerHTML = "";

    if (!insights || insights.length === 0) {
        ul.innerHTML = "<li>Nenhum insight relevante identificado para este período.</li>";
        return;
    }

    insights.forEach(item => {
        const li = document.createElement("li");
        li.textContent = typeof item === "string" ? item : (item.mensagem || JSON.stringify(item));
        ul.appendChild(li);
    });
}

function atualizarTextoElemento(id, texto) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = texto;
    }
}