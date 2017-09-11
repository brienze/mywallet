package br.com.mywallet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brienze on 5/4/17.
 */


public class CategoriaBuilder {
    static CategoriaBuilder _instance;



    private CategoriaBuilder(){

    }

    public static CategoriaBuilder instance(){
        if(_instance == null){
            _instance = new CategoriaBuilder();
        }
        return _instance;
    }

    /**
     * Habitação
     Dia a Dia
     Transporte
     Lazer
     Educação
     Outros
     Saúde
     Despesas Pessoais
     * @return
     */
    public List<Categoria> obterCategorias(){
        List<Categoria> categorias = new ArrayList<>();

        Categoria habitacao = new Categoria("Habitacao");

        habitacao.addSubCategorias(
                new Categoria("Aluguel / Prestação"),
                new Categoria("Condomínio"),
                new Categoria("IPTU + Taxas Municipais"),
                new Categoria("Conta de energia"),
                new Categoria("Telefone fixo"),
                new Categoria("Telefones celulares"),
                new Categoria("Internet"),
                new Categoria("TV por assinatura"),
                new Categoria("Supermercado"),
                new Categoria("Padaria"),
                new Categoria("Lavanderia"),
                new Categoria("Seguro"),
                new Categoria("Limpeza"),
                new Categoria("Outros"),
                new Categoria("Gas")
        );

        categorias.add(habitacao);

        Categoria diaAdia = new Categoria("Dia a Dia");
        diaAdia.addSubCategorias(
                new Categoria("Alimentos"),
                new Categoria("Medicamentos"),
                new Categoria("Lanches"),
                new Categoria("Vestuário"),
                new Categoria("Almoço"),
                new Categoria("Cafezinho"));

        categorias.add(diaAdia);

        Categoria transporte = new Categoria("Transporte");
        transporte.addSubCategorias(
                new Categoria("IPVA"),
                new Categoria("Seguro"),
                new Categoria("Combustível"),
                new Categoria("Estacionamentos"),
                new Categoria("Lavagens"),
                new Categoria("Mecânico"),
                new Categoria("Multas"),
                new Categoria("Bilhete Unico"),
                new Categoria("Taxi-Uber"),
                new Categoria("Pedagio"),
                new Categoria("Estacionamento"));

        categorias.add(transporte);

        Categoria lazer = new Categoria("Lazer");
        lazer.addSubCategorias(
                new Categoria("Restaurantes"),
                new Categoria("Passagens"),
                new Categoria("Hospedagens"),
                new Categoria("Passeios"),
                new Categoria("Outros"),
                new Categoria("Cinema"),
                new Categoria("Teatro"));

        categorias.add(lazer);

        Categoria educacao = new Categoria("Educacao");
        educacao.addSubCategorias(
                new Categoria("Escola-Faculdade"),
                new Categoria("Cursos"),
                new Categoria("Idiomas"),
                new Categoria("Livros Revistas"),
                new Categoria("Outros"));

        categorias.add(educacao);

        Categoria saude = new Categoria("Saude");
        saude.addSubCategorias(
                new Categoria("Plano de Saude"),
                new Categoria("Médicos e Terapeutas"),
                new Categoria("Dentistas"),
                new Categoria("Medicamentos"),
                new Categoria("Seguro Vida"),
                new Categoria("Acupuntura"),
                new Categoria("Outros"));

        categorias.add(saude);

        Categoria despesasPessoais = new Categoria("Despesas Pessoais");
        despesasPessoais.addSubCategorias(
                new Categoria("Higiene Pessoal"),
                new Categoria("Cosméticos"),
                new Categoria("Cabeleireiro"),
                new Categoria("Academia"),
                new Categoria("Esportes"),
                new Categoria("Presentes"),
                new Categoria("Outros"));

        categorias.add(despesasPessoais);

        Categoria taxasJuros = new Categoria("Taxas-Juros");
        taxasJuros.addSubCategorias(
                new Categoria("Anuidade cartoes"),
                new Categoria("Taxa bancaria"),
                new Categoria("Cheque especial"));

        categorias.add(taxasJuros);

        Categoria auxilio = new Categoria("Auxilio");
        auxilio.addSubCategorias(
                new Categoria("Instituicao caridade"),
                new Categoria("Luiz - Pai da Carol"),
                new Categoria("Pais Edu"),
                new Categoria("Antonia"),
                new Categoria("Ester"));

        categorias.add(auxilio);

        return categorias;
    }



}
