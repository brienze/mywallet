package br.com.mywallet.ui;

import br.com.mywallet.model.Transacao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class DetalheCreditoDebitoController {

    @FXML
    private Label lblDataAtual;

    @FXML
    private TableView<Transacao> tblDebitosCartaoCredito, tblDebitosContaCorrente;

    @FXML
    private TableView<Transacao> tblCreditos;

    @FXML
    private Label lblDebitos;

    @FXML
    private Label lblCredito;

    @FXML
    private Label lblContaCorrente;

    @FXML
    private Label lblCartaoCredito;

    @FXML
    private Button btnFechar;

    @FXML
    private TableColumn<Transacao, LocalDate> colContaCorrenteData;

    @FXML
    private TableColumn<Transacao, LocalDate> colContaCorrenteDescricao;

    @FXML
    private TableColumn<Transacao, LocalDate> colContaCorrenteValor;

    @FXML
    private TableColumn<Transacao, LocalDate> colContaCorrenteCategoria;

    @FXML
    private TableColumn<Transacao, LocalDate> colCreditoData;

    @FXML
    private TableColumn<Transacao, LocalDate> colCreditoDescricao;

    @FXML
    private TableColumn<Transacao, LocalDate> colCreditoValor;

    @FXML
    private TableColumn<Transacao, LocalDate> colCreditoCategoria;

    @FXML
    private TableColumn<Transacao, LocalDate> colCartaoData;

    @FXML
    private TableColumn<Transacao, LocalDate> colCartaoDescricao;

    @FXML
    private TableColumn<Transacao, LocalDate> colCartaoValor;

    @FXML
    private TableColumn<Transacao, LocalDate> colCartaoCategoria;

    @FXML
    private Label lblFirst, lblSecond,lblThird;

    String first, second, third;

    List<Transacao> creditos, debitosCartaoCredito, debitosContaCorrente;

    @FXML
    void doFechar(ActionEvent event) {
       // ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
        Stage stage = (Stage)btnFechar.getScene().getWindow();
        stage.close();
    }

    public DetalheCreditoDebitoController(){

    }


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        DateTimeFormatter formatadorMes =
                DateTimeFormatter.ofPattern("dd/MM/YYYY");

        lblDataAtual.setText(formatadorMes.format(LocalDate.now()));

        definirColunas();


    }

    private void definirColunas() {
        colCartaoData.setCellValueFactory(new PropertyValueFactory("dataFatura"));
        colCartaoCategoria.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("categoria"));
        colCartaoDescricao.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("descricao"));
        colCartaoValor.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("valor"));

        colContaCorrenteData.setCellValueFactory(new PropertyValueFactory("data"));
        colContaCorrenteCategoria.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("categoria"));
        colContaCorrenteDescricao.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("descricao"));
        colContaCorrenteValor.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("valor"));

        colCreditoData.setCellValueFactory(new PropertyValueFactory("data"));
        colCreditoCategoria.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("categoria"));
        colCreditoDescricao.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("descricao"));
        colCreditoValor.setCellValueFactory(new PropertyValueFactory<Transacao, LocalDate>("valor"));
    }

    private void definirValores() {
        tblDebitosCartaoCredito.getItems().setAll(debitosCartaoCredito);
        tblDebitosContaCorrente.getItems().setAll(debitosContaCorrente);
        tblCreditos.getItems().setAll(creditos);

        Double somaCreditoTotal = creditos.stream().mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaContaCorrente = debitosContaCorrente.stream().mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaCartaoCredito = debitosCartaoCredito.stream().mapToDouble(p->p.getValor().doubleValue()).sum();

        Locale localeBR = new Locale("pt", "BR");
        NumberFormat dinheiro = NumberFormat.getCurrencyInstance(localeBR);

        lblDebitos.setText(dinheiro.format(somaContaCorrente+somaCartaoCredito));
        lblCredito.setText(dinheiro.format(somaCreditoTotal));
        lblContaCorrente.setText(dinheiro.format(somaContaCorrente));
        lblCartaoCredito.setText(dinheiro.format(somaCartaoCredito));
    }

    public void initData(String first, String second, String last, List<Transacao> debitosContaCorrente,List<Transacao> debitosCartaoCredito,List<Transacao> creditos){

        lblFirst.setText(first);
        lblSecond.setText(second);
        lblThird.setText(last);

        this.creditos = creditos;
        this.debitosCartaoCredito = debitosCartaoCredito;
        this.debitosContaCorrente = debitosContaCorrente;
        definirValores();
    }

}

