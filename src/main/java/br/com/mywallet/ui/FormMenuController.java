/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mywallet.ui;

import br.com.mywallet.service.TransacaoService;
import br.com.mywallet.ui.config.Configuration;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author MBRIENZE
 */
public class FormMenuController implements Initializable {
   /*
    @FXML
    private Button close;
    @FXML
    private Button maximize;
    @FXML
    private Button minimize;*/
    @FXML
    private Button resize;
    @FXML
    private Button fullscreen;
    @FXML
    private Label title,lbFuncional,lbStressTest,lbConfiguracao,lbRelatorios,lbFerramentas,lbAjuda,lbSobre;

    private int stateMenu;

    @Autowired
    private TransacaoService transacaoService;

    Stage stage;
    Rectangle2D rec2;
    Double w,h;

    @FXML
    private VBox listMenu;
    @FXML
    private ScrollPane paneData;

    @FXML
    private StatusBar statusBar;

    @FXML
    private Label lbUser;

    Configuration con = new Configuration();
    @FXML
    private Button btnLogout;

    private Button butFuncional;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        stateMenu = 1;
        rec2 = Screen.getPrimary().getVisualBounds(); 
        w = 0.1;
        h = 0.1;
        //listMenu.getItems().addAll("  Funcional", "  Stress Test","  Relatórios","  Ferramentas","  Configuração"," Ajuda "," Sobre ");

        Label aLabel = new Label();
        //listMenu2.getChildren().add()
        Platform.runLater(() -> {
            //Scene scene = paneData.getScene();
            //stage = (Stage) scene.getWindow();
            /*Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

            //set Stage boundaries to visible bounds of the main screen
            stage.setX(primaryScreenBounds.getMinX());
            stage.setY(primaryScreenBounds.getMinY());
            stage.setWidth(primaryScreenBounds.getWidth());
            stage.setHeight(primaryScreenBounds.getHeight());*/

            //stage.setMaximized(false);
            //stage.setHeight(rec2.getHeight());
            //maximize.getStyleClass().add("decoration-button-restore");
           // resize.setVisible(false);
            //listMenu.getSelectionModel().select(0);
            // con.loadAnchorPane(paneData, "FuncionalView.fxml");
          //  con.loadBorderPane(paneData, "FuncionalBorderView.fxml");
            BorderPane p = null;
            FXMLLoader loader = null;
            try {
                loader = new FXMLLoader(getClass().getResource("/fxml/HomeDashBoardView.fxml"));
             //   p = FXMLLoader.load(getClass().getResource("FuncionalBorderResponsiveView.fxml"));
                p = loader.load();
                con.setFuncionalView("/fxml/HomeDashBoardView.fxml",p);
                //vendaController controller = loader.<vendaController>getController();
                //controller.setStatusPane(statusBar);
            } catch (IOException e) {
                e.printStackTrace();
            }
            paneData.setContent(p);
            //listMenu.requestFocus();
            //listM
            // enu.setStyle("-fx-background-insets: 0 ;");
        });

        //addingItemMenus
        lbFuncional = new Label("Home");
        lbStressTest = new Label("Extrato");
        lbConfiguracao = new Label("Planejamento");
        lbRelatorios = new Label("Relatórios");
        lbFerramentas = new Label("Pagamentos");
        lbAjuda = new Label("Ajuda");
        lbSobre = new Label("Sobre");

        butFuncional = new Button("Home");

        lbFuncional.setDisable(false);
        lbFuncional.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
             //   con.loadBorderPane(paneData, "FuncionalBorderView.fxml");
                if(stateMenu != 1) {
                    con.loadBorderPane(paneData, "HomeDashBoardView.fxml");
                    stateMenu = 1;
                }
            }
        });

        butFuncional.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                //   con.loadBorderPane(paneData, "FuncionalBorderView.fxml");
                if(stateMenu != 1){
                    con.loadBorderPane(paneData, "HomeDashBoardView.fxml");
                    stateMenu = 1;
                }

            }
        });
        lbStressTest.setDisable(false);
        lbStressTest.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override public void handle(MouseEvent e) {
                if(stateMenu != 2) {
                    //transacaoService.findAll();
                    con.loadBorderPane(paneData, "ExtratoView.fxml");
                    stateMenu = 2;
                }
            }
        });

        lbConfiguracao.setDisable(false);
        lbConfiguracao.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                if(stateMenu != 3) {
                    con.loadBorderPane(paneData, "PlanejamentoFinanceiro.fxml");
                    stateMenu =3;
                }
            }
        });

        lbRelatorios.setDisable(false);
        lbRelatorios.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override public void handle(MouseEvent e) {
                if(stateMenu != 4) {
                    con.loadBorderPane(paneData, "RelatoriosView.fxml");
                    stateMenu = 4;
                }
            }
        });

        lbFerramentas.setDisable(false);
        lbFerramentas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                if(stateMenu != 5) {
                    con.loadStackPane(paneData, "stackFerramentasPane.fxml");
                    stateMenu = 5;
                }
            }
        });

        lbAjuda.setDisable(false);
        lbAjuda.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override public void handle(MouseEvent e) {
                if(stateMenu != 6) {
                    con.loadStackPane(paneData, "AjudaPane.fxml");
                    stateMenu = 6;
                }
            }
        });

        lbSobre.setDisable(false);
        lbSobre.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                if(stateMenu != 7) {
                    con.loadStackPane(paneData, "SobrePane.fxml");
                    stateMenu = 7;
                }
            }
        });

        lbFuncional.setId("label_menu");
        lbStressTest.setId("label_menu");
        lbConfiguracao.setId("label_menu");
        lbRelatorios.setId("label_menu");
        lbFerramentas.setId("label_menu");
        lbAjuda.setId("label_menu");
        lbSobre.setId("label_menu");

        listMenu.setSpacing(50);
        listMenu.getChildren().addAll(lbFuncional, lbStressTest, lbConfiguracao, lbRelatorios, lbFerramentas, lbAjuda, lbSobre);

    }
    
   /* @FXML
    private void aksiMaximized(ActionEvent event) {
        stage = (Stage) maximize.getScene().getWindow();
        if (stage.isMaximized()) {
            if (w == rec2.getWidth() && h == rec2.getHeight()) {
                stage.setMaximized(false);
                stage.setHeight(600);
                stage.setWidth(800);
                stage.centerOnScreen();
                maximize.getStyleClass().remove("decoration-button-restore");
                resize.setVisible(true);
            }else{
                stage.setMaximized(false);
                maximize.getStyleClass().remove("decoration-button-restore");
                resize.setVisible(true);
            }
            
        }else{
            stage.setMaximized(true);
            stage.setHeight(rec2.getHeight());
            maximize.getStyleClass().add("decoration-button-restore");
            resize.setVisible(false);
        }
    }*/

   /* @FXML
    private void aksiminimize(ActionEvent event) {
        stage = (Stage) minimize.getScene().getWindow();
        if (stage.isMaximized()) {
            w = rec2.getWidth();
            h = rec2.getHeight();
            stage.setMaximized(false);
            stage.setHeight(h);
            stage.setWidth(w);
            stage.centerOnScreen();
            Platform.runLater(() -> {
                stage.setIconified(true);
            });
        }else{
            stage.setIconified(true);
        }        
    }*/

    @FXML
    private void aksiResize(ActionEvent event) {
    }

    /*@FXML
    private void aksifullscreen(ActionEvent event) {
        stage = (Stage) fullscreen.getScene().getWindow();
        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
        }else{
            stage.setFullScreen(true);
        }
    }*/

    /*@FXML
    private void aksiClose(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }*/

    @FXML
    void ferramentasClicked(MouseEvent event) {
        con.loadStackPane(paneData, "stackFerramentasPane.fxml");
    }

    @FXML
    void funcionalClickedd(ActionEvent event) {

        //con.loadAnchorPane(paneData, "FuncionalBorderView.fxml");
        con.loadAnchorPane(paneData, "FuncionalBorderResponsiveView.fxml");
    }

    @FXML
    void stressTestClickedd(ActionEvent event) {
        con.loadAnchorPane(paneData, "stressTest.fxml");
    }

    @FXML
    void funcionalClicked(MouseEvent event) {
        con.loadAnchorPane(paneData, "FuncionalView.fxml");
    }

    @FXML
    void ajudaClicked(MouseEvent event) {
        con.loadStackPane(paneData, "AjudaPane.fxml");
    }

    @FXML
    void relatoriosClicked(MouseEvent event) {
        con.loadStackPane(paneData, "RelatoriosPane.fxml");
    }

    @FXML
    void sobreClicked(MouseEvent event) {
        con.loadStackPane(paneData, "SobrePane.fxml");
    }

    @FXML
    void stressTestClicked(MouseEvent event) {
        con.loadAnchorPane(paneData, "stressTest.fxml");
    }

    @FXML
    void configuracaoClicked(MouseEvent event) {
        con.loadAnchorPane(paneData, "stackConfigurationPane.fxml");
    }

}
