package com.trocabook.Trocabook.controllers.request;

import com.trocabook.Trocabook.model.dto.UsuarioAnalyticsDTO;
import java.util.List;

public record PrevisaoUsuariosAnalyticsRequest(
        List<UsuarioAnalyticsDTO> usuarios,
        Integer horizontePrevisao,
        Integer quantidadeTeste
) {
    public PrevisaoUsuariosAnalyticsRequest(List<UsuarioAnalyticsDTO> usuarios) {
        this(usuarios, 12, 6);
    }
}