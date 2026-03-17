package compiler;

import java.util.Map;
import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Compiler {

    public enum Token {
        LP("("), RP(")"), COMMA(","), SEMI_COLON(";"), EOF("EOF"),
        ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/"), MOD("%"),
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
    Map<String, Integer> locals = null;
    Map<String, Integer> globals = new LinkedHashMap<>();
    Map<String, Integer> functions = new LinkedHashMap<>();

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
            case '%': return token(Token.MOD);
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
            throw error("'%s' expected, but '%s'", expect, token);
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
            // IDの後にカッコが続くのは関数呼び出し
            if (eat(Token.LP)) {
                if (!functions.containsKey(name))
                    throw error("Function '%s' is not defined", name);
                if (!eat(Token.RP)) {
                    expression();
                    while (eat(Token.COMMA))
                        expression();
                    must(Token.RP);
                }
                codes.add(Instruction.call(functions.get(name)));
            } else {
                // ローカル変数検索
                if (locals != null && locals.containsKey(name))
                    codes.add(Instruction.loadLocal(locals.get(name)));
                // グローバル変数検索
                else if (globals.containsKey(name))
                    codes.add(Instruction.loadGlobal(globals.get(name)));
                else
                    throw error("Variable '%s' is not defined", name);
            }
        } else if (eat(Token.INT)) {
            int value = Integer.parseInt(eatenString);
            codes.add(Instruction.loadConst(value));
        } else
            throw error("Unknown token '%s'", token);
    }

    void multExpression() {
        factor();
        while (true)
            if (eat(Token.MULTIPLY)) {
                factor();
                codes.add(Instruction.MULTIPLY);
            } else if (eat(Token.DIVIDE)) {
                factor();
                codes.add(Instruction.DIVIDE);
            } else if (eat(Token.MOD)) {
                factor();
                codes.add(Instruction.MOD);
            } else
                break;
    }

    void addExpression() {
        int sign = 1;
        if (eat(Token.ADD))
            sign = 1;
        else if(eat(Token.SUBTRACT))
            sign = -1;
        multExpression();
        if (sign < 0)
            codes.add(Instruction.NEGATIVE);
        while (true)
            if (eat(Token.ADD)) {
                multExpression();
                codes.add(Instruction.ADD);
            } else if (eat(Token.SUBTRACT)) {
                multExpression();
                codes.add(Instruction.SUBTRACT);
            } else
                break;
    }

    void expression() {
        addExpression();
        if (eat(Token.EQ)) {
            addExpression();
            codes.add(Instruction.EQ);
        } else if (eat(Token.NE)) {
            addExpression();
            codes.add(Instruction.NE);
        } else if (eat(Token.LT)) {
            addExpression();
            codes.add(Instruction.LT);
        } else if (eat(Token.LE)) {
            addExpression();
            codes.add(Instruction.LE);
        } else if (eat(Token.GT)) {
            addExpression();
            codes.add(Instruction.GT);
        } else if (eat(Token.GE)) {
            addExpression();
            codes.add(Instruction.GE);
        }
    }

    void assignStatement() {
        String name = eatenString;
        must(Token.ASSIGN);
        expression();
        if (locals != null && locals.containsKey(name))
            codes.add(Instruction.storeLocal(locals.get(name)));
        else if (globals.containsKey(name))
            codes.add(Instruction.storeGlobal(globals.get(name)));
        else
            throw error("Variable '%s' is not defined", name);
        must(Token.SEMI_COLON);
    }

    void ifStatement() {
        expression();
        must(Token.THEN);
        int thenPos = codes.size();
        codes.add(Instruction.branchFalse(Integer.MIN_VALUE));
        statements();
        if (eat(Token.ELSE)) {
            int elsePos = codes.size();
            codes.add(Instruction.branch(Integer.MIN_VALUE));
            codes.set(thenPos, Instruction.branchFalse(codes.size()));
            statements();
            codes.set(elsePos, Instruction.branch(codes.size()));
        } else
            codes.set(thenPos, Instruction.branchFalse(codes.size()));
        must(Token.END);
    }

    void whileStatement() {
        int start = codes.size();
        expression();
        must(Token.DO);
        int doPos = codes.size();
        codes.add(Instruction.branchFalse(Integer.MIN_VALUE));
        statements();
        codes.add(Instruction.branch(start));
        must(Token.END);
        codes.set(doPos, Instruction.branchFalse(codes.size()));
    }

    void displayStatement() {
        expression();
        codes.add(Instruction.DISPLAY);
        must(Token.SEMI_COLON);
    }

    void statements() {
        while (true) {
            if (eat(Token.ID))
                assignStatement();
            else if (eat(Token.IF))
                ifStatement();
            else if (eat(Token.WHILE))
                whileStatement();
            else if (eat(Token.DISPLAY))
                displayStatement();
            else
                break;
        }
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

    void var(List<String> localNames) {
        must(Token.ID);
        String name = eatenString;
        if (eat(Token.ASSIGN))
            expression();
        else
            codes.add(Instruction.loadConst(0));
        localNames.add(name);
    }

    void vars(List<String> localNames) {
        var(localNames);
        while (eat(Token.COMMA))
            var(localNames);
        must(Token.SEMI_COLON);
    }

    void arguments(List<String> argNames) {
        must(Token.LP);
        if (!eat(Token.RP)) {
            eat(Token.ID);
            argNames.add(eatenString);
            while (eat(Token.COMMA)) {
                eat(Token.ID);
                argNames.add(eatenString);
            }
            must(Token.RP);
        }
    }

    void func() {
        must(Token.ID);
        String name = eatenString;
        // 関数の開始アドレスを登録する。
        // 関数本体の定義に先行して登録するので再帰呼出し可。
        functions.put(name, codes.size());
        List<String> argNames = new ArrayList<>();
        arguments(argNames);
        int argSize = argNames.size();
        List<String> localNames = new ArrayList<>();
        if (eat(Token.VAR))
            vars(localNames);
        locals = new LinkedHashMap<>();
        // 引数のアドレスを登録
        for (int i = 0; i < argSize; ++i)
            locals.put(argNames.get(i), -argSize - 3 + i);
        // ローカル変数のアドレスを登録
        for (int i = 0, size = localNames.size(); i < size; ++i)
            locals.put(localNames.get(i), i);
        // 関数名と同名の戻り値変数を登録
        locals.put(name, -1);
        statements();
        locals = null;
        must(Token.END);
        // 関数からリターンするコードを追加する
        codes.add(Instruction.retFunc(argSize));
    }

    void program() {
        must(Token.PROGRAM);
        if (eat(Token.VAR))
            vars();
        // 関数定義をBRANCHでスキップする。
        int start = codes.size();
        codes.add(Instruction.branch(Integer.MIN_VALUE));
        while (eat(Token.FUNC))
            func();
        // コード本体の開始
        codes.set(start, Instruction.branch(codes.size()));
        statements();
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
