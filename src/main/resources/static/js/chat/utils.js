/**
 * Funções utilitárias e de formatação do Chat.
 */

export function formatarTipoNegociacao(tipo) {
    switch (tipo) {
        case 'TROCA':
            return 'Troca';
        case 'VENDA':
            return 'Venda';
        case 'AMBOS':
            return 'Troca e venda';
        default:
            return tipo || '';
    }
}

export function obterTopicoConversa(uidAnuncio, uidRemetente, uidDestinatario) {
    // Ordenação lexicográfica pura (Array.sort sem comparador usa Unicode),
    // idêntica ao String.compareTo() do Java usado no TrocabookChatService.
    const [u1, u2] = [uidRemetente, uidDestinatario].sort();
    return `/topic/conversa.${uidAnuncio}.${u1}.${u2}`;
}