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
import org.jbunce.analizadorsintactico.algorithms.FirstFollowSet;
import org.jbunce.analizadorsintactico.algorithms.Token;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HelloController implements Initializable {

    private final HashMap<String, List<List<Object>>> grammar = new HashMap<>();

    @FXML private VBox filesVbox;
    @FXML private CodeArea codeArea;
    @FXML private VirtualizedScrollPane<CodeArea> codeScrollPane;
    @FXML private TextFlow logArea;
    @FXML private Label codet;

    private Set<Token> firstSet;
    private Set<Token> followSet;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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

        Token letters = new Token("names", "\\b(?:forif|iffor|for|if)*([A-Za-z]+)(?:forif|iffor|for|if)*\\b");
        Token numbers = new Token("numbers", "[0-9]");
        Token types = new Token("types", "(int|char|bool|string)");
        Token typesV = new Token("types", "(int|char|bool|string|void)");
        Token semicolon = new Token("semicolon", ";");
        Token equals = new Token("symbols", "=");
        Token quotes = new Token("symbols", "\"");
        Token simpleQuotes = new Token("symbols", "'");
        Token operator = new Token("symbols", "(<|>|<=|>=|==|!=)");
        Token to = new Token("symbols", "to");
        Token sm = new Token("symbols", ">");
        Token cm = new Token("symbols", ",");
        Token kl = new Token("symbols", "\\{");
        Token kr = new Token("symbols", "\\}");
        Token booleanV = new Token("boolean", "(true|false)");
        Token forT = new Token("for", "for");
        Token P1 = new Token("P1", "\\(");
        Token P2 = new Token("P2", "\\)");
        Token ifT = new Token("if", "if");
        Token ol = new Token("&&", "\\|\\|");
        Token scl = new Token("scl", "<");

        // General rules
        grammar.put("T", List.of(List.of(types)));
        grammar.put("TV", List.of(List.of(typesV)));
        grammar.put("L", List.of(List.of(letters)));
        grammar.put("D", List.of(List.of(numbers)));
        grammar.put("B", List.of(List.of(booleanV)));
        grammar.put("SC", List.of(List.of(semicolon)));
        grammar.put("E", List.of(List.of(equals)));
        grammar.put("SM", List.of(List.of(sm)));
        grammar.put("CM", List.of(List.of(cm)));
        grammar.put("KL", List.of(List.of(kl)));
        grammar.put("KR", List.of(List.of(kr)));
        grammar.put("C", List.of(List.of(quotes)));
        grammar.put("CL", List.of(List.of(simpleQuotes)));
        grammar.put("OP", List.of(List.of(operator)));
        grammar.put("FT", List.of(List.of(to)));
        grammar.put("F", List.of(List.of(forT)));
        grammar.put("P1", List.of(List.of(P1)));
        grammar.put("P2", List.of(List.of(P2)));
        grammar.put("I", List.of(List.of(ifT)));
        grammar.put("OL", List.of(List.of(ol)));
        grammar.put("SCL", List.of(List.of(scl)));

        grammar.put("K", List.of(List.of("KL", "KR")));
        grammar.put("P", List.of(List.of("P1", "P2")));
        grammar.put("VS", List.of(List.of("V", "SC")));
        grammar.put("CN", List.of(List.of("C", "N")));
        grammar.put("CLD", List.of(List.of("CL", "D")));
        grammar.put("CLL", List.of(List.of("CL", "L")));
        grammar.put("N", List.of(List.of("L", "N"), List.of("L", "D"), List.of("L")));

        grammar.put("V", List.of(List.of("CN", "C"), List.of("CLD", "CL"), List.of("CLL", "CL"), List.of("B"), List.of("D")));

        // Variable declaration
        grammar.put("VAR", List.of(List.of("TF", "NS"), List.of("TFN", "EV")));
        grammar.put("NS", List.of(List.of("N", "SC")));
        grammar.put("TF", List.of(List.of("T", "SM")));
        grammar.put("TFN", List.of(List.of("TF", "N")));
        grammar.put("EV", List.of(List.of("E", "VS")));

        // For loop
        grammar.put("FOR", List.of(List.of("FN", "RF"), List.of("FN", "RFF")));
        grammar.put("FN", List.of(List.of("F", "N")));
        grammar.put("RF", List.of(List.of("PA", "PK")));
        grammar.put("PA", List.of(List.of("P", "AT")));
        grammar.put("AT", List.of(List.of("N", "TON"), List.of("N", "TOD"), List.of("D", "TON"), List.of("D", "TOD")));
        grammar.put("TON", List.of(List.of("FT", "N")));
        grammar.put("TOD", List.of(List.of("FT", "D")));
        grammar.put("PK", List.of(List.of("P2", "SK")));
        grammar.put("SK", List.of(List.of("SM", "KL")));

        grammar.put("RFF", List.of(List.of("PA", "PF")));
        grammar.put("PF", List.of(List.of("P2", "SF")));
        grammar.put("SF", List.of(List.of("SM", "FU")));
        grammar.put("FU", List.of(List.of("N", "PSC"), List.of("N", "PAF")));
        grammar.put("PSC", List.of(List.of("P", "SC")));
        grammar.put("PAF", List.of(List.of("P1", "AF")));
        grammar.put("AF", List.of(List.of("V", "P2SC")));
        grammar.put("P2SC", List.of(List.of("P2", "SC")));

        // IF statement
        grammar.put("IF_STATEMENT", List.of(List.of("IF", "TS")));
        grammar.put("IF", List.of(List.of("I", "PAI")));
        grammar.put("PAI", List.of(List.of("P1O", "P2")));
        grammar.put("P1O", List.of(List.of("P1", "O")));
        grammar.put("O", List.of(List.of("OPE", "OLO"), List.of("OPE")));
        grammar.put("OPE", List.of(List.of("N", "OPN"), List.of("N", "OPV"), List.of("V", "OPN"), List.of("V", "OPV")));
        grammar.put("OPN", List.of(List.of("OP", "N")));
        grammar.put("OPV", List.of(List.of("OP", "V")));
        grammar.put("OLO", List.of(List.of("OL", "O")));

        grammar.put("TS", List.of(List.of("SM", "IFC")));
        grammar.put("IFC", List.of(List.of("K", "EC")));
        grammar.put("EC", List.of(List.of("E", "FI")));
        grammar.put("FI", List.of(List.of("SM", "K")));

        // Function
        grammar.put("FUNCTION", List.of(List.of("TVL", "NAR"), List.of("TVL", "NR")));
        grammar.put("TVL", List.of(List.of("TV", "SCL")));
        grammar.put("NAR", List.of(List.of("N", "ARFP")));
        grammar.put("ARFP", List.of(List.of("P1", "ARF")));
        grammar.put("ARF", List.of(List.of("TNCM", "ARF"), List.of("TNCM")));
        grammar.put("TNCM", List.of(List.of("TN", "CM")));
        grammar.put("TN", List.of(List.of("T", "N")));

        grammar.put("NR", List.of(List.of("N", "RFN")));
        grammar.put("RFN", List.of(List.of("P", "SC")));

        firstSet = new HashSet<>(FirstFollowSet.calculateFirstSet(grammar));
        followSet = new HashSet<>(FirstFollowSet.calculateFollowSet(grammar));

        System.out.println("First set: " + firstSet);
        System.out.println("Follow set: " + followSet);

    }

    @FXML
    public void onBuildClick() {
        Platform.runLater(() -> executor.submit(this::validate));
    }

    private void validate() {
        String text = codeArea.getText();
        text = text.replace(" ", "");
        text = text.replace("\n", "");

        for (Token token : firstSet) {
            Pattern pattern = Pattern.compile(token.getPattern());
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                System.out.println("Token: " + token.getName());
                log.info("Token: " + token.getName() + "\n");
                text = text.replace(matcher.group(), "");
            }
        }

        if (!text.isEmpty()) {
            log.warn("Unexpected" + text + "\n");
            System.out.println("Unexpected: " + text);
        } else {
            log.info("Success" + "\n");
            System.out.println("Success");
        }
    }

}