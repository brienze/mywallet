package br.com.mywallet.service;

import br.com.mywallet.model.Conta;
import br.com.mywallet.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Marcos Brienze
 */
@Service
@Transactional
public class ContaServiceImpl implements ContaService {

    public ContaRepository getRepository() {
        return repository;
    }

    public void setRepository(ContaRepository repository) {
        this.repository = repository;
    }

    @Autowired
    private ContaRepository repository;

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
    public Conta save(Conta conta) {
        return repository.save(conta);
    }

    @Override
    public void saveAll(List<Conta> contas) {
         repository.save(contas);
    }

    @Override
    public List<Conta> findByTipoConta(Integer tipoConta) {
        return repository.findByTipoConta( tipoConta);
    }

    @Override
    public List<Conta> findAll() {
        return repository.findAll();
    }

}

