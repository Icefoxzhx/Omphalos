package IR.inst;

import IR.Block;
import IR.operand.*;

import java.util.ArrayList;
import java.util.HashSet;

public class Branch extends Inst{
    public Operand val;
    public Block trueDest,falseDest;

    public Branch(Block block, Operand val, Block trueDest, Block falseDest) {
        super(block,null);
        this.val = val;
        this.trueDest = trueDest;
        this.falseDest = falseDest;
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
        return "branch "+val.toString()+", "+trueDest.name+", "+falseDest.name;
    }
}
