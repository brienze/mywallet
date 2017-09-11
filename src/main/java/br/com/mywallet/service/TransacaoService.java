package br.com.mywallet.service;

import br.com.mywallet.model.Transacao;

import java.util.Date;
import java.util.List;

/**
 * Date: 27.08.15
 * Time: 17:22
 *
 * @author Ruslan Molchanov (ruslanys@gmail.com)
 * @author http://mruslan.com
 */
public interface TransacaoService {

    Transacao save(Transacao transacao);
    void saveAll(List<Transacao> transacao);

    List<Transacao> findByData (Date data);

    List<Transacao> findAll ();

    List<Transacao> findByDataFatura(Date data);

    void remove(Transacao transacao);
}
