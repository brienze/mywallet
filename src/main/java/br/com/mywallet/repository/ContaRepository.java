package br.com.mywallet.repository;

import br.com.mywallet.model.Conta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**

 * @author Marcos Brienze

 */
@Transactional(propagation = Propagation.MANDATORY)
public interface ContaRepository extends CrudRepository<Conta, Long> {

    List<Conta> findAll();
    List<Conta> findByTipoConta(Integer tipoConta);

}
