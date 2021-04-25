package IR;

import IR.inst.Branch;
import IR.inst.Inst;
import IR.inst.J;

import java.util.ArrayList;

public class Block {
    public String name;
    public ArrayList<Inst> insts=new ArrayList<>();
    public ArrayList<Block> pred=new ArrayList<>();
    public ArrayList<Block> succ=new ArrayList<>();
    public int loopDepth;
    public boolean terminated=false;
    public Inst getCond(){
        if(insts.size()<2) return null;
        return insts.get(insts.size()-2);
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
        if(insts.isEmpty()) return;
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
}
