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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
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
    TextField usernameField;
    Button submitButton;
    Button helpButton;
    Label prevNameResults;
    HttpClient httpClient;
    Gson gson;
    TextFlow directionsTextFlow;
    Text boldText;
    Text regularText;
    VBox resultLayer;
    ProgressBar pb; //pb for aesthetics :)
    Runnable runnable;
    ImageView imageView;
    Alert helpAlert;
    String username;
    long usernameID;
    String displayName;
    CheckBox checkBox;
    StackPane stackPane;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public App() {
        root = new VBox(6);
        root.setPadding(new Insets(4, 4, 3, 4));
        root.setAlignment(Pos.CENTER);

        interactLayer = new HBox(4);
        interactLayer.setAlignment(Pos.CENTER);

        usernameField = new TextField("username");
        usernameField.setMinWidth(150);

        HBox.setHgrow(usernameField, Priority.ALWAYS);

        submitButton = new Button("Submit");
        submitButton.setMinHeight(12);
        submitButton.setMinWidth(54);
        submitButton.setMaxWidth(54);
        submitButton.setPadding(new Insets(4));

        imageView = new ImageView(new Image("file:resources/help.png"));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(14);

        helpButton = new Button("?"); // 
        helpButton.setFont(Font.font(Font.getDefault().getName(), FontWeight.EXTRA_BOLD, 16));
        helpButton.setPadding(new Insets(0));
        helpButton.setMinWidth(25);
        helpButton.setMaxWidth(25);
        helpButton.setMinHeight(25);
        helpButton.setMaxHeight(25);

        resultLayer = new VBox();
        resultLayer.setAlignment(Pos.CENTER);

        prevNameResults = new Label();
        prevNameResults.setStyle("-fx-text-alignment: center;");
        // so initial app launch text is centered since this is empty
        prevNameResults.setManaged(false);

        directionsTextFlow = new TextFlow();
        directionsTextFlow.setStyle("-fx-text-alignment: center;");

        boldText = new Text("");
        boldText.setStyle("-fx-font-weight: bold;");

        regularText = new Text("Submit a Roblox username to" + "\n" + "see their previous usernames.");

        httpClient = HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .followRedirects(Redirect.NORMAL)
            .build();

        gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

        pb = new ProgressBar();
        pb.setMaxHeight(10);
        pb.setMinHeight(10);
        pb.setMaxWidth(Double.MAX_VALUE); // maxwidth is small by default
        HBox.setHgrow(pb, Priority.ALWAYS);

        //ive tried to increase setWidth to make each tip be one line, but the width isnt working for the first show()
        helpAlert = new Alert(AlertType.CONFIRMATION,
            "- Submit a Roblox username (not their display name) to see their previous usernames." + "\n" +
            "- Spaces entered into the search box will be converted into an underscore." + "\n" +
            "- Press Enter while in the search box to instantly search.", 
            ButtonType.OK
        );
        helpAlert.setTitle("Directions & Help");
        helpAlert.setHeaderText("Directions & Help");

        runnable = doAPIcallsRunnable();

        checkBox = new CheckBox("OCR");
        checkBox.setPadding(new Insets(0, 7, 0 , 0));

        stackPane = new StackPane();
        StackPane.setAlignment(checkBox, Pos.CENTER_RIGHT);
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        stackPane.getChildren().addAll(usernameField, checkBox);
        interactLayer.getChildren().addAll(stackPane, helpButton, submitButton);
        resultLayer.getChildren().addAll(directionsTextFlow, prevNameResults);
        directionsTextFlow.getChildren().addAll(boldText, regularText);
        root.getChildren().addAll(interactLayer, resultLayer, pb);

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode().getName().equals("Enter")) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.start();
            } else if (event.getCode().getName().equals("ALT")) {
                if (checkBox.isSelected() == true) {
                    checkBox.setSelected(false);
                } else {
                    checkBox.setSelected(true);
                }
            }
        });

        submitButton.setOnAction((event -> {
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
        stage.setTitle("Check Previous Roblox Usernames");
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
            Platform.runLater(() -> { // default values
                regularText.setStyle("");                
                regularText.setManaged(true); // so everything is centered when regularText is empty
            });
            try {
                //if user typed a space, change it to an underscore
                String temp = usernameField.getText();
                username = "";
                for (int i = 0; i < temp.length(); i++) {
                    if (temp.charAt(i) == ' ') {
                        username += '_';
                    } else {
                        username += temp.charAt(i);
                    }
                }
                String requestBody = "{\"usernames\": [\"" + username + "\"], \"excludeBannedUsers\": true}";
                HttpRequest requestUsernameToID = HttpRequest.newBuilder()
                    .uri(URI.create("https://users.roblox.com/v1/usernames/users"))
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json")
                    .POST(BodyPublishers.ofString(requestBody))
                    .build();
                HttpResponse<String> response1 =
                    httpClient.send(requestUsernameToID, BodyHandlers.ofString());
                if (response1.statusCode() != 200) {
                    Platform.runLater(() -> {
                        boldText.setText("");
                        regularText.setText("Error: status code " + response1.statusCode());
                    });
                    throw new Exception("error: status code " + response1.statusCode());
                }
                UsernameResponse usernameResponse =
                    gson.fromJson(response1.body(), UsernameResponse.class);
                if (usernameResponse.data.length == 0) {
                    Platform.runLater(() -> {
                        boldText.setText("Username not found.");
                        regularText.setText("\nPlease check and try again.");
                    });
                    throw new Exception("username not found");
                }
                username = usernameResponse.data[0].name; // update to correct casing
                usernameID = usernameResponse.data[0].id;
                displayName = usernameResponse.data[0].displayName;

                // second API call
                String usernameHistoryURI = "https://users.roblox.com/v1/users/" +
                usernameID + "/username-history?limit=10&sortOrder=Asc";
                
                HttpRequest requestIDtoPrevNames = HttpRequest.newBuilder()
                .uri(URI.create(usernameHistoryURI))
                .build();
                
                HttpResponse<String> response2 =
                httpClient.send(requestIDtoPrevNames, BodyHandlers.ofString());
                
                if (response2.statusCode() != 200) {
                    Platform.runLater(() -> {
                        boldText.setText("");
                        regularText.setText("Error: status code " + response2.statusCode());
                    });
                    throw new Exception("error: status code " + response2.statusCode());
                }
                
                PreviousNamesResponse previousNamesResponse =
                gson.fromJson(response2.body(), PreviousNamesResponse.class);
                
                PreviousName[] pnrData = previousNamesResponse.data;
                
                // underline bold text also
                Platform.runLater(setBoldTextRunnable(displayName, username));
                
                if (pnrData.length == 0) {
                    Platform.runLater(() -> regularText.setText("\nNo previous usernames found."));
                    throw new Exception("no previous usernames found");
                }
                
                String prevNames = "";
                for (int i = 0; i < pnrData.length; i++) {
                    prevNames += pnrData[i].name + "\n";
                }
                
                prevNameResults.setManaged(true);
                Platform.runLater(prevNameResultsRunnable(prevNames));
                //regularText.setStyle("-fx-underline: true;");
                Platform.runLater(() -> regularText.setText(""));
                regularText.setManaged(false);
                //System.out.println(pnrData[0].name);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    prevNameResults.setText("");
                    prevNameResults.setManaged(false);
                });
                System.out.println(e);
            } finally {
                Platform.runLater(() -> stage.sizeToScene());
            }
        };
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
        
        return a;
    }

    /**
     * Returns a Runnable that sets prevNameResults to the previous usernames received.
     * @param prevNames the String that contains the previous usernames
     * @return a Runnable that sets prevNameResults to the previous usernames received
     */
    private Runnable prevNameResultsRunnable(String prevNames) {
        return () -> prevNameResults.setText(prevNames);
    }

    /**
     * Returns a Runnable that sets boldText text and its style.
     * @param displayName the given user's display name
     * @param username the given user's display name
     * @return Runnable that sets boldText text and its style
     */
    private Runnable setBoldTextRunnable(String displayName, String username) {
        return () -> {
            boldText.setText(displayName + " (@" + username + ")");
            boldText.setStyle("-fx-underline: true; -fx-font-weight: bold;");
        };
    }
} // ApiApp
