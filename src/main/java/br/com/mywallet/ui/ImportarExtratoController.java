package br.com.mywallet.ui;

import br.com.mywallet.model.Conta;
import br.com.mywallet.model.Transacao;
import br.com.mywallet.service.ContaService;
import br.com.mywallet.utils.ExtratoBancarioImporter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
public class ImportarExtratoController {
    static final Logger logger = LoggerFactory.getLogger(ImportarExtratoController.class);
    private FileChooser fileChooser;

    @Autowired
    private ContaService contaService;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="cbConta"
    private ComboBox<Conta> cbConta; // Value injected by FXMLLoader

    @FXML // fx:id="cbMes"
    private ComboBox<String> cbMes; // Value injected by FXMLLoader

    @FXML
    private ToggleSwitch tsTipoConta;

    @FXML // fx:id="butImportar"
    private Button butImportar; // Value injected by FXMLLoader
    Consumer<List<Transacao>> consumer;

    public void definirCallback(Consumer<List<Transacao>> consumer){
        this.consumer = consumer;
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

        ObservableList<String> datas = FXCollections.observableArrayList("11-2016", "12-2016","01-2017","02-2017", "03-2017","04-2017","05-2017","06-2017","07-2017","08-2017","09-2017","10-2017","11-2017","12-2017");

        List<Conta> contas = contaService.findByTipoConta(1);
        List<Conta> cartao = contaService.findByTipoConta(2);

        cbMes.getItems().setAll(datas);


        tsTipoConta.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    //Conta corrente
                    cbConta.getItems().setAll(contas);
                }else{
                    //Cartao
                    cbConta.getItems().setAll(cartao);
                }
            }
        });
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("Extrato");
        fileChooser.setInitialDirectory(
                new File("/Users/brienze/IdeaProjects/BankStatementImporter")
        );
    }

    @FXML
    void importar(javafx.event.ActionEvent ace){
        fileChooser = new FileChooser();
        configureFileChooser(fileChooser);
        File arquivoExtrato = fileChooser.showOpenDialog(((Button)ace.getSource()).getScene().getWindow());
        List<Transacao> transacoes = null;
        if (arquivoExtrato != null) {
            String arquivo = arquivoExtrato.getPath();
            Conta conta = cbConta.getSelectionModel().getSelectedItem();
            String dataFaturamento = cbMes.getSelectionModel().getSelectedItem();
            String[] yearMonthInput = dataFaturamento.split("-");

            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH,Integer.valueOf(yearMonthInput[0])-1);
            c.set(Calendar.YEAR,Integer.valueOf(yearMonthInput[1]));
            LocalDate yearMonth = LocalDate.of(Integer.valueOf(yearMonthInput[1]),Integer.valueOf(yearMonthInput[0]),1);
            try {
                List<String> transacoesStr = null;
                if(conta.getNomeConta().equals("Itau")){
                    transacoesStr = new ExtratoBancarioImporter().parsePdfDocumentContaCorrente(arquivoExtrato);
                }else{
                    transacoesStr = new ExtratoBancarioImporter().parsePdfDocument(arquivoExtrato);
                }

                transacoes = loadFromLista(transacoesStr, conta,yearMonth );
                //crud.saveOrUpdateTira(_extrato);
            } catch (IOException e) {
                logger.warn("Erro ao importar extrato (" + arquivo + ") !", e);
                e.printStackTrace();
                return;
            } catch (URISyntaxException e) {
                logger.warn("Erro ao importar extrato (" + arquivo + ") !", e);
                e.printStackTrace();
                return;
            }
            consumer.accept(transacoes);
        }
    }



    private List<Transacao> loadFromLista(List<String> transacoes, Conta conta, LocalDate dataFaturamento) throws URISyntaxException {

        List<List<String>> lines = new ArrayList<>();
        transacoes.forEach(s->lines.add(Arrays.asList(s.split(";"))));

        List<Transacao> listaTransacoes = new ArrayList<>();
        Locale localeBR = new Locale("pt", "BR");

        lines.forEach(value -> {
            String descricao = value.get(1);
            String data = value.get(0);
            String valor = value.get(2);
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy");
            String year = dataFaturamento.format(formatter2);

            DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-dd-MMM");
            data = year + "-" + data;

            //formatter = formatter.withLocale(localeBR);
            LocalDate localDate = LocalDate.parse(data,formatter);

            boolean isCredito = false;
            Boolean isDebito = true;
            //'\u00AD' 173
            isCredito = valor.contains("\u00AD");
            if(isCredito){
                isDebito = false;
                valor = valor.replace('\u00AD',' ');
            }

            if(valor.contains("9,49")){
                System.out.println();
            }



            valor = "R$ " + valor.trim();

/*
            DecimalFormatSymbols brazilSymbol = new DecimalFormatSymbols(localeBR);
            brazilSymbol.setDecimalSeparator(',');
            brazilSymbol.setGroupingSeparator('.');

            String formatBR = "###,###.00";

            NumberFormat nf =
                    NumberFormat.getNumberInstance(new
                            Locale("pt","BR"));

            DecimalFormat df = new DecimalFormat (formatBR,brazilSymbol);
            df.setGroupingSize(3);
            */
            BigDecimal bd = null;


            BigDecimal money = null;
            //try {
            //valor = UtilDate.converterMoeda(valor);
            //Double value2 = df.parse(valor).doubleValue();

            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            Number number = null;

            try {

                number = numberFormat.parse(valor);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            money = BigDecimal.valueOf(number.floatValue());
            //Double value2 = df.parse(valor).doubleValue();
            //df.setParseBigDecimal (true);
            //money = (BigDecimal) df.parse (valor);
            //} catch (ParseException e) {
            //    e.printStackTrace();
            //}

            //BigDecimal money = BigDecimal.valueOf(Long.valueOf(valor));
            listaTransacoes.add(new Transacao(localDate,descricao,money, isDebito,conta, conta.getNomeConta().equals("Itau")?null:dataFaturamento));
        });
        return listaTransacoes;
    }

    private List<Transacao> loadFromCSV(String fileName, Conta conta, LocalDate dataFaturamento) throws URISyntaxException {

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        List<List<String>> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            stream.forEach(s->lines.add(Arrays.asList(s.split(";"))));

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Transacao> listaTransacoes = new ArrayList<>();
        lines.remove(0);//remove column names
        Locale localeBR = new Locale("pt", "BR");

        lines.forEach(value -> {
            String descricao = value.get(1);
            String data = value.get(0);
            String valor = value.get(2);

            DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-dd-MMM");
            data = "2017-" + data;

            formatter = formatter.withLocale(localeBR);
            LocalDate localDate = LocalDate.parse(data,formatter);

            valor = valor.trim();

            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            Number number = null;
            try {
                number = numberFormat.parse(valor);
            } catch (ParseException e) {
                e.printStackTrace();
            }

/*
            DecimalFormatSymbols brazilSymbol = new DecimalFormatSymbols(localeBR);
            brazilSymbol.setDecimalSeparator(',');
            brazilSymbol.setGroupingSeparator('.');

            String formatBR = "###,###.00";

            NumberFormat nf =
                    NumberFormat.getNumberInstance(new
                            Locale("pt","BR"));

            DecimalFormat df = new DecimalFormat (formatBR,brazilSymbol);
            df.setGroupingSize(3);
            BigDecimal bd = null;


            BigDecimal money = null;
            //try {
            //valor = UtilDate.converterMoeda(valor);
            //Double value2 = df.parse(valor).doubleValue();
            Float aValor = Float.parseFloat(valor);
            */
            Float aValor = number.floatValue();
            Boolean isDebito = true;
            if(aValor < 0){
                isDebito = false;
                aValor = aValor * -1;
            }
            BigDecimal money = null;
            money = BigDecimal.valueOf(aValor);
            //Double value2 = df.parse(valor).doubleValue();
            //df.setParseBigDecimal (true);
            //money = (BigDecimal) df.parse (valor);
            //} catch (ParseException e) {
            //    e.printStackTrace();
            //}

            //BigDecimal money = BigDecimal.valueOf(Long.valueOf(valor));
            listaTransacoes.add(new Transacao(localDate,descricao,money, isDebito,conta, dataFaturamento));
        });
        return listaTransacoes;
    }

    public static void main(String[] args){

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(

                                new Locale("pt", "BR"));

        Number number =
                null;
        try {
            number = numberFormat.parse("R$ 52,10");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(number);
        System.out.println(number.getClass());

        BigDecimal bigDecimal =
                BigDecimal.valueOf(number.doubleValue());
        System.out.println(bigDecimal);

        String valor = "52,15";
        Locale localeBR = new Locale("pt", "BR");

        DecimalFormatSymbols brazilSymbol = new DecimalFormatSymbols(localeBR);
        brazilSymbol.setDecimalSeparator(',');
        brazilSymbol.setGroupingSeparator('.');

        String formatBR = "###.###,00";

        NumberFormat nf =
                NumberFormat.getNumberInstance(new
                        Locale("pt","BR"));

        //DecimalFormat df = new DecimalFormat (formatBR,brazilSymbol);
        //df.setGroupingSize(3);
        BigDecimal bd = null;

        try {
            Number valor3 = nf.parse(valor);
            //Number valor2 = df.parse(valor);
            //Float aValor = valor2.floatValue();
            System.out.println(valor3);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        BigDecimal money = null;
        //try {
        //valor = UtilDate.converterMoeda(valor);
        //Double value2 = df.parse(valor).doubleValue();
        Float aValor = Float.parseFloat(valor);

    }


}
