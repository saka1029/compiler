package compiler;

import java.util.List;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Processor {

    public int pc = 0, sp = 0, bp = 0;
    public boolean halt = false;
    public int[] stack = new int[200];
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
        halt = false;
        while (!halt)
            codes.get(pc++).execute(this);
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
