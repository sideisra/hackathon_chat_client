package de.saxsys.hackathon.marie.baerschen.dolphin;

import static de.saxsys.hackathon.marie.baerschen.dolphin.ChatterConstants.ATTR_DATE;
import static de.saxsys.hackathon.marie.baerschen.dolphin.ChatterConstants.ATTR_MESSAGE;
import static de.saxsys.hackathon.marie.baerschen.dolphin.ChatterConstants.ATTR_NAME;
import static de.saxsys.hackathon.marie.baerschen.dolphin.ChatterConstants.CMD_INIT;
import static de.saxsys.hackathon.marie.baerschen.dolphin.ChatterConstants.CMD_POLL;
import static de.saxsys.hackathon.marie.baerschen.dolphin.ChatterConstants.CMD_POST;
import static de.saxsys.hackathon.marie.baerschen.dolphin.ChatterConstants.PM_ID_INPUT;
import static de.saxsys.hackathon.marie.baerschen.dolphin.ChatterConstants.TYPE_POST;
import static org.opendolphin.binding.JFXBinder.bind;
import groovy.util.Eval;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.opendolphin.binding.Converter;
import org.opendolphin.core.ModelStoreEvent;
import org.opendolphin.core.ModelStoreListener;
import org.opendolphin.core.client.ClientAttribute;
import org.opendolphin.core.client.ClientDolphin;
import org.opendolphin.core.client.ClientPresentationModel;
import org.opendolphin.core.client.comm.OnFinishedHandlerAdapter;

public class ChatMainController {

    static ClientDolphin clientDolphin;

    @FXML
    private TextField nameField;
    @FXML
    private TextArea postField;
    @FXML
    private Button newButton;
    @FXML
    private VBox chatWindow;

    private final ClientPresentationModel postModel;

    public ChatMainController() {
        ClientAttribute nameAttribute = new ClientAttribute(ATTR_NAME, "");
        ClientAttribute postAttribute = new ClientAttribute(ATTR_MESSAGE, "");
        ClientAttribute dateAttribute = new ClientAttribute(ATTR_DATE, "");
        postModel = clientDolphin.presentationModel(PM_ID_INPUT, nameAttribute, postAttribute, dateAttribute);
    }

    @FXML
    public void initialize() {

        setupBinding();
        addClientSideAction();

        clientDolphin.send(CMD_INIT, new OnFinishedHandlerAdapter() {
            @Override
            public void onFinished(List<ClientPresentationModel> presentationModels) {
                System.out.println("" + presentationModels.size() + "bekommen");

                longPoll();

            }
        });
    }

    private boolean channelBlocked = false;

    private void release() {
        if (!channelBlocked)
            return; // avoid too many unblocks
        channelBlocked = false;
        String url = "https://klondike.canoo.com/dolphin-grails/chatter/release";
        String result = Eval.x(url, "x.toURL().text").toString();
        System.out.println("release result = " + result);
    }

    private void longPoll() {
        channelBlocked = true;
        clientDolphin.send(CMD_POLL, new OnFinishedHandlerAdapter() {
            @Override
            public void onFinished(List<ClientPresentationModel> presentationModels) {
                longPoll();
            }
        });
    }

    private final Converter withRelease = new Converter() {

        @Override
        public Object convert(Object value) {
            release();
            return value;
        }
    };

    private void setupBinding() {

        bind("text").of(nameField).to(ATTR_NAME).of(postModel, withRelease);
        bind(ATTR_NAME).of(postModel).to("text").of(nameField);

        bind("text").of(postField).to(ATTR_MESSAGE).of(postModel, withRelease);
        bind(ATTR_MESSAGE).of(postModel).to("text").of(postField);

        clientDolphin.addModelStoreListener(TYPE_POST, new ModelStoreListener() {
            @Override
            public void modelStoreChanged(ModelStoreEvent event) {
                if (event.getType() == ModelStoreEvent.Type.ADDED) {
                    System.out.println(" wir haben den pm bekommen:  " + event.getPresentationModel().getId());
                    final ClientPresentationModel nextPost = (ClientPresentationModel) event.getPresentationModel();

                    HBox box = new HBox();

                    box.setOnMouseClicked(new EventHandler<Event>() {

                        @Override
                        public void handle(Event event) {
                            clientDolphin.apply(nextPost).to(postModel);
                            release();

                        }
                    });

                    Label userName = new Label(nextPost.getAt(ATTR_NAME).getValue().toString());
                    bind("text").of(userName).to(ATTR_NAME).of(nextPost, withRelease);
                    bind(ATTR_NAME).of(nextPost).to("text").of(userName);
                    Label postDate = new Label(nextPost.getAt(ATTR_DATE).getValue().toString());
                    bind("text").of(postDate).to(ATTR_DATE).of(nextPost, withRelease);
                    bind(ATTR_DATE).of(nextPost).to("text").of(postDate);
                    Label userPost = new Label(nextPost.getAt(ATTR_MESSAGE).getValue().toString());
                    bind("text").of(userPost).to(ATTR_MESSAGE).of(nextPost, withRelease);
                    bind(ATTR_MESSAGE).of(nextPost).to("text").of(userPost);

                    box.getChildren().addAll(userName, postDate, userPost);

                    chatWindow.getChildren().addAll(box);
                }
                if (event.getType() == ModelStoreEvent.Type.REMOVED) {
                    System.out.println(" wir haben den pm geloescht:  " + event.getPresentationModel().getId());
                }
            }
        });

        // on select : pm per id:
        // pm = clientDolphin.getAt("meineid")
        // clientDolphin.apply(pm).to(postModel);
        // release();

    }

    private void addClientSideAction() {
        newButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                clientDolphin.send(CMD_POST);
                release();
            }
        });
    }
}
