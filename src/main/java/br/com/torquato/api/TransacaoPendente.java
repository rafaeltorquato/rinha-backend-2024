package br.com.torquato.api;

public record TransacaoPendente(int valor,
                                TipoTransacao tipo,
                                String descricao) {


    public boolean isValida() {
        return !(descricao == null || descricao.isEmpty() || descricao.length() > 10);
    }

}
