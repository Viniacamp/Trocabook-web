function obterHeadersCSRF() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = {
        "Content-Type": "application/json"
    };

    if (token && header) {
        headers[header] = token;
    }

    return headers;
}

export async function buscarAnalyticsAnuncios() {
    const response = await fetch("/api/admin/analytics/anuncios", {
        method: "POST",
        headers: obterHeadersCSRF()
    });

    if (!response.ok) {
        throw new Error(`Erro ao buscar analytics de anúncios: ${response.status}`);
    }

    const texto = await response.text();
    return texto ? JSON.parse(texto) : null;
}

export async function buscarAnalyticsNegociacoes() {
    const response = await fetch("/api/admin/analytics/negociacoes", {
        method: "POST",
        headers: obterHeadersCSRF()
    });

    if (!response.ok) {
        throw new Error(`Erro ao buscar analytics de negociações: ${response.status}`);
    }

    const texto = await response.text();
    return texto ? JSON.parse(texto) : null;
}

export async function buscarAnalyticsPrevisao() {
    const response = await fetch("/api/admin/analytics/previsao-temporal", {
        method: "POST",
        headers: obterHeadersCSRF()
    });

    if (!response.ok) {
        throw new Error(`Erro ao buscar previsão temporal de usuários: ${response.status}`);
    }

    const texto = await response.text();
    return texto ? JSON.parse(texto) : null;
}