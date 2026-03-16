package compiler;

import java.util.List;
import java.util.stream.IntStream;

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
    public static abstract class InstAbs implements Instruction {
        public final int n;
        public InstAbs(int n) { this.n = n; }
        @Override
        public boolean equals(Object obj) {
            return obj != null
                && obj.getClass() == getClass()
                && obj instanceof InstAbs ia
                && ia.n == n;
        }
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + n;
        }
    }
    static class LoadConst extends InstAbs {
        LoadConst(int n) { super(n); }
        @Override public void execute(Processor p) { p.push(n); }
    }
    public static Instruction loadConst(int constant) { return new LoadConst(constant); }

    static class LoadGlobal extends InstAbs {
        LoadGlobal(int n) { super(n); }
        @Override public void execute(Processor p) { p.push(p.stack[n + 1]); }
    }
    public static Instruction loadGlobal(int address) { return new LoadGlobal(address); }

    static class StoreGlobal extends InstAbs {
        StoreGlobal(int n) { super(n); }
        @Override public void execute(Processor p) { p.stack[n + 1] = p.pop(); }
    }
    public static Instruction storeGlobal(int address) { return new StoreGlobal(address); }

    static class LoadLocal extends InstAbs {
        LoadLocal(int n) { super(n); }
        @Override public void execute(Processor p) { p.push(p.stack[p.bp + n]); }
    }
    public static Instruction loadLocal(int offset) { return new LoadLocal(offset); }

    static class StoreLocal extends InstAbs {
        StoreLocal(int n) { super(n); }
        @Override public void execute(Processor p) { p.stack[p.bp + n] = p.pop(); }
    }
    public static Instruction storeLocal(int offset) { return new StoreLocal(offset); }

    static class Branch extends InstAbs {
        Branch(int n) { super(n); }
        @Override public void execute(Processor p) { p.pc = n; }
    }
    public static Instruction branch(int address) { return new Branch(address); }

    static class BranchFalse extends InstAbs {
        BranchFalse(int n) { super(n); }
        @Override public void execute(Processor p) { if (p.pop() == 0) p.pc = n; }
    }
    public static Instruction branchFalse(int address) { return new BranchFalse(address); }

    static class Call extends InstAbs {
        Call(int n) { super(n); }
        @Override public void execute(Processor p) {
            p.push(p.pc);
            p.push(p.bp);
            p.push(0);
            p.pc = n;
            p.bp = p.sp;
        }
    }
    public static Instruction call(int address) { return new Call(address); }

    static class RetFunc extends InstAbs {
        RetFunc(int n) { super(n); }
        @Override public void execute(Processor p) {
            p.sp = p.bp;
            int rv = p.pop();
            p.bp = p.pop();
            p.pc = p.pop();
            p.sp -= n;
            p.push(rv);
        }
    }
    public static Instruction retFunc(int argSize) { return new RetFunc(argSize); }

    static class RetProc extends InstAbs {
        RetProc(int n) { super(n); }
        @Override public void execute(Processor p) {
            p.sp = p.bp;
            p.pop();        // drop 戻り値
            p.bp = p.pop();
            p.pc = p.pop();
            p.sp -= n;
        }
    }
    public static Instruction retProc(int argSize) { return new RetProc(argSize); }

    static String get(List<Instruction> x, int i) {
        return i < x.size() ? x.get(i).toString() : " . ";
    }

    public static boolean compare(List<Instruction> e, List<Instruction> c) {
        int es = e.size(), cs = c.size();
        System.out.println("size: " + es + (es == cs ? " == " : " != ") + cs);
        int max = Math.max(e.size(), c.size());
        for (int i = 0; i < max; ++i)
            System.out.println(i + " : "
                + get(e, i)
                + (i < es && i < cs && e.get(i).equals(c.get(i)) ? " == " : " != ")
                + get(c, i));
        return es == cs && IntStream.range(0, es).allMatch(k -> e.get(k).equals(c.get(k)));
    }
}
