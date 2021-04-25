package IR.inst;

import IR.Block;
import IR.operand.*;

import java.util.ArrayList;

public class Calc extends Inst{
    public Operand rs1,rs2;
    public String op;
    public Calc(Block block, String op, Register rd, Operand rs1, Operand rs2){
        super(block, rd);
        this.op=op;
        this.rs1=rs1;
        this.rs2=rs2;
    }

    @Override
    public ArrayList<Operand> getUse() {
        ArrayList<Operand> res=new ArrayList<>();
        res.add(rs1);
        if(rs2!=null) res.add(rs2);
        return res;
    }

    @Override
    public void replaceUse(Operand x, Operand y) {
        if(rs1==x) rs1=y;
        if(rs2==x) rs2=y;
    }

    @Override
    public String toString() {
        return op+" "+reg.toString()+", "+rs1.toString()+ (rs2==null ? "" : ", "+rs2);
    }
}
