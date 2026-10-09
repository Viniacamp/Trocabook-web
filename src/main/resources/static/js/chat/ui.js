/**
 * Elementos do DOM e manipulação de interface do Chat.
 */

export const elements = {
    get chatList() { return document.getElementById('chatList'); },
    get chatPlaceholder() { return document.getElementById('chatPlaceholder'); },
    get chatHeader() { return document.getElementById('chatHeader'); },
    get chatAviso() { return document.getElementById('chatAviso'); },
    get messagesContainer() { return document.getElementById('messagesContainer'); },
    get chatForm() { return document.getElementById('chatForm'); },
    get bookInfo() { return document.getElementById('bookInfo'); },

    get chatUsuarioFoto() { return document.getElementById('chatUsuarioFoto'); },
    get chatUsuarioNome() { return document.getElementById('chatUsuarioNome'); },

    get bookCapa() { return document.getElementById('bookCapa'); },
    get bookTitulo() { return document.getElementById('bookTitulo'); },
    get bookDescricao() { return document.getElementById('bookDescricao'); },
    get negociacaoConteudo() { return document.getElementById('negociacaoConteudo'); },

    get uidAnuncioInput() { return document.getElementById('uidAnuncio'); },
    get uidDestinatarioInput() { return document.getElementById('uidUsuarioDestinatario'); },
    get uidRemetenteInput() { return document.getElementById('uidUsuarioRemetente'); },

    get pesquisaConversas() { return document.getElementById('pesquisaConversas'); },
    get conteudoInput() { return document.getElementById('conteudo'); },

    get modalTipoOferta() { return document.getElementById('modalTipoOferta'); },
    get modalSelecionarLivro() { return document.getElementById('modalSelecionarLivro'); },
    get modalDescricaoOferta() { return document.getElementById('modalDescricaoOferta'); },
    get listaLivrosOferta() { return document.getElementById('listaLivrosOferta'); },
    get descricaoOfertaAnunciada() { return document.getElementById('descricaoOfertaAnunciada'); },
    get descricaoOfertaManual() { return document.getElementById('descricaoOfertaManual'); },
    get contadorDescricaoOferta() { return document.getElementById('contadorDescricaoOferta'); }
};

export function mostrarCarregamentoConversa(uidAnuncio, uidDestinatario) {
    if (elements.uidAnuncioInput) elements.uidAnuncioInput.value = uidAnuncio;
    if (elements.uidDestinatarioInput) elements.uidDestinatarioInput.value = uidDestinatario;

    if (elements.messagesContainer) {
        elements.messagesContainer.innerHTML = '';
        elements.messagesContainer.style.display = 'flex';
    }
    if (elements.chatPlaceholder) elements.chatPlaceholder.style.display = 'none';
    if (elements.chatHeader) elements.chatHeader.style.display = 'flex';
    if (elements.chatAviso) elements.chatAviso.style.display = 'block';
    if (elements.chatForm) elements.chatForm.style.display = 'flex';
    if (elements.bookInfo) elements.bookInfo.style.display = 'block';
}

export function exibirErroConversa() {
    if (elements.messagesContainer) {
        elements.messagesContainer.innerHTML = `
            <div class="chat-error">
                <i class="fa-solid fa-triangle-exclamation"></i>
                <p>Não foi possível carregar esta conversa.</p>
            </div>
        `;
    }
}

export function atualizarUsuarioHeader(usuarioNegociante) {
    if (!usuarioNegociante) return;
    if (elements.chatUsuarioNome) {
        elements.chatUsuarioNome.textContent = usuarioNegociante.nome || 'Usuário';
    }
    if (elements.chatUsuarioFoto) {
        elements.chatUsuarioFoto.src = usuarioNegociante.fotoPerfil || '/img/user.png';
    }
}

export function atualizarLivroInfo(anuncio) {
    if (!anuncio) return;
    if (elements.bookTitulo) {
        elements.bookTitulo.textContent = anuncio.titulo || 'Livro';
    }
    if (elements.bookCapa) {
        elements.bookCapa.src = anuncio.capa || '/img/capa-padrao.png';
    }
    if (elements.bookDescricao) {
        elements.bookDescricao.textContent = anuncio.descricao || 'Anunciante não informou uma descrição.';
    }
}

export function destacarConversaAtual(uidAnuncio, uidDestinatario) {
    if (!uidAnuncio || !uidDestinatario) {
        return;
    }

    document.querySelectorAll('.chat-item').forEach(item => {
        const mesmaConversa =
            item.dataset.anuncio === uidAnuncio &&
            item.dataset.destinatario === uidDestinatario;

        item.classList.toggle('active', mesmaConversa);
    });
}
