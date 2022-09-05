package vlad.bsuir.network.lab3.client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import vlad.bsuir.network.lab3.FileStorageResource;
import vlad.bsuir.network.lab3.Main;

import javafx.application.Application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.stage.Stage;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.*;

public class Client extends Application {

    private static  final String CLIENT_DIR = "d:\\Учёба\\Лабораторные\\Семестр 4\\КСиС\\RestHttp\\client";
    private WebTarget target;
    private static String fileSelectedName = new String();
    private final ListView<String> fileListView = new ListView<String>();

    public Client() {
        try {
            javax.ws.rs.client.Client client = ClientBuilder.newClient().property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
            this.target = client.target(Main.BASE_URI);
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final Group group = new Group();
        BorderPane Anp = new BorderPane();
        Scene scene = new Scene(Anp, 900, 600);
        Client clientAPI = new Client();

        ObservableList<String> fileList = getObservableList();
        MultipleSelectionModel<String> listSelectionModel = fileListView.getSelectionModel();

        fileListView.setItems(fileList);
        fileListView.setOrientation(Orientation.VERTICAL);
        fileListView.setPrefSize(400, 600);

        group.getChildren().add(fileListView);

        listSelectionModel.selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println(": " + newValue + "  ;  " +  oldValue +" ;");
                fileSelectedName = newValue;
            }
        });


        Anp.setLeft(fileListView);
        Anp.setRight(getButtonPane());
        primaryStage.setResizable(false);
        primaryStage.setTitle("Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String getList() {
        WebTarget tr = target.path("file").path("list");
        return tr.request().get(String.class);
    }

    private ObservableList<String> getObservableList(){
        ObservableList<String> List = null;
        String[] fileArr = getList().split("\\|");
        List = FXCollections.observableArrayList(fileArr);
        return List;
    }

    private VBox getButtonPane(){
        Button btnGET = new Button("Get");
        Button btnPUT = new Button("Put");
        Button btnPOST = new Button("Post");
        Button btnMOVE = new Button("Move");
        Button btnCOPY = new Button("Copy");
        Button btnDELETE = new Button("Delete");

        Button[] buttons = {btnCOPY, btnDELETE, btnGET, btnMOVE, btnPOST, btnPUT};
        for(Button button: buttons){
            button.setMinWidth(90);
        }

        VBox buttonBox = new VBox(10);
        buttonBox.setPrefWidth(140);
        buttonBox.setStyle("-fx-background-color: #abcdea");

        buttonBox.getChildren().addAll(btnCOPY, btnDELETE, btnGET, btnMOVE, btnPOST, btnPUT);

        TitledPane buttonTitle = new TitledPane("Methods",buttonBox);
        buttonTitle.setExpanded(false);

        VBox vPanel = new VBox(10);
        vPanel.setPrefWidth(140);
        vPanel.setStyle("-fx-background-color: #abcdea");

        vPanel.getChildren().add(buttonTitle);

        btnCOPY.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copyFile();
                ObservableList<String> fileList = getObservableList();
                fileListView.setItems(fileList);
            }
        });

        btnDELETE.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteFile();
                ObservableList<String> fileList = getObservableList();
                fileListView.setItems(fileList);
            }
        });

        btnGET.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                getFile();
                ObservableList<String> fileList = getObservableList();
                fileListView.setItems(fileList);
            }
        });

        btnMOVE.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                moveFile();
                ObservableList<String> fileList = getObservableList();
                fileListView.setItems(fileList);
            }
        });

        btnPOST.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                postFile();
                ObservableList<String> fileList = getObservableList();
                fileListView.setItems(fileList);
            }
        });

        btnPUT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                putFile();
                ObservableList<String> fileList = getObservableList();
                fileListView.setItems(fileList);

            }
        });

        return vPanel;
    }

    private void copyFile(){
        //String listStr = getList();
        //String[] list = (listStr != null) ? listStr.split("\\|") : null;

        String fileName = fileSelectedName;
        if(getFileName(fileName) == false){
            Alert msgErr = new Alert(Alert.AlertType.ERROR);
            msgErr.setHeaderText(null);
            msgErr.setContentText("Файл не выбран");
            msgErr.showAndWait();
        }

        WebTarget tr = target.path("file").path("copy").
                queryParam("from", fileName).
                queryParam("to", fileName + "-copy");
        Response response = tr.request().method("COPY");

        System.out.println(response);
    }

    private void deleteFile(){
        //String listStr = getList();
        //String[] list = (listStr != null) ? listStr.split("\\|") : null;

        String fileName = fileSelectedName;
        if(getFileName(fileName) == false){
            Alert msgErr = new Alert(Alert.AlertType.ERROR);
            msgErr.setHeaderText(null);
            msgErr.setContentText("Файл не выбран");
            msgErr.showAndWait();
        }

        WebTarget tr = target.path("file").path("delete").queryParam("name", fileName);
        Response response = tr.request().delete();

        System.out.println(response);
    }

    private void getFile(){
        //String listStr = getList();
        //String[] list = (listStr != null) ? listStr.split("\\|") : null;

        String fileName = fileSelectedName;
        if(getFileName(fileName) == false){
            Alert msgErr = new Alert(Alert.AlertType.ERROR);
            msgErr.setHeaderText(null);
            msgErr.setContentText("Файл не выбран");
            msgErr.showAndWait();
        }

        WebTarget tr = target.path("file").path("read").queryParam("name", fileName);
        Response response = tr.request().get();

        //String contentDesc = (String)response.getHeaders().getFirst("content-disposition");
        //String fileName = (contentDesc.indexOf("filename = ") > 0) ?
        //        contentDesc.substring(contentDesc.indexOf("filename = ") + 11).trim() : null;

        //System.out.println(fileName);

        InputStream is = response.readEntity(InputStream.class);

        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(CLIENT_DIR + File.separatorChar +
                            ((fileName != null) ? fileName : "test")));

            int bytesRead = 0;
            byte[] buffer = new byte[10240];

            while((bytesRead = is.read(buffer, 0, 10240)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();
            bos.close();
            is.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        File file = new File(CLIENT_DIR + File.separatorChar +
                ((fileName != null) ? fileName : "test"));
    }

    private  void putFile(){
        try {
            File file = new File(CLIENT_DIR);
            String fileName = file.list()[0];

            final javax.ws.rs.client.Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).
                    register(FileStorageResource.class).build();

            //System.out.println(CLIENT_DIR + File.separatorChar + fileName);

            final StreamDataBodyPart filePart = new StreamDataBodyPart(
                    "part",
                    new BufferedInputStream(new FileInputStream(CLIENT_DIR + File.separatorChar + fileName)));

            FormDataMultiPart multiPart = new FormDataMultiPart();
            multiPart.field("name", fileName + "-copy");
            multiPart.bodyPart(filePart);

            final WebTarget target = client.target(Main.BASE_URI + "file/write");
            final Response response = target.request().put(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

            System.out.println(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private  void postFile(){
        try {
            String listStr = getList();
            String[] list = (listStr != null) ? listStr.split("\\|") : null;

            String fileName = fileSelectedName;
            if (getFileName(fileName) == false) {
                Alert msgErr = new Alert(Alert.AlertType.ERROR);
                msgErr.setHeaderText(null);
                msgErr.setContentText("Файл не выбран");
                msgErr.showAndWait();
            }

            File file = new File(CLIENT_DIR);
            String fileNameFrom = file.list()[0];

            final javax.ws.rs.client.Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).
                    register(FileStorageResource.class).build();

            //System.out.println(CLIENT_DIR + File.separatorChar + fileName);

            final StreamDataBodyPart filePart = new StreamDataBodyPart(
                    "part",
                    new BufferedInputStream(new FileInputStream(CLIENT_DIR + File.separatorChar + fileNameFrom)));

            FormDataMultiPart multiPart = new FormDataMultiPart();
            multiPart.field("name", fileName);
            multiPart.bodyPart(filePart);

            final WebTarget target = client.target(Main.BASE_URI + "file/append");
            final Response response = target.request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

            System.out.println(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void moveFile(){
       // String listStr = getList();
       // String[] list = (listStr != null) ? listStr.split("\\|") : null;

        String fileName = fileSelectedName;
        if(getFileName(fileName) == false){
            Alert msgErr = new Alert(Alert.AlertType.ERROR);
            msgErr.setHeaderText(null);
            msgErr.setContentText("Файл не выбран");
            msgErr.showAndWait();
        }

        WebTarget tr = target.path("file").path("move").
                queryParam("from", fileName).
                queryParam("to", fileName + "-move");
        Response response = tr.request().method("MOVE");

        System.out.println(response);
    }

    private boolean getFileName(String file){
        boolean isSelected = true;
        if(file.length() == 0){
            isSelected = false;
        }

        return isSelected;
    }
}
