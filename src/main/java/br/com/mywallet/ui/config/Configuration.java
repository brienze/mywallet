/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mywallet.ui.config;

import br.com.mywallet.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.PropertySheet;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author mbrienze
 */
public class Configuration {
    static final org.slf4j.Logger logger = LoggerFactory.getLogger(Configuration.class);
    private PropertySheet propertySheet = new PropertySheet();

    private HashMap<String,Pane> panels;

    public Configuration() {
        panels = new HashMap<>();
    }
    
    public static void dialog(Alert.AlertType alertType,String s){
        Alert alert = new Alert(alertType,s);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Info");
        alert.showAndWait();
    }
    
    public void newStage(Stage stage, Label lb, String load, String judul, boolean resize, StageStyle style, boolean maximized){
       try {
            Stage st = new Stage();
            stage = (Stage) lb.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(load));
            Scene scene = new Scene(root);
            st.initStyle(style);
            st.setResizable(resize);
            st.setMaximized(maximized);
            st.setTitle(judul);
            st.setScene(scene);
            st.show();
            stage.close();
        } catch (Exception e) {
           logger.error("Erro ao carregar " + load + ": "+e.getMessage());
           e.printStackTrace();
        } 
    }

    public void newResponsiveStage(Stage stage, Label lb, String load, String judul, boolean resize, StageStyle style, boolean maximized){
        try {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            Stage st = new Stage();
            stage = (Stage) lb.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(load));
            Scene scene = new Scene(root);
            st.initStyle(style);
            st.setResizable(resize);
            st.setMaximized(maximized);
            st.setTitle(judul);
            st.setScene(scene);

            //set Stage boundaries to visible bounds of the main screen
            //stage.setX(primaryScreenBounds.getMinX()-15);
            //stage.setY(primaryScreenBounds.getMinY());
            st.setWidth(primaryScreenBounds.getWidth()*0.71);
            st.setHeight(primaryScreenBounds.getHeight()*0.90);

            st.show();

            st.setOnCloseRequest(e->Platform.exit());

            stage.close();
        } catch (Exception e) {
            logger.error("Erro ao carregar " + load + ": "+e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void newStage2(Stage stage, Button lb, String load, String judul, boolean resize, StageStyle style, boolean maximized){
       try {
            Stage st = new Stage();
            stage = (Stage) lb.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(load));
            Scene scene = new Scene(root);
            st.initStyle(style);
            st.setResizable(resize);
            st.setMaximized(maximized);
            st.setTitle(judul);
            st.setScene(scene);
            st.show();
            stage.close();
        } catch (Exception e) {
           logger.error("Erro ao carregar " + load + ": "+e.getMessage());
           e.printStackTrace();
        } 
    }

    public void setFuncionalView(String a,BorderPane bp){
        panels.put(a,bp);
    }

    public void loadBorderPane(AnchorPane ap, String a){
        try {
            BorderPane bp = (BorderPane)panels.get(a);
            if(bp == null){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/"+a));
                loader.setControllerFactory(Application.getContext()::getBean);
                bp = loader.load();
                //bp = FXMLLoader.load(getClass().getResource("/fxml/"+a));
                panels.put(a,bp);
            }
            ap.getChildren().setAll(bp);
        } catch (IOException e) {
            logger.error("Erro ao carregar " + "/fxml/"+a + ": "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadBorderPane(ScrollPane ap, String a){
        try {
            BorderPane bp = (BorderPane)panels.get(a);
            if(bp == null){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/"+a));
                loader.setControllerFactory(Application.getContext()::getBean);
                bp = loader.load();
                panels.put(a,bp);
            }
            ap.setContent(bp);
        } catch (IOException e) {
            logger.error("Erro ao carregar " + "/fxml/"+a + ": "+e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void loadAnchorPane(AnchorPane ap, String a){
        try {
            AnchorPane bp = (AnchorPane)panels.get(a);
            if(bp == null){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/"+a));
                loader.setControllerFactory(Application.getContext()::getBean);
                bp = loader.load();
                //bp = FXMLLoader.load(getClass().getResource("/fxml/"+a));
                panels.put(a,bp);
            }
            ap.getChildren().setAll(bp);
        } catch (IOException e) {
            logger.error("Erro ao carregar " + "/fxml/"+a + ": "+e.getMessage());
            e.printStackTrace();
        }   
    }

    public void loadAnchorPane(ScrollPane ap, String a){
        try {
            AnchorPane bp = (AnchorPane)panels.get(a);
            if(bp == null){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/"+a));
                loader.setControllerFactory(Application.getContext()::getBean);
                bp = loader.load();

                //bp = FXMLLoader.load(getClass().getResource("/fxml/"+a));
                panels.put(a,bp);
            }
            ap.setContent(bp);
        } catch (IOException e) {
            logger.error("Erro ao carregar " + "/fxml/"+a + ": "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadStackPane(AnchorPane ap, String a){
        try {
            StackPane bp = (StackPane)panels.get(a);
            if(bp == null){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/"+a));
                loader.setControllerFactory(Application.getContext()::getBean);
                bp = loader.load();
                //bp = FXMLLoader.load(getClass().getResource("/fxml/"+a));
                panels.put(a,bp);
            }
            ap.getChildren().setAll(bp);
        } catch (IOException e) {
            logger.error("Erro ao carregar " + "/fxml/"+a + ": "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadStackPane(ScrollPane ap, String a){
        try {
            StackPane bp = (StackPane)panels.get(a);
            if(bp == null){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/"+a));
                loader.setControllerFactory(Application.getContext()::getBean);
                bp = loader.load();

                //bp = FXMLLoader.load(getClass().getResource("/fxml/"+a));
                panels.put(a,bp);
            }
            ap.setContent(bp);
        } catch (IOException e) {
            logger.error("Erro ao carregar " + "/fxml/"+a + ": "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void defineConfigurationPanel(AnchorPane ap) {
        VBox infoPane = new VBox(10);

        Button button = new Button("Title");
        TextField textField = new TextField();


        CheckBox toolbarModeVisible = new CheckBox("Show Mode Buttons");
        toolbarModeVisible.selectedProperty().bindBidirectional(propertySheet.modeSwitcherVisibleProperty());

        CheckBox toolbarSearchVisible = new CheckBox("Show Search Field");
        toolbarSearchVisible.selectedProperty().bindBidirectional(propertySheet.searchBoxVisibleProperty());

        infoPane.getChildren().add(toolbarModeVisible);
        infoPane.getChildren().add(toolbarSearchVisible);

        infoPane.getChildren().add(button);
        infoPane.getChildren().add(textField);

        //return infoPane;

        ap.getChildren().setAll(infoPane);
    }










    public static void setModelColumn(TableColumn tb,String a){
        tb.setCellValueFactory(new PropertyValueFactory(a));
    }


}
