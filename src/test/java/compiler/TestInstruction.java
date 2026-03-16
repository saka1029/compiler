package compiler;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TestInstruction {

    @Test
    public void testArithmeticOperation() {
        Processor processor = new Processor(List.of(
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.ADD,
            p -> assertEquals(16, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.SUBTRACT,
            p -> assertEquals(8, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.MULTIPLY,
            p -> assertEquals(48, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.DIVIDE,
            p -> assertEquals(3, p.pop()),
            Instruction.HALT
        ));
        processor.run();
        assertEquals(0, processor.sp);
    }

    @Test
    public void testEq() {
        Processor processor = new Processor(List.of(
            Instruction.loadConst(12),
            Instruction.loadConst(12),
            Instruction.EQ,
            p -> assertEquals(1, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.EQ,
            p -> assertEquals(0, p.pop()),
            Instruction.loadConst(4),
            Instruction.loadConst(12),
            Instruction.EQ,
            p -> assertEquals(0, p.pop()),
            Instruction.HALT
        ));
        processor.run();
        assertEquals(0, processor.sp);
    }

    @Test
    public void testNe() {
        Processor processor = new Processor(List.of(
            Instruction.loadConst(12),
            Instruction.loadConst(12),
            Instruction.NE,
            p -> assertEquals(0, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.NE,
            p -> assertEquals(1, p.pop()),
            Instruction.loadConst(4),
            Instruction.loadConst(12),
            Instruction.NE,
            p -> assertEquals(1, p.pop()),
            Instruction.HALT
        ));
        processor.run();
        assertEquals(0, processor.sp);
    }

    @Test
    public void testLt() {
        Processor processor = new Processor(List.of(
            Instruction.loadConst(12),
            Instruction.loadConst(12),
            Instruction.LT,
            p -> assertEquals(0, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.LT,
            p -> assertEquals(0, p.pop()),
            Instruction.loadConst(4),
            Instruction.loadConst(12),
            Instruction.LT,
            p -> assertEquals(1, p.pop()),
            Instruction.HALT
        ));
        processor.run();
        assertEquals(0, processor.sp);
    }

    @Test
    public void testLe() {
        Processor processor = new Processor(List.of(
            Instruction.loadConst(12),
            Instruction.loadConst(12),
            Instruction.LE,
            p -> assertEquals(1, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.LE,
            p -> assertEquals(0, p.pop()),
            Instruction.loadConst(4),
            Instruction.loadConst(12),
            Instruction.LE,
            p -> assertEquals(1, p.pop()),
            Instruction.HALT
        ));
        processor.run();
        assertEquals(0, processor.sp);
    }

    @Test
    public void testGt() {
        Processor processor = new Processor(List.of(
            Instruction.loadConst(12),
            Instruction.loadConst(12),
            Instruction.GT,
            p -> assertEquals(0, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.GT,
            p -> assertEquals(1, p.pop()),
            Instruction.loadConst(4),
            Instruction.loadConst(12),
            Instruction.GT,
            p -> assertEquals(0, p.pop()),
            Instruction.HALT
        ));
        processor.run();
        assertEquals(0, processor.sp);
    }

    @Test
    public void testGe() {
        Processor processor = new Processor(List.of(
            Instruction.loadConst(12),
            Instruction.loadConst(12),
            Instruction.GE,
            p -> assertEquals(1, p.pop()),
            Instruction.loadConst(12),
            Instruction.loadConst(4),
            Instruction.GE,
            p -> assertEquals(1, p.pop()),
            Instruction.loadConst(4),
            Instruction.loadConst(12),
            Instruction.GE,
            p -> assertEquals(0, p.pop()),
            Instruction.HALT
        ));
        processor.run();
        assertEquals(0, processor.sp);
    }

}
