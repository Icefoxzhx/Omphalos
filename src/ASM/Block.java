package ASM;

import ASM.inst.Branch;
import ASM.inst.Inst;
import ASM.inst.J;

import java.util.ArrayList;

public class Block {
    public String name;
    public ArrayList<Inst> insts=new ArrayList<>();
    public ArrayList<Block> pred=new ArrayList<>();
    public ArrayList<Block> succ=new ArrayList<>();
    public int loopDepth;

    public Inst getTerminator(){
        if(insts.isEmpty()) return null;
        return insts.get(insts.size()-1);
    }
    public void removeTerminator(){
        if(insts.isEmpty()) return;
        Block dest;
        if(insts.get(insts.size()-1) instanceof J){
            dest=((J) insts.get(insts.size()-1)).dest;
        }else if(insts.get(insts.size()-1) instanceof Branch){
            dest=((Branch) insts.get(insts.size()-1)).dest;
        }else return;
        dest.pred.remove(this);
        succ.remove(dest);
        insts.remove(insts.size()-1);
    }
    public Block(int loopDepth, String name){
        this.loopDepth=loopDepth;
        this.name="."+name;
    }
}
