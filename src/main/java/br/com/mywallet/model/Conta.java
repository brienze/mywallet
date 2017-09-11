package br.com.mywallet.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by brienze on 5/2/17.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_conta", length = 1, discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
public class Conta implements Serializable {

public Conta(){

}
    @Id
    @GeneratedValue
    @Column(name="id_conta")
    private Integer idConta;

    @Column(name="nome")
    private String nomeConta;

    @Column(insertable=false, updatable=false, name = "tipo_conta")
    private Integer tipoConta;


    public Conta(Integer id, String nome){
        setIdConta(id);
        setNomeConta(nome);
    }


    public void setTipoConta(Integer tipoConta) {
        this.tipoConta = tipoConta;
    }

    public Integer getTipoConta() {
        return this.tipoConta;
    }

    public Integer getIdConta() {
        return idConta;
    }
    public void setIdConta(Integer id) {
        this.idConta = id;
    }

    public String getNomeConta() {
        return nomeConta;
    }

    public void setNomeConta(String nomeConta) {
        this.nomeConta = nomeConta;
    }

    @Override
    public String toString(){
        return nomeConta;
    }

}
