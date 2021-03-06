package IR;

import IR.inst.Branch;
import IR.inst.Inst;
import IR.inst.J;
import IR.inst.Phi;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.HashMap;

public class Block {
    public String name;
    public ArrayList<Inst> insts=new ArrayList<>();
    public ArrayList<Block> pred=new ArrayList<>();
    public ArrayList<Block> succ=new ArrayList<>();
    public HashMap<Register, Operand> pCopy=new HashMap<>();
    public int loopDepth;
    public boolean terminated=false;
    public Phi branchPhi=null;
    public Inst getCond(){
        if(insts.size()<2) return null;
        return insts.get(insts.size()-2);
    }

    public Inst getTerminator(){
        if(insts.isEmpty()) return null;
        return insts.get(insts.size()-1);
    }
    public void addTerminator(Inst xx){
        if(terminated) return;
        insts.add(xx);
        if(xx instanceof J){
            succ.add(((J) xx).dest);
            ((J) xx).dest.pred.add(this);
        }else if(xx instanceof Branch){
            succ.add(((Branch) xx).trueDest);
            succ.add(((Branch) xx).falseDest);
            ((Branch) xx).trueDest.pred.add(this);
            ((Branch) xx).falseDest.pred.add(this);
        }
        terminated=true;
    }
    public void removeTerminator(){
        if(!terminated||insts.isEmpty()) return;
        Inst xx=insts.get(insts.size()-1);
        if(xx instanceof J){
            succ.remove(((J) xx).dest);
            ((J) xx).dest.pred.remove(this);
        }else if(xx instanceof Branch){
            succ.remove(((Branch) xx).trueDest);
            succ.remove(((Branch) xx).falseDest);
            ((Branch) xx).trueDest.pred.remove(this);
            ((Branch) xx).falseDest.pred.remove(this);
        }
        insts.remove(insts.size()-1);
        terminated=false;
    }
    public Block(int loopDepth, String name){
        this.loopDepth=loopDepth;
        this.name=name;
    }

    public void UpdatePhi(Block oldBlock, Block newBlock){
        for(Inst inst : this.insts){
            if(inst instanceof Phi){
                for(int i=0;i<((Phi) inst).blocks.size();++i){
                    if(((Phi) inst).blocks.get(i)==oldBlock) ((Phi) inst).blocks.set(i,newBlock);
                }
            }
        }
    }

    public void UpdateBranch(Block oldBlock, Block newBlock){
        for(Inst inst : this.insts){
            if(inst instanceof J){
                if(((J) inst).dest==oldBlock) ((J) inst).dest=newBlock;
            }else if(inst instanceof Branch){
                if(((Branch) inst).trueDest==oldBlock) ((Branch) inst).trueDest=newBlock;
                if(((Branch) inst).falseDest==oldBlock) ((Branch) inst).falseDest=newBlock;
            }
        }
    }

    public String toString(){
        return name;
    }
}
