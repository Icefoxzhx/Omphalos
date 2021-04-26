package Optimizer;

import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.*;
import IR.operand.Register;

import java.util.*;

public class ADCE {
    public Root root;

    public HashMap<Register, ArrayList<Inst> > regDef;
    public HashSet<Inst> liveInst;
    public Queue<Inst>q;

    public ADCE(Root root){
        this.root=root;
    }

    public void regDefCollect(Function func){
        regDef=new HashMap<>();
        func.blocks.forEach(block->block.insts.forEach(inst->{
            if(inst.reg!=null){
                if(!regDef.containsKey(inst.reg)) regDef.put(inst.reg, new ArrayList<>());
                regDef.get(inst.reg).add(inst);
            }
        }));
    }

    public void doFunc(Function func){
        liveInst=new HashSet<>();
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
                    regDef.get(x).forEach(defInst->{
                        if(!liveInst.contains(defInst)){
                            liveInst.add(defInst);
                            q.add(defInst);
                        }
                    });
                }
            });
        }
        func.blocks.forEach(block -> block.insts.removeIf(inst->!liveInst.contains(inst)));
    }
    public void run(){
        root.func.forEach(this::doFunc);
    }
}
