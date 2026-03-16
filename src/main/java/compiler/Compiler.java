package compiler;

import java.util.Map;
import static java.util.Map.entry;

import java.util.ArrayList;
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
    List<Instruction> codes = new ArrayList<>();

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

    void program() {

    }

    public static Processor parse(String input) {
        Compiler compiler = new Compiler(input);
        compiler.token();
        compiler.program();
        return new Processor(compiler.codes);
    }

}
