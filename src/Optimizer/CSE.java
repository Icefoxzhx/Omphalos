package Optimizer;

import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.*;
import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CSE {
    public Root root;
    public Function currentFunc;
    public LinkedHashMap<Register, ArrayList<Inst>> regUse;

    public CSE(Root root){
        this.root=root;
    }

    public boolean same(Cmp a, Cmp b){
        return a.op.equals(b.op)&&a.rs1.equals(b.rs1)&&a.rs2.equals(b.rs2);
    }

    public boolean same(Calc a, Calc b){
        return a.op.equals(b.op)&&a.rs1.equals(b.rs1)&&a.rs2.equals(b.rs2);
    }

    public void regUseCollect(){
        regUse=new LinkedHashMap<>();
        currentFunc.blocks.forEach(block->block.insts.forEach(inst-> inst.getUse().forEach(x->{
            if(x instanceof Register){
                if(!regUse.containsKey(x)) regUse.put((Register) x,new ArrayList<>());
                regUse.get(x).add(inst);
            }
        })));
    }

    public void Replace(Register reg, Operand val){
        regUse.get(reg).forEach(inst -> inst.replaceUse(reg,val));
    }

    public void doBlock(Block block){
        for(int i=0;i<block.insts.size();++i){
            Inst inst=block.insts.get(i);
            for(int j=i+1;j<block.insts.size();++j){
                Inst inst2=block.insts.get(j);
                if((inst instanceof Calc && inst2 instanceof Calc && same((Calc) inst,(Calc) inst2))||
                    inst instanceof Cmp && inst2 instanceof Cmp && same((Cmp)inst,(Cmp)inst2)){
                    Replace(inst2.reg,inst.reg);
                    block.insts.remove(j);
                    --j;
                }
            }
        }
    }
    public void run(){
        root.func.forEach(func->{
            currentFunc=func;
            regUseCollect();
            func.blocks.forEach(this::doBlock);
        });
    }
}