package org.jbunce.analizadorsintactico;

import ch.qos.logback.classic.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.jbunce.analizadorsintactico.algorithms.Token;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ApplicationController implements Initializable {

    @FXML private VBox filesVbox;
    @FXML private CodeArea codeArea;
    @FXML private VirtualizedScrollPane<CodeArea> codeScrollPane;
    @FXML private TextFlow logArea;
    @FXML private Label codet;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final HashMap<String, List<String>> classification = new HashMap<>();

    private List<Token> tokens;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAndStopAllAppenders();

        TextFlowAppender appender = new TextFlowAppender(logArea);
        appender.start();

        logger.addAppender(appender);

        codeArea.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        codeScrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Platform.runLater(() -> {
            IntFunction<Node> lineNumberFactory = LineNumberFactory.get(codeArea);
            codeArea.setParagraphGraphicFactory(lineNumberFactory);
        });

        Token numbers = new Token("numbers", "[0-9]");
        Token types = new Token("types", "(int|char|bool|string|void)");
        Token operators = new Token("operators", "(<|>|<=|>=|==|!=)");
        Token symbols = new Token("symbols", "(<|>|'|\"|;|=|,|\\+)");
        Token structure = new Token("structure", "(\\|\\|)|(\\{|\\}|\\(|\\))");
        Token reserved = new Token("reserved", "(true|false|if|for|to|else)");
        Token nonSpecific = new Token("names", "[A-Za-z]+");

        tokens = List.of(
          numbers,
          types,
          reserved,
          nonSpecific,
          operators,
          symbols,
          structure
        );

        for (Token token : tokens) {
            classification.put(token.getName(), new ArrayList<>());
        }
    }

    @FXML
    public void onBuildClick() {
        //validate();
        this.validate();
    }

    private void validate() {
        String text = codeArea.getText();
        text = text.replace("\n", "");
        text = text.replace(" ", "");

        for (Token token : tokens) {
            Pattern pattern = Pattern.compile(token.getPattern());
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                String matchedSymbol = matcher.group();
                classification.get(token.getName()).add(matchedSymbol);
                text = text.replaceFirst(Pattern.quote(matchedSymbol), "");
            }
        }

        log.info("Classification: \n");
        for (String tokenName : classification.keySet()) {
            log.info("-------" + tokenName + ":" + "\n");
            classification.get(tokenName).forEach(s -> log.info(s + ", "));
            log.info("\n");
        }

        if (!text.isEmpty()) {
            log.warn("Unexpected: " + text + "\n");
            System.out.println("Unexpected: " + text);
        } else {
            log.info("Success" + "\n");
            System.out.println("Success");
        }
    }
}
