package br.com.mywallet.service;

import br.com.mywallet.model.Conta;

import java.util.List;

/**
 */
public interface ContaService {

    Conta save(Conta conta);
    void saveAll(List<Conta> contas);

    List<Conta> findAll();

    List<Conta> findByTipoConta(Integer tipoConta);

}
