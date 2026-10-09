/**
 * Módulo da lista de conversas da barra lateral (renderização, busca e polling).
 */

import { elements, destacarConversaAtual } from './ui.js';
import {
    getConversaAtual,
    getAssinaturaConversas,
    setAssinaturaConversas,
    setIntervaloConversas,
    clearIntervaloConversas
} from './state.js';
import { atualizarConversasApi } from './api.js';

export function renderizarListaConversas(conversas, onSelectConversa) {
    if (!elements.chatList) return;

    elements.chatList.innerHTML = '';

    if (conversas.length === 0) {
        elements.chatList.innerHTML = `
            <li class="chat-empty-list">
                <p>Nenhuma conversa iniciada.</p>
            </li>
        `;
        return;
    }

    conversas.forEach(conversa => {
        const item = document.createElement('li');
        item.className = 'chat-item';
        item.dataset.anuncio = conversa.uidAnuncio;
        item.dataset.destinatario = conversa.uidDestinatario;

        const foto = document.createElement('img');
        foto.src = conversa.fotoDestinatario || '/img/user.png';
        foto.alt = 'Usuário';
        foto.className = 'chat-avatar';

        const info = document.createElement('div');
        info.className = 'chat-item-info';

        const nome = document.createElement('strong');
        nome.textContent = conversa.nomeDestinatario || 'Usuário';

        const ultimaMensagem = document.createElement('span');
        ultimaMensagem.textContent = conversa.ultimaMensagem || '';

        info.appendChild(nome);
        info.appendChild(ultimaMensagem);

        item.appendChild(foto);
        item.appendChild(info);

        item.addEventListener('click', function () {
            if (typeof onSelectConversa === 'function') {
                onSelectConversa(this.dataset.anuncio, this.dataset.destinatario, this);
            }
        });

        elements.chatList.appendChild(item);
    });

    const conversaAtual = getConversaAtual();
    destacarConversaAtual(conversaAtual.uidAnuncio, conversaAtual.uidDestinatario);
}

export function iniciarAtualizacaoConversas(onSelectConversa) {
    pararAtualizacaoConversas();
    const timer = setInterval(() => {
        atualizarConversas(onSelectConversa);
    }, 4000);
    setIntervaloConversas(timer);
}

export function pararAtualizacaoConversas() {
    clearIntervaloConversas();
}

export async function atualizarConversas(onSelectConversa) {
    try {
        const conversas = await atualizarConversasApi();
        if (!conversas) {
            return;
        }

        const novaAssinatura = JSON.stringify(
            conversas.map(conversa => ({
                anuncio: conversa.uidAnuncio,
                destinatario: conversa.uidDestinatario,
                nome: conversa.nomeDestinatario,
                foto: conversa.fotoDestinatario,
                mensagem: conversa.ultimaMensagem
            }))
        );

        if (novaAssinatura === getAssinaturaConversas()) {
            return;
        }

        setAssinaturaConversas(novaAssinatura);
        renderizarListaConversas(conversas, onSelectConversa);
    } catch (erro) {
        console.error('Erro ao atualizar conversas:', erro);
    }
}

export function filtrarConversas(termo) {
    const termoBusca = (termo || '').toLowerCase().trim();

    document.querySelectorAll('.chat-item').forEach(item => {
        const nome = item.querySelector('strong')?.textContent.toLowerCase() || '';
        item.style.display = nome.includes(termoBusca) ? '' : 'none';
    });
}
