package br.com.mywallet.ui;

import br.com.mywallet.Application;
import br.com.mywallet.model.Categoria;
import br.com.mywallet.model.CategoriaBuilder;
import br.com.mywallet.model.Transacao;
import br.com.mywallet.service.CategoriaService;
import br.com.mywallet.service.TransacaoService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ExtratoController{

    private Pattern pattern;
    private Matcher matcher;


    private static final String DATE_PATTERN =
            "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])";

    private LocalDate mesAtual, mesCorrente;

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private CategoriaService categoriaService;

    List<Transacao> _transacoes;
    static final Logger logger = LoggerFactory.getLogger(ExtratoController.class);
    private FileChooser fileChooser;

    private List<Transacao> _listaTransacoes;

    @FXML
    private Label lblSaldoTotal, lblCreditoTotal, lblSaldoTodasContas,lblSaldoItau,lblSaldoCitibank,lblSaldoCartoes,lblSaldoPersMaster,lblSaldoPersVisa,lblSaldoMaster;


    @FXML // fx:id="cbMes"
    private ComboBox<YearMonth> cbData; // Value injected by FXMLLoader

    @FXML
    private Button btnSelectDuplicates;



    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="categoriasCb"
    private ComboBox<?> categoriasCb; // Value injected by FXMLLoader

    @FXML // fx:id="tagsCb"
    private ComboBox<?> tagsCb; // Value injected by FXMLLoader

    @FXML // fx:id="tagsCb"
    private TableColumn<Transacao,String> colCategoria; // Value injected by FXMLLoader

    @FXML // fx:id="extratoTabela"
    private TableView<Transacao> extratoTabela; // Value injected by FXMLLoader
    @FXML
    private TableColumn<?, ?> colData, colDataFatura;

    @FXML
    private TableColumn<Transacao, String> colDescricao;
    @FXML
    private TableColumn<Transacao, Boolean> colDebitoCredito;

    @FXML
    private TableColumn<?, ?> colValor;

    @FXML
    private Label lblDataAtual, lblMesCorrente, lblMesSeguinte, lblMesAnterior;

    private Transacao _clickedRow;

    private Map<String,Categoria> _categoriasDefinidas;
    private Map<String,Categoria> _categoriasDefinidasChave;


    @FXML
    void doSelectDuplicates(ActionEvent event) {
        Set<Transacao> allItems = new HashSet<>();

        Set<Transacao> duplicates = extratoTabela.getItems().stream()
                .filter(n -> !allItems.add(n)) //Set.add() returns false if the item was already in the set.
                .collect(Collectors.toSet());

        //selecionar duplicatas na tabela
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        if(duplicates.size() > 0){
            duplicates.stream().forEach(t->extratoTabela.getSelectionModel().select(t));
            alerta.setTitle("Duplicatas");
            alerta.setHeaderText("Encontrou");
            alerta.setContentText("Foram encontrados " + duplicates.size() + " transacões iguais");
        }else{
            alerta.setTitle("Duplicatas");
            alerta.setHeaderText("Não encontrou");
            alerta.setContentText("Não foram encontrados transacões iguais");
        }
        alerta.showAndWait();
    }

    private void createCategoriasDefinidas(){
        ExtratoController temp = new ExtratoController();

        Categoria cate = new Categoria();

        cate.getIdCategoria();

        _categoriasDefinidas = _listaTransacoes.stream().filter(p->p.getCategoria()!=null).collect(Collectors.toMap(p->
        {   String descr = p.getDescricao().trim().toUpperCase();
            String possivelData = descr.length() > 8 ?
                    (descr.substring(descr.length()-5,descr.length())).replaceAll("\\s+", " ").trim().toUpperCase()
                    :
                    descr.replaceAll("\\s+", " ").trim().toUpperCase();
            boolean hasData = validate(possivelData);
            if(hasData){
                possivelData = descr.substring(0,descr.length()-5).replaceAll("\\s+", " ").trim().toUpperCase();
            }else{
                possivelData = descr;
            }
            possivelData = possivelData.trim();
            System.out.println("Tem data ? [" + descr + "] X [" + possivelData + "] = " + hasData);
         return possivelData;

        }, p->p.getCategoria(),(s1,s2)->s2));
    }

    private void definirItemMenuTabela(){
        extratoTabela.setRowFactory(new Callback<TableView<Transacao>, TableRow<Transacao>>() {
            @Override
            public TableRow<Transacao> call(TableView<Transacao> tableView) {

                final TableRow<Transacao> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (! row.isEmpty() && event.getButton()== MouseButton.PRIMARY
                            && event.getClickCount() == 2) {

                        _clickedRow = row.getItem();

                    }
                });

                final ContextMenu contextMenu = new ContextMenu();
                final MenuItem addMenuItemCredito = new MenuItem("Definir Credito");
                addMenuItemCredito.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Transacao trn = row.getItem();
                        trn.setIsDebito(false);
                        try{
                            transacaoService.save(trn);
                        }catch(Throwable t){
                            t.printStackTrace();
                        }
                        definirValores(mesCorrente);

                    }
                });

                final MenuItem addMenuItemDebito = new MenuItem("Definir Debito");
                addMenuItemDebito.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Transacao trn = row.getItem();
                        trn.setIsDebito(true);
                        try{
                            transacaoService.save(trn);
                        }catch(Throwable t){
                            t.printStackTrace();
                        }
                        definirValores(mesCorrente);

                    }
                });

                final MenuItem addMenuItemDelete = new MenuItem("Remover");
                addMenuItemDelete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tableView.getSelectionModel().getSelectedItems().forEach(trn->{
                            trn.setIsDebito(true);
                            try{
                                transacaoService.remove(trn);
                                //_listaTransacoes.remove(trn);
                            }catch(Throwable t){
                                t.printStackTrace();
                            }
                        });
                        //Transacao trn = row.getItem();
                        _listaTransacoes = transacaoService.findAll();
                        definirValores(mesCorrente);

                    }
                });

                final MenuItem addMenuItemChave = new MenuItem("Definir Chave");
                addMenuItemChave.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        TextInputDialog dialog = new TextInputDialog("walter");
                        dialog.setTitle("Chave");
                        dialog.setHeaderText("Definir chave");
                        dialog.setContentText("Entre com chave de busca");

                        // Traditional way to get the response value.
                        Optional<String> result = dialog.showAndWait();
                        if (result.isPresent()){
                            String chave = result.get();
                            Transacao trn = row.getItem();
                            _listaTransacoes.stream().filter(p->p.getCategoria() == null && p.getDescricao().toUpperCase().contains(chave.toUpperCase())).forEach(p->{
                                p.setCategoria(trn.getCategoria());
                            });
                        }

                    }
                });

                final MenuItem addMenuItemFaturaCartao = new MenuItem("Definir fatura de cartao");
                addMenuItemFaturaCartao.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Transacao trn = row.getItem();
                        trn.setContabiliza(false);
                        try{
                            transacaoService.save(trn);
                        }catch(Throwable t){
                            t.printStackTrace();
                        }
                        definirValores(mesCorrente);

                    }
                });

                contextMenu.getItems().add(addMenuItemCredito);
                contextMenu.getItems().add(addMenuItemFaturaCartao);
                contextMenu.getItems().add(addMenuItemDebito);
                contextMenu.getItems().add(addMenuItemDelete);
                contextMenu.getItems().add(addMenuItemChave);
                // Set context menu on row, but use a binding to make it only show for non-empty rows:
                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu)null)
                                .otherwise(contextMenu)
                );
                return row ;
            }
        });
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        pattern = Pattern.compile(DATE_PATTERN);
        mesAtual = LocalDate.now();
        _listaTransacoes = transacaoService.findAll();
        extratoTabela.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE
        );
        definirItemMenuTabela();
        createCategoriasDefinidas();

        definirMes(mesAtual);

        assert categoriasCb != null : "fx:id=\"categoriasCb\" was not injected: check your FXML file 'ExtratoView.fxml'.";
        assert tagsCb != null : "fx:id=\"tagsCb\" was not injected: check your FXML file 'ExtratoView.fxml'.";
        assert extratoTabela != null : "fx:id=\"extratoTabela\" was not injected: check your FXML file 'ExtratoView.fxml'.";

        setModelColumn(colData, "data");
        setModelColumn(colDescricao, "descricao");
        setModelColumn(colValor, "valor");
        setModelColumn(colCategoria, "nomeCategoria");
        setModelColumn(colDataFatura, "dataFatura");
        setModelColumn(colDebitoCredito, "isDebito");

        colDebitoCredito.setCellFactory(column -> {
            return new TableCell<Transacao, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");

                    } else {
                        String value = "";
                        if(item == false){
                            setTextFill(Color.BLUE);
                            setStyle("-fx-alignment: center");
                            value = "C";
                        }else{
                            setTextFill(Color.BLACK);
                            setStyle("-fx-alignment: center");
                            value = "D";
                        }
                        setText(value);

                        // Style all dates in March with a different color.
                        /*if (item.getMonth() == Month.MARCH) {
                            setTextFill(Color.CHOCOLATE);
                            setStyle("-fx-background-color: yellow");
                        } else {
                            setTextFill(Color.BLACK);
                            setStyle("");
                        }*/
                    }
                }
            };
        });


        ObservableList<YearMonth> datas = FXCollections.observableArrayList(YearMonth.of(2016,11),YearMonth.of(2016,12),YearMonth.of(2017,1)
               , YearMonth.of(2017,2),YearMonth.of(2017,3),YearMonth.of(2017,4),YearMonth.of(2017,5));
        cbData.getItems().setAll(datas);

        /*extratoTabela.setRowFactory(tv -> {
            TableRow<Transacao> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (! row.isEmpty() && event.getButton()== MouseButton.PRIMARY
                        && event.getClickCount() == 2) {

                    _clickedRow = row.getItem();

                }
            });
            return row ;
        });*/


        Callback<TableColumn<Transacao, String>, TableCell<Transacao, String>> cellFactory =
                new Callback<TableColumn<Transacao, String>, TableCell<Transacao, String>>() {
                    public TableCell call(TableColumn p) {
                        TableCell cell = new TableCell<Transacao, String>() {
                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(empty ? null : getString());
                                setGraphic(null);
                            }

                            private String getString() {
                                return getItem() == null ? "" : getItem().toString();
                            }
                        };

                        cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                if (event.getClickCount() > 1) {
                                    if(cell.getItem() != null){
                                        System.out.println("double click on "+cell.getItem());
                                    }
                                    Parent root = null;
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DefinirCategoria.fxml"));
                                    try {
                                        loader.setControllerFactory(Application.getContext()::getBean);
                                        root = loader.load();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Scene scene = new Scene(root);
                                    Stage stage = new Stage();
                                    Object obj = event.getSource();

                                    stage.initOwner(categoriasCb.getScene().getWindow());

                                    stage.initModality(Modality.WINDOW_MODAL);
                                    stage.setScene(scene);
                                    stage.initStyle(StageStyle.DECORATED);

                                    DefinirCategoriaController controller = loader.<DefinirCategoriaController>getController();

                                    Consumer<Categoria> aConsumer = new Consumer<Categoria>() {
                                        @Override
                                        public void accept(Categoria categoria) {
                                            //cell.setItem(categoria);
                                            _clickedRow.setCategoria(categoria);
                                            atualizarCategoriasFromHistorico(_clickedRow);//atualizar historico mediante categoria recém-definida
                                            //transacaoService.save(_clickedRow);
                                            // extratoTabela.getColumns().get(2).setVisible(false);
                                            // extratoTabela.getColumns().get(2).setVisible(true);
                                            //extratoTabela.getItems().clear();
                                            //extratoTabela.getItems().setAll(_transacoes);
                                            definirCategoriasFromHistorico(_listaTransacoes);//verificar para todas transações sem categoria se há alguma que possa ser atualizada
                                            try{
                                                transacaoService.saveAll(_listaTransacoes);
                                            }catch(Throwable t){
                                                t.printStackTrace();
                                            }

                                            irMesAtual();
                                            stage.close();
                                        }
                                    };
                                    controller.definirCallback(aConsumer);
                                    stage.show();
                                }
                            }
                        });
                        return cell;
                    }
                };
        colCategoria.setCellFactory(cellFactory);
    }

    private void definirMes(LocalDate dataCorrente){

        DateTimeFormatter formatadorMes =
                DateTimeFormatter.ofPattern("MM/YYYY");
        mesCorrente = dataCorrente;
        lblMesCorrente.setText(formatadorMes.format(dataCorrente));
        LocalDate dataAnterior = dataCorrente.minusMonths(1);
        LocalDate dataPosterior = dataCorrente.plusMonths(1);
        lblMesAnterior.setText(formatadorMes.format(dataAnterior));
        lblMesSeguinte.setText(formatadorMes.format(dataPosterior));

        definirValores(dataCorrente);

    }

    private void definirValores(LocalDate dataCorrente){
        extratoTabela.getItems().clear();

        YearMonth yearMonth = YearMonth.from(dataCorrente);

        List<Transacao> transacoesContaCorrente = _listaTransacoes.stream().filter(p->{
            boolean isMatch = false;
            if(p.getConta().getTipoConta().equals(1)){
                isMatch = YearMonth.from(p.getData()).equals(yearMonth);
            }
            return isMatch;
        }).collect(Collectors.toList());
        List<Transacao> transacoesCartaoCredito = _listaTransacoes.stream().filter(p->p.getConta().getTipoConta().equals(2) && YearMonth.from(p.getData()).equals(yearMonth)).collect(Collectors.toList());

        List<Transacao> todasTransacoes = new ArrayList<>(transacoesContaCorrente.size() + transacoesCartaoCredito.size());
        todasTransacoes.addAll(transacoesCartaoCredito);
        todasTransacoes.addAll(transacoesContaCorrente);

        Double somaTotal = todasTransacoes.stream().filter(t->t.getIsDebito()&& t.isContabiliza()).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaCreditoTotal = todasTransacoes.stream().filter(t->t.getIsDebito() == false && t.isContabiliza()).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaContaCorrente = transacoesContaCorrente.stream().filter(t->t.getConta().getTipoConta().equals(1) && t.getIsDebito()&& t.isContabiliza()).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaCartaoCredito = transacoesCartaoCredito.stream().filter(t->t.getConta().getTipoConta().equals(2)&& t.getIsDebito()&& t.isContabiliza()).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaItau = transacoesContaCorrente.stream().filter(t->t.getConta().getTipoConta().equals(1) && t.getIsDebito() && t.isContabiliza()&& t.getConta().getNomeConta().equals("Itau")).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaCitibank = transacoesContaCorrente.stream().filter(t->t.getConta().getTipoConta().equals(1) && t.getIsDebito()&& t.isContabiliza() && t.getConta().getNomeConta().equals("Citibank")).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaPersMaster = transacoesCartaoCredito.stream().filter(t->t.getConta().getTipoConta().equals(2) && t.getIsDebito() && t.isContabiliza()&& t.getConta().getNomeConta().equals("Personalite Master")).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaPersVisa = transacoesCartaoCredito.stream().filter(t->t.getConta().getTipoConta().equals(2) && t.getIsDebito() && t.isContabiliza()&& t.getConta().getNomeConta().equals("Personalite Visa")).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaAmex = transacoesCartaoCredito.stream().filter(t->t.getConta().getTipoConta().equals(2) && t.getIsDebito() && t.isContabiliza()&& t.getConta().getNomeConta().equals("Amex")).mapToDouble(p->p.getValor().doubleValue()).sum();
        Double somaVisa = transacoesCartaoCredito.stream().filter(t->t.getConta().getTipoConta().equals(2) && t.getIsDebito() && t.isContabiliza()&& t.getConta().getNomeConta().equals("Visa")).mapToDouble(p->p.getValor().doubleValue()).sum();


        Locale localeBR = new Locale("pt", "BR");
        NumberFormat dinheiro = NumberFormat.getCurrencyInstance(localeBR);

        lblSaldoTotal.setText(dinheiro.format(somaTotal));
        if(somaCreditoTotal != null){
            lblCreditoTotal.setText(dinheiro.format(somaCreditoTotal));
        }

        lblSaldoTodasContas.setText(dinheiro.format(somaContaCorrente));
        lblSaldoItau.setText(dinheiro.format(somaItau));
        lblSaldoCitibank.setText(dinheiro.format(somaCitibank));
        lblSaldoCartoes.setText(dinheiro.format(somaCartaoCredito));
        lblSaldoPersMaster.setText(dinheiro.format(somaPersMaster));
        lblSaldoPersVisa .setText(dinheiro.format(somaPersVisa));
        lblSaldoMaster.setText(dinheiro.format(somaAmex));

        extratoTabela.getItems().setAll(todasTransacoes);

    }

    public boolean validate(final String date){

        matcher = pattern.matcher(date);

        if(matcher.matches()){

            matcher.reset();

            if(matcher.find()){

                String day = matcher.group(1);
                String month = matcher.group(2);


                if (day.equals("31") &&
                        (month.equals("4") || month .equals("6") || month.equals("9") ||
                                month.equals("11") || month.equals("04") || month .equals("06") ||
                                month.equals("09"))) {
                    return false; // only 1,3,5,7,8,10,12 has 31 days
                } else{
                    return true;
                }
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        List<Categoria> categorias = categoriaService.findByCategoriaPaiIsNull();

        if(categorias.size() == 0){
            categorias = CategoriaBuilder.instance().obterCategorias();
            categoriaService.saveAll(categorias);
            categorias = categoriaService.findByCategoriaPaiIsNull();
        }
    }

    @FXML
    void selecionar(ActionEvent event) {
        YearMonth selecionado = cbData.getSelectionModel().getSelectedItem();
        definirMes(selecionado.atDay(1));
    }

    static void setModelColumn(TableColumn tb, String a){
        tb.setCellValueFactory(new PropertyValueFactory(a));
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("Extrato");
        fileChooser.setInitialDirectory(
                new File("/Users/brienze/IdeaProjects/BankStatementImporter")
        );
    }

    @FXML
    void selecionarTodasTransacoes(MouseEvent event) {
        YearMonth yearMonth = getDataAtual();
        List<Transacao> transacoes = _transacoes.stream().filter(t->YearMonth.from(t.getData()).equals(yearMonth)).collect(Collectors.toList());
        //List<Transacao> transacoes2 = _transacoes.stream().filter(t->YearMonth.from(t.getDataFatura()).equals(yearMonth)).collect(Collectors.toList());
        extratoTabela.getItems().setAll(transacoes);
        //extratoTabela.getItems().setAll(transacoes2);
    }

    @FXML
    void selecionarCartaoMaster(MouseEvent event) {
        YearMonth yearMonth = getDataAtual();
        List<Transacao> transacoes = _transacoes.stream().filter(t->t.getConta().getTipoConta().equals(2) && t.getConta().getNomeConta().equals("Amex")&& YearMonth.from(t.getData()).equals(yearMonth)).collect(Collectors.toList());
        extratoTabela.getItems().clear();
        extratoTabela.getItems().setAll(transacoes);

     }

    @FXML
    void selecionarCartaoPersMaster(MouseEvent event) {
        YearMonth yearMonth = getDataAtual();
        List<Transacao> transacoes = _listaTransacoes.stream().filter(t->t.getConta().getTipoConta().equals(2) && t.getConta().getNomeConta().equals("Personalite Master")&& YearMonth.from(t.getData()).equals(yearMonth)).collect(Collectors.toList());
        extratoTabela.getItems().clear();
        extratoTabela.getItems().setAll(transacoes);

     }

    @FXML
    void selecionarCartaoPersVisa(MouseEvent event) {
        YearMonth yearMonth = getDataAtual();
        List<Transacao> transacoes = _listaTransacoes.stream().filter(t->t.getConta().getTipoConta().equals(2) && t.getConta().getNomeConta().equals("Personalite Visa")&& YearMonth.from(t.getData()).equals(yearMonth)).collect(Collectors.toList());
        extratoTabela.getItems().clear();
        extratoTabela.getItems().setAll(transacoes);
    }

    @FXML
    void selecionarContaCitibank(MouseEvent event) {
        YearMonth yearMonth = getDataAtual();
        List<Transacao> transacoes = _listaTransacoes.stream().filter(t->t.getConta().getTipoConta().equals(1) && t.getConta().getNomeConta().equals("Citibank")&& YearMonth.from(t.getData()).equals(yearMonth)).collect(Collectors.toList());
        extratoTabela.getItems().clear();
        extratoTabela.getItems().setAll(transacoes);
    }

    private YearMonth getDataAtual(){
        String mesAtual = lblMesCorrente.getText();
        DateTimeFormatter formatadorMes =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        mesAtual = "01/" + mesAtual ;

        Locale localeBR = new Locale("pt", "BR");
        formatadorMes = formatadorMes.withLocale( localeBR );  // Locale specifies human language for translating, and cultural norms for lowercase/uppercase and abbreviations and such. Example: Locale.US or Locale.CANADA_FRENCH
        LocalDate date = LocalDate.parse(mesAtual, formatadorMes);


        YearMonth yearMonth = YearMonth.from(date);

        return yearMonth;
    }

    @FXML
    void selecionarContaItau(MouseEvent event) {
        YearMonth yearMonth = getDataAtual();
        List<Transacao> transacoes = _listaTransacoes.stream().filter(t->t.getConta().getTipoConta().equals(1) && t.getConta().getNomeConta().equals("Itau") && YearMonth.from(t.getData()).equals(yearMonth)).collect(Collectors.toList());
        extratoTabela.getItems().clear();
        extratoTabela.getItems().setAll(transacoes);
    }

    @FXML
    void selecionarTodasContas(MouseEvent event) {
        YearMonth yearMonth = getDataAtual();

        List<Transacao> transacoes = _transacoes.stream().filter(t->t.getConta().getTipoConta().equals(1) && YearMonth.from(t.getData()).equals(yearMonth)).collect(Collectors.toList());
        extratoTabela.getItems().clear();
        extratoTabela.getItems().setAll(transacoes);
    }



    @FXML
    void selecionarTodosCartoes(MouseEvent event) {
        YearMonth yearMonth = getDataAtual();
        List<Transacao> transacoes = _listaTransacoes.stream().filter(t->t.getConta().getTipoConta().equals(2) && YearMonth.from(t.getData()).equals(yearMonth)).collect(Collectors.toList());
        extratoTabela.getItems().clear();
        extratoTabela.getItems().setAll(transacoes);
    }

    void irMesAtual() {
        String mesAnterior = lblMesCorrente.getText();
        DateTimeFormatter formatadorMes =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        mesAnterior = "01/" + mesAnterior ;

        Locale localeBR = new Locale("pt", "BR");
        formatadorMes = formatadorMes.withLocale( localeBR );  // Locale specifies human language for translating, and cultural norms for lowercase/uppercase and abbreviations and such. Example: Locale.US or Locale.CANADA_FRENCH
        LocalDate date = LocalDate.parse(mesAnterior, formatadorMes);

        definirMes(date);
    }

    @FXML
    void irMesAnterior(MouseEvent event) {
        String mesAnterior = lblMesAnterior.getText();
        DateTimeFormatter formatadorMes =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        mesAnterior = "01/" + mesAnterior ;

        Locale localeBR = new Locale("pt", "BR");
        formatadorMes = formatadorMes.withLocale( localeBR );  // Locale specifies human language for translating, and cultural norms for lowercase/uppercase and abbreviations and such. Example: Locale.US or Locale.CANADA_FRENCH
        LocalDate date = LocalDate.parse(mesAnterior, formatadorMes);

        definirMes(date);
    }

    @FXML
    void irMesSeguinte(MouseEvent event) {
        String mesAnterior = lblMesSeguinte.getText();
        DateTimeFormatter formatadorMes =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        mesAnterior = "01/" + mesAnterior ;

        Locale localeBR = new Locale("pt", "BR");
        formatadorMes = formatadorMes.withLocale( localeBR );  // Locale specifies human language for translating, and cultural norms for lowercase/uppercase and abbreviations and such. Example: Locale.US or Locale.CANADA_FRENCH
        LocalDate date = LocalDate.parse(mesAnterior, formatadorMes);

        definirMes(date);
    }

    @FXML
    void importar(javafx.event.ActionEvent ace){
        //show cartao form

        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/importarExtrato.fxml"));
        try {
            loader.setControllerFactory(Application.getContext()::getBean);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        //stage.initOwner(((MenuItem)event.getSource()).getScene().getWindow());

        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
        stage.initStyle(StageStyle.DECORATED);

        ImportarExtratoController controller = loader.<ImportarExtratoController>getController();

        Consumer<List<Transacao>> aConsumer = new Consumer<List<Transacao>>() {
            @Override
            public void accept(List<Transacao> transacoes) {
                if(transacoes.size() > 0){
                    definirCategoriasFromHistorico(transacoes);
                    _listaTransacoes.addAll(transacoes);
                    stage.close();
                    definirMes(transacoes.get(0).getData());
                    try{
                        transacaoService.saveAll(transacoes);
                    }catch(Throwable t){
                        t.printStackTrace();
                    }

                }
            }
        };
        controller.definirCallback(aConsumer);
        stage.show();
    }

    private void atualizarCategoriasFromHistorico(Transacao  transacao){
        String descr = transacao.getDescricao();
        String possivelData = descr.length() > 8 ?
                (descr.substring(descr.length()-5,descr.length())).replaceAll("\\s+", " ").trim().toUpperCase()
                :
                descr.replaceAll("\\s+", " ").trim().toUpperCase();
        boolean hasData = validate(possivelData);
        if(hasData){
            possivelData = descr.substring(0,descr.length()-5).replaceAll("\\s+", " ").trim().toUpperCase();
        }else{
            possivelData = descr;
        }
        possivelData = possivelData.trim();

        _categoriasDefinidas.put(possivelData,transacao.getCategoria());
    }

    private void definirCategoriasFromHistorico(List<Transacao>  transacoes){
        transacoes.stream().filter(p->p.getCategoria()==null).forEach(p->{

            String descr = p.getDescricao();
            String possivelData = descr.length() > 8 ?
                    (descr.substring(descr.length()-5,descr.length())).replaceAll("\\s+", " ").trim().toUpperCase()
                    :
                    descr.replaceAll("\\s+", " ").trim().toUpperCase();
            boolean hasData = validate(possivelData);
            if(hasData){
                possivelData = descr.substring(0,descr.length()-5).replaceAll("\\s+", " ").trim().toUpperCase();
            }else{
                possivelData = descr;
            }
            possivelData = possivelData.trim();

            Categoria c = _categoriasDefinidas.get(possivelData);
            if(c!=null){
                p.setCategoria(c);
            }
        });
    }

    public TransacaoService getTransacaoService() {
        return transacaoService;
    }

    public void setTransacaoService(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    public static void main(String[] args) {
        String descr1 = "RSHOP-BADARO  SUCO-30/01";
        String descr2 = "RSHOP-BADARO       SUCO-02/02";
        descr1 = descr1.replaceAll("\\s+", " ");
        descr2 = descr2.replaceAll("\\s+", " ");
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance(descr1.length());
        Integer distance = levenshteinDistance.apply(descr1,descr2);
        System.out.println("[" + descr1 + "] x [" + descr2  + "] = " + distance);
    }

}

