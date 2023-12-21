package kim.charlesb.rcou;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

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
    TextFlow directionsTextFlow;
    Text boldText;
    Text regularText;
    VBox resultLayer;
    ProgressBar progressBar; //progressBar for aesthetics :)
    BorderPane progressBarLayer;
    Runnable runnable;
    Alert helpAlert;
    String displayName;
    CheckBox checkBox;
    StackPane stackPane;
    String prevNames;
    HBox checkBoxLayer;
    Separator separator;
    TilePane tilePane;
    
    /**
    * Constructs an {@code App} object. This default (i.e., no argument)
    * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
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
        
        resultLayer = new VBox();
        resultLayer.setAlignment(Pos.CENTER);
        
        directionsTextFlow = new TextFlow();
        directionsTextFlow.setStyle("-fx-text-alignment: center;");
        
        boldText = new Text("");
        //boldText.setStyle("-fx-font-weight: bold;");
        boldText.setStyle("-fx-underline: true; -fx-font-weight: bold;");
        
        regularText = new Text("Submit a Roblox username to" + "\n" + "see their previous usernames.");
        
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
        "- Submit a Roblox username (not their display name) to see their previous usernames." + "\n" +
        "- Press Enter while in the search box to instantly search." + "\n" +
        "\n" +
        "                     " + "-=[ SINGLE-SEARCH MODE (default) ]=-" + "\n" +
        "- Spaces entered into the search box will be converted into an underscore." + "\n" +
        "\n" +
        "                                 " + "-=[ OCR MODE ]=-" + "\n",
        ButtonType.OK
        );
        helpAlert.setTitle("Directions & Help");
        helpAlert.setHeaderText("Directions & Help");
        
        runnable = doAPIcallsRunnable();
        
        checkBox = new CheckBox("OCR");
        //checkBox.setPadding(new Insets(0, 7, 0, 0)); //7 away from left border of textField
        
        stackPane = new StackPane();
        StackPane.setAlignment(checkBox, Pos.CENTER_RIGHT);
        HBox.setHgrow(stackPane, Priority.ALWAYS);

        checkBoxLayer = new HBox(5);
        checkBoxLayer.setPadding(new Insets(0, 8, 0, 8));

        separator = new Separator();
        separator.setPadding(new Insets(0, 4, 0, 4));
        HBox.setHgrow(separator, Priority.ALWAYS);

        //tilePane made in submitButton onAction
    } // ApiApp
    
    /** {@inheritDoc} */
    @Override
    public void init() {
        //stackPane.getChildren().addAll(textField, checkBox);
        interactLayer.getChildren().addAll(textField, helpButton, submitButton);
        resultLayer.getChildren().addAll(directionsTextFlow); //prevNameResults);
        directionsTextFlow.getChildren().addAll(boldText, regularText);
        checkBoxLayer.getChildren().addAll(checkBox);
        progressBarLayer.setBottom(progressBar);
        root.getChildren().addAll(interactLayer, checkBoxLayer, separator, resultLayer, progressBarLayer);
        
        textField.setOnKeyPressed(event -> {
            if (event.getCode().getName().equals("Enter")) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.start();
            } else if (event.getCode().getName().equals("Alt")) {
                if (checkBox.isSelected() == true) {
                    checkBox.setSelected(false);
                } else {
                    checkBox.setSelected(true);
                }
            }
        });
        
        submitButton.setOnAction((event -> {
            tilePane = new TilePane();
            tilePane.setHgap(4);
            tilePane.setPrefColumns(4);
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.start();
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
    * Returns a Runnable that takes in a user-specified username, then
    * requests for the userID for that username. Then, requests for all
    * of the previous usernames for that userID. Presents the data accordingly.
    * @return Runnable for a thread to execute
    */
    private Runnable doAPIcallsRunnable() {
        return () -> {
            submitButton.setDisable(true);
            System.out.println(textField.getWidth());
            try {
                String inputFormatted = "";
                String username = "";
                if (checkBox.isSelected()) {
                    System.out.println("OCR mode");
                    inputFormatted = separateNames(textField.getText());
                } else {
                    System.out.println("single-search mode");
                    //if user typed a space, change it to an underscore
                    String temp = textField.getText();
                    
                    username = "";
                    for (int i = 0; i < temp.length(); i++) {
                        if (temp.charAt(i) == ' ') {
                            username += '_';
                        } else {
                            username += temp.charAt(i);
                        }
                    }
                    inputFormatted = "\"" + username + "\"";
                }
                String requestBody = "{\"usernames\": [" + inputFormatted + "], \"excludeBannedUsers\": true}";
                HttpRequest usernameToIDRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://users.roblox.com/v1/usernames/users"))
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

                HttpResponse<String> response1 = httpClient.send(usernameToIDRequest, BodyHandlers.ofString());
                UsernameResponse usernameResponse = gson.fromJson(response1.body(), UsernameResponse.class);

                if (response1.statusCode() != 200) {
                    setBoldText("");
                    setRegularText("Error: status code " + response1.statusCode());
                    throw new Exception("error: status code " + response1.statusCode());
                }

                if (usernameResponse.data.length == 0) {
                    setBoldText("Username(s) not found.");
                    setRegularText("Please check and try again.");
                    throw new Exception("username(s) not found: " + inputFormatted);
                }
                username = usernameResponse.data[0].name; // update to correct casing                
                displayName = usernameResponse.data[0].displayName;
                prevNames = "";
                
                // second API call
                // for every user responded with
                for (int i = 0; i < usernameResponse.data.length; i++) {
                    getPrevNamesFromID(usernameResponse.data[i].id, usernameResponse.data[i].name);
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    System.out.println("regularText: " + regularText.getText());
                    stage.sizeToScene();
                });
                System.out.println(e);
            } finally {
                System.out.println();
            }
        };
    }
    
    private void getPrevNamesFromID(long usernameID, String username) {
        try {
            String usernameHistoryURI = "https://users.roblox.com/v1/users/" + usernameID + "/username-history?limit=10&sortOrder=Asc";
            HttpRequest prevNamesFromIDRequest = HttpRequest.newBuilder()
            .uri(URI.create(usernameHistoryURI))
            .build();
            HttpResponse<String> response2 = httpClient.send(prevNamesFromIDRequest, BodyHandlers.ofString());
            
            if (response2.statusCode() != 200) {                
                setBoldText("");
                setRegularText("Error: status code " + response2.statusCode());
                
                throw new Exception("error: status code " + response2.statusCode());
            }
            
            PreviousNamesResponse previousNamesResponse = gson.fromJson(response2.body(), PreviousNamesResponse.class);
            PreviousName[] pnrData = previousNamesResponse.data;
            
            
            setBoldText(displayName + " (@" + username + ")");
            
            if (pnrData.length == 0) {
                setRegularText("No previous usernames found.");
                throw new Exception("no previous usernames found");
            }
            
            for (int i = 0; i < pnrData.length - 1; i++) {
                prevNames += pnrData[i].name + "\n";
            }
            prevNames += pnrData[pnrData.length - 1].name; //last index no "\n"
            
            setRegularText(prevNames);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            Platform.runLater(() -> {
                submitButton.setDisable(false);
                System.out.println("regularText: " + regularText.getText());
                stage.sizeToScene();
            });
        }
    }
    
    private String formatInput(String a) {
        String fin = "";
        if (checkBox.isSelected() == true) {
            fin = separateNames(a);
        }
        return a;
    }
    
    private String separateNames(String a) {
        String fin = "";
        try {
            if (isAtSign(a.charAt(0))) {
                // ERROR!
            } else {
                int start = a.indexOf("@") + 1;
                int end = -1;
                while (start != 0) { // since start is + 1
                    end = a.indexOf(" ", start);
                    if (end == -1) { // (for last iter) there is no space at the end of the input string
                        end = a.length();
                    }
                    fin += "\"" + a.substring(start, end) + "\", "; // {"name", }
                    a = a.substring(end, a.length());
                    start = a.indexOf("@") + 1;
                    //System.out.println(fin);
                }
                fin = fin.substring(0, fin.length() - 2); //remove last ", "
                System.out.println("done: " + fin);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return fin;
    }
    
    private static boolean isAtSign(char a) {
        if (a == '@') {
            return true;
        }
        return false;
    }
    
    private void setBoldText(String a) {
        Platform.runLater(() -> {
            boldText.setText(a + "\n");
        });
    }
    
    private void setRegularText(String a) {
        Platform.runLater(() -> {
            regularText.setText(a);
        });
    }
} // ApiApp
