package de.saxsys.hackathon.marie.baerschen.dolphin;

import org.opendolphin.core.client.ClientDolphin;
import org.opendolphin.core.client.ClientModelStore;
import org.opendolphin.core.client.comm.BlindCommandBatcher;
import org.opendolphin.core.client.comm.HttpClientConnector;
import org.opendolphin.core.client.comm.JavaFXUiThreadHandler;
import org.opendolphin.core.comm.JsonCodec;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {



	public static void main(String[] args) throws Exception {
		ClientDolphin clientDolphin = new ClientDolphin();
		clientDolphin.setClientModelStore(new ClientModelStore(clientDolphin));
		BlindCommandBatcher batcher = new BlindCommandBatcher();
		batcher.setMergeValueChanges(true);
		HttpClientConnector connector = new HttpClientConnector(clientDolphin,
				batcher, "https://klondike.canoo.com/dolphin-grails/dolphin/");
		connector.setCodec(new JsonCodec());
		connector.setUiThreadHandler(new JavaFXUiThreadHandler());
		clientDolphin.setClientConnector(connector);

		ChatMainController.clientDolphin = clientDolphin;
		Application.launch(MainApp.class);
	}

    public void start(Stage stage) throws Exception {


        String fxmlFile = "/fxml/chatMain.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));


        Scene scene = new Scene(rootNode, 600, 800);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("ChatAthon");
        stage.setScene(scene);
        stage.show();
    }
}
