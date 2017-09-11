package br.com.mywallet.model;

import java.util.List;

/**
 * Created by brienze on 4/28/17.
 */
public class Extrato {
    public Extrato(){

    }
    private List<Transacao> transacoes;

    public Extrato(List<Transacao> listaTransacoes) {
        this.setTransacoes(listaTransacoes);
    }

    public List<Transacao> getTransacoes() {
        return transacoes;
    }

    public void setTransacoes(List<Transacao> transacoes) {
        this.transacoes = transacoes;
    }
}
