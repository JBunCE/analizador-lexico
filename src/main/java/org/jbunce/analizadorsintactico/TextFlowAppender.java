package org.jbunce.analizadorsintactico;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TextFlowAppender extends AppenderBase<ILoggingEvent> {
    private final TextFlow textFlow;

    public TextFlowAppender(TextFlow textFlow) {
        this.textFlow = textFlow;
    }

    @Override
    protected void append(ILoggingEvent event) {
        Platform.runLater(() -> {
            Text text = new Text(event.getMessage());
            text.setFont(Font.font("Consolas", 15));
            textFlow.getChildren().add(text);
        });
    }
}
