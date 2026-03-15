package compiler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Processor {

    public int pc = 0, sp = 0, bp = 0;
    public int[] stack = new int[100];
    public final List<Instruction> codes;

    Processor(List<Instruction> codes) {
        this.codes = codes;
    }

    public static Processor of(List<Instruction> codes) {
        return new Processor(codes);
    }

    public void push(int value) {
        stack[sp++] = value;
    }

    public int pop() {
        return stack[--sp];
    }

    public void nop() {
    }

    public void run() {
        while (true) {
            Instruction inst = codes.get(pc++);
            if (inst == Instruction.HALT)
                break;
            inst.execute(this);
        }
    }

    @Override
    public String toString() {
        return "Processor(pc=%d, bp=%d, sp=%d, stack=%s)".formatted(
            pc, bp, sp,
            IntStream.range(0, sp)
                .mapToObj(i -> "" + stack[i])
                .collect(Collectors.joining(", ", "[", "]")));
    }
}
