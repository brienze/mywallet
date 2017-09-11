package br.com.mywallet.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by brienze on 5/11/17.
 */
@Entity
@DiscriminatorValue(value = "2")
public class CartaoCredito extends Conta implements Serializable {


    @Column(name="vencimento_fatura")
    private Integer vencimentoFatura;

    @Column(name="data_limite")
    private Integer dataLimite;


    public Integer getVencimentoFatura() {
        Categoria minhaCategoria = new Categoria();
        minhaCategoria.getNome();

        return vencimentoFatura;
    }

    public void setVencimentoFatura(Integer vencimentoFatura) {
        this.vencimentoFatura = vencimentoFatura;
    }

    public Integer getDataFatura() {
        return dataLimite;
    }

    public void setDataFatura(Integer dataFatura) {
        this.dataLimite = dataFatura;
    }


}
