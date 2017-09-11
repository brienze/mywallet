package br.com.mywallet.ui;

import br.com.mywallet.Application;
import br.com.mywallet.model.Categoria;
import br.com.mywallet.model.CategoriaBuilder;
import br.com.mywallet.model.Transacao;
import br.com.mywallet.service.CategoriaService;
import br.com.mywallet.service.TransacaoService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RelatorioController{

    private LocalDate mesAtual;

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private CategoriaService categoriaService;

    List<Transacao> _transacoes;
    static final Logger logger = LoggerFactory.getLogger(RelatorioController.class);

    private List<Transacao> _listaTransacoes;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private Label lblDataAtual;

    @FXML
    private LineChart<?, ?> lineChartCreditoDebito;

    @FXML
    CategoryAxis xAxis, lineChartSubCategoriasXAxis, xAxisEssencial;
    @FXML
    NumberAxis yAxis, lineChartSubCategoriasYAxis, yAxisEssencial;

    @FXML
    private LineChart<?, ?> lineChartCategorias;

    @FXML
    private LineChart<?,?> lineChartCatSub,lineChartSubCategorias,lineChartDebitosEssenciais;
    @FXML
    private PieChart pieChartSubcategorias;

    @FXML
    private PieChart pieChartCategorias, pieChartSubcategoriasAssociada;

    Map<YearMonth, Long> debitosCCPorMes;
    Map<YearMonth, Long> debitosEssenciaisPorMes;
    Map<YearMonth, Long> debitosNaoEssenciaisPorMes;
    Map<YearMonth, Long> debitosCartaoPorMes;
    Map<YearMonth, Long> creditoPorMes;
    Map<YearMonth, Long> debitosPorMes;

    boolean _isEssencial = false;

    public RelatorioController(){
        executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
    }


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        DateTimeFormatter formatadorMes =
                DateTimeFormatter.ofPattern("dd/MM/YYYY");

        lblDataAtual.setText(formatadorMes.format(LocalDate.now()));

        _listaTransacoes = transacaoService.findAll();

        definirValores();
        defineCreditoDebitoChart();
        defineEssencialDebitoCreditoChart();
        defineCategoriasChart();
        defineSubCategoriasChart();

        pieChartSubcategoriasAssociada.setTitle("<- SubCategorias");
        pieChartSubcategoriasAssociada.setLabelLineLength(10);
        pieChartSubcategorias.setTitle("SubCategorias");
        pieChartSubcategorias.setLabelLineLength(10);
        pieChartCategorias.setTitle("Categorias");
        pieChartCategorias.setLabelLineLength(10);
        //pieChartCategorias.setLegendSide(Side.BOTTOM);
    }

    public void defineCreditoDebitoChart(){
        xAxis.setLabel("Mês");
        lineChartCreditoDebito.setTitle("Créditos e Débitos");
        lineChartCreditoDebito.setCursor(Cursor.CROSSHAIR);

        BiConsumer<String, YearMonth> umclique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                singleClickAction(key,yearMonth);
            }
        };

        BiConsumer<String, YearMonth> duploClique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                doubleClickAction(key,yearMonth);
            }
        };

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Débito");
        debitosPorMes.keySet().stream().sorted().forEach(k->
                {
                    final XYChart.Data<String, Long> data = new XYChart.Data(k.toString(), debitosPorMes.get(k));

                    data.setNode(new HoveredThresholdNode("Debito",k,debitosPorMes.get(k), umclique, duploClique));
                    series1.getData().add(data);}
        );

        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Crédito");
        creditoPorMes.keySet().stream().sorted().forEach(k->
        {
            final XYChart.Data<String, Long> data = new XYChart.Data(k.toString(), creditoPorMes.get(k));

            data.setNode(new HoveredThresholdNode("Credito",k,creditoPorMes.get(k),umclique, duploClique));
            series2.getData().add(data);}
        );

        XYChart.Series series3 = new XYChart.Series();
        series3.setName("Investimento");
        creditoPorMes.keySet().stream().sorted().forEach(k->
                {
                    long diferenca = creditoPorMes.get(k) - debitosPorMes.get(k);
                    if(diferenca < 0){
                        diferenca = 0;
                    }
                    final XYChart.Data<String, Long> data = new XYChart.Data(k.toString(), diferenca);

                    data.setNode(new HoveredThresholdNode("Investimento",k,diferenca,umclique, duploClique));
                    series3.getData().add(data);}
        );

        lineChartCreditoDebito.getData().addAll(series1, series2,series3);
    }

    public void defineEssencialDebitoCreditoChart(){
        xAxisEssencial.setLabel("Mês");
        lineChartDebitosEssenciais.setTitle("Gastos (Não)Essenciais x Creditos");
        lineChartDebitosEssenciais.setCursor(Cursor.CROSSHAIR);

        BiConsumer<String, YearMonth> umclique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                singleClickAction(key,yearMonth);
                singleClickActionEssencialCategoria(key,yearMonth);

            }
        };

        BiConsumer<String, YearMonth> duploClique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                doubleClickEssencialAction(key,yearMonth);
            }
        };

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Débito Essencial");
        debitosEssenciaisPorMes.keySet().stream().sorted().forEach(k->
                {
                    final XYChart.Data<String, Long> data = new XYChart.Data(k.toString(), debitosEssenciaisPorMes.get(k));

                    data.setNode(new HoveredThresholdNode("Debito Essencial",k,debitosEssenciaisPorMes.get(k), umclique, duploClique));
                    series1.getData().add(data);}
        );

        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Débito Não Essencial");
        debitosNaoEssenciaisPorMes.keySet().stream().sorted().forEach(k->
                {
                    final XYChart.Data<String, Long> data = new XYChart.Data(k.toString(), debitosNaoEssenciaisPorMes.get(k));

                    data.setNode(new HoveredThresholdNode("Debito Não Essencial",k,debitosNaoEssenciaisPorMes.get(k), umclique, duploClique));
                    series2.getData().add(data);}
        );

        XYChart.Series series3 = new XYChart.Series();
        series3.setName("Crédito");
        creditoPorMes.keySet().stream().sorted().forEach(k->
                {
                    final XYChart.Data<String, Long> data = new XYChart.Data(k.toString(), creditoPorMes.get(k));

                    data.setNode(new HoveredThresholdNode("Credito",k,creditoPorMes.get(k),umclique, duploClique));
                    series3.getData().add(data);}
        );

        lineChartDebitosEssenciais.getData().addAll(series1, series2,series3);
    }

    public void defineCategoriasChart(){
        xAxis.setLabel("Mês");
        lineChartCategorias.setTitle("Categorias");
        lineChartCategorias.setCursor(Cursor.CROSSHAIR);

        Function<Transacao, YearMonth> collect = t->YearMonth.from(t.getData());

        Map<YearMonth,Map<Categoria,Long>> debitos = _listaTransacoes.stream()
                .filter(t-> t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null )
                .collect(Collectors.groupingBy(collect,Collectors.groupingBy(t->t.getCategoria().getCategoriaPai(),Collectors.summingLong(Transacao::getValorLong))));

        Map<String,XYChart.Series> listaSeries = new HashMap<>();

        BiConsumer<String, YearMonth> umclique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                singleClickActionCategoria(key,yearMonth);
                singleClickActionSubCategoria(key,yearMonth);
            }
        };

        BiConsumer<String, YearMonth> duploClique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                doubleClickActionCategorias(key,yearMonth);
            }
        };

        debitos.keySet().stream().sorted().forEach(year->{
            Map<Categoria, Long> categoria = debitos.get(year);
            categoria.keySet().stream().forEach(categ->{
                Long qtde = categoria.get(categ);
                final XYChart.Series series = listaSeries.get(categ.getNome());
                if(series == null){
                    final XYChart.Series serie = new XYChart.Series();
                    serie.setName(categ.getNome());
                    listaSeries.put(categ.getNome(),serie);
                }
                final XYChart.Series series2 = listaSeries.get(categ.getNome());

                final XYChart.Data<String, Long> data = new XYChart.Data(year.toString(), qtde);
                data.setNode(new HoveredThresholdNode(categ.getNome(),year,qtde,umclique, duploClique));
                series2.getData().add(data);
            });
        });

        listaSeries.values().stream().forEach(aSerie->lineChartCategorias.getData().add(aSerie));

    }

    public void defineSubCategoriasChart(){
        xAxis.setLabel("Mês");
        lineChartSubCategorias.setTitle("SubCategorias");
        lineChartSubCategorias.setCursor(Cursor.CROSSHAIR);

        Function<Transacao, YearMonth> collect = t->YearMonth.from(t.getData());

        Map<YearMonth,Map<Categoria,Long>> debitos = _listaTransacoes.stream()
                .filter(t-> t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null )
                .sorted((t1,t2)->t1.getData().compareTo(t2.getData())).collect(Collectors.groupingBy(collect,Collectors.groupingBy(t->t.getCategoria(),Collectors.summingLong(Transacao::getValorLong))));

        Map<String,XYChart.Series> listaSeries = new HashMap<>();

        BiConsumer<String, YearMonth> umclique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                singleClickActionCategoria(key,yearMonth);
                singleClickActionSubCategoria(key,yearMonth);
            }
        };

        BiConsumer<String, YearMonth> duploClique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                doubleClickActionCategorias(key,yearMonth);
            }
        };

        debitos.keySet().stream().sorted().forEach(year->{
            Map<Categoria, Long> categoria = debitos.get(year);
            categoria.keySet().stream().forEach(categ->{
                Long qtde = categoria.get(categ);
                final XYChart.Series series = listaSeries.get(categ.getNome());
                if(series == null){
                    final XYChart.Series serie = new XYChart.Series();
                    serie.setName(categ.getNome());
                    listaSeries.put(categ.getNome(),serie);
                }
                final XYChart.Series series2 = listaSeries.get(categ.getNome());
                DateTimeFormatter formatadorMes = DateTimeFormatter.ofPattern("MMMM");
                final String dataTrn = formatadorMes.format(year.atEndOfMonth());

                final XYChart.Data<YearMonth, Long> data = new XYChart.Data(dataTrn, qtde);
                data.setNode(new HoveredThresholdNode(categ.getNome(),year,qtde,umclique, duploClique));
                series2.getData().add(data);
            });
        });

        listaSeries.values().stream().forEach(aSerie->lineChartSubCategorias.getData().add(aSerie));

    }

    public void defineSubcategoriaPieChart(){

    }

    private void definirValores(){
        Function<Transacao,YearMonth> collectContaCorrente = t->YearMonth.from(t.getData());
        Function<Transacao,YearMonth> collectCartaoCredito = t->YearMonth.from(t.getData());

        debitosCCPorMes =
                _listaTransacoes.stream()
                        .filter(t-> t.getIsDebito() && t.isContabiliza() && t.getConta().getTipoConta().equals(1))//conta corrente
                        .collect(Collectors.groupingBy(collectContaCorrente,Collectors.summingLong(Transacao::getValorLong)));


         debitosCartaoPorMes =
                _listaTransacoes.stream()
                        .filter(t-> t.getIsDebito() && t.isContabiliza() && t.getConta().getTipoConta().equals(2))//cartao credito
                        .collect(Collectors.groupingBy(collectCartaoCredito,Collectors.summingLong(Transacao::getValorLong)));

        debitosEssenciaisPorMes =
                _listaTransacoes.stream()
                        .filter(t-> t.getIsDebito() && t.isContabiliza() && (t.getCategoria() !=null && t.getCategoria().getEssencial()))
                        .collect(Collectors.groupingBy(collectCartaoCredito,Collectors.summingLong(Transacao::getValorLong)));


        debitosNaoEssenciaisPorMes =
                _listaTransacoes.stream()
                        .filter(t-> t.getIsDebito() && t.isContabiliza() && (t.getCategoria() !=null && !t.getCategoria().getEssencial()))
                        .collect(Collectors.groupingBy(collectCartaoCredito,Collectors.summingLong(Transacao::getValorLong)));


        creditoPorMes =
                _listaTransacoes.stream()
                        .filter(t-> t.getIsDebito() == false && t.isContabiliza())//credito
                        .collect(Collectors.groupingBy(collectContaCorrente,Collectors.summingLong(Transacao::getValorLong)));

        Map<YearMonth, YearMonth> datas = new HashMap<>();

        debitosCCPorMes.keySet().stream().forEach(p->datas.putIfAbsent(p,p));
        debitosCartaoPorMes.keySet().stream().forEach(p->datas.putIfAbsent(p,p));

        debitosPorMes = new HashMap<>();
        datas.keySet().stream().sorted().forEach((k)-> {
            debitosPorMes.put(k,(debitosCCPorMes.get(k)==null?0:debitosCCPorMes.get(k)) + (debitosCartaoPorMes.get(k)==null?0:debitosCartaoPorMes.get(k)) );
                }
        );
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        List<Categoria> categorias = categoriaService.findByCategoriaPaiIsNull();

        if (categorias.size() == 0) {
            categorias = CategoriaBuilder.instance().obterCategorias();
            categoriaService.saveAll(categorias);
            categorias = categoriaService.findByCategoriaPaiIsNull();
        }
    }

    boolean dragFlag = false;

    int clickCounter = 0;

    ScheduledThreadPoolExecutor executor;

    ScheduledFuture<?> scheduledFuture;


    public TransacaoService getTransacaoService() {
        return transacaoService;
    }

    public void setTransacaoService(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }


    /** a node which displays a value on hover, but is otherwise empty */
    class HoveredThresholdNode extends StackPane {
        private YearMonth minhaData;
        private BiConsumer<String, YearMonth> umClique;
        private BiConsumer<String, YearMonth> duploClique;
        private String key;
        public YearMonth getMinhaData(){
            return this.minhaData;
        }
        public void setMinhaData(YearMonth data){
            this.minhaData = data;
        }



        HoveredThresholdNode(String key, YearMonth data, long value, BiConsumer<String, YearMonth> umclique, BiConsumer<String, YearMonth> duploclique) {
            this.umClique = umclique;
            this.duploClique = duploclique;
            this.key = key;

            setMinhaData(data);
            setPrefSize(15, 15);

            final Label label = createDataThresholdLabel(key + "-" + value);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                }
            });
            //setOnMouseClicked(new MouseClickedEvent());
            setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    if (e.getButton().equals(MouseButton.PRIMARY)) {
                        dragFlag = true;
                    }
                }
            });

            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    if (e.getButton().equals(MouseButton.PRIMARY)) {
                        if (!dragFlag) {
                            //System.out.println(++clickCounter + " " + e.getClickCount());
                            if (e.getClickCount() == 1) {
                                scheduledFuture = executor.schedule(() -> umClique.accept(key,getMinhaData()), 500, TimeUnit.MILLISECONDS);
                            } else if (e.getClickCount() > 1) {
                                if (scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone()) {
                                    scheduledFuture.cancel(false);
                                    duploClique.accept(key,getMinhaData());
                                }
                            }
                        }
                        dragFlag = false;
                    }
                }
            });
        }



        private Label createDataThresholdLabel(String value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");

            /*if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            } else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            } else {
                label.setTextFill(Color.FIREBRICK);
            }*/
            label.setTextFill(Color.FIREBRICK);

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }


    }

    public void singleClickAction(String key, YearMonth data) {
        System.out.println("Single-click action executed.");
    }

    public void doubleClickEssencialAction(String key, YearMonth data) {
        System.out.println("Data: " + data);
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetalheCreditoDebito.fxml"));
        try {
            loader.setControllerFactory(Application.getContext()::getBean);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        Stage stage = new Stage();


        stage.initOwner(lineChartDebitosEssenciais.getScene().getWindow());

        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
        stage.initStyle(StageStyle.DECORATED);

        DetalheCreditoDebitoController controller = loader.<DetalheCreditoDebitoController>getController();
        List<Transacao> debitosEssenciais = _listaTransacoes.stream().filter(p->{
            boolean dataIgual = false;
            dataIgual = p.isContabiliza() && p.getIsDebito() && p.getCategoria()!=null && p.getCategoria().getEssencial() && YearMonth.from(p.getData()).equals(data);
            return dataIgual;
        }).collect(Collectors.toList());
        List<Transacao> debitosNaoEssenciais = _listaTransacoes.stream().filter(p->{
            boolean dataIgual = false;
            dataIgual = p.isContabiliza() && p.getIsDebito() && p.getCategoria()!=null && !p.getCategoria().getEssencial() && YearMonth.from(p.getData()).equals(data);
            return dataIgual;
        }).collect(Collectors.toList());
        List<Transacao> creditos = _listaTransacoes.stream().filter(p->p.isContabiliza() && p.getIsDebito()==false && YearMonth.from(p.getData()).equals(data)).collect(Collectors.toList());

        controller.initData("Débitos Essencial","Débitos Não Essencial","Créditos",debitosEssenciais,debitosNaoEssenciais,creditos);
        stage.show();
    }

    public void doubleClickAction(String key, YearMonth data) {
        System.out.println("Data: " + data);
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetalheCreditoDebito.fxml"));
        try {
            loader.setControllerFactory(Application.getContext()::getBean);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        Stage stage = new Stage();


        stage.initOwner(lineChartCreditoDebito.getScene().getWindow());

        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
        stage.initStyle(StageStyle.DECORATED);

        DetalheCreditoDebitoController controller = loader.<DetalheCreditoDebitoController>getController();
        List<Transacao> debitosContaCorrente = _listaTransacoes.stream().filter(p->{
            boolean dataIgual = false;
            dataIgual = p.isContabiliza() && p.getIsDebito() && p.getConta().getTipoConta().equals(1) && YearMonth.from(p.getData()).equals(data);
            return dataIgual;
        }).collect(Collectors.toList());
        List<Transacao> debitosCartao = _listaTransacoes.stream().filter(p->p.isContabiliza() && p.getIsDebito() && p.getConta().getTipoConta().equals(2) && YearMonth.from(p.getData()).equals(data)).collect(Collectors.toList());
        List<Transacao> creditos = _listaTransacoes.stream().filter(p->p.isContabiliza() && p.getIsDebito()==false && YearMonth.from(p.getData()).equals(data)).collect(Collectors.toList());

        controller.initData("Débitos Conta Corrente","Débitos Cartão Crédito","Créditos",debitosContaCorrente,debitosCartao,creditos);
        stage.show();
    }

    /**
     * Grafico de Pizza das Subcategorias
     * @param categoriaPai
     * @param data
     */
    public void singleClickActionSubCategoria(String categoriaPai, YearMonth data){
        pieChartSubcategorias.getData().clear();
        pieChartSubcategorias.setLegendVisible(false);
        Map<Categoria,Long> categoriasValores = _listaTransacoes.stream()
                .filter(t-> YearMonth.from(t.getData()).equals(data) && t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null )
                .collect(Collectors.groupingBy(t->t.getCategoria(),Collectors.summingLong(Transacao::getValorLong)));


        ObservableList<PieChart.Data> pieChartData = FXCollections.emptyObservableList();
        List<PieChart.Data> listaDataPie = new ArrayList<>();
        categoriasValores.keySet().stream().forEach(c-> {
                    final Long value = categoriasValores.get(c);
                    final String nomeCategoria = c.getNome();
                    final PieChart.Data dataPie = new PieChart.Data(nomeCategoria, value);
                    listaDataPie.add(dataPie);
                }
        );

        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 10 arial;");


       // Platform.runLater(()->{
            for (final PieChart.Data dataPie : listaDataPie) {
                dataPie.nameProperty().bind(
                        Bindings.concat(
                                dataPie.getName(), " ", dataPie.pieValueProperty()
                        )
                );
            }
        //});

        Platform.runLater(()->{
            pieChartSubcategorias.getData().addAll(listaDataPie);
            for (final PieChart.Data dataPie : pieChartSubcategorias.getData()) {
                dataPie.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                        new EventHandler<MouseEvent>() {
                            @Override public void handle(MouseEvent e) {


                                caption.setTranslateX(e.getSceneX());
                                caption.setTranslateY(e.getSceneY());
                                caption.setVisible(true);
                                caption.setText(String.valueOf(dataPie.getPieValue()) + "%");
                            }
                        });
            }
        });

    }
    private YearMonth dataSelecionada;

    YearMonth getDataSelecionada(){
        return dataSelecionada;
    }

    /**
     * Grafico de Pizza das categorias
     * @param tipo Essencial ou não
     * @param data
     */
    public void singleClickActionEssencialCategoria(String tipo, YearMonth data){

        pieChartSubcategoriasAssociada.getData().clear();

        pieChartCategorias.getData().clear();
        pieChartCategorias.setLegendVisible(false);
        dataSelecionada = data;

        Map<Categoria,Long> categoriasValoresTemp = null;
        if(tipo.equals("Credito")){
            categoriasValoresTemp = _listaTransacoes.stream()
                    .filter(t-> YearMonth.from(t.getData()).equals(data) && t.getIsDebito() == false && t.isContabiliza() && t.getCategoria() != null
                    )
                    .collect(Collectors.groupingBy(t->t.getCategoria().getCategoriaPai(),Collectors.summingLong(Transacao::getValorLong)));

        }else{
            boolean isEssencial = tipo.equals("Debito Essencial");
            categoriasValoresTemp = _listaTransacoes.stream()
                    .filter(t-> YearMonth.from(t.getData()).equals(data) && t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null && t.getCategoria().getEssencial() == isEssencial
                    )
                    .collect(Collectors.groupingBy(t->t.getCategoria().getCategoriaPai(),Collectors.summingLong(Transacao::getValorLong)));
            _isEssencial = isEssencial;
        }

        final Map<Categoria,Long> categoriasValores = categoriasValoresTemp;
        List<PieChart.Data> listaDataPie = new ArrayList<>();
        categoriasValores.keySet().stream().forEach(c-> {
                    final Long value = categoriasValores.get(c);
                    final String nomeCategoria = c.getNome();
                    final PieChart.Data dataPie = new PieChart.Data(nomeCategoria, value);
                    listaDataPie.add(dataPie);
                }
        );

        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 10 arial;");
        //Platform.runLater(()->{
        for (final PieChart.Data dataPie : listaDataPie) {
            dataPie.nameProperty().bind(
                    Bindings.concat(
                            dataPie.getName()," - ",dataPie.getPieValue()
                    )
            );
        }
        // });

        Platform.runLater(()->{
            pieChartCategorias.getData().addAll(listaDataPie);
            for (final PieChart.Data dataPie : listaDataPie) {
                dataPie.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                        new EventHandler<MouseEvent>() {
                            @Override public void handle(MouseEvent e) {
                                //mostrarSubCategorias(dataPie.getName(),getDataSelecionada());
                                mostrarSubCategoriasEssencial(dataPie.getName(),getDataSelecionada());

                                caption.setTranslateX(e.getSceneX());
                                caption.setTranslateY(e.getSceneY());
                                caption.setVisible(true);
                                caption.setText(String.valueOf(dataPie.getPieValue()) + "%");
                            }
                        });
            }
        });

    }

    /**
     * Grafico de Pizza das categorias
     * @param categoriaPai
     * @param data
     */
    public void singleClickActionCategoria(String categoriaPai, YearMonth data){
        pieChartSubcategoriasAssociada.getData().clear();

        pieChartCategorias.getData().clear();
        pieChartCategorias.setLegendVisible(false);
        dataSelecionada = data;
        Map<Categoria,Long> categoriasValores = _listaTransacoes.stream()
                .filter(t-> YearMonth.from(t.getData()).equals(data) && t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null )
                .collect(Collectors.groupingBy(t->t.getCategoria().getCategoriaPai(),Collectors.summingLong(Transacao::getValorLong)));

        List<PieChart.Data> listaDataPie = new ArrayList<>();
        categoriasValores.keySet().stream().forEach(c-> {
            final Long value = categoriasValores.get(c);
            final String nomeCategoria = c.getNome();
            final PieChart.Data dataPie = new PieChart.Data(nomeCategoria, value);
            listaDataPie.add(dataPie);
        }
        );

        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 10 arial;");
        //Platform.runLater(()->{
            for (final PieChart.Data dataPie : listaDataPie) {
                dataPie.nameProperty().bind(
                        Bindings.concat(
                                dataPie.getName()
                        )
                );
            }
       // });

        Platform.runLater(()->{
            pieChartCategorias.getData().addAll(listaDataPie);
            for (final PieChart.Data dataPie : listaDataPie) {
                dataPie.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                        new EventHandler<MouseEvent>() {
                            @Override public void handle(MouseEvent e) {
                                mostrarSubCategorias(dataPie.getName(),getDataSelecionada());


                                caption.setTranslateX(e.getSceneX());
                                caption.setTranslateY(e.getSceneY());
                                caption.setVisible(true);
                                caption.setText(String.valueOf(dataPie.getPieValue()) + "%");
                            }
                        });
            }
        });

    }

    /**
     * Grafico de Pizza das Subcategorias
     * @param categoriaPai
     * @param data
     */
    public void mostrarSubCategoriasEssencial(String categoriaPai, YearMonth data){
        categoriaPai = categoriaPai.substring(0,categoriaPai.indexOf(" - "));
        pieChartSubcategoriasAssociada.getData().clear();
        pieChartSubcategoriasAssociada.setLegendVisible(false);

        final String nomeCategoriaPai = categoriaPai;

        Map<Categoria,Long> categoriasValoresTemp = null;
        if(categoriaPai.equals("Credito")){
            categoriasValoresTemp = _listaTransacoes.stream()
                    .filter(t-> YearMonth.from(t.getData()).equals(data) && t.getIsDebito() == false  && t.getCategoria() != null && t.getCategoria().getCategoriaPai().getNome().equals(nomeCategoriaPai)
                    )
                    .collect(Collectors.groupingBy(t->t.getCategoria(),Collectors.summingLong(Transacao::getValorLong)));

        }else{

            categoriasValoresTemp = _listaTransacoes.stream()
                    .filter(t-> {
                        boolean isEssencial = (t.getCategoria() != null)?t.getCategoria().getEssencial()==_isEssencial:false;
                        boolean isCategoria = isEssencial?t.getCategoria().getCategoriaPai().getNome().equals(nomeCategoriaPai):false;
                        if(isCategoria && isEssencial && YearMonth.from(t.getData()).equals(data)){
                            System.out.println("");
                        }
                        return isEssencial && isCategoria && YearMonth.from(t.getData()).equals(data) && t.getIsDebito() && t.isContabiliza();
                            }
                    )
                    .collect(Collectors.groupingBy(t->t.getCategoria(),Collectors.summingLong(Transacao::getValorLong)));

        }

        final Map<Categoria,Long> categoriasValores = categoriasValoresTemp;

        ObservableList<PieChart.Data> pieChartData = FXCollections.emptyObservableList();
        List<PieChart.Data> listaDataPie = new ArrayList<>();
        categoriasValores.keySet().stream().forEach(c-> {
                    final Long value = categoriasValores.get(c);
                    final String nomeCategoria = c.getNome();
                    final PieChart.Data dataPie = new PieChart.Data(nomeCategoria, value);
                    listaDataPie.add(dataPie);
                }
        );



        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 10 arial;");


        // Platform.runLater(()->{
        for (final PieChart.Data dataPie : listaDataPie) {
            dataPie.nameProperty().bind(
                    Bindings.concat(
                            dataPie.getName(), " ", dataPie.pieValueProperty()
                    )
            );
        }
        // });





        Platform.runLater(()->{
            pieChartSubcategoriasAssociada.getData().addAll(listaDataPie);
            for (final PieChart.Data dataPie : pieChartSubcategoriasAssociada.getData()) {
                dataPie.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                        new EventHandler<MouseEvent>() {
                            @Override public void handle(MouseEvent e) {
                                mostrarSubCategoriasLineChart(dataPie.getName(),getDataSelecionada());

                                caption.setTranslateX(e.getSceneX());
                                caption.setTranslateY(e.getSceneY());
                                caption.setVisible(true);
                                caption.setText(String.valueOf(dataPie.getPieValue()) + "%");
                            }
                        });
            }
        });

        //mostrar categorias ao longo do tempo
        //lineChartCatSub
        lineChartCatSub.getData().clear();

        lineChartSubCategoriasXAxis.setLabel("Mês");
        lineChartCatSub.setTitle("Categorias");
        lineChartCatSub.setCursor(Cursor.CROSSHAIR);

        Function<Transacao, YearMonth> collect = t->YearMonth.from(t.getData());

        Map<YearMonth,Map<Categoria,Long>> debitos = _listaTransacoes.stream()
                .filter(t-> t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null && t.getCategoria().getCategoriaPai().getNome().equals(nomeCategoriaPai))
                .collect(Collectors.groupingBy(collect,Collectors.groupingBy(t->t.getCategoria().getCategoriaPai(),Collectors.summingLong(Transacao::getValorLong))));

        Map<String,XYChart.Series> listaSeries = new HashMap<>();

        BiConsumer<String, YearMonth> umclique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                singleClickActionCategoria(key,yearMonth);
                singleClickActionSubCategoria(key,yearMonth);
            }
        };

        BiConsumer<String, YearMonth> duploClique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                doubleClickActionCategorias(key,yearMonth);
            }
        };

        debitos.keySet().stream().sorted().forEach(year->{
            Map<Categoria, Long> categoria = debitos.get(year);
            categoria.keySet().stream().forEach(categ->{
                Long qtde = categoria.get(categ);
                final XYChart.Series series = listaSeries.get(categ.getNome());
                if(series == null){
                    final XYChart.Series serie = new XYChart.Series();
                    serie.setName(categ.getNome());
                    listaSeries.put(categ.getNome(),serie);
                }
                final XYChart.Series series2 = listaSeries.get(categ.getNome());

                final XYChart.Data<String, Long> data2 = new XYChart.Data(year.toString(), qtde);
                data2.setNode(new HoveredThresholdNode(categ.getNome(),year,qtde,umclique, duploClique));
                series2.getData().add(data2);
            });
        });

        listaSeries.values().stream().forEach(aSerie->lineChartCatSub.getData().add(aSerie));


    }

    /**
     * Grafico de Pizza das Subcategorias
     * @param categoriaPai
     * @param data
     */
    public void mostrarSubCategorias(String categoriaPai, YearMonth data){
        pieChartSubcategoriasAssociada.getData().clear();
        pieChartSubcategoriasAssociada.setLegendVisible(false);
        Map<Categoria,Long> categoriasValores = _listaTransacoes.stream()
                .filter(t-> YearMonth.from(t.getData()).equals(data) && t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null && t.getCategoria().getCategoriaPai().getNome().equals(categoriaPai))
                .collect(Collectors.groupingBy(t->t.getCategoria(),Collectors.summingLong(Transacao::getValorLong)));


        ObservableList<PieChart.Data> pieChartData = FXCollections.emptyObservableList();
        List<PieChart.Data> listaDataPie = new ArrayList<>();
        categoriasValores.keySet().stream().forEach(c-> {
                    final Long value = categoriasValores.get(c);
                    final String nomeCategoria = c.getNome();
                    final PieChart.Data dataPie = new PieChart.Data(nomeCategoria, value);
                    listaDataPie.add(dataPie);
                }
        );



        final Label caption = new Label("");
        caption.setTextFill(Color.DARKORANGE);
        caption.setStyle("-fx-font: 10 arial;");


       // Platform.runLater(()->{
            for (final PieChart.Data dataPie : listaDataPie) {
                dataPie.nameProperty().bind(
                        Bindings.concat(
                                dataPie.getName(), " ", dataPie.pieValueProperty()
                        )
                );
            }
       // });





        Platform.runLater(()->{
            pieChartSubcategoriasAssociada.getData().addAll(listaDataPie);
            for (final PieChart.Data dataPie : pieChartSubcategoriasAssociada.getData()) {
                dataPie.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
                        new EventHandler<MouseEvent>() {
                            @Override public void handle(MouseEvent e) {
                                mostrarSubCategoriasLineChart(dataPie.getName(),getDataSelecionada());

                                caption.setTranslateX(e.getSceneX());
                                caption.setTranslateY(e.getSceneY());
                                caption.setVisible(true);
                                caption.setText(String.valueOf(dataPie.getPieValue()) + "%");
                            }
                        });
            }
        });

        //mostrar categorias ao longo do tempo
        //lineChartCatSub
        lineChartCatSub.getData().clear();

        lineChartSubCategoriasXAxis.setLabel("Mês");
        lineChartCatSub.setTitle("Categorias");
        lineChartCatSub.setCursor(Cursor.CROSSHAIR);

        Function<Transacao, YearMonth> collect = t->YearMonth.from(t.getData());

        Map<YearMonth,Map<Categoria,Long>> debitos = _listaTransacoes.stream()
                .filter(t-> t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null && t.getCategoria().getCategoriaPai().getNome().equals(categoriaPai))
                .collect(Collectors.groupingBy(collect,Collectors.groupingBy(t->t.getCategoria().getCategoriaPai(),Collectors.summingLong(Transacao::getValorLong))));

        Map<String,XYChart.Series> listaSeries = new HashMap<>();

        BiConsumer<String, YearMonth> umclique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                singleClickActionCategoria(key,yearMonth);
                singleClickActionSubCategoria(key,yearMonth);
            }
        };

        BiConsumer<String, YearMonth> duploClique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                doubleClickActionCategorias(key,yearMonth);
            }
        };

        debitos.keySet().stream().sorted().forEach(year->{
            Map<Categoria, Long> categoria = debitos.get(year);
            categoria.keySet().stream().forEach(categ->{
                Long qtde = categoria.get(categ);
                final XYChart.Series series = listaSeries.get(categ.getNome());
                if(series == null){
                    final XYChart.Series serie = new XYChart.Series();
                    serie.setName(categ.getNome());
                    listaSeries.put(categ.getNome(),serie);
                }
                final XYChart.Series series2 = listaSeries.get(categ.getNome());

                final XYChart.Data<String, Long> data2 = new XYChart.Data(year.toString(), qtde);
                data2.setNode(new HoveredThresholdNode(categ.getNome(),year,qtde,umclique, duploClique));
                series2.getData().add(data2);
            });
        });

        listaSeries.values().stream().forEach(aSerie->lineChartCatSub.getData().add(aSerie));


    }

    /**
     * Grafico de Pizza das Subcategorias
     * @param categoriaNome
     * @param data
     */
    public void mostrarSubCategoriasLineChart(String categoriaNome, YearMonth data){

        categoriaNome=categoriaNome.replaceAll("[*0-9]", "");
        categoriaNome=categoriaNome.replaceAll("\\.", "");
        categoriaNome=categoriaNome.replaceAll(",", "");

        final String nomeCategoria = categoriaNome.trim();

        //mostrar categorias ao longo do tempo

        lineChartCatSub.getData().clear();

        lineChartSubCategoriasXAxis.setLabel("Mês");
        lineChartCatSub.setTitle("SubCategorias");
        lineChartCatSub.setCursor(Cursor.CROSSHAIR);

        Function<Transacao, YearMonth> collect = t->YearMonth.from(t.getData());

        Map<YearMonth,Map<Categoria,Long>> debitos = _listaTransacoes.stream()
                .filter(t-> t.getIsDebito() && t.isContabiliza() && t.getCategoria() != null && t.getCategoria().getNome().equals(nomeCategoria))
                .collect(Collectors.groupingBy(collect,Collectors.groupingBy(t->t.getCategoria(),Collectors.summingLong(Transacao::getValorLong))));

        Map<String,XYChart.Series> listaSeries = new HashMap<>();

        BiConsumer<String, YearMonth> umclique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                singleClickActionCategoria(key,yearMonth);
                singleClickActionSubCategoria(key,yearMonth);
            }
        };

        BiConsumer<String, YearMonth> duploClique = new BiConsumer<String, YearMonth>() {
            @Override
            public void accept(String key, YearMonth yearMonth) {
                doubleClickActionCategorias(key,yearMonth);
            }
        };

        debitos.keySet().stream().sorted().forEach(year->{
            Map<Categoria, Long> categoria = debitos.get(year);
            categoria.keySet().stream().forEach(categ->{
                Long qtde = categoria.get(categ);
                final XYChart.Series series = listaSeries.get(categ.getNome());
                if(series == null){
                    final XYChart.Series serie = new XYChart.Series();
                    serie.setName(categ.getNome());
                    listaSeries.put(categ.getNome(),serie);
                }
                final XYChart.Series series2 = listaSeries.get(categ.getNome());

                final XYChart.Data<String, Long> data2 = new XYChart.Data(year.toString(), qtde);
                data2.setNode(new HoveredThresholdNode(categ.getNome(),year,qtde,umclique, duploClique));
                series2.getData().add(data2);
            });
        });

        listaSeries.values().stream().forEach(aSerie->lineChartCatSub.getData().add(aSerie));


    }

    public void doubleClickActionCategorias(String categoria, YearMonth data) {
        System.out.println("Data: " + data);
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetalheCreditoDebito.fxml"));
        try {
            loader.setControllerFactory(Application.getContext()::getBean);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        Stage stage = new Stage();


        stage.initOwner(lineChartCreditoDebito.getScene().getWindow());

        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
        stage.initStyle(StageStyle.DECORATED);

        DetalheCreditoDebitoController controller = loader.<DetalheCreditoDebitoController>getController();
        List<Transacao> debitosContaCorrente = _listaTransacoes.stream().filter(p->{
            boolean dataIgual = false;
            String nomeCategoria = (p.getCategoria()==null?"null":p.getCategoria().getCategoriaPai().getNome());
            System.out.println(nomeCategoria );
            dataIgual = p.isContabiliza() && p.getIsDebito() && p.getCategoria() != null && p.getConta().getTipoConta().equals(1) && p.getCategoria().getCategoriaPai().getNome().equals(categoria)  && YearMonth.from(p.getData()).equals(data);
            return dataIgual;
        }).collect(Collectors.toList());
        List<Transacao> debitosCartao = _listaTransacoes.stream().filter(p->p.isContabiliza() && p.getIsDebito() && p.getCategoria() != null && p.getConta().getTipoConta().equals(2) && p.getCategoria().getCategoriaPai().getNome().equals(categoria) && YearMonth.from(p.getData()).equals(data)).collect(Collectors.toList());
        List<Transacao> creditos = _listaTransacoes.stream().filter(p->p.isContabiliza() && p.getIsDebito()==false && YearMonth.from(p.getData()).equals(data)).collect(Collectors.toList());

        controller.initData("Débitos Conta Corrente","Débitos Cartão Crédito","Créditos",debitosContaCorrente,debitosCartao,creditos);
        stage.show();
    }



}

