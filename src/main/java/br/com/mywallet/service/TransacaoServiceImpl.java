package br.com.mywallet.service;

import br.com.mywallet.model.Transacao;
import br.com.mywallet.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Date: 27.08.15
 * Time: 17:23
 *
 * @author Ruslan Molchanov (ruslanys@gmail.com)
 * @author http://mruslan.com
 */
@Service
@Transactional
public class TransacaoServiceImpl implements TransacaoService {

    public TransacaoRepository getRepository() {
        return repository;
    }

    public void setRepository(TransacaoRepository repository) {
        this.repository = repository;
    }

    @Autowired
    private TransacaoRepository repository;

    /**
     * Метод добавляет парочку записей в БД после запуска приложения,
     * чтобы не было совсем пусто.
     *
     * Из-за того, что подключена H2 (in-memory) БД.
     */
    @PostConstruct
    public void generateTestData() {
        //save(new Contact("Иван Иванов", "+123456789", "ivan@ivan.ov"));
        //save(new Contact("Петр Петров", "+987654321", "petr@pe.tr"));
    }

    @Override
    public Transacao save(Transacao transacao) {
        return repository.save(transacao);
    }

    @Override
    public void saveAll(List<Transacao> transacoes) {
         repository.save(transacoes);
    }

    @Override
    public List<Transacao> findByData(Date data) {
        return repository.findByData( data);
    }

    @Override
    public List<Transacao> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Transacao> findByDataFatura(Date data) {

        return repository.findByDataFatura( data);
    }

    @Override
    public void remove(Transacao transacao){
        repository.delete(transacao.getIdTransacao());
    }
}

