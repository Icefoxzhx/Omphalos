package Optimizer;

import IR.inst.Call;
import IR.inst.Inst;
import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.J;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Simplify {
    public Root root;
    public Function currentFunc=null;
    public LinkedHashSet<Register> regUse;
    public Simplify(Root root){
        this.root=root;
    }

    public void dfsBlock(Block block){
        currentFunc.blocks.add(block);
        block.succ.forEach(x->{
            if(!currentFunc.blocks.contains(x)) dfsBlock(x);
        });
    }

    public void regUseCollect(){
        regUse=new LinkedHashSet<>();
        currentFunc.blocks.forEach(block->block.insts.forEach(inst->
                inst.getUse().forEach(x->{
                    if(x instanceof Register) regUse.add((Register) x);
                })));
    }
    public void removeDeadBlock(){
        currentFunc.blocks.forEach(x->{
            for(int i=0;i<x.pred.size();++i){
                if(!currentFunc.blocks.contains(x.pred.get(i))){
                    x.pred.remove(i);
                    --i;
                }
            }
        });
    }

    public void removeDeadInst(){
        boolean flag=true;
        while(flag){
            flag=false;
            regUseCollect();
            for(Block block: currentFunc.blocks){
                for(int i=0;i<block.insts.size();++i){
                    Inst inst=block.insts.get(i);
                    if(inst.reg!=null&&!regUse.contains(inst.reg)&&!(inst instanceof Call)){
                        block.insts.remove(i);
                        --i;
                        flag=true;
                    }
                }
            }
        }
    }

    public void BlockMerge(){
        for(int i=0;i<currentFunc.blocks.size();++i){
            Block block=currentFunc.blocks.get(i);
            if(block.pred.size()==1&&block.pred.get(0).getTerminator() instanceof J) {
                Block b = block.pred.get(0);
                b.removeTerminator();
                for(int ii=0;ii<block.insts.size()-1;++ii){
                    Inst inst=block.insts.get(ii);
                    inst.block=b;
                    b.insts.add(inst);
                }
                b.addTerminator(block.getTerminator());
                b.getTerminator().block=b;
                currentFunc.blocks.remove(i);
                --i;
            }
        }
    }
    public void doFunc(Function func){
        currentFunc=func;
        currentFunc.blocks=new ArrayList<>();
        dfsBlock(currentFunc.beginBlock);
        removeDeadBlock();
        removeDeadInst();
        BlockMerge();
    }
    public void run(){
        root.func.forEach(this::doFunc);
    }
}
