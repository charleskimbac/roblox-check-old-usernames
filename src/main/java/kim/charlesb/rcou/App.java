package kim.charlesb.rcou;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
* Application that takes in a Roblox username, converts the username
* into their userID, then shows their previous usernames, if any exist.
*/
public class App extends Application {
    Stage stage;
    Scene scene;
    VBox root;
    HBox interactLayer;
    TextField textField;
    Button submitButton;
    Button helpButton;
    HttpClient httpClient;
    Gson gson;
    TextFlow textFlow;
    ProgressBar progressBar; //progressBar for aesthetics :)
    BorderPane progressBarLayer;
    Runnable runnable;
    Alert helpAlert;
    CheckBox checkBox;
    StackPane stackPane;
    HBox checkBoxLayer;
    Separator separator;
    TilePane tilePane;
    int previousNameShowLimit;
    
    /**
    * Constructs an {@code App} object.
    */
    public App() {
        root = new VBox(6);
        root.setPadding(new Insets(4, 4, 3, 4));
        root.setAlignment(Pos.CENTER);
        
        interactLayer = new HBox(4);
        interactLayer.setAlignment(Pos.CENTER);
        
        textField = new TextField("username");
        textField.setMinWidth(150);
        textField.setMaxWidth(Double.MAX_VALUE); // maxwidth is small by default
        
        HBox.setHgrow(textField, Priority.ALWAYS);
        
        submitButton = new Button("Submit");
        submitButton.setMinHeight(12);
        submitButton.setMinWidth(54);
        submitButton.setMaxWidth(54);
        submitButton.setPadding(new Insets(4));
        
        helpButton = new Button("?"); // 
        helpButton.setFont(Font.font(Font.getDefault().getName(), FontWeight.EXTRA_BOLD, 16));
        helpButton.setPadding(new Insets(0));
        helpButton.setMinWidth(25);
        helpButton.setMaxWidth(25);
        helpButton.setMinHeight(25);
        helpButton.setMaxHeight(25);

        textFlow = new TextFlow();
        textFlow.setStyle("-fx-text-alignment: center;");
        
        httpClient = HttpClient.newBuilder()
        .version(Version.HTTP_2)
        .followRedirects(Redirect.NORMAL)
        .build();
        
        gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
        
        progressBar = new ProgressBar();
        progressBar.setMaxHeight(10);
        progressBar.setMinHeight(10);
        progressBar.setMaxWidth(Double.MAX_VALUE); // maxwidth is small by default
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        progressBarLayer = new BorderPane();
        progressBarLayer.setPadding(new Insets(5, 0, 0, 0));
        VBox.setVgrow(progressBarLayer, Priority.ALWAYS);
        
        //tried to increase setWidth to make each tip be one line, but the width isnt working for the first show()
        helpAlert = new Alert(AlertType.CONFIRMATION,
        "Submit Roblox username(s) (not their display name) into the text field to see their previous usernames. \n" +
        "Press Enter while in the search box to instantly search. \n" +
        "\n" +
        "                     " + "-=[ SINGLE-SEARCH MODE (default) ]=- \n" +
        "- Spaces entered into the search box will be converted into an underscore. \n" +
        "\n" +
        "                                 " + "-=[ OCR MODE ]=-" + "\n" +
        "- The search should be formatted as `displayName1 @username1 displayName2 @username2 etc...` \n" +
        "- It is recommended to use Copilot/Bing AI to abstract the text from a screenshot of the Roblox player (`ESC`) menu. \n" +
        "- Recommended prompt: \"Give me all of the text in the image in plain text and in one line. " +
            "Make sure to include the parts with the '@' sign. Do not use commas, but separate each word with a space.\" \n" +
        "\n" +
        "https://github.com/charleskimbac/roblox-check-old-usernames",
        ButtonType.OK
        );
        helpAlert.setTitle("Directions & Help");
        helpAlert.setHeaderText("Directions & Help");
        
        runnable = beginSearchRunnable();
        
        checkBox = new CheckBox("OCR");
        
        stackPane = new StackPane();
        StackPane.setAlignment(checkBox, Pos.CENTER_RIGHT);
        HBox.setHgrow(stackPane, Priority.ALWAYS);

        checkBoxLayer = new HBox(5);
        checkBoxLayer.setPadding(new Insets(0, 8, 0, 8));

        separator = new Separator();
        separator.setPadding(new Insets(0, 4, 0, 4));
        HBox.setHgrow(separator, Priority.ALWAYS);

        tilePane = new TilePane();
        tilePane.setPadding(new Insets(10, 4, 10, 4));
        tilePane.setHgap(20);
        tilePane.setVgap(8);
        tilePane.setPrefColumns(1); //1 by default since only 1 TextFlow initially, changed to 4 later (if count > 4)
        tilePane.setAlignment(Pos.CENTER); //if < 4 TextFlows, then center. but > 4, centerleft

        previousNameShowLimit = 5; //5 by default
    } // ApiApp
    
    /** {@inheritDoc} */
    @Override
    public void init() {
        interactLayer.getChildren().addAll(textField, helpButton, submitButton);

        Text boldUnderlineText = new Text("");        
        boldUnderlineText.setStyle("-fx-underline: true; -fx-font-weight: bold;");
        Text regularText = new Text("Submit a Roblox username to" + "\n" + "see their previous usernames.");
        textFlow.getChildren().addAll(boldUnderlineText, regularText);

        checkBoxLayer.getChildren().addAll(checkBox);
        progressBarLayer.setBottom(progressBar);
        tilePane.getChildren().addAll(textFlow); //initial message
        root.getChildren().addAll(interactLayer, checkBoxLayer, separator, tilePane, progressBarLayer);
        
        root.setOnKeyPressed(event -> {
            String eventCodeName = event.getCode().getName();
            if (eventCodeName.equals("Enter")) {
                startSearch();
            } else if (eventCodeName.equals("Alt")) {
                if (checkBox.isSelected() == true) {
                    checkBox.setSelected(false);
                } else {
                    checkBox.setSelected(true);
                }
            }
        });
        
        submitButton.setOnAction((event -> {
            startSearch();
        }));
        
        helpButton.setOnAction((event) -> {
            helpAlert.show();
        });
    }
    
    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        scene = new Scene(root);
        stage.setTitle("Roblox: Check Old Usernames");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * Empties existing TilePane, then opens a new thread to start finding previous usernames.
     */
    private void startSearch() {
        if (submitButton.isDisable()) { //if already running
            return;
        }
        submitButton.setDisable(true);
        textField.requestFocus();
        tilePane.getChildren().clear(); //reset tilePane
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
    * Returns a Runnable that takes in the user-specified username(s), then
    * requests for their respective userID(s). Calls the next method (display previous usernames)
    * for each userID found.
    * @return Runnable for a thread to execute
    */
    private Runnable beginSearchRunnable() {
        return () -> {
            String[] input = new String[0];
            if (checkBox.isSelected()) { //OCR mode
                if (textField.getText().length() == 0) {
                    handleUnexpectedInput();
                    return;
                }
                input = separateNames(textField.getText());
                if (input.length == 0) { //if input is in bad format
                    handleUnexpectedInput();
                    return;
                }
            } else { //single mode
                //if user typed a space, change it to an underscore
                String actual = textField.getText();
                if (actual.length() > 25) { //single search terms should be less than 25chars
                    handleUnexpectedInput();
                    return;
                }
                String converted = "";
                for (int i = 0; i < actual.length(); i++) {
                    if (actual.charAt(i) == ' ') {
                        converted += '_';
                    } else {
                        converted += actual.charAt(i);
                    }
                }
                input = new String[]{converted};
            }
            
            int count = input.length;
            updateTilePaneColumnsAlignment(count);
            
            for (int i = 0; i < count; i++) {
                try {
                    String requestBody = "{\"usernames\": [\"" + input[i] + "\"], \"excludeBannedUsers\": true}";
                    HttpRequest usernameToIDRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://users.roblox.com/v1/usernames/users"))
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json")
                    .POST(BodyPublishers.ofString(requestBody))
                    .build();
                    
                    HttpResponse<String> response1 = httpClient.send(usernameToIDRequest, BodyHandlers.ofString());
                    UsernameResponse usernameResponse = gson.fromJson(response1.body(), UsernameResponse.class);
                    
                    if (response1.statusCode() != 200) {
                        addPreviousNameViewer("", "Error 1/2: status code " + response1.statusCode());
                    } else {
                        if (usernameResponse.data.length == 0) {
                            addPreviousNameViewer("@" + input[i] + " not found.", "Please check and try again.");
                            continue;
                        }
                        // second API call
                        getPrevNamesFromID(usernameResponse.data[0].id, usernameResponse.data[0].name, usernameResponse.data[0].displayName);
                    }
                } catch (IOException|InterruptedException e) {
                    System.out.println(e);
                    Platform.runLater(() -> {
                        submitButton.setDisable(false);
                        stage.sizeToScene();
                    });
                }
            }
            Platform.runLater(() -> submitButton.setDisable(false));
        };
    }

    /**
     *  Handles unexpected input.
     */
     private void handleUnexpectedInput() {
        addPreviousNameViewer("An error occurred.", "Double check the search mode or search format.");
        Platform.runLater(() -> {
            submitButton.setDisable(false);
            updateTilePaneColumnsAlignment(1);
            stage.sizeToScene();
        });
    }
    
    /**
     * Changes tilePane prefColumns and alignment to ensure no extra empty space is present if count < 4.
     * @param count number of tilePane children to be added
     */
     private void updateTilePaneColumnsAlignment(int count) {
        if (count < 4) {
            tilePane.setPrefColumns(count); //so no extra space is present
            tilePane.setAlignment(Pos.CENTER);
        } else {
            tilePane.setPrefColumns(4);
            tilePane.setAlignment(Pos.CENTER_LEFT);
        }
    }
    
    /**
     * Finds the previous usernames given the usernameID, then calls addPreviousNameViewer().
     * @param usernameID
     * @param username
     * @param displayName
     */
    private void getPrevNamesFromID(long usernameID, String username, String displayName) {
        String bold = "";
        String regular = "";
        try {
            String usernameHistoryURI = "https://users.roblox.com/v1/users/" + usernameID + "/username-history?sortOrder=Asc&limit=100";
            HttpRequest prevNamesFromIDRequest = HttpRequest.newBuilder()
            .uri(URI.create(usernameHistoryURI))
            .build();
            HttpResponse<String> response2 = httpClient.send(prevNamesFromIDRequest, BodyHandlers.ofString());
            
            if (response2.statusCode() != 200) {                
                addPreviousNameViewer("", "Error 2/2: status code " + response2.statusCode());
                throw new IOException("Error 2/2: status code " + response2.statusCode());
            }
            
            PreviousNamesResponse previousNamesResponse = gson.fromJson(response2.body(), PreviousNamesResponse.class);
            PreviousName[] pnrData = previousNamesResponse.data;
            
            bold = displayName + " (@" + username + ")";
            
            if (pnrData.length == 0) {
                regular = "No previous usernames found.";
            } else {
                for (int i = 0; i < previousNameShowLimit && i < pnrData.length; i++) { //up to previousNameShowLimit shown
                    regular += pnrData[i].name + "\n";
                    if (i == previousNameShowLimit - 1 && pnrData.length > previousNameShowLimit) { //if there are more than previousNameShowLimit, show how many more
                        regular += "+" + (pnrData.length - previousNameShowLimit) + "  "; //+2 spaces for next line
                    }
                }
                regular.substring(0, regular.length() - 2); //remove last newline
            }
            addPreviousNameViewer(bold, regular);
        } catch (IOException|InterruptedException e) {
            System.out.println(e);
        } finally {
            Platform.runLater(() -> {             
                stage.sizeToScene();
            });
        }
    }
    
    /**
    * Adds a PreviousNameViewer to the tilePane.
    * @param bold
    * @param regular
    */
    private void addPreviousNameViewer(String bold, String regular) {
        Platform.runLater(() -> {
            PreviousNameViewer previousNameViewer = new PreviousNameViewer(bold, regular);
            tilePane.getChildren().addAll(previousNameViewer);
        });
    }
    
    /**
    * Separates the input string into an array of usernames only.
    * @param a
    * @return
    */
    private String[] separateNames(String a) {
        if (a.length() == 0) {
            handleUnexpectedInput();
            return new String[0];
        }
        ArrayList<String> fin = new ArrayList<>();
        int start = a.indexOf("@") + 1;
        int end = -1;
        while (start != 0) { // since start is + 1
            end = a.indexOf(" ", start);
            if (end == -1) { // (for last iter) there is no space at the end of the input string
                end = a.length();
            }
            fin.add(a.substring(start, end));
            a = a.substring(end, a.length());
            start = a.indexOf("@") + 1;
        }
        return fin.toArray(new String[0]);
    }
} // ApiApp
