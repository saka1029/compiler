package compiler;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import compiler.Compiler.Token;

public class TestCompiler {

    @Test
    public void testToken() {
        Compiler compiler = new Compiler("func fact(xy, z)  511");
        assertEquals(Token.FUNC, compiler.token());
        assertEquals(Token.ID, compiler.token()); assertEquals("fact", compiler.tokenString);
        assertEquals(Token.LP, compiler.token());
        assertEquals(Token.ID, compiler.token()); assertEquals("xy", compiler.tokenString);
        assertEquals(Token.COMMA, compiler.token());
        assertEquals(Token.ID, compiler.token()); assertEquals("z", compiler.tokenString);
        assertEquals(Token.RP, compiler.token());
        assertEquals(Token.INT, compiler.token()); assertEquals("511", compiler.tokenString);
        assertEquals(Token.EOF, compiler.token());
    }

    @Test
    public void testProgram() {
        String input = """
            program
            var a, b = 3 + 2, c = 4;
            end
        """;
        Compiler compiler = new Compiler(input);
        compiler.token();
        compiler.program();
        assertEquals(7, compiler.codes.size());
        assertEquals(Map.of("a", 0, "b", 1, "c", 2), compiler.globals);
    }

    @Test
    public void testDisplayStatement() {
        String input = """
            program
            display 3 + 2;
            end
        """;
        Processor processor = Compiler.parse(input);
        processor.run();
    }

    static String get(List<Instruction> x, int i) {
        return i < x.size() ? x.get(i).toString() : " . ";
    }

    static void print(List<Instruction> e, List<Instruction> c) {
        System.out.println("size: " + e.size() + " : " + c.size());
        int max = Math.max(e.size(), c.size());
        for (int i = 0; i < max; ++i)
            System.out.println(i + " : "
                + (i < e.size() && i < c.size() ? e.get(i).equals(c.get(i)) : " ???? ")
                + " : " + get(e, i) + " : " + get(c, i));
    }
    @Test
    public void testIfStatement() {
        String input = """
            program
            var y = 1;
            if y then y = 101; else y = 100; end
            display y;
            end
        """;
        Processor processor = Compiler.parse(input);
        List<Instruction> expected = List.of(
            Instruction.branch(2),
            Instruction.loadConst(1),
            Instruction.loadGlobal(0),
            Instruction.branchFalse(7),
            Instruction.loadConst(101),
            Instruction.storeGlobal(0),
            Instruction.branch(9),
            Instruction.loadConst(100),
            Instruction.storeGlobal(0),
            Instruction.loadGlobal(0),
            Instruction.DISPLAY,
            Instruction.HALT
        );
        print(expected, processor.codes);
        assertEquals(expected.size(), processor.codes.size());
        processor.run();
    }
}
