package ASM.inst;

import ASM.Block;
import ASM.operand.Register;

import java.io.PrintStream;
import java.util.HashSet;

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
    public HashSet<Register> getUse() {
        return new HashSet<>();
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>();
    }

    @Override
    public void replaceUse(Register x, Register y) {

    }

    @Override
    public void replaceDef(Register x, Register y) {

    }
}
