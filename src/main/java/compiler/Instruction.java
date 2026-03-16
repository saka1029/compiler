package compiler;

public interface Instruction {

    void execute(Processor processor);

    public static final Instruction HALT = p -> p.halt = true;
    public static final Instruction DUMP = p -> System.out.println(p);
    public static final Instruction DISPLAY = p -> System.out.println(p.pop());
    public static final Instruction LOAD_RETURN = p -> p.push(p.stack[p.bp - 1]);
    public static final Instruction STORE_RETURN = p -> p.stack[p.bp - 1] = p.pop();
    public static final Instruction NEGATIVE =  p -> p.push(-p.pop());
    public static final Instruction ADD =  p -> p.stack[p.sp - 2] += p.stack[--p.sp];
    public static final Instruction SUBTRACT = p -> p.stack[p.sp - 2] -= p.stack[--p.sp];
    public static final Instruction MULTIPLY =  p -> p.stack[p.sp - 2] *= p.stack[--p.sp];
    public static final Instruction DIVIDE = p -> p.stack[p.sp - 2] /= p.stack[--p.sp];
    public static final Instruction EQ = p -> p.push(p.stack[--p.sp] == p.stack[--p.sp] ? 1:0);
    public static final Instruction NE = p -> p.push(p.stack[--p.sp] != p.stack[--p.sp] ? 1:0);
    public static final Instruction LT = p -> p.push(p.stack[--p.sp] > p.stack[--p.sp] ? 1:0);
    public static final Instruction LE = p -> p.push(p.stack[--p.sp] >= p.stack[--p.sp] ? 1:0);
    public static final Instruction GT = p -> p.push(p.stack[--p.sp] < p.stack[--p.sp] ? 1:0);
    public static final Instruction GE = p -> p.push(p.stack[--p.sp] <= p.stack[--p.sp] ? 1:0);
    public static Instruction loadConst(int constant) { return p -> p.push(constant); }
    public static Instruction loadGlobal(int address) { return p -> p.push(p.stack[address]); }
    public static Instruction storeGlobal(int address) { return p -> p.stack[address] = p.pop(); }
    public static Instruction loadLocal(int offset) { return p -> p.push(p.stack[p.bp + offset]); }
    public static Instruction storeLocal(int offset) { return p -> p.stack[p.bp + offset] = p.pop(); }
    public static Instruction branch(int address) { return p -> p.pc = address; }
    public static Instruction branchFalse(int address) { return p -> { if (p.pop() == 0) p.pc = address; };}
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
}
