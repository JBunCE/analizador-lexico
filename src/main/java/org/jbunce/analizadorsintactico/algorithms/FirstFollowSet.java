package org.jbunce.analizadorsintactico.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirstFollowSet {

    public static List<Token> calculateFirstSet(HashMap<String, List<List<Object>>> grammar) {
        List<Token> firstSet = new ArrayList<>();

        for (String key : grammar.keySet()) {
            firstSet.add(getFirst(key, grammar));
        }

        return firstSet;
    }

    public static List<Token> calculateFollowSet(HashMap<String, List<List<Object>>> grammar) {
        List<Token> secondSet = new ArrayList<>();

        for (String key : grammar.keySet()) {
            secondSet.add(getSecond(key, grammar));
        }

        return secondSet;
    }

    private static Token getFirst(String key, HashMap<String, List<List<Object>>> grammar) {
        List<List<Object>> nonTerminal = grammar.get(key);

        for (List<Object> productions : nonTerminal) {

            if (productions.size() == 1) {
                return (Token) productions.get(0);
            } else {
                return getFirst((String) productions.get(0), grammar);
            }

        }

        throw new RuntimeException("The grammar is not complete");
    }

    private static Token getSecond(String key, HashMap<String, List<List<Object>>> grammar) {

        for (String k : grammar.keySet()) {
            List<List<Object>> productions = grammar.get(k);

            for (List<Object> production : productions) {
                if (production.contains(key)) {
                    int index = production.indexOf(key);

                    if (production.size() - 1 < index + 1) {
                        return getSecond(k, grammar);
                    } else {
                        return getFirst((String) production.get(index + 1), grammar);
                    }
                }
            }
        }

        return new Token("EOF", "$");
    }
}
