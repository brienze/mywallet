package br.com.mywallet.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brienze on 4/28/17.
 */
@Entity
@Table
public class Categoria implements Serializable {

    @Id
    @GeneratedValue
    @Column(name="id_categoria")
    private Integer idCategoria;

    @Column(unique = true)
    private String nome;

    @Column(name="is_essencial")
    private Boolean isEssencial;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_categoria_pai")
    private Categoria categoriaPai;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "categoriaPai")
    private List<Categoria> subcategorias;

    public Categoria(){
        subcategorias = new ArrayList<>();
    }

    public Categoria(String nome, Integer categoriaId){
        this();
        idCategoria=categoriaId;
        this.nome = nome;
    }

    public Categoria(String nome){
        this();
        this.nome = nome;
    }

    public void setCategoriaPai(Categoria cat){
        categoriaPai = cat;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer id) {
        this.idCategoria = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoriaPai() {
        return categoriaPai;
    }

    public List<Categoria> getSubCategorias() {
        String nome = getNome();

        return subcategorias;
    }

    public void setSubCategorias(List<Categoria> subcategorias) {
        this.subcategorias = subcategorias;
    }

    public void addSubCategorias(Categoria... categorias ){
        for (Categoria categoria: categorias){
            subcategorias.add(categoria);
        }
    }

    @Override
    public String toString(){
        return getNome();
    }

    public Boolean getEssencial() {
        return isEssencial;
    }

    public void setEssencial(Boolean essencial) {
        isEssencial = essencial;
    }
}