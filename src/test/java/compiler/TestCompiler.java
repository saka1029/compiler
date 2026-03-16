package compiler;

import static org.junit.Assert.assertEquals;

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
            var a, b = 3, c = 4;
            end
        """;
        Processor processor = Compiler.parse(input);
        assertEquals(4, processor.codes.size());
        processor.run();
        assertEquals(3, processor.sp);
        assertEquals(4, processor.pop());
        assertEquals(3, processor.pop());
        assertEquals(0, processor.pop());
    }
}
