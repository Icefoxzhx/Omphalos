package IR.inst;

import java.io.PrintStream;

public class Label extends Inst{
    public int label;
    public Label(int label){
        this.label=label;
    }

    @Override
    public String toString() {
        return "L"+label+":";
    }

    @Override
    public void printASM(PrintStream prt) {
        prt.println(".L"+label+":");
    }
}
