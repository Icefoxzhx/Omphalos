package BackEnd;

import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.*;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.Iterator;
import java.util.Map;

public class PhiEliminater {
    public Root root;

    public PhiEliminater(Root root){
        this.root=root;
    }

    public void Eliminate(Function func){
        int block_id=0;
        for(int ii=0;ii<func.blocks.size();++ii){
            Block b=func.blocks.get(ii);
            boolean flag=false;
            for(int i=0;i<b.insts.size();++i){
                Inst inst=b.insts.get(i);
                if(inst instanceof Phi){
                    flag=true;
                    break;
                }
            }
            if(!flag) continue;

            for(int i=0;i<b.pred.size();++i){
                Block x=b.pred.get(i);
                if(x.succ.size()>1){
                    Block tmp=new Block(0,"block.phi."+(block_id++));
                    func.blocks.add(tmp);
                    tmp.insts.add(new J(tmp,b));
                    tmp.terminated=true;
                    tmp.succ.add(b);
                    b.pred.set(i,tmp);
                    b.UpdatePhi(x,tmp);
                    for(int j=0;j<x.succ.size();++j){
                        if(x.succ.get(j)==b) x.succ.set(j,tmp);
                    }
                    x.UpdateBranch(b,tmp);
                }
            }

            for(int i=0;i<b.insts.size();++i){
                Inst inst=b.insts.get(i);
                if(inst instanceof Phi){
                    for(int j=0;j<((Phi) inst).blocks.size();++j){
                        if(((Phi) inst).vals!=null) ((Phi) inst).blocks.get(j).pCopy.put(inst.reg,((Phi) inst).vals.get(j));
                    }
                    b.insts.remove(i);
                    --i;
                }
            }
        }

        for(Block b : func.blocks){
            while(!b.pCopy.isEmpty()){
                boolean flag=true;
                while(flag){
                    flag=false;
                    Iterator<Map.Entry<Register, Operand>> it=b.pCopy.entrySet().iterator();
                    while (it.hasNext()){
                        Map.Entry<Register,Operand> x=it.next();
                        if(!(x.getValue() instanceof Register) || !b.pCopy.containsKey(x.getValue())){
                            b.insts.add(b.insts.size()-1,new Assign(b,x.getKey(),x.getValue()));
                            it.remove();
                            flag=true;
                        }
                    }
                }
                Iterator<Map.Entry<Register, Operand>> it=b.pCopy.entrySet().iterator();
                if(it.hasNext()){
                    Map.Entry<Register,Operand> x=it.next();
                    Register tmp=new Register("tmp");
                    b.insts.add(b.insts.size()-1,new Assign(b,tmp,x.getValue()));
                    b.pCopy.forEach((key,val)->{
                        if(val==x.getValue()) b.pCopy.replace(key,tmp);
                    });
                }
            }
        }
    }

    public void run(){
        root.func.forEach(this::Eliminate);
    }
}
