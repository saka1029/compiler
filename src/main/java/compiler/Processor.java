package compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Processor {

    public int pc = 0, sp = 0, bp = 0;
    public boolean halt = false;
    public int[] stack = new int[200];
    public final List<Instruction> codes;
    public Supplier<Integer> input = () -> 0;
    public Consumer<Integer> output = System.out::println;

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

    public List<Integer> run(int... inputs) {
        List<Integer> outputs = new ArrayList<>();
        input = new Supplier<Integer>() {
            int index = 0;
            @Override
            public Integer get() {
                if (index >= inputs.length)
                    throw new NoSuchElementException("Too many input");
                return inputs[index++];
            }
        };
        output = i -> outputs.add(i);
        halt = false;
        while (!halt)
            codes.get(pc++).execute(this);
        return outputs;
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
