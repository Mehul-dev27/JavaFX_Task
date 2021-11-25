package com.max.ToDoList;

import datamodel.TodoData;
import datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;


public class DialogController {
    @FXML
    private TextArea FullDescription;
    @FXML
    private TextField shortDescriptionField;
    @FXML
    private DatePicker deadLinePiker;

    public TodoItem processResult(){
        String shortDescription = shortDescriptionField.getText().trim();
        String details = FullDescription.getText().trim();
        LocalDate dueDate = deadLinePiker.getValue();

        TodoItem newItem = new TodoItem(shortDescription,details,dueDate);

        TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }

}
