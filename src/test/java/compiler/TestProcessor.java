package compiler;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TestProcessor {

    @Test
    public void testRun() {
        List<Instruction> codes = List.of(
            Instruction.loadConst(2),
            Instruction.loadConst(3),
            Instruction.ADD,
            Instruction.HALT
        );
        Processor processor = Processor.of(codes);
        processor.run();
        assertEquals(1, processor.sp);
        assertEquals(5, processor.pop());
    }

    /**
     * func P(a, b)
     *    var c = 3, d = 4;
     *    P = a + b + c + d;
     * end
     * P(1, 2);     // -> 10
     */
    @Test
    public void testCallFunc() {
        int argSize = 2;
        List<Instruction> codes = List.of(
            Instruction.loadConst(1),                // P(1,
            Instruction.loadConst(2),                //   2)
            Instruction.call(4),
            Instruction.HALT,
            Instruction.loadConst(3),                // func P(a, b) var c = 3,
            Instruction.loadConst(4),                //                  d = 4;
            Instruction.loadLocal(-argSize - 3 + 0), // P = a + b + c + d;
            Instruction.loadLocal(-argSize - 3 + 1),
            p -> System.out.println(p),
            Instruction.ADD,
            Instruction.loadLocal(0),
            Instruction.ADD,
            Instruction.loadLocal(1),
            Instruction.ADD,
            Instruction.STORE_RETURN,              // 戻り値をセット
            Instruction.retFunc(argSize)
        );
        Processor processor = Processor.of(codes);
        processor.run();
        assertEquals(1, processor.sp);
        assertEquals(10, processor.pop());
    }

    @Test
    public void testCallProc() {
        int argSize = 1;
        List<Instruction> codes = List.of(
            Instruction.loadConst(8),                // P(1)
            Instruction.call(4),
            p -> assertEquals(0, p.sp),    // 戻ったらスタックが空であること。
            Instruction.HALT,
            Instruction.loadConst(3),                // proc P(a) var c = 3;
            Instruction.loadLocal(-argSize - 3 + 0), // c = a + c;
            Instruction.loadLocal(0),
            Instruction.ADD,
            Instruction.storeLocal(0),
            p -> assertEquals(11, p.stack[p.bp + 0]),  // c == 11であること。
            Instruction.retProc(argSize)
        );
        Processor processor = Processor.of(codes);
        processor.run();
        assertEquals(0, processor.sp);
    }

    /**
     * func fact(n)
     *      if n <= 0 then
     *          fact = 1;
     *      else
     *          fact = fact(n - 1) * n;
     *      end
     */
    @Test
    public void testRecursion() {
        int argSize = 1;
        List<Instruction> codes = List.of(
            Instruction.loadConst(0),
            Instruction.call(22),
            p -> assertEquals(1, p.pop()),
            Instruction.loadConst(1),
            Instruction.call(22),
            p -> assertEquals(1, p.pop()),
            Instruction.loadConst(2),
            Instruction.call(22),
            p -> assertEquals(2, p.pop()),
            Instruction.loadConst(3),
            Instruction.call(22),
            p -> assertEquals(6, p.pop()),
            Instruction.loadConst(4),
            Instruction.call(22),
            p -> assertEquals(24, p.pop()),
            Instruction.loadConst(5),
            Instruction.call(22),
            p -> assertEquals(120, p.pop()),
            Instruction.loadConst(6),
            Instruction.call(22),
            p -> assertEquals(720, p.pop()),
            Instruction.HALT,
    /*22*/  Instruction.loadLocal(-argSize - 3 + 0),
            p -> { if (p.pop() > 0) p.pc = 26; },
            Instruction.loadConst(1),
            Instruction.branch(32),
    /*26*/  Instruction.loadLocal(-argSize - 3 + 0),
            Instruction.loadConst(1),
            Instruction.SUBTRACT,
            Instruction.call(22),
            Instruction.loadLocal(-argSize - 3 + 0),
            Instruction.MULTIPLY,
    /*32*/  Instruction.STORE_RETURN,
            Instruction.retFunc(argSize)
        );
        Processor processor = Processor.of(codes);
        processor.run();
    }
}
