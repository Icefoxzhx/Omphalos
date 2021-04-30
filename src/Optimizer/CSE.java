package Optimizer;

import IR.Block;
import IR.Root;
import IR.inst.*;

public class CSE {
    Root root;

    public CSE(Root root){
        this.root=root;
    }

    public boolean same(Cmp a, Cmp b){

        return false;
    }

    public boolean same(Calc a, Calc b){

        return false;
    }

    public void doBlock(Block block){
        for(int i=0;i<block.insts.size();++i){
            Inst inst=block.insts.get(i);
            for(int j=i+1;j<block.insts.size();++j){
                Inst inst2=block.insts.get(j);
                if((inst instanceof Calc && inst2 instanceof Calc && same((Calc) inst,(Calc) inst2))||
                    inst instanceof Cmp && inst2 instanceof Cmp && same((Cmp)inst,(Cmp)inst2)){
                    block.insts.set(j,new Assign(block,inst2.reg,inst.reg));
                }
            }
        }
    }
    public void run(){
        root.func.forEach(func->func.blocks.forEach(this::doBlock));
    }
}