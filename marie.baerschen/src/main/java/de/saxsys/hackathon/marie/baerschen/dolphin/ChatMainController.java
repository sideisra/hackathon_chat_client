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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.opendolphin.binding.Converter;
import org.opendolphin.core.ModelStoreEvent;
import org.opendolphin.core.ModelStoreListener;
import org.opendolphin.core.PresentationModel;
import org.opendolphin.core.client.ClientAttribute;
import org.opendolphin.core.client.ClientDolphin;
import org.opendolphin.core.client.ClientPresentationModel;
import org.opendolphin.core.client.comm.OnFinishedHandlerAdapter;

public class ChatMainController {

    private static int BOX_WIDTH = 30;
    private static int BOX_HEIGHT = 30;

    static ClientDolphin clientDolphin;

    @FXML
    private TextField nameField;
    @FXML
    private TextArea postField;
    @FXML
    private Button newButton;
    @FXML
    private Pane chatWindow;

    private final ClientPresentationModel postModel;

    private final Map<String, TitledPane> participants = new HashMap<>();

    private final Random rand = new Random();

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
            public void modelStoreChanged(final ModelStoreEvent event) {
                if (event.getType() == ModelStoreEvent.Type.ADDED) {
                    System.out.println(" wir haben den pm bekommen:  " + event.getPresentationModel().getId());
                    final ClientPresentationModel nextPost = (ClientPresentationModel) event.getPresentationModel();

                    final VBox participantHolderContent =
                            (VBox) getParticipantHolder(getUserId(event.getPresentationModel()),
                                    event.getPresentationModel()).getContent();

                    final HBox box = new HBox();

                    box.setId(event.getPresentationModel().getId());

                    box.setOnMouseClicked(new EventHandler<Event>() {

                        @Override
                        public void handle(Event event) {

                            PresentationModel dolphin = clientDolphin.getAt(((HBox) event.getSource()).getId());

                            if (dolphin != null) {
                                String dolphinsContent = getUserId(dolphin);

                                String postModelContent = getUserId(postModel);

                                if (dolphinsContent.equals(postModelContent)) {
                                    clientDolphin.apply(nextPost).to(postModel);
                                    release();
                                }

                            }

                        }
                    });

                    final Label postDate = new DateLabel(nextPost.getAt(ATTR_DATE).getValue().toString());
                    postDate.getStyleClass().add("post");
                    bind("formattedDate").of(postDate).to(ATTR_DATE).of(nextPost, withRelease);
                    bind(ATTR_DATE).of(nextPost).to("formattedDate").of(postDate);
                    final Label userPost = new Label(nextPost.getAt(ATTR_MESSAGE).getValue().toString());
                    userPost.getStyleClass().add("post");
                    bind("text").of(userPost).to(ATTR_MESSAGE).of(nextPost, withRelease);
                    bind(ATTR_MESSAGE).of(nextPost).to("text").of(userPost);

                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            box.getChildren().addAll(postDate, userPost);

                            participantHolderContent.getChildren().add(box);
                        }

                    });
                }
                if (event.getType() == ModelStoreEvent.Type.REMOVED) {
                    System.out.println(" wir haben den pm geloescht:  " + event.getPresentationModel().getId());
                    final TitledPane participantHolder =
                            getParticipantHolder(getUserId(event.getPresentationModel()), event.getPresentationModel());
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            HBox boxToRemove = null;
                            for (Node hbox : ((VBox) participantHolder.getContent()).getChildren()) {
                                HBox post = (HBox) hbox;
                                if (post.getId().equals(event.getPresentationModel().getId())) {
                                    boxToRemove = post;
                                }
                            }
                            ((VBox) participantHolder.getContent()).getChildren().removeAll(boxToRemove);
                            if (((VBox) participantHolder.getContent()).getChildren().isEmpty()) {
                                chatWindow.getChildren().removeAll(participantHolder);
                                participants.remove(getUserId(event.getPresentationModel()));
                            }
                        }

                    });

                }
            }

        });

        // on select : pm per id:
        // pm = clientDolphin.getAt("meineid")
        // clientDolphin.apply(pm).to(postModel);
        // release();

    }

    protected String getUserName(PresentationModel presentationModel) {
        return presentationModel.getAt(ATTR_NAME).getValue().toString();
    }

    private String getUserId(PresentationModel presentationModel) {
        return presentationModel.getAt(ATTR_NAME).getQualifier().split("-")[0];
    }

    private TitledPane getParticipantHolder(String id, PresentationModel presentationModel) {
        TitledPane holder = participants.get(id);
        if (holder == null) {
            holder = new MovableTitledPane(presentationModel.getAt(ATTR_NAME).getValue().toString(), new VBox());

            holder.setTranslateX(rand.nextInt((int) chatWindow.getWidth() - BOX_WIDTH));
            holder.setTranslateY(rand.nextInt((int) chatWindow.getHeight() - BOX_HEIGHT));
            holder.getStyleClass().add("vbox");
            final TitledPane consistentHolder = holder;
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    chatWindow.getChildren().add(consistentHolder);
                }

            });
            participants.put(id, holder);
        }
        bind("text").of(holder).to(ATTR_NAME).of(presentationModel);
        bind(ATTR_NAME).of(presentationModel).to("text").of(holder);
        return holder;
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
