package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import sample.Analyze.AnalyzeClass;

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
        AnalyzeClass analyzeClass = new AnalyzeClass();
        try {
            analyzeClass.makeAnalyze(TextArea_InCode.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextArea_CryptCode.setText(analyzeClass.getOutSource());
        TextArea_ReservedWords.setText(analyzeClass.getTable("ReservedWords"));
        TextArea_Identifiers.setText(analyzeClass.getTable("Identifiers"));
        TextArea_Constants.setText(analyzeClass.getTable("Constants"));
        TextArea_Operations.setText(analyzeClass.getTable("Operations"));
        TextArea_Dividers.setText(analyzeClass.getTable("Dividers"));
        TextArea_ErrorTokens.setText(analyzeClass.getTable("ErrorTokens"));
        TextArea_SyntaxError.setText(analyzeClass.syntaxTest(analyzeClass.getListOfAllTokens()) + analyzeClass.getSemanticReport());
        TextArea_TableOfTokens.setText(analyzeClass.printListOfAllTokens());
    }
}
