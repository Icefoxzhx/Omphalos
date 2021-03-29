package IR.inst;

import java.io.PrintStream;

public class Label extends Inst{
    public String label;
    public Label(String label){
        this.label=label;
    }

    @Override
    public String toString() {
        return "."+label+":";
    }

    @Override
    public void printASM(PrintStream prt) {
        prt.println("."+label+":");
    }
}
