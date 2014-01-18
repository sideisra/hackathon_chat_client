package de.saxsys.hackathon.marie.baerschen.dolphin;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

public class DateLabel extends Label {

    private final StringProperty formattedDate;

    public DateLabel(String text) {
        super(text);
        formattedDate = new SimpleStringProperty(text);
        this.textProperty().bind(formattedDate.concat(":"));
    }

    public StringProperty formattedDateProperty() {
        return formattedDate;
    }

    public String getFormattedDate() {
        return this.formattedDate.get();
    }

    public void setFormattedDate(String newDate) {
        this.formattedDate.set(newDate);
    }
}
