package com.max.ToDoList;

import datamodel.TodoData;
import datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Predicate;

public class Controller {
    private List<TodoItem> todoItems;
    @FXML
    private ListView<TodoItem> todoListView;
    @FXML
    private TextArea itemDetailsTextArea;
    @FXML
    private Label deadLineId;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;
    @FXML
    private FilteredList<TodoItem> filteredList;
    @FXML
    private Predicate<TodoItem> wantAllItem;
    @FXML
    private Predicate<TodoItem> todayItem;


    public void initialize(){
      /*  TodoItem item4 = new TodoItem("Meeting","meeting with the clients",
                LocalDate.of(2020, Month.SEPTEMBER,12));
        TodoItem item1 = new TodoItem("My Birth Card","By Birthday Card",
                LocalDate.of(2020, Month.AUGUST,15));
        TodoItem item2 = new TodoItem("Assignment ","Complete Assignment of college",
                LocalDate.of(2020, Month.SEPTEMBER,20));
        TodoItem item5 = new TodoItem("Cleaning","Cleaning ward rob",
                LocalDate.of(2020, Month.AUGUST,8));
        todoItems = new ArrayList<TodoItem>();
        TodoItem item3 = new TodoItem("Data Model","Complete my project on data model",
                LocalDate.of(2020, Month.AUGUST,19));
        todoItems.add(item1);
        todoItems.add(item2);
        todoItems.add(item3);
        todoItems.add(item4);
        todoItems.add(item5);

        TodoData.getInstance().setTodoItems(todoItems);*/

        listContextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteTodoItem(item);
            }
        });
        listContextMenu.getItems().setAll(deleteItem);


        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
                if(newValue != null){
                    TodoItem todoItem = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(todoItem.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    deadLineId.setText(df.format(todoItem.getDueDate()));
                }

            }
        });

        wantAllItem = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return true;
            }
        };
        todayItem = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return (item.getDueDate().equals(LocalDate.now()));
            }
        };
        filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(),wantAllItem);
        SortedList<TodoItem> sortedList = new SortedList<TodoItem>(filteredList,
                new Comparator<TodoItem>() {
                    @Override
                    public int compare(TodoItem o1, TodoItem o2) {
                        return o2.getDueDate().compareTo(o1.getDueDate());
                    }
                });

        todoListView.setItems(sortedList);
     //   todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
     //   todoListView.setItems(TodoData.getInstance().getTodoItems());
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> param) {
                ListCell<TodoItem> cell = new ListCell<TodoItem>(){
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if(item.getDueDate().isBefore((LocalDate.now()))){
                                setTextFill(Color.RED);
                            } else if(item.getDueDate().isAfter((LocalDate.now()))){
                                setTextFill(Color.GREEN);
                            }else{
                                setTextFill(Color.BLUE);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) ->{
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }
        });
    }

    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog= new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Todo item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialogue.fxml"));
        try{
           // Parent root = FXMLLoader.load(getClass().getResource("todoItemDialogue.fxml"));
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if((result.isPresent()) && (result.get() == ButtonType.OK)){
          DialogController controller = fxmlLoader.getController();
          TodoItem newItem = controller.processResult();
        //  todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
          todoListView.getSelectionModel().select(newItem);

        }
    }


    @FXML
    public void deleteTodoItem(TodoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item: "+item.getShortDescription());
        alert.setContentText("Are you sure to delete ?");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)){
            TodoData.getInstance().deleteTodoItem(item);
        }
    }
  /*  @FXML
    public void deleteAllTodoItem(TodoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item: "+item.getShortDescription());
        alert.setContentText("Are you sure to delete ?");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)){
            TodoData.getInstance().deleteAllItem(item);
        }
    }
*/
    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            if (keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteTodoItem(selectedItem);
            }
        }
    }

    @FXML
    public void deleteKeyPressed(){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            deleteTodoItem(selectedItem);
        }
    }

    public void handleClickListView(){
      TodoItem item = todoListView.getSelectionModel().getSelectedItem();
       itemDetailsTextArea.setText(item.getDetails());
       deadLineId.setText(item.getDueDate().toString());
        /*StringBuilder sb = new StringBuilder(item.getDetails());
        sb.append("\n\n\n\n");
        sb.append("Due : ");
        sb.append(item.getDueDate().toString());
        itemDetailsTextArea.setText(sb.toString());*/
    }

    public void handleFilterButton(){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(todayItem);
            if(filteredList.isEmpty()){
                itemDetailsTextArea.clear();
                deadLineId.setText("");
            } else if(filteredList.contains(selectedItem)){
                todoListView.getSelectionModel().select(selectedItem);
            } else {
                todoListView.getSelectionModel().selectFirst();
            }
        }else {
            filteredList.setPredicate(wantAllItem);
            todoListView.getSelectionModel().select(selectedItem);
        }
    }

    @FXML
    public void handleExit(){
        Platform.exit();
    }

}
