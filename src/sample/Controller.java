package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import sample.Analyze.Anal;

import java.io.*;

public class Controller {

    @FXML
    private TextArea TextArea_InCode;

    @FXML
    private TextArea TextArea_CryptCode;

    @FXML
    private TextArea TextArea_ReservedWords;

    @FXML
    private TextArea TextArea_Identifiers;

    @FXML
    private TextArea TextArea_Constants;

    @FXML
    private TextArea TextArea_Operations;

    @FXML
    private TextArea TextArea_Dividers;

    @FXML
    private TextArea TextArea_ErrorTokens;

    @FXML
    private TextArea TextArea_TableOfTokens;

    @FXML
    private TextArea TextArea_SyntaxError;

    @FXML
    private void click(ActionEvent event) {
        Anal anal = new Anal();
        try {
            anal.makeAnalyze(TextArea_InCode.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextArea_CryptCode.setText(anal.getOutSource());
        TextArea_ReservedWords.setText(anal.getTable("ReservedWords"));
        TextArea_Identifiers.setText(anal.getTable("Identifiers"));
        TextArea_Constants.setText(anal.getTable("Constants"));
        TextArea_Operations.setText(anal.getTable("Operations"));
        TextArea_Dividers.setText(anal.getTable("Dividers"));
        TextArea_ErrorTokens.setText(anal.getTable("ErrorTokens"));
        TextArea_SyntaxError.setText(anal.syntaxTest(anal.getListOfAllTokens())+anal.getSemanticReport());
        TextArea_TableOfTokens.setText(anal.printListOfAllTokens());
    }
}
