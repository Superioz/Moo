package de.superioz.moo.api.util;

import lombok.Getter;

public enum Operator {

    UNKNOWN("?", "unk"),
    AND("&", "and"),
    OR("|", "or"),
    EQUALS("=", "eq"),
    NOT_EQUALS("!=", "neq"),
    GREATER_THAN(">", "gt"),
    GREATER_THAN_OR_EQUALS(">=", "gte"),
    LESS_THAN("<", "lt"),
    LESS_THAN_OR_EQUALS("<=", "lte");

    @Getter
    private String symbol;
    @Getter
    private String shortcut;

    Operator(String symbol, String shortcut) {
        this.symbol = symbol;
        this.shortcut = shortcut;
    }

    public static Operator fromOperator(String op) {
        if(op.startsWith("$")) {
            op = op.replaceFirst("\\$", "");
        }

        for(Operator operator : Operator.values()) {
            if(operator.getShortcut().equals(op)) {
                return operator;
            }
        }
        return Operator.UNKNOWN;
    }

}
