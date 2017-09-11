package br.com.mywallet.ui;

import br.com.mywallet.model.Categoria;
import br.com.mywallet.service.CategoriaService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;


@Component
public class DefinirCategoriaController {
    static final Logger logger = LoggerFactory.getLogger(DefinirCategoriaController.class);
    static List<Categoria> categorias;

    @Autowired
    private CategoriaService categoriaService;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private HBox vbData;

    @FXML
    private ListView<Categoria> lstCategorias;

    @FXML
    private ListView<Categoria> lstSubcategorias;

    private Consumer<Categoria> consumer;

    public void definirCallback(Consumer<Categoria> consumer){
        this.consumer = consumer;
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        if(categorias == null){
            categorias = categoriaService.findByCategoriaPaiIsNull();
        }


       //List<Categoria> categorias = CategoriaBuilder.instance().obterCategorias();

        lstCategorias.getItems().setAll(categorias);

        lstCategorias.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Categoria>() {

            @Override
            public void changed(ObservableValue<? extends Categoria> observable, Categoria oldValue, Categoria newValue) {
                // Your action here
                System.out.println("Selected item: " + newValue.getNome() + " id: " + newValue.getIdCategoria());

                lstSubcategorias.getItems().clear();
                lstSubcategorias.getItems().setAll(newValue.getSubCategorias());
            }
        });

        lstSubcategorias.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Categoria>() {

            @Override
            public void changed(ObservableValue<? extends Categoria> observable, Categoria oldValue, Categoria newValue) {
                // Your action here
                System.out.println("Selected item: " + newValue.getNome() + " id: " + newValue.getIdCategoria());

                consumer.accept(newValue);

            }
        });

        //cbSubcategoria.getItems().setAll(contas);
    }


    @FXML
    void definir(javafx.event.ActionEvent ace){

    }




}
