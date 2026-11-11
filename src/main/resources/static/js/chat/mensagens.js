/**
 * Módulo de mensagens do Chat (renderização, envio, edição, exclusão e polling).
 */

import { elements } from './ui.js';
import {
    getUidUsuarioLogado,
    getConversaAtual,
    getAssinaturaMensagens,
    setAssinaturaMensagens,
    setIntervaloMensagens,
    clearIntervaloMensagens,
    setConversaAtualNegociacao
} from './state.js';
import {
    enviarMensagemApi,
    atualizarMensagensApi,
    editarMensagemApi,
    excluirMensagemApi
} from './api.js';

let atualizacaoMensagensId = 0;
let mensagemEmEdicaoId = null;
let mensagemEmEdicaoDiv = null;
let mensagemEmExclusaoId = null;
let mensagemEmExclusaoDiv = null;

export function configurarModaisMensagem() {
    const fecharModais = () => {
        if (elements.modalEditarMensagem) elements.modalEditarMensagem.style.display = 'none';
        if (elements.modalExcluirMensagem) elements.modalExcluirMensagem.style.display = 'none';
        mensagemEmEdicaoId = null;
        mensagemEmEdicaoDiv = null;
        mensagemEmExclusaoId = null;
        mensagemEmExclusaoDiv = null;
    };

    document.addEventListener('click', evento => {
        const alvo = evento.target;
        if (alvo instanceof Element && alvo.closest('.menu-opcoes')) return;

        document.querySelectorAll('.menu-dropdown').forEach(menu => {
            menu.style.display = 'none';
        });
    });

    document.querySelectorAll('#modalEditarMensagem .modal-troca-fechar, #btnCancelarEdicao').forEach(btn => {
        btn.addEventListener('click', fecharModais);
    });

    document.querySelectorAll('#modalExcluirMensagem .modal-troca-fechar, #btnCancelarExclusao').forEach(btn => {
        btn.addEventListener('click', fecharModais);
    });

    if (elements.btnSalvarEdicao) {
        elements.btnSalvarEdicao.addEventListener('click', async () => {
            if (!mensagemEmEdicaoId || !mensagemEmEdicaoDiv) return;
            if (elements.btnSalvarEdicao.disabled) return;
            const novoConteudo = elements.inputEditarMensagem ? elements.inputEditarMensagem.value.trim() : '';
            if (!novoConteudo) {
                alert('A mensagem não pode estar vazia.');
                return;
            }

            elements.btnSalvarEdicao.disabled = true;
            try {
                const { ok, data } = await editarMensagemApi(mensagemEmEdicaoId, novoConteudo);
                if (ok) {
                    const p = mensagemEmEdicaoDiv.querySelector('p');
                    if (p) p.textContent = novoConteudo;
                    fecharModais();
                } else {
                    alert('Erro ao alterar mensagem.');
                }
            } catch (erro) {
                console.error('Erro ao editar mensagem:', erro);
                alert('Não foi possível alterar a mensagem.');
            } finally {
                elements.btnSalvarEdicao.disabled = false;
            }
        });
    }

    if (elements.btnConfirmarExclusao) {
        elements.btnConfirmarExclusao.addEventListener('click', async () => {
            if (!mensagemEmExclusaoId || !mensagemEmExclusaoDiv) return;
            if (elements.btnConfirmarExclusao.disabled) return;

            elements.btnConfirmarExclusao.disabled = true;
            try {
                const { ok, data } = await excluirMensagemApi(mensagemEmExclusaoId);
                if (ok) {
                    mensagemEmExclusaoDiv.remove();
                    fecharModais();
                } else {
                    alert('Erro ao excluir mensagem.');
                }
            } catch (erro) {
                console.error('Erro ao excluir mensagem:', erro);
                alert('Não foi possível excluir a mensagem.');
            } finally {
                elements.btnConfirmarExclusao.disabled = false;
            }
        });
    }
}

export function gerarAssinaturaMensagens(mensagens) {
    if (!mensagens || !Array.isArray(mensagens)) {
        return '[]';
    }

    return JSON.stringify(
        mensagens.map(msg => ({
            id: msg.id || msg._id,
            conteudo: msg.conteudo,
            uidRemetente: msg.uidRemetente
        }))
    );
}

export function renderizarMensagens(mensagens) {
    if (!elements.messagesContainer) return;

    elements.messagesContainer.innerHTML = '';

    mensagens.forEach(mensagem => {
        adicionarMensagemNoContainer(mensagem);
    });

    elements.messagesContainer.scrollTop = elements.messagesContainer.scrollHeight;
}

export function adicionarMensagemNoContainer(msg) {
    if (!elements.messagesContainer) return;

    // Tenta todas as variações possíveis de nomes de campos de ID vindos do back-end
    const msgId = msg.id || msg._id || msg.idMensagem || msg.uuid || '';

    if (!msgId) {
        console.warn("Aviso: Mensagem recebida sem ID visível:", msg);
    }

    // Evita duplicar elemento se já estiver presente no DOM
    if (msgId && document.querySelector(`[data-id="${msgId}"]`)) {
        return;
    }

    const souEu = msg.uidRemetente === getUidUsuarioLogado();

    const div = document.createElement('div');
    div.className = `message ${souEu ? 'user' : 'negociador'}`;

    if (msgId) {
        div.setAttribute('data-id', msgId);
    }

    // Restante do código do avatar, balão e menu...
    if (!souEu) {
        const img = document.createElement('img');
        img.src = (elements.chatUsuarioFoto && elements.chatUsuarioFoto.src) || '/img/user.png';
        img.alt = 'Foto do usuário';
        img.className = 'message-avatar';
        div.appendChild(img);
    }

    const balao = document.createElement('div');
    balao.className = 'message-balao';

    const p = document.createElement('p');
    p.textContent = msg.conteudo || '';
    balao.appendChild(p);

    if (souEu) {
        const menuOpcoes = document.createElement('div');
        menuOpcoes.className = 'menu-opcoes';

        const icon = document.createElement('i');
        icon.className = 'fa-solid fa-ellipsis-vertical';
        icon.onclick = function (e) {
            if (e) e.stopPropagation();
            abrirMenuMensagem(this);
        };

        const menuDropdown = document.createElement('div');
        menuDropdown.className = 'menu-dropdown';

        const botaoEditar = document.createElement('button');
        botaoEditar.type = 'button';
        botaoEditar.textContent = 'Editar';
        botaoEditar.onclick = function (e) {
            if (e) e.stopPropagation();
            editarMensagem(this);
        };

        const botaoExcluir = document.createElement('button');
        botaoExcluir.type = 'button';
        botaoExcluir.textContent = 'Excluir';
        botaoExcluir.onclick = function (e) {
            if (e) e.stopPropagation();
            excluirMensagem(this);
        };

        menuDropdown.appendChild(botaoEditar);
        menuDropdown.appendChild(botaoExcluir);

        menuOpcoes.appendChild(icon);
        menuOpcoes.appendChild(menuDropdown);

        balao.appendChild(menuOpcoes);
    }

    div.appendChild(balao);
    elements.messagesContainer.appendChild(div);

    elements.messagesContainer.scrollTop = elements.messagesContainer.scrollHeight;
}

export async function enviarMensagem(event) {
    if (event) {
        event.preventDefault();
    }

    const conversaAtual = getConversaAtual();

    if (!conversaAtual.uidAnuncio || !conversaAtual.uidDestinatario) {
        return;
    }

    const input = elements.conteudoInput || document.getElementById('conteudo');
    if (!input) return;

    const conteudo = input.value.trim();
    if (!conteudo) {
        return;
    }

    const mensagem = {
        uidAnuncio: conversaAtual.uidAnuncio,
        uidRemetente: getUidUsuarioLogado(),
        uidDestinatario: conversaAtual.uidDestinatario,
        conteudo: conteudo
    };

    try {
        // Limpa o input imediatamente para melhor experiência do usuário
        input.value = '';
        input.focus();

        const data = await enviarMensagemApi(mensagem);


        if (data && data.data && data.data.id) {
            adicionarMensagemNoContainer(data.data);
        }
    } catch (erro) {
        console.error('Erro ao enviar mensagem:', erro);
        alert('Não foi possível enviar a mensagem.');
    }
}

export function abrirMenuMensagem(icon) {
    const menu = icon.nextElementSibling;

    document.querySelectorAll('.menu-dropdown').forEach(outroMenu => {
        if (outroMenu !== menu) {
            outroMenu.style.display = 'none';
        }
    });

    menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
}

export function editarMensagem(botao) {
    const msgDiv = botao.closest('.message');
    const idMensagem = msgDiv ? msgDiv.getAttribute('data-id') : null;

    if (!idMensagem) {
        console.error("Elemento HTML da mensagem sem data-id:", msgDiv);
        alert('ID da mensagem não encontrado para edição.');
        return;
    }

    const p = msgDiv.querySelector('p');
    mensagemEmEdicaoId = idMensagem;
    mensagemEmEdicaoDiv = msgDiv;

    if (elements.inputEditarMensagem) {
        elements.inputEditarMensagem.value = p ? p.textContent : '';
    }
    if (elements.modalEditarMensagem) {
        elements.modalEditarMensagem.style.display = 'flex';
        if (elements.inputEditarMensagem) {
            elements.inputEditarMensagem.focus();
        }
    }

    const menu = botao.closest('.menu-dropdown');
    if (menu) menu.style.display = 'none';
}

export function excluirMensagem(botao) {
    const msgDiv = botao.closest('.message');
    const idMensagem = msgDiv ? msgDiv.getAttribute('data-id') : null;

    if (!idMensagem) {
        console.error("Elemento HTML da mensagem sem data-id:", msgDiv);
        alert('ID da mensagem não encontrado para exclusão.');
        return;
    }

    mensagemEmExclusaoId = idMensagem;
    mensagemEmExclusaoDiv = msgDiv;

    if (elements.modalExcluirMensagem) {
        elements.modalExcluirMensagem.style.display = 'flex';
    }

    const menu = botao.closest('.menu-dropdown');
    if (menu) menu.style.display = 'none';
}

export function iniciarAtualizacaoMensagens(onNegociacaoAtualizada) {
    pararAtualizacaoMensagens();
    atualizarMensagens(onNegociacaoAtualizada);
    const timer = setInterval(() => {
        atualizarMensagens(onNegociacaoAtualizada);
    }, 4000);
    setIntervaloMensagens(timer);
}

export function pararAtualizacaoMensagens() {
    atualizacaoMensagensId++;
    clearIntervaloMensagens();
}

export async function atualizarMensagens(onNegociacaoAtualizada) {
    const atualizacaoId = atualizacaoMensagensId;
    const conversaAtual = getConversaAtual();
    const uidAnuncio = conversaAtual.uidAnuncio;
    const uidDestinatario = conversaAtual.uidDestinatario;

    if (!uidAnuncio || !uidDestinatario) {
        return;
    }

    try {
        const atualizacao = await atualizarMensagensApi(
            uidAnuncio,
            uidDestinatario
        );

        if (
            !atualizacao ||
            atualizacaoId !== atualizacaoMensagensId ||
            uidAnuncio !== getConversaAtual().uidAnuncio ||
            uidDestinatario !== getConversaAtual().uidDestinatario
        ) {
            return;
        }

        const mensagens = atualizacao.mensagens || [];
        const negociacaoAtualizada = atualizacao.negociacao || null;

        const statusAnterior = conversaAtual.negociacao?.status || null;
        const statusAtual = negociacaoAtualizada?.status || null;

        setConversaAtualNegociacao(negociacaoAtualizada);

        if (statusAnterior !== statusAtual && typeof onNegociacaoAtualizada === 'function') {
            onNegociacaoAtualizada(conversaAtual.anuncio, negociacaoAtualizada);
        }

        const novaAssinatura = gerarAssinaturaMensagens(mensagens);
        if (novaAssinatura !== getAssinaturaMensagens()) {
            setAssinaturaMensagens(novaAssinatura);
            renderizarMensagens(mensagens);
        }
    } catch (erro) {
        console.error('Erro no polling da conversa:', erro);
    }
}