package compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void testMod() {
        String input = """
            program
                var a = 11 % 3, b = a + 9;
            end
        """;
        Processor processor = Compiler.parse(input);
        processor.run();
        assertEquals(2, processor.stack[0]);
        assertEquals(11, processor.stack[1]);
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

    @Test
    public void testIfStatement() {
        String input = """
            program
                var y = 1;
                if y then
                    y = 101;
                else
                    y = 100;
                end
                display y;
            end
        """;
        Processor processor = Compiler.parse(input);
        List<Instruction> expected = List.of(
            Instruction.loadConst(1),
            Instruction.branch(2),
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
        assertTrue(Instruction.equals(expected, processor.codes));
        processor.run();
        assertEquals(101, processor.stack[1]);
    }

    @Test
    public void testIfStatement2() {
        String input = """
            program
                var y = 0;
                if y then
                    y = 101;
                else
                    y = 100;
                end
                display y;
            end
        """;
        Processor processor = Compiler.parse(input);
        List<Instruction> expected = List.of(
            Instruction.loadConst(0),
            Instruction.branch(2),
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
        assertTrue(Instruction.equals(expected, processor.codes));
        processor.run();
        assertEquals(100, processor.stack[0]);
    }

    @Test
    public void testWhileStatement() {
        String input = """
            program
                var y = 3, s = 0;
                while y > 0 do
                    s = s + y;
                    y = y - 1;
                end
            end
        """;
        Processor processor = Compiler.parse(input);
        List<Instruction> expected = List.of(
            Instruction.loadConst(3),
            Instruction.loadConst(0),
            Instruction.branch(3),
            Instruction.loadGlobal(0),
            Instruction.loadConst(0),
            Instruction.GT,
            Instruction.branchFalse(16),
            Instruction.loadGlobal(1),
            Instruction.loadGlobal(0),
            Instruction.ADD,
            Instruction.storeGlobal(1),
            Instruction.loadGlobal(0),
            Instruction.loadConst(1),
            Instruction.SUBTRACT,
            Instruction.storeGlobal(0),
            Instruction.branch(3),
            Instruction.HALT
        );
        assertTrue(Instruction.equals(expected, processor.codes));
        processor.run();
        assertEquals(6, processor.stack[1]);
    }

    @Test
    public void testFunction() {
        String input = """
            program
                var n = 4, result;
                func fact(i)
                    if i <= 0 then
                        fact = 1;
                    else
                        fact = fact(i - 1) * i;
                    end
                end
                result = fact(n);
            end
        """;
        Processor processor = Compiler.parse(input);
        List<Instruction> expected = List.of(
            Instruction.loadConst(4),       //          load #4 (for n)
            Instruction.loadConst(0),       //          load #0 (for result)
            Instruction.branch(18),         //          branch main (skip func definitions)
            Instruction.loadLocal(-4),      //          load i
            Instruction.loadConst(0),       //          load #0
            Instruction.LE,                 //          le
            Instruction.branchFalse(10),    //          branchFalse else
            Instruction.loadConst(1),       //          load #1
            Instruction.storeLocal(-1),     //          store fact
            Instruction.branch(17),         //          branch fi
            Instruction.loadLocal(-4),      // else:    load i
            Instruction.loadConst(1),       //          locad #1
            Instruction.SUBTRACT,           //          subtract
            Instruction.call(3),            //          call fact
            Instruction.loadLocal(-4),      //          load i
            Instruction.MULTIPLY,           //          multiply
            Instruction.storeLocal(-1),     //          store fact
            Instruction.retFunc(1),         // fi:      retFunc 1   (1 for argSize)
            Instruction.loadGlobal(0),      // main:    load n
            Instruction.call(3),            //          call fact
            Instruction.storeGlobal(1),     //          store result
            Instruction.HALT                //          halt
        );
        assertTrue(Instruction.equals(expected, processor.codes));
        processor.run();
        assertEquals(24, processor.stack[1]);   // fact(n) = fact(4) = 24
    }

    @Test
    public void testSum() {
        // 戻り値用の変数sumはCALL実行時に0に初期化される。
        String input = """
            program
                var n = 100, result;
                func sum(n)
                    var i = 1;
                    while i <= n do
                        sum = sum + i;
                        i = i + 1;
                    end
                end
                result = sum(n);
            end
        """;
        List<Instruction> expected = List.of(
            Instruction.loadConst(100),     //          load #100
            Instruction.loadConst(0),       //          load #0
            Instruction.branch(18),         //          branch main
            Instruction.loadConst(1),       //          load #1
            Instruction.loadLocal(0),       // wstart:  load i
            Instruction.loadLocal(-4),      //          load n
            Instruction.LE,                 //          le
            Instruction.branchFalse(17),    //          brancFalse wend
            Instruction.loadLocal(-1),      //          load sum
            Instruction.loadLocal(0),       //          load i
            Instruction.ADD,                //          add
            Instruction.storeLocal(-1),     //          store sum
            Instruction.loadLocal(0),       //          load i
            Instruction.loadConst(1),       //          load #1
            Instruction.ADD,                //          add
            Instruction.storeLocal(0),      //          store i
            Instruction.branch(4),          //          branch wstart
            Instruction.retFunc(1),         // wend:    retFunc 1
            Instruction.loadGlobal(0),      // main:    load n
            Instruction.call(3),            //          call sum
            Instruction.storeGlobal(1),     //          store result
            Instruction.HALT
        );
        Processor processor = Compiler.parse(input);
        assertTrue(Instruction.equals(expected, processor.codes));
        assertEquals(expected, processor.codes);
        processor.run();
        assertEquals(5050, processor.stack[1]);
        processor.sp = processor.bp = processor.pc = 0;
        processor.run();
        assertEquals(5050, processor.stack[1]);
    }
}
