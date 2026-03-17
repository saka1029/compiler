package compiler;

import java.util.List;
import java.util.stream.IntStream;

public interface Instruction {

    void execute(Processor processor);

    public static abstract class IName implements Instruction {
        final String n;
        public IName(String n) { this.n = n; }
        @Override public String toString() { return n; }
    }

    public static final Instruction HALT = new IName("HALT") { @Override public void execute(Processor p) { p.halt = true; } };
    public static final Instruction DUMP = new IName("DUMP") { @Override public void execute(Processor p) { System.out.println(p); }};
    public static final Instruction DISPLAY = new IName("DISPLAY") { @Override public void execute(Processor p) { System.out.println(p.pop()); }};
    public static final Instruction NEGATIVE =  new IName("NEGATIVE") { @Override public void execute(Processor p) { p.push(-p.pop()); }};
    public static final Instruction ADD =  new IName("ADD") { @Override public void execute(Processor p) { p.stack[p.sp - 2] += p.stack[--p.sp]; }};
    public static final Instruction SUBTRACT = new IName("SUBTRACT") { @Override public void execute(Processor p) { p.stack[p.sp - 2] -= p.stack[--p.sp]; }};
    public static final Instruction MULTIPLY =  new IName("MULTIPLY") { @Override public void execute(Processor p) { p.stack[p.sp - 2] *= p.stack[--p.sp]; }};
    public static final Instruction DIVIDE = new IName("DIVIDE") { @Override public void execute(Processor p) { p.stack[p.sp - 2] /= p.stack[--p.sp]; }};
    public static final Instruction MOD = new IName("MOD") { @Override public void execute(Processor p) { p.stack[p.sp - 2] %= p.stack[--p.sp]; }};
    public static final Instruction EQ = new IName("EQ") { @Override public void execute(Processor p) { p.push(p.stack[--p.sp] == p.stack[--p.sp] ? 1:0); }};
    public static final Instruction NE = new IName("NE") { @Override public void execute(Processor p) { p.push(p.stack[--p.sp] != p.stack[--p.sp] ? 1:0); }};
    public static final Instruction LT = new IName("LT") { @Override public void execute(Processor p) { p.push(p.stack[--p.sp] > p.stack[--p.sp] ? 1:0); }};
    public static final Instruction LE = new IName("LE") { @Override public void execute(Processor p) { p.push(p.stack[--p.sp] >= p.stack[--p.sp] ? 1:0); }};
    public static final Instruction GT = new IName("GT") { @Override public void execute(Processor p) { p.push(p.stack[--p.sp] < p.stack[--p.sp] ? 1:0); }};
    public static final Instruction GE = new IName("GE") { @Override public void execute(Processor p) { p.push(p.stack[--p.sp] <= p.stack[--p.sp] ? 1:0); }};

    public static abstract class IInt implements Instruction {
        public final int n;
        public IInt(int n) { this.n = n; }
        @Override
        public boolean equals(Object obj) {
            return obj != null
                && obj.getClass() == getClass()
                && obj instanceof IInt ia
                && ia.n == n;
        }
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + n;
        }
    }
    static class LoadConst extends IInt {
        LoadConst(int n) { super(n); }
        @Override public void execute(Processor p) { p.push(n); }
    }
    public static Instruction loadConst(int constant) { return new LoadConst(constant); }

    static class LoadGlobal extends IInt {
        LoadGlobal(int n) { super(n); }
        @Override public void execute(Processor p) { p.push(p.stack[n]); }
    }
    public static Instruction loadGlobal(int address) { return new LoadGlobal(address); }

    static class StoreGlobal extends IInt {
        StoreGlobal(int n) { super(n); }
        @Override public void execute(Processor p) {
            p.stack[n] = p.pop(); }
    }
    public static Instruction storeGlobal(int address) { return new StoreGlobal(address); }

    static class LoadLocal extends IInt {
        LoadLocal(int n) { super(n); }
        @Override public void execute(Processor p) { p.push(p.stack[p.bp + n]); }
    }
    public static Instruction loadLocal(int offset) { return new LoadLocal(offset); }

    static class StoreLocal extends IInt {
        StoreLocal(int n) { super(n); }
        @Override public void execute(Processor p) { p.stack[p.bp + n] = p.pop(); }
    }
    public static Instruction storeLocal(int offset) { return new StoreLocal(offset); }

    static class Branch extends IInt {
        Branch(int n) { super(n); }
        @Override public void execute(Processor p) { p.pc = n; }
    }
    public static Instruction branch(int address) { return new Branch(address); }

    static class BranchFalse extends IInt {
        BranchFalse(int n) { super(n); }
        @Override public void execute(Processor p) { if (p.pop() == 0) p.pc = n; }
    }
    public static Instruction branchFalse(int address) { return new BranchFalse(address); }

    static class Call extends IInt {
        Call(int n) { super(n); }
        @Override public void execute(Processor p) {
            p.push(p.pc);
            p.push(p.bp);
            p.push(0);      // 関数戻り値格納変数の初期化
            p.pc = n;
            p.bp = p.sp;
        }
    }
    public static Instruction call(int address) { return new Call(address); }

    static class RetFunc extends IInt {
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

    static class RetProc extends IInt {
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

    public static void print(List<Instruction> e, List<Instruction> c) {
        int es = e.size(), cs = c.size();
        System.out.println("size: " + es + (es == cs ? " == " : " <> ") + cs);
        int max = Math.max(e.size(), c.size());
        for (int i = 0; i < max; ++i)
            System.out.println(i + " : "
                + get(e, i)
                + (i < es && i < cs && e.get(i).equals(c.get(i)) ? " == " : " <> ")
                + get(c, i));
    }

    public static boolean equals(List<Instruction> e, List<Instruction> c) {
        int es = e.size(), cs = c.size();
        boolean equals = es == cs && IntStream.range(0, es).allMatch(k -> e.get(k).equals(c.get(k)));
        if (equals) return true;
        print(e, c);
        return false;
    }
}
