package kim.charlesb.rcou;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

class PreviousNameViewer extends VBox {
    private VBox root;
    private TextFlow textFlow;
    private Text boldText;
    private Text normalText;
    
    public PreviousNameViewer(String bold, String normal) {
        boldText.setText(normal);
        normalText.setText("\n" + normal);
        textFlow.getChildren().addAll(boldText, normalText);
        root.getChildren().addAll(textFlow);
    }
}