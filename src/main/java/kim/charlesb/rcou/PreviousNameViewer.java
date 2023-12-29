package kim.charlesb.rcou;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

class PreviousNameViewer extends VBox {
    private TextFlow textFlow;
    private Text boldUnderlineText;
    private Text normalText;
    
    public PreviousNameViewer(String bold, String normal) {
        boldUnderlineText = new Text(bold);
        boldUnderlineText.setStyle("-fx-underline: true; -fx-font-weight: bold; -fx-font-size: 9pt;");
        normalText = new Text("\n" + normal);
        normalText.setStyle("-fx-font-size: 9pt;");
        textFlow = new TextFlow(boldUnderlineText, normalText);
        textFlow.setStyle("-fx-text-alignment: center;");
        this.getChildren().addAll(textFlow);
    }
}