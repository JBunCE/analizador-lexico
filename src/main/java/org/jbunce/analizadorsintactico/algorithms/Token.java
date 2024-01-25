package org.jbunce.analizadorsintactico.algorithms;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class Token {

    private final String name;

    private final String pattern;

    private final List<String> classified = List.of("");

    public Token(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return name.equals(token.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return pattern;
    }
}
