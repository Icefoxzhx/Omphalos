package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.HashSet;

public class J extends Inst{
    public Block dest;
    public J(Block block, Block dest){
        super(block,null);
        this.dest=dest;
    }

    @Override
    public ArrayList<Operand> getUse() {
        return new ArrayList<>();
    }

    @Override
    public void replaceUse(Operand x, Operand y) {

    }

    @Override
    public String toString() {
        return "j "+dest.name;
    }
}
