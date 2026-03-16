package compiler;

import java.util.Map;
import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Compiler {

    public enum Token {
        LP("("), RP(")"), COMMA(","), SEMI_COLON(";"), EOF("EOF"),
        ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/"),
        ASSIGN("="), EQ("=="), NE("!="), LT("<"), LE("<="), GT(">"), GE(">="),
        NOT("!"), AND("&"), OR("|"), CAND("&&"), COR("||"),
        PROGRAM("program"), FUNC("func"),
        VAR("var"), END("end"),
        IF("if"), THEN("then"), ELSE("else"),
        WHILE("while"), DO("do"), DISPLAY("display"),
        ID("ID"), INT("INT");

        final String n;

        private Token(String n) {
            this.n = n;
        }

        @Override
        public String toString() {
            return n;
        }

        public final static Map<String, Token> RESERVED = Map.ofEntries(
            entry("program", Token.PROGRAM), entry("func", Token.FUNC),
            entry("var", Token.VAR), entry("end", Token.END),
            entry("if", Token.IF), entry("then", Token.THEN), entry("else", Token.ELSE),
            entry("while", Token.WHILE), entry("do", Token.DO), entry("display", Token.DISPLAY)
        );
    }

    static RuntimeException error(String message, Object... args) {
        return new RuntimeException(message.formatted(args));
    }

    final int[] input;
    int index, ch;
    Token token; String tokenString;
    Token eaten; String eatenString;

    List<Instruction> codes = new ArrayList<>();
    Map<String, Integer> globals = new LinkedHashMap<>();

    Compiler(String input) {
        this.input = input.codePoints().toArray();
        this.index = 0;
        ch();
    }

    int ch() {
        if (index < input.length)
            return ch = input[index++];
        index = input.length + 1;
        return ch = -1;
    }

    static boolean isIdFirst(int ch) {
        return Character.isLetter(ch) || ch == '_';
    }

    static boolean isIdRest(int ch) {
        return isIdFirst(ch) || Character.isDigit(ch);
    }

    static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    void spaces() {
        while (Character.isWhitespace(ch))
            ch();
    }

    Token token(Token t) {
        ch();
        return token = t;
    }
    Token token(Token first, int next, Token second) {
        if (ch() == next)
            return token(second);
        else
            return token =first;
    }

    Token word() {
        int start = index - 1;
        do {
            ch();
        } while (isIdRest(ch));
        tokenString = new String(input, start, index - start - 1);
        Token word = Token.RESERVED.get(tokenString);
        return token = word != null ? word : Token.ID;
    }

    Token integer() {
        int start = index - 1;
        do {
            ch();
        } while (isDigit(ch));
        tokenString = new String(input, start, index - start - 1);
        return token = Token.INT;
    }

    Token token() {
        spaces();
        switch (ch) {
            case -1: return Token.EOF;
            case '(': return token(Token.LP);
            case ')': return token(Token.RP);
            case ',': return token(Token.COMMA);
            case ';': return token(Token.SEMI_COLON);
            case '+': return token(Token.ADD);
            case '-': return token(Token.SUBTRACT);
            case '*': return token(Token.MULTIPLY);
            case '/': return token(Token.DIVIDE);
            case '=': return token(Token.ASSIGN, '=', Token.EQ);
            case '!': return token(Token.NOT, '=', Token.NE);
            case '<': return token(Token.LT, '=', Token.LE);
            case '>': return token(Token.GT, '=', Token.GE);
            case '&': return token(Token.AND, '&', Token.CAND);
            case '|': return token(Token.OR, '|', Token.COR);
            default:
                if (isIdFirst(ch))
                    return word();
                else if (isDigit(ch))
                    return integer();
                else
                    throw error("Unknown char '%c'", (char)ch);
        }
    }

    boolean eat(Token... expects) {
        for (Token expect : expects)
            if (token == expect) {
                eaten = token;
                eatenString = tokenString;
                token();
                return true;
            }
        return false;
    }

    void must(Token expect) {
        if (token != expect)
            throw error("%s expected", expect);
        eaten = token;
        eatenString = tokenString;
        token();
    }

    void factor() {
        if (eat(Token.LP)) {
            expression();
            must(Token.RP);
        } else if (eat(Token.ID)) {
            String name = eatenString;
            if (eat(Token.LP)) {
                if (!eat(Token.RP)) {
                    expression();
                    while (eat(Token.COMMA))
                        expression();
                }
            } else {
                ;
            }
        } else if (eat(Token.INT)) {
            int value = Integer.parseInt(eatenString);
            codes.add(Instruction.loadConst(value));
        }
    }

    void term() {
        factor();
        while (true)
            if (eat(Token.MULTIPLY)) {
                factor();
                codes.add(Instruction.MULTIPLY);
            } else if (eat(Token.DIVIDE)) {
                factor();
                codes.add(Instruction.DIVIDE);
            } else
                break;
    }

    void expression() {
        int sign = 1;
        if (eat(Token.ADD))
            sign = 1;
        else if(eat(Token.SUBTRACT))
            sign = -1;
        term();
        if (sign < 0)
            codes.add(Instruction.NEGATIVE);
        while (true)
            if (eat(Token.ADD)) {
                term();
                codes.add(Instruction.ADD);
            } else if (eat(Token.SUBTRACT)) {
                term();
                codes.add(Instruction.SUBTRACT);
            } else
                break;
    }

    void var() {
        must(Token.ID);
        String name = eatenString;
        if (eat(Token.ASSIGN))
            expression();
        else
            codes.add(Instruction.loadConst(0));
        globals.put(name, globals.size());
    }

    void vars() {
        var();
        while (eat(Token.COMMA))
            var();
        must(Token.SEMI_COLON);
    }

    void program() {
        must(Token.PROGRAM);
        if (eat(Token.VAR))
            vars();
        must(Token.END);
        codes.add(Instruction.HALT);
    }

    public static Processor parse(String input) {
        Compiler compiler = new Compiler(input);
        compiler.token();
        compiler.program();
        return new Processor(compiler.codes);
    }

}
