package IR.inst;

import java.io.PrintStream;

public class J extends Inst{
    public String dest;
    public J(String dest){
        this.dest=dest;
    }

    @Override
    public String toString() {
        return dest;
    }

    @Override
    public void printASM(PrintStream prt) {
        prt.println("\tj ."+dest);
    }
}
