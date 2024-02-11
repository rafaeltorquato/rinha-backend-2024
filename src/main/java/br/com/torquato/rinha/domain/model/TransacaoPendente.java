package br.com.torquato.rinha.domain.model;

import java.util.Set;

public record TransacaoPendente(int valor,
                                String tipo,
                                String descricao) {
    public boolean isValida() {
        return !(descricao == null ||
                descricao.isEmpty() ||
                descricao.length() > 10 ||
                tipo == null ||
                !Set.of("c", "d").contains(tipo));
    }

}
