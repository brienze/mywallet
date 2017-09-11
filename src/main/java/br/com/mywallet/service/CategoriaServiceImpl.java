package br.com.mywallet.service;

import br.com.mywallet.model.Categoria;
import br.com.mywallet.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
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
public class CategoriaServiceImpl implements CategoriaService {

    public CategoryRepository getRepository() {
        return repository;
    }

    public void setRepository(CategoryRepository repository) {
        this.repository = repository;
    }

    @Autowired
    private CategoryRepository repository;

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
    public Categoria save(Categoria transacao) {

        return repository.save(transacao);
    }

    @Override
    public void saveAll(List<Categoria> transacoes) {
         repository.save(transacoes);
    }

    @Override
    public List<Categoria> findAll() {

        return repository.findAll();
    }

    @Override
    public List<Categoria> findByCategoriaPaiIsNull(){
        return repository.findByCategoriaPaiIsNull();
    }


}
