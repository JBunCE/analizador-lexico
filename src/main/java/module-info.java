module org.jbunce.analizadorsintactico {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    requires lombok;

    opens org.jbunce.analizadorsintactico to javafx.fxml;
    exports org.jbunce.analizadorsintactico;
}