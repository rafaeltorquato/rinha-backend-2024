package br.com.torquato.rinha.domain.model;

import java.util.Set;

public record TransacaoPendente(double valor,
                                String tipo,
                                String descricao) {

    private static final Set<String> TIPOS_VALIDOS = Set.of("c", "d");

    public boolean isValida() {
        return !(descricao == null ||
                descricao.isEmpty() ||
                descricao.length() > 10 ||
                tipo == null ||
                !TIPOS_VALIDOS.contains(tipo) ||
                valor % 1 != 0);
    }

}
