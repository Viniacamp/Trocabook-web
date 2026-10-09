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
