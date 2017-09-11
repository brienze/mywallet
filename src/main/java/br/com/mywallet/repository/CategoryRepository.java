package br.com.mywallet.repository;

import br.com.mywallet.model.Categoria;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Date: 27.08.15
 * Time: 17:21
 *
 * @author Ruslan Molchanov (ruslanys@gmail.com)
 * @author http://mruslan.com
 */
@Transactional(propagation = Propagation.MANDATORY)
public interface CategoryRepository extends CrudRepository<Categoria, Long> {

    List<Categoria> findAll();

    List<Categoria> findByCategoriaPaiIsNull();

}
