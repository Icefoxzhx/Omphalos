package IR.inst;

import java.io.PrintStream;

public class J extends Inst{
    public int dest;
    public J(int dest){
        this.dest=dest;
    }

    @Override
    public String toString() {
        return "j .L"+dest;
    }

    @Override
    public void printASM(PrintStream prt) {
        prt.println("\tj .L"+dest);
    }
}
