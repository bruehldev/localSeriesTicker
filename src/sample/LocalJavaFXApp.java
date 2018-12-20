package sample;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.json.JSONArray;

public class LocalJavaFXApp extends Application {

    /* Parameters to add series in right order */
    TableView tableView = new TableView();
    private ObservableList<TVSeries> TVlist;
    int selectedID = 0;
    TMDBController TMDB = new TMDBController();
    JSONArray SearchArr;
    ArrayList<TVSeries> searchResult = new ArrayList<TVSeries>();
    final TVSeriesService service = new TVSeriesService();
    // Add after selection
    ObservableList<String> options =
            FXCollections.observableArrayList();
    ComboBox resultComboBox = new ComboBox(options);
    Button buttonAdd = new Button("Add");
    // Search textfield
    final TextField searchTextField = new TextField();
    final TextField seasonTextField = new TextField();
    final TextField episodeTextField = new TextField();
    Label note = new Label("Please select an entry");
    Button confirmEditButton = new Button("Confirm");
    Label episodeLabel = new Label("Please select an entry");
    Label TVName = new Label();
    Label seasonLabel = new Label("Season");
    String lastUpdate = "No information";
    String nextEpisode = "No information";

    private void init(Stage primaryStage) {
        // Initiate stage
        Group root = new Group();
        primaryStage.setScene(new Scene(root,800,1000));
        VBox vbox = new VBox(5);
        vbox.setPadding(new Insets(12));
        tableView.setPrefSize(700,600);
        tableView.centerShapeProperty();
        tableView.setCenterShape(true);




        // Refresh button
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                service.restart();
            }
        });

        // Edit button
        Button showButton = new Button("Edit");
        showButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                if(!tableView.getSelectionModel().getSelectedItems().isEmpty()){
                    TVlist = tableView.getSelectionModel().getSelectedItems();
                    TVName = new Label(TVlist.get(0).getName());
                    seasonTextField.setPromptText("Season");
                    seasonTextField.setMinWidth(25);

                    episodeTextField.setPromptText("Episode");
                    episodeTextField.setMinWidth(25);
                    vbox.getChildren().addAll(TVName,seasonLabel,seasonTextField,episodeLabel,episodeTextField,confirmEditButton);
                    note.setVisible(false);
                }
                else {
                    vbox.getChildren().addAll(note);
                }

            }
        });

        // Confirmeditbutton
        confirmEditButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                seasonTextField.getText();
                String sqlSeason = "Update seriesTicker.TVSeries set current_season = '"+seasonTextField.getText()+"' where id = '"+TVlist.get(0).getId()+"'";
                String sqlEpisode = "Update seriesTicker.TVSeries set current_episode = '"+episodeTextField.getText()+"' where id = '"+TVlist.get(0).getId()+"'";
                DBConnector.executeSQL(sqlSeason);
                DBConnector.executeSQL(sqlEpisode);
                vbox.getChildren().removeAll(confirmEditButton,episodeTextField,episodeLabel,seasonTextField,seasonLabel,TVName);
                service.restart();
            }
        });

        // Setup add TextField
        searchTextField.setPromptText("TV Series");
        searchTextField.setMinWidth(25);


        // Search button
        Button buttonSearch = new Button("Search");
        buttonSearch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!searchTextField.getText().trim().isEmpty()) {
                    // Form correct syntax of query
                    String query = searchTextField.getText().replaceAll(" ", "+");

                    // Download JSON with query
                    SearchArr = TMDB.resultJSONArray(query);

                    // Add results to combobox
                    for (int i = 0; i < SearchArr.length(); i++) {
                        TVSeries currentResult = new TVSeries(SearchArr.getJSONObject(i).getInt("id"),
                                SearchArr.getJSONObject(i).getString("name"));
                        searchResult.add(currentResult);
                        resultComboBox.getItems().add(currentResult.getName());
                    }

                    // Add to select from reults
                    vbox.getChildren().addAll(resultComboBox, buttonAdd);
                    //service.restart();
                }
            }
        });


        /* Listenor on add Button (add to database) */
        buttonAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                // Retrieve selected TV series from combobox
                for (int i = 0; i < searchResult.size(); i++) {
                    if (resultComboBox.getValue().toString().replaceAll("\\*","").matches(searchResult.get(i).getName().replaceAll("\\*",""))) {
                        selectedID = searchResult.get(i).getId();
                        break;
                    }
                }

                // Add to database
                String sql = null;
                System.out.println("selectedID: " + selectedID);
                System.out.println("selectedName: " + resultComboBox.getValue());
                System.out.println("comboboxID: " + resultComboBox.getId());

                // Getting all information from JSON
                try {
                    lastUpdate = TMDB.getNextEpAr(selectedID).getString("air_date");
                }catch (Exception e3) {
                    System.out.println("No last Update");
                    System.out.println(e3);
                }
                try {
                    nextEpisode = TMDB.downloadResultJSON(selectedID).getString("air_date");
                }catch (Exception e4) {
                    System.out.println("No episode");
                    System.out.println(e4);
                }





                sql = "INSERT INTO SeriesTicker.TVSeries " + "VALUES (" + selectedID + ", '" + resultComboBox.getValue().toString() + "', 1, 1, '" + lastUpdate + "','" + nextEpisode + "')";
                DBConnector.executeSQL(sql);

                // Reset search
                searchTextField.clear();
                vbox.getChildren().removeAll(resultComboBox, buttonAdd);
                resultComboBox.valueProperty().set(null);
                options.clear();
                searchResult.clear();
                SearchArr = null;
                selectedID = 0;
                lastUpdate = "No information";
                nextEpisode = "No information";
                service.restart();
            }
        });



        /* Add to vbox & set indicators */
        vbox.getChildren().addAll(tableView, refreshButton, searchTextField, buttonSearch, showButton);
        Region veil = new Region();
        veil.setStyle("-fx-background-color: rgba(255, 255, 255, 1)");
        ProgressIndicator p = new ProgressIndicator();
        p.setMaxSize(150, 150);


        /*  Define table columns: */
        // ID column
        TableColumn IDCol = new TableColumn();
        IDCol.setText("id");
        IDCol.setCellValueFactory(new PropertyValueFactory("id"));
        tableView.getColumns().add(IDCol);
        // name column
        TableColumn nameCol = new TableColumn();
        nameCol.setText("name");
        nameCol.setCellValueFactory(new PropertyValueFactory("name"));
        tableView.getColumns().add(nameCol);
        // current_season column
        TableColumn current_seasonCol = new TableColumn();
        current_seasonCol.setText("current_season");
        current_seasonCol.setCellValueFactory(new PropertyValueFactory("current_season"));
        tableView.getColumns().add(current_seasonCol);
        // current_episode column
        TableColumn current_episodeCol = new TableColumn();
        current_episodeCol.setText("current_episode");
        current_episodeCol.setCellValueFactory(new PropertyValueFactory("current_episode"));
        tableView.getColumns().add(current_episodeCol);
        // nextEpisodeDate column
        TableColumn nextEpisodeDateCol = new TableColumn();
        nextEpisodeDateCol.setText("nextEpisodeDate");
        nextEpisodeDateCol.setCellValueFactory(new PropertyValueFactory("nextEpisodeDate"));
        tableView.getColumns().add(nextEpisodeDateCol);
        // lastUpdate column
        TableColumn lastUpdateCol = new TableColumn();
        lastUpdateCol.setText("lastUpdate");
        lastUpdateCol.setCellValueFactory(new PropertyValueFactory("lastUpdate"));
        tableView.getColumns().add(lastUpdateCol);

        // Set bindings
        p.progressProperty().bind(service.progressProperty());
        veil.visibleProperty().bind(service.runningProperty());
        p.visibleProperty().bind(service.runningProperty());
        tableView.itemsProperty().bind(service.valueProperty());

        // Add stages
        StackPane stack = new StackPane();
        stack.getChildren().addAll(vbox, veil, p);
        root.getChildren().add(stack);
        service.start();
    }

    /**
     * Start stage
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }

    /**
     * Main methode
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

}