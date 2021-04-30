package Optimizer;

import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.*;
import IR.operand.Register;

import java.util.*;

public class ADCE {
    public Root root;

    public LinkedHashMap<Register, Inst > regDef;
    public LinkedHashSet<Inst> liveInst;
    public Queue<Inst>q;

    public ADCE(Root root){
        this.root=root;
    }

    public void regDefCollect(Function func){
        regDef=new LinkedHashMap<>();
        func.blocks.forEach(block->block.insts.forEach(inst->{
            if(inst.reg!=null){
                regDef.put(inst.reg,inst) ;
            }
        }));
    }

    public void doFunc(Function func){
        liveInst=new LinkedHashSet<>();
        q=new LinkedList<>();
        regDefCollect(func);
        func.blocks.forEach(block -> block.insts.forEach(inst->{
            if(inst instanceof Return || inst instanceof Store || inst instanceof Call || inst instanceof J || inst instanceof Branch){
                liveInst.add(inst);
                q.add(inst);
            }
        }));
        while(!q.isEmpty()){
            Inst inst=q.poll();
            inst.getUse().forEach(x->{
                if(x instanceof Register && regDef.containsKey(x)){
                    Inst defInst=regDef.get(x);
                    if(!liveInst.contains(defInst)){
                        liveInst.add(defInst);
                        q.add(defInst);
                    }
                }
            });
        }
        func.blocks.forEach(block -> block.insts.removeIf(inst->!liveInst.contains(inst)));
    }
    public void run(){
        root.func.forEach(this::doFunc);
    }
}
