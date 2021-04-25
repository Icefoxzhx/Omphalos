package IR.inst;

import IR.Block;
import IR.operand.*;

import java.util.ArrayList;
import java.util.Collections;

public class Assign extends Inst{
    public Operand val;
    public Assign(Block block, Register rd, Operand val){
        super(block,rd);
        this.val=val;
    }

    @Override
    public ArrayList<Operand> getUse() {
        return new ArrayList<>(Collections.singletonList(val));
    }

    @Override
    public void replaceUse(Operand x, Operand y) {
        if(val==x) val=y;
    }

    @Override
    public String toString() {
        return reg.toString()+" = "+val.toString();
    }

}
