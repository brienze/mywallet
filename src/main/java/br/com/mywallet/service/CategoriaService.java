package br.com.mywallet.service;

import br.com.mywallet.model.Categoria;

import java.util.List;


public interface CategoriaService {

    public Categoria save(Categoria transacao);
    public void saveAll(List<Categoria> transacao);

    public List<Categoria> findAll();

    public List<Categoria> findByCategoriaPaiIsNull();



}
