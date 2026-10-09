/**
 * Chamadas HTTP/API relacionadas ao Chat.
 */

export async function carregarDadosConversa(uidAnuncio, uidDestinatario) {
    const resposta = await fetch(
        `/chat/conversar/dados` +
        `?anuncio=${encodeURIComponent(uidAnuncio)}` +
        `&destinatario=${encodeURIComponent(uidDestinatario)}`
    );

    if (!resposta.ok) {
        throw new Error('Erro ao carregar conversa.');
    }

    const data = await resposta.json();
    if (data.status !== 'success') {
        throw new Error(data.message || 'Não foi possível carregar a conversa.');
    }

    return data.data;
}

export async function enviarMensagemApi(mensagem) {
    const resposta = await fetch('/chat/mensagens', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(mensagem)
    });

    if (!resposta.ok) {
        throw new Error('Erro ao enviar mensagem.');
    }

    const data = await resposta.json();
    return data;
}

export async function atualizarConversasApi() {
    const resposta = await fetch('/chat/conversas/atualizar');
    if (!resposta.ok) {
        return null;
    }
    const data = await resposta.json();
    if (data.status !== 'success') {
        return null;
    }
    return data.data || [];
}

export async function atualizarMensagensApi(uidAnuncio, uidDestinatario) {
    const resposta = await fetch(
        `/chat/conversa/atualizar` +
        `?anuncio=${encodeURIComponent(uidAnuncio)}` +
        `&destinatario=${encodeURIComponent(uidDestinatario)}`
    );

    if (!resposta.ok) {
        return null;
    }

    const data = await resposta.json();
    if (data.status !== 'success') {
        return null;
    }
    return data.data;
}

export async function editarMensagemApi(idMensagem, novoConteudo) {
    const resposta = await fetch(`/chat/mensagens/${encodeURIComponent(idMensagem)}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            conteudo: novoConteudo
        })
    });

    const data = await resposta.json();
    return { ok: resposta.ok, data };
}

export async function excluirMensagemApi(idMensagem) {
    const resposta = await fetch(`/chat/mensagens/${encodeURIComponent(idMensagem)}`, {
        method: 'DELETE'
    });

    const data = await resposta.json();
    return { ok: resposta.ok, data };
}

export async function criarNegociacaoApi(anuncioId, tipoNegociacao, anuncioOferecidoId, descricaoOferta) {
    const resposta = await fetch('/chat/negociacoes', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            anuncioId: anuncioId,
            tipoNegociacao: tipoNegociacao,
            anuncioOferecidoId: anuncioOferecidoId,
            descricaoOferta: descricaoOferta
        })
    });

    const data = await resposta.json();
    if (!resposta.ok || data.status !== 'success') {
        throw new Error(data.message || 'Erro ao criar negociação.');
    }
    return data.data;
}

export async function aceitarNegociacaoApi(idNegociacao) {
    const resposta = await fetch(
        `/chat/negociacoes/${encodeURIComponent(idNegociacao)}/aceitar`,
        {
            method: 'PUT'
        }
    );

    const data = await resposta.json();
    if (!resposta.ok || data.status !== 'success') {
        throw new Error(data.message || 'Não foi possível aceitar a proposta.');
    }
    return data.data;
}

export async function recusarNegociacaoApi(idNegociacao) {
    const resposta = await fetch(
        `/chat/negociacoes/${encodeURIComponent(idNegociacao)}/recusar`,
        {
            method: 'PUT'
        }
    );

    const data = await resposta.json();
    if (!resposta.ok || data.status !== 'success') {
        throw new Error(data.message || 'Não foi possível recusar a proposta.');
    }
    return data.data;
}

export async function listarAnunciosOfereciveisApi() {
    const resposta = await fetch('/chat/anuncios-ofereciveis');
    const data = await resposta.json();
    if (!resposta.ok || data.status !== 'success') {
        throw new Error(data.message || 'Não foi possível carregar seus anúncios.');
    }
    return data.data || [];
}
