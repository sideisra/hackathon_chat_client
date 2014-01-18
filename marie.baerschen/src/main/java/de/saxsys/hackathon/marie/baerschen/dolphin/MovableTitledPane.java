package de.saxsys.hackathon.marie.baerschen.dolphin;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;

public class MovableTitledPane extends TitledPane {
    private double deltaX;
    private double deltaY;

    public MovableTitledPane(String title, Node content) {
        super(title, content);
        this.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent de) {
                deltaX = getLayoutX() - de.getSceneX();
                deltaY = getLayoutY() - de.getSceneY();
            }
        });

        this.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent me) {
                mouseDragged(me.getSceneX(), me.getSceneY());
            }
        });

    }

    private void mouseDragged(double x, double y) {
        this.setLayoutX(x + deltaX);
        this.setLayoutY(y + deltaY);
    }

}
