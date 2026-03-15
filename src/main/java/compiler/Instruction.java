package compiler;

import java.util.function.Consumer;

public interface Instruction {

    void execute(Processor processor);

    public static Instruction HALT = p -> p.halt = true;
    public static Instruction DUMP = p -> System.out.println(p);
    public static Instruction STORE_RETURN = p -> p.stack[p.bp - 1] = p.pop();
    public static Instruction ADD = p -> p.push(p.pop() + p.pop());
    public static Instruction SUBTRACT = p -> p.push(-p.pop() + p.pop());
    public static Instruction MULTIPOLY = p -> p.push(p.pop() * p.pop());
    public static Instruction DIVIDE = p -> { int r = p.pop(); p.push(p.pop() / r); };
    public static Instruction loadConst(int constant) { return p -> p.push(constant); }
    public static Instruction loadGlobal(int address) { return p -> p.push(p.stack[address]); }
    public static Instruction storeGlobal(int address) { return p -> p.stack[address] = p.pop(); }
    public static Instruction loadLocal(int offset) { return p -> p.push(p.stack[p.bp + offset]); }
    public static Instruction storeLocal(int offset) { return p -> p.stack[p.bp + offset] = p.pop(); }
    public static Instruction call(int address) {
        return p -> {
            p.push(p.pc);
            p.push(p.bp);
            p.push(0);
            p.pc = address;
            p.bp = p.sp;
        };
    }
    public static Instruction retFunc(int argSize) {
        return p -> {
            p.sp = p.bp;
            int rv = p.pop();
            p.bp = p.pop();
            p.pc = p.pop();
            p.sp -= argSize;
            p.push(rv);
        };
    }
    public static Instruction retProc(int argSize) {
        return p -> {
            p.sp = p.bp;
            p.pop();        // drop 戻り値
            p.bp = p.pop();
            p.pc = p.pop();
            p.sp -= argSize;
        };
    }
    public static Instruction inspect(Consumer<Processor> handler) {
        return p -> handler.accept(p);
    }
}
