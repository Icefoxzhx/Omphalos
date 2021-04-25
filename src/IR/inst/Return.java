package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Return extends Inst{
    public Operand val;

    public Return(Block block, Operand val){
        super(block,null);
        this.val=val;
    }


    @Override
    public ArrayList<Operand> getUse() {
        ArrayList<Operand> res=new ArrayList<>();
        res.add(val);
        return res;
    }

    @Override
    public void replaceUse(Operand x, Operand y) {
        if(val==x) val=y;
    }

    @Override
    public String toString() {
        return "ret";
    }
}
