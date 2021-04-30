package IR.inst;

import IR.Block;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;

public class Phi extends Inst{
    public ArrayList<Block> blocks=new ArrayList<>();
    public ArrayList<Operand> vals=new ArrayList<>();
    public Register phiReg;
    public boolean domPhi=false;

    public Phi(Block block, Register rd){
        super(block,rd);
        phiReg=rd;
    }

    public void add(Block block, Operand val){
        blocks.add(block);
        vals.add(val);
    }

    @Override
    public ArrayList<Operand> getUse() {
        return vals;
    }

    @Override
    public void replaceUse(Operand x, Operand y) {
        for(int i=0;i<vals.size();++i){
            if(vals.get(i)==x){
                vals.set(i,y);
            }
        }
    }

    public void replaceUse(Operand x, Operand y, Block b) {
        for(int i=0;i<vals.size();++i){
            if(vals.get(i)==x){
                vals.set(i,y);
                blocks.set(i,b);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder res=new StringBuilder(reg.toString()+" = phi (");
        for(int i=0;i<blocks.size();++i){
            res.append(" [ ").append(vals.get(i).toString()).append(", ").append(blocks.get(i).toString()).append(" ]");
        }
        return res.toString();
    }
}
