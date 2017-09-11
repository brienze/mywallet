package br.com.mywallet.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Created by brienze on 4/28/17.
 */
@Entity
@Table
public class Transacao implements Serializable {

    @Id
    @GeneratedValue
    @Column(name="id_transacao")
    private Integer idTransacao;

    @Column
    private LocalDate data;

    @Column(name="data_fatura")
    private LocalDate dataFatura;

    @ManyToOne
    @JoinColumn(name = "id_conta")
    private Conta conta;

    @Column
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "transacao_tag", joinColumns = { @JoinColumn(name = "id_transacao") }, inverseJoinColumns = { @JoinColumn(name = "id_tag") })
    private Set<Tag> tags;

    @Column
    private BigDecimal valor;

    @Column( name="is_debito")
    private Boolean isDebito;

    @Column( name="is_contabiliza")
    private Boolean isContabiliza;

    @Transient
    private Integer idTableView;

    public Transacao(){

    }

    public Transacao(LocalDate data, String descricao, BigDecimal valor, Boolean isDebito, Conta conta, LocalDate dataFaturamento){
        this.data = data;
        this.descricao = descricao;
        this.valor = valor;
        this.setIsDebito(isDebito);
        setDataFatura(dataFaturamento);
        setConta(conta);
        setContabiliza(true);
        setIsDebito(true);
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public LocalDate getDataFatura() {
        return dataFatura;
    }

    public void setDataFatura(LocalDate dataFatura) {
        this.dataFatura = dataFatura;
    }

    public Integer getIdTransacao() {
        return idTransacao;
    }

    public void setIdTransacao(Integer id) {
        this.idTransacao = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getNomeCategoria() {
        return categoria!= null ? categoria.getNome(): "";
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Boolean getIsDebito() {
        return isDebito;
    }

    public Long getValorLong() {
        return valor.longValue();
    }

    public void setIsDebito(Boolean debito) {
        isDebito = debito;
    }


    public Boolean isContabiliza() {
        return isContabiliza;
    }

    public void setContabiliza(Boolean contabiliza) {
        isContabiliza = contabiliza;
    }

    /**
     * Igualdade refere-se a valor, data e descrição
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj instanceof Transacao == false){
            return false;
        }
        Transacao trn = (Transacao)obj;
        //comparar valor com duas casas decimais
        final DecimalFormat df = new DecimalFormat("#.##");

        return df.format(this.getValor()).equals(df.format(trn.getValor()))
                && this.getData().equals(trn.getData())
                && this.getDescricao().trim().toUpperCase().equals(trn.getDescricao().trim().toUpperCase());
    }
    /*public boolean equals(Object obj){
        if(obj instanceof Transacao == false){
            return false;
        }
        if(this == obj){
            return true;
        }
        Transacao trn = (Transacao)obj;
        return this.getIdTransacao().equals(trn.getIdTransacao());
    }*/

    @Override
    /*public int hashCode(){
        return Objects.hashCode(this.idTransacao);
    }*/
    public int hashCode(){
        final DecimalFormat df = new DecimalFormat("#.##");
        return Objects.hash(this.data, this.getDescricao().trim().toUpperCase(),df.format(this.getValor()));
    }


    public Integer getIdTableView() {
        return idTableView;
    }

    public void setIdTableView(Integer idTableView) {
        this.idTableView = idTableView;
    }
}
