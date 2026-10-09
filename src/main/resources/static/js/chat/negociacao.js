/**
 * Módulo de negociações (proposta, troca, venda, aceite, recusa e modais).
 */

import { elements } from './ui.js';
import {
    getUidUsuarioLogado,
    getConversaAtual,
    setConversaAtualNegociacao,
    getAnuncioOferecidoSelecionado,
    setAnuncioOferecidoSelecionado
} from './state.js';
import { formatarTipoNegociacao } from './utils.js';
import {
    criarNegociacaoApi,
    aceitarNegociacaoApi,
    recusarNegociacaoApi,
    listarAnunciosOfereciveisApi
} from './api.js';

export function renderizarNegociacao(anuncio, negociacao) {
    if (!elements.negociacaoConteudo) return;

    elements.negociacaoConteudo.innerHTML = '';

    if (!anuncio) {
        return;
    }

    const souAnunciante = anuncio.uidUsuario === getUidUsuarioLogado();

    if (!negociacao) {
        if (souAnunciante) {
            elements.negociacaoConteudo.innerHTML = `
                <div class="negociacao-status">
                    <i class="fa-solid fa-clock"></i>
                    <span>Aguardando proposta de negociação.</span>
                </div>
            `;
            return;
        }

        renderizarBotaoProporNegociacao(anuncio);
        return;
    }

    switch (negociacao.status) {
        case 'PENDENTE':
            if (souAnunciante) {
                const statusContainer = document.createElement('div');
                statusContainer.className = 'negociacao-status';

                const icone = document.createElement('i');
                icone.className = 'fa-solid fa-handshake';

                const titulo = document.createElement('strong');
                titulo.textContent = 'Proposta recebida';

                const tipo = document.createElement('span');
                tipo.textContent = `Tipo: ${formatarTipoNegociacao(negociacao.tipoNegociacao)}`;

                statusContainer.appendChild(icone);
                statusContainer.appendChild(titulo);
                statusContainer.appendChild(tipo);

                if (negociacao.tipoNegociacao === 'TROCA') {
                    const detalhesOferta = criarDetalhesOferta(negociacao);
                    statusContainer.appendChild(detalhesOferta);
                }

                const acoes = document.createElement('div');
                acoes.className = 'negociacao-acoes-resposta';

                const botaoAceitar = document.createElement('button');
                botaoAceitar.type = 'button';
                botaoAceitar.className = 'btn-negociacao btn-aceitar';
                botaoAceitar.innerHTML = '<i class="fa-solid fa-check"></i> Aceitar';
                botaoAceitar.addEventListener('click', () => {
                    aceitarNegociacao(negociacao.id);
                });

                const botaoRecusar = document.createElement('button');
                botaoRecusar.type = 'button';
                botaoRecusar.className = 'btn-negociacao btn-recusar';
                botaoRecusar.innerHTML = '<i class="fa-solid fa-xmark"></i> Recusar';
                botaoRecusar.addEventListener('click', () => {
                    recusarNegociacao(negociacao.id);
                });

                acoes.appendChild(botaoAceitar);
                acoes.appendChild(botaoRecusar);
                statusContainer.appendChild(acoes);

                elements.negociacaoConteudo.appendChild(statusContainer);
            } else {
                elements.negociacaoConteudo.innerHTML = `
                    <div class="negociacao-status">
                        <i class="fa-solid fa-clock"></i>
                        <strong>Proposta enviada</strong>
                        <span>Tipo: ${formatarTipoNegociacao(negociacao.tipoNegociacao)}</span>
                        <span>Aguardando resposta do anunciante.</span>
                    </div>
                `;
            }
            break;

        case 'EM_ANDAMENTO':
            elements.negociacaoConteudo.innerHTML = `
                <div class="negociacao-status">
                    <i class="fa-solid fa-handshake"></i>
                    <strong>Negociação em andamento</strong>
                    <span>Tipo: ${formatarTipoNegociacao(negociacao.tipoNegociacao)}</span>
                    <a href="/negociacoes/${encodeURIComponent(negociacao.id)}" class="btn-ver-negociacao">
                        <i class="fa-solid fa-arrow-up-right-from-square"></i>
                        Ver negociação
                    </a>
                </div>
            `;
            break;

        case 'FINALIZADA':
            elements.negociacaoConteudo.innerHTML = `
                <div class="negociacao-status">
                    <i class="fa-solid fa-circle-check"></i>
                    <strong>Negociação finalizada</strong>
                    <a href="/negociacoes/${encodeURIComponent(negociacao.id)}" class="btn-ver-negociacao">
                        <i class="fa-solid fa-receipt"></i>
                        Ver detalhes
                    </a>
                </div>
            `;
            break;

        case 'RECUSADA':
        case 'CANCELADA':
            if (souAnunciante) {
                elements.negociacaoConteudo.innerHTML = `
                    <div class="negociacao-status">
                        <span>Negociação encerrada.</span>
                    </div>
                `;
            } else {
                renderizarBotaoProporNegociacao(anuncio);
            }
            break;
    }
}

export function renderizarBotaoProporNegociacao(anuncio) {
    if (!elements.negociacaoConteudo) return;

    if (anuncio.tipoNegociacao === 'AMBOS') {
        elements.negociacaoConteudo.innerHTML = `
            <div class="negociacao-acoes">
                <strong>Como deseja negociar?</strong>
                <button type="button" class="btn-negociacao" id="btnProporTroca">
                    <i class="fa-solid fa-arrows-rotate"></i>
                    Propor troca
                </button>
                <button type="button" class="btn-negociacao" id="btnProporVenda">
                    <i class="fa-solid fa-tag"></i>
                    Comprar
                </button>
            </div>
        `;

        document.getElementById('btnProporTroca')?.addEventListener('click', () => proporNegociacao('TROCA'));
        document.getElementById('btnProporVenda')?.addEventListener('click', () => proporNegociacao('VENDA'));
        return;
    }

    const texto = anuncio.tipoNegociacao === 'TROCA' ? 'Propor troca' : 'Comprar';

    elements.negociacaoConteudo.innerHTML = `
        <div class="negociacao-acoes">
            <button type="button" class="btn-negociacao" id="btnProporUnico">
                <i class="fa-solid fa-handshake"></i>
                ${texto}
            </button>
        </div>
    `;

    document.getElementById('btnProporUnico')?.addEventListener('click', () => proporNegociacao(anuncio.tipoNegociacao));
}

export function criarDetalhesOferta(negociacao) {
    const container = document.createElement('div');
    container.className = 'negociacao-oferta';

    if (negociacao.tipoNegociacao !== 'TROCA') {
        return container;
    }

    const tituloSecao = document.createElement('strong');
    tituloSecao.className = 'negociacao-oferta-titulo';
    tituloSecao.textContent = 'Livro oferecido';
    container.appendChild(tituloSecao);

    if (negociacao.anuncioOferecidoId) {
        const livro = document.createElement('div');
        livro.className = 'negociacao-oferta-livro';

        const imagem = document.createElement('img');
        imagem.className = 'negociacao-oferta-capa';
        imagem.src = negociacao.capaLivroOferecido || '/img/capa-padrao.png';
        imagem.alt = negociacao.tituloLivroOferecido
            ? `Capa de ${negociacao.tituloLivroOferecido}`
            : 'Capa do livro oferecido';

        const informacoes = document.createElement('div');
        informacoes.className = 'negociacao-oferta-info';

        const titulo = document.createElement('strong');
        titulo.textContent = negociacao.tituloLivroOferecido || 'Livro oferecido';

        const identificacao = document.createElement('span');
        identificacao.className = 'negociacao-oferta-anunciado';
        identificacao.textContent = 'Anunciado no Trocabook';

        informacoes.appendChild(titulo);
        informacoes.appendChild(identificacao);

        livro.appendChild(imagem);
        livro.appendChild(informacoes);
        container.appendChild(livro);
    } else if (negociacao.descricaoOferta) {
        const descricaoLivro = document.createElement('p');
        descricaoLivro.className = 'negociacao-oferta-descricao';
        descricaoLivro.textContent = negociacao.descricaoOferta;
        container.appendChild(descricaoLivro);
        return container;
    }

    if (negociacao.descricaoOferta) {
        const observacaoContainer = document.createElement('div');
        observacaoContainer.className = 'negociacao-oferta-observacao';

        const label = document.createElement('span');
        label.textContent = 'Observação:';

        const observacao = document.createElement('p');
        observacao.textContent = negociacao.descricaoOferta;

        observacaoContainer.appendChild(label);
        observacaoContainer.appendChild(observacao);
        container.appendChild(observacaoContainer);
    }

    return container;
}

export async function proporNegociacao(tipoNegociacao) {
    const conversaAtual = getConversaAtual();
    if (!conversaAtual.uidAnuncio) {
        return;
    }

    if (tipoNegociacao === 'TROCA') {
        setAnuncioOferecidoSelecionado(null);
        abrirModalTipoOferta();
        return;
    }

    const confirmar = confirm('Deseja enviar uma proposta de compra?');
    if (!confirmar) {
        return;
    }

    await enviarPropostaNegociacao(tipoNegociacao, null, null);
}

export async function enviarPropostaNegociacao(tipoNegociacao, anuncioOferecidoId, descricaoOferta) {
    const conversaAtual = getConversaAtual();
    try {
        const negociacao = await criarNegociacaoApi(
            conversaAtual.uidAnuncio,
            tipoNegociacao,
            anuncioOferecidoId,
            descricaoOferta
        );

        setConversaAtualNegociacao(negociacao);
        fecharModalTroca();
        renderizarNegociacao(conversaAtual.anuncio, negociacao);
    } catch (erro) {
        console.error('Erro ao criar negociação:', erro);
        alert(erro.message || 'Não foi possível enviar a proposta.');
    }
}

export async function aceitarNegociacao(idNegociacao) {
    const confirmar = confirm('Deseja aceitar esta proposta?');
    if (!confirmar) {
        return;
    }

    const conversaAtual = getConversaAtual();
    try {
        const negociacao = await aceitarNegociacaoApi(idNegociacao);
        setConversaAtualNegociacao(negociacao);
        renderizarNegociacao(conversaAtual.anuncio, negociacao);
    } catch (erro) {
        console.error('Erro ao aceitar negociação:', erro);
        alert('Não foi possível aceitar a proposta.');
    }
}

export async function recusarNegociacao(idNegociacao) {
    const confirmar = confirm('Deseja recusar esta proposta?');
    if (!confirmar) {
        return;
    }

    const conversaAtual = getConversaAtual();
    try {
        const negociacao = await recusarNegociacaoApi(idNegociacao);
        setConversaAtualNegociacao(negociacao);
        renderizarNegociacao(conversaAtual.anuncio, negociacao);
    } catch (erro) {
        console.error('Erro ao recusar negociação:', erro);
        alert('Não foi possível recusar a proposta.');
    }
}

export function abrirModalTipoOferta() {
    if (elements.modalTipoOferta) {
        elements.modalTipoOferta.style.display = 'flex';
    }
}

export function fecharModalTroca() {
    document.querySelectorAll('.modal-troca').forEach(modal => {
        modal.style.display = 'none';
    });

    setAnuncioOferecidoSelecionado(null);

    if (elements.descricaoOfertaAnunciada) {
        elements.descricaoOfertaAnunciada.value = '';
    }

    if (elements.descricaoOfertaManual) {
        elements.descricaoOfertaManual.value = '';
    }

    if (elements.contadorDescricaoOferta) {
        elements.contadorDescricaoOferta.textContent = '0 / 500';
    }
}

export function voltarModalTipoOferta() {
    document.querySelectorAll('.modal-troca').forEach(modal => {
        modal.style.display = 'none';
    });

    if (elements.modalTipoOferta) {
        elements.modalTipoOferta.style.display = 'flex';
    }
}

export async function selecionarLivroAnunciado() {
    if (elements.modalTipoOferta) elements.modalTipoOferta.style.display = 'none';
    if (elements.modalSelecionarLivro) elements.modalSelecionarLivro.style.display = 'flex';

    if (elements.listaLivrosOferta) {
        elements.listaLivrosOferta.innerHTML = `
            <div class="carregando-oferta">
                <i class="fa-solid fa-spinner fa-spin"></i>
                Carregando seus livros...
            </div>
        `;
    }

    try {
        const anuncios = await listarAnunciosOfereciveisApi();
        renderizarLivrosOferta(anuncios);
    } catch (erro) {
        console.error('Erro ao carregar anúncios:', erro);
        if (elements.listaLivrosOferta) {
            elements.listaLivrosOferta.innerHTML = `
                <div class="oferta-vazia">
                    Não foi possível carregar seus livros.
                </div>
            `;
        }
    }
}

export function selecionarLivroNaoAnunciado() {
    if (elements.modalTipoOferta) elements.modalTipoOferta.style.display = 'none';
    if (elements.modalSelecionarLivro) elements.modalSelecionarLivro.style.display = 'none';
    if (elements.modalDescricaoOferta) elements.modalDescricaoOferta.style.display = 'flex';

    if (elements.descricaoOfertaManual) {
        elements.descricaoOfertaManual.focus();
    }
}

export async function confirmarLivroAnunciado() {
    const anuncioId = getAnuncioOferecidoSelecionado();
    if (!anuncioId) {
        alert('Selecione o livro que deseja oferecer.');
        return;
    }

    const descricao = elements.descricaoOfertaAnunciada
        ? elements.descricaoOfertaAnunciada.value.trim()
        : '';

    await enviarPropostaNegociacao(
        'TROCA',
        anuncioId,
        descricao || null
    );
}

export async function confirmarLivroNaoAnunciado() {
    const descricao = elements.descricaoOfertaManual
        ? elements.descricaoOfertaManual.value.trim()
        : '';

    if (!descricao) {
        alert('Descreva o livro que deseja oferecer.');
        return;
    }

    await enviarPropostaNegociacao(
        'TROCA',
        null,
        descricao
    );
}

export function renderizarLivrosOferta(anuncios) {
    if (!elements.listaLivrosOferta) return;

    elements.listaLivrosOferta.innerHTML = '';

    if (anuncios.length === 0) {
        elements.listaLivrosOferta.innerHTML = `
            <div class="oferta-vazia">
                <i class="fa-solid fa-book-open"></i>
                <p>
                    Você não possui anúncios ativos
                    disponíveis para troca.
                </p>
                <button
                    type="button"
                    class="btn-negociacao"
                    id="btnOfertaVaziaNaoAnunciado">
                    Descrever outro livro
                </button>
            </div>
        `;

        document.getElementById('btnOfertaVaziaNaoAnunciado')?.addEventListener('click', selecionarLivroNaoAnunciado);
        return;
    }

    anuncios.forEach(anuncio => {
        const card = document.createElement('button');
        card.type = 'button';
        card.className = 'livro-oferta-card';
        card.dataset.id = anuncio.id;

        const imagem = document.createElement('img');
        imagem.src = anuncio.capa || '/img/capa-padrao.png';
        imagem.alt = `Capa de ${anuncio.titulo || 'livro'}`;

        const info = document.createElement('div');
        info.className = 'livro-oferta-info';

        const titulo = document.createElement('strong');
        titulo.textContent = anuncio.titulo || 'Livro';

        const tipo = document.createElement('span');
        tipo.textContent = formatarTipoNegociacao(anuncio.tipoNegociacao);

        info.appendChild(titulo);
        info.appendChild(tipo);

        card.appendChild(imagem);
        card.appendChild(info);

        card.addEventListener('click', () => {
            document.querySelectorAll('.livro-oferta-card').forEach(item => {
                item.classList.remove('selecionado');
            });

            card.classList.add('selecionado');
            setAnuncioOferecidoSelecionado(anuncio.id);
        });

        elements.listaLivrosOferta.appendChild(card);
    });
}

export function configurarModaisNegociacao() {
    // Contador de caracteres para oferta manual
    if (elements.descricaoOfertaManual && elements.contadorDescricaoOferta) {
        elements.descricaoOfertaManual.addEventListener('input', () => {
            elements.contadorDescricaoOferta.textContent =
                `${elements.descricaoOfertaManual.value.length} / 500`;
        });
    }

    // Botões de fechar dos modais
    document.querySelectorAll('.modal-troca-fechar').forEach(btn => {
        btn.addEventListener('click', fecharModalTroca);
    });

    // Botões do modal tipo de oferta
    document.getElementById('btnSelecionarLivroAnunciado')?.addEventListener('click', selecionarLivroAnunciado);
    document.getElementById('btnSelecionarLivroNaoAnunciado')?.addEventListener('click', selecionarLivroNaoAnunciado);

    // Botões de voltar
    document.getElementById('btnVoltarModalSelecionar')?.addEventListener('click', voltarModalTipoOferta);
    document.getElementById('btnVoltarModalDescricao')?.addEventListener('click', voltarModalTipoOferta);

    // Botões de confirmação
    document.getElementById('btnConfirmarLivroAnunciado')?.addEventListener('click', confirmarLivroAnunciado);
    document.getElementById('btnConfirmarLivroNaoAnunciado')?.addEventListener('click', confirmarLivroNaoAnunciado);
}
