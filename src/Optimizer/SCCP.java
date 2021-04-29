package Optimizer;

import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.*;
import IR.operand.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class SCCP {
    public Root root;
    public Function currentFunc;
    public LinkedHashSet<Block> visited;
    public boolean flag;
    public LinkedHashMap<Register, ArrayList<Inst>> regUse;

    public SCCP(Root root){
        this.root=root;
    }

    public void regUseCollect(){
        regUse=new LinkedHashMap<>();
        currentFunc.blocks.forEach(block->block.insts.forEach(inst->{
            inst.getUse().forEach(x->{
                if(x instanceof Register){
                    if(!regUse.containsKey(x)) regUse.put((Register) x,new ArrayList<>());
                    regUse.get(x).add(inst);
                }
            });
        }));
    }
    public void Replace(Register reg, Operand val){
        regUse.get(reg).forEach(inst -> inst.replaceUse(reg,val));
    }
    public boolean doCalc(Calc inst) {
        if (inst.rs1 instanceof ConstInt && inst.rs2 instanceof ConstInt) {
            if (((inst.op.equals("div") || inst.op.equals("rem")) && ((ConstInt) inst.rs2).val == 0)) return false;
            int res = switch (inst.op) {
                case "mul" -> ((ConstInt) inst.rs1).val * ((ConstInt) inst.rs2).val;
                case "div" -> ((ConstInt) inst.rs1).val / (((ConstInt) inst.rs2).val);
                case "rem" -> ((ConstInt) inst.rs1).val % ((ConstInt) inst.rs2).val;
                case "sub" -> ((ConstInt) inst.rs1).val - ((ConstInt) inst.rs2).val;
                case "sll" -> ((ConstInt) inst.rs1).val << ((ConstInt) inst.rs2).val;
                case "sra" -> ((ConstInt) inst.rs1).val >> ((ConstInt) inst.rs2).val;
                case "and" -> ((ConstInt) inst.rs1).val & ((ConstInt) inst.rs2).val;
                case "xor" -> ((ConstInt) inst.rs1).val ^ ((ConstInt) inst.rs2).val;
                case "or" -> ((ConstInt) inst.rs1).val | ((ConstInt) inst.rs2).val;
                case "add" -> ((ConstInt) inst.rs1).val + ((ConstInt) inst.rs2).val;
                default -> 0;
            };
            Replace(inst.reg, new ConstInt(res));
            return true;
        }
        return false;
    }

    public boolean doCmp(Cmp inst) {
        if (inst.rs1 instanceof ConstInt && inst.rs2 instanceof ConstInt) {
            int res = switch (inst.op) {
                case "slt" -> ((ConstInt) inst.rs1).val < ((ConstInt) inst.rs2).val ? 1 : 0;
                case "sgt" -> ((ConstInt) inst.rs1).val > ((ConstInt) inst.rs2).val ? 1 : 0;
                case "sle" -> ((ConstInt) inst.rs1).val <= ((ConstInt) inst.rs2).val ? 1 : 0;
                case "sge" -> ((ConstInt) inst.rs1).val >= ((ConstInt) inst.rs2).val ? 1 : 0;
                case "eq" -> ((ConstInt) inst.rs1).val == ((ConstInt) inst.rs2).val ? 1 : 0;
                case "ne" -> ((ConstInt) inst.rs1).val != ((ConstInt) inst.rs2).val ? 1 : 0;
                default -> 0;
            };
            Replace(inst.reg, new ConstInt(res));
            return true;
        }
        if(inst.op.equals("eq") && inst.rs1==inst.rs2){
            Replace(inst.reg, new ConstInt(1));
            return true;
        }
        return false;
    }

    public void doBlock(Block block){
        visited.add(block);
        for(int i=0;i<block.insts.size();++i){
            Inst inst=block.insts.get(i);
            if(inst instanceof Calc){
                if(doCalc((Calc)inst)){
                    block.insts.remove(i);
                    --i;
                    flag=true;
                }
            }else if(inst instanceof Cmp){
                if(doCmp((Cmp)inst)){
                    block.insts.remove(i);
                    --i;
                    flag=true;
                }
            }else if(inst instanceof Branch){
                if(((Branch)inst).val instanceof ConstInt){
                    block.removeTerminator();
                    block.addTerminator(new J(block,((ConstInt) ((Branch)inst).val).val==0?((Branch)inst).falseDest:((Branch)inst).trueDest));
                    flag=true;
                }
            }
        }
        block.succ.forEach(x->{
            if(!visited.contains(x)){
                doBlock(x);
            }
        });
    }
    public void doFunc(Function func){
        currentFunc=func;
        flag=true;
        while(flag){
            flag=false;
            regUseCollect();
            visited=new LinkedHashSet<>();
            doBlock(func.beginBlock);
        }
    }
    public void run(){
        root.func.forEach(this::doFunc);
    }
}
