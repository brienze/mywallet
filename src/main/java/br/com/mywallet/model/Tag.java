package br.com.mywallet.model;

import javax.persistence.*;

/**
 * Created by brienze on 4/28/17.
 */
@Entity
@Table
public class Tag {

    public Tag(){

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Id
    @GeneratedValue
    @Column(name="id_tag")
    private Integer id;

    @Column(unique = true)
    private String nome;
}
