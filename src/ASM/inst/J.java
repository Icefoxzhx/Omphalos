package ASM.inst;

import ASM.Block;
import ASM.operand.Register;

import java.io.PrintStream;
import java.util.LinkedHashSet;

public class J extends Inst{
    public Block dest;
    public J(Block dest){
        this.dest=dest;
    }

    @Override
    public String toString() {
        return "j "+dest.name;
    }

    @Override
    public LinkedHashSet<Register> getUse() {
        return new LinkedHashSet<>();
    }

    @Override
    public LinkedHashSet<Register> getDef() {
        return new LinkedHashSet<>();
    }

    @Override
    public void replaceUse(Register x, Register y) {

    }

    @Override
    public void replaceDef(Register x, Register y) {

    }
}
