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
    const u1 = uidRemetente.localeCompare(uidDestinatario) < 0 ? uidRemetente : uidDestinatario;
    const u2 = uidRemetente.localeCompare(uidDestinatario) < 0 ? uidDestinatario : uidRemetente;

    return `/topic/conversa.${uidAnuncio}.${u1}.${u2}`;
}