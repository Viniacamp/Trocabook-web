package com.trocabook.Trocabook.model.dto;

import com.trocabook.Trocabook.model.Usuario;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdminUsuarioInput(

        @NotNull(message = "O status é obrigatório")
        Usuario.StatusUsuario status,

        @Min(value = 0, message = "A avaliação mínima é 0")
        @Max(value = 5, message = "A avaliação máxima é 5")
        double avaliacao

) {
}