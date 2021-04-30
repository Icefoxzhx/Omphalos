package BackEnd;

import ASM.Block;
import ASM.Function;
import ASM.operand.*;
import ASM.inst.*;
import IR.inst.Assign;
import IR.inst.Cmp;
import IR.inst.Return;
import IR.operand.ConstInt;
import IR.operand.ConstStr;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static java.lang.Math.abs;

public class ASMBuilder {
    public IR.Root IRRoot;
    public ASM.Root ASMRoot;

    public Function currentFunc=null;
    public Block currentBlock=null;
    public LinkedHashMap<IR.operand.Operand, Register> regMap=new LinkedHashMap<>();
    public LinkedHashMap<IR.Block, Block> blockMap=new LinkedHashMap<>();
    public LinkedHashMap<IR.Function, Function> funcMap=new LinkedHashMap<>();

    public ASMBuilder(IR.Root IRRoot,ASM.Root ASMRoot){
        this.IRRoot=IRRoot;
        this.ASMRoot=ASMRoot;
    }

    public Function getFunction(IR.Function func){
        if(funcMap.get(func)==null) funcMap.put(func,new Function(func.name));
        return funcMap.get(func);
    }

    public Block getBlock(IR.Block block){
        if(blockMap.get(block)==null) blockMap.put(block,new Block(block.loopDepth,block.name));
        return blockMap.get(block);
    }
    public Register getReg(IR.operand.Operand x){
        if(x==null)
            return null;
        if(x instanceof IR.operand.Register){
            if(regMap.get(x)==null) regMap.put(x,new VReg(((IR.operand.Register) x).name));
            return regMap.get(x);
        }else{
            VReg tmp=new VReg("tmp");
            if(x instanceof IR.operand.ConstStr){
                currentBlock.insts.add(new Li(tmp, new Symbol(((ConstStr) x).name),"la"));
            }else{
                currentBlock.insts.add(new Li(tmp,new Imm(((ConstInt)x).val),"li"));
            }
            return tmp;
        }
    }

    public void assign(Register x, IR.operand.Operand y){
        if(y instanceof IR.operand.Register){
            currentBlock.insts.add(new Mv(x,getReg(y)));
        }else{
            if(y instanceof IR.operand.ConstInt){
                currentBlock.insts.add(new Li(x,new Imm(((ConstInt) y).val),"li"));
            }else currentBlock.insts.add(new Mv(x,getReg(y)));
        }
    }

    public void doInst(IR.inst.Inst inst){
        if(inst instanceof IR.inst.Assign){
            assign(getReg(inst.reg), ((Assign) inst).val);
        }else if(inst instanceof IR.inst.Calc){
            Register rd=getReg(inst.reg),rs1;
            Operand rs2;
            String op=((IR.inst.Calc) inst).op;
            if(op.equals("mul")||op.equals("div")||op.equals("rem")){
                rs1=getReg(((IR.inst.Calc) inst).rs1);
                rs2=getReg(((IR.inst.Calc) inst).rs2);
            }else{
                if(((IR.inst.Calc) inst).rs2 instanceof IR.operand.ConstInt){
                    rs1=getReg(((IR.inst.Calc) inst).rs1);
                    int val= ((ConstInt) ((IR.inst.Calc) inst).rs2).val;
                    if(val>=-2048&&val<=2047){
                        rs2=new Imm(val);
                        if(op.equals("sub")){
                            op="addi";
                            ((Imm)rs2).val=-((Imm)rs2).val;
                        }else op=op+"i";
                    }else{
                        rs2=getReg(((IR.inst.Calc) inst).rs2);
                    }
                }else if(((IR.inst.Calc) inst).rs1 instanceof IR.operand.ConstInt && ((IR.inst.Calc) inst).rs2!=null){
                    int val=((ConstInt) ((IR.inst.Calc) inst).rs1).val;
                    if(!op.equals("sll") && !op.equals("sla") && !op.equals("sub") && val>=-2048&&val<=2047){
                        rs1=getReg(((IR.inst.Calc) inst).rs2);
                        rs2=new Imm(val);
                        op=op+"i";
                    }else{
                        rs1=getReg(((IR.inst.Calc) inst).rs1);
                        rs2=getReg(((IR.inst.Calc) inst).rs2);
                    }
                }else{
                    rs1=getReg(((IR.inst.Calc) inst).rs1);
                    rs2=getReg(((IR.inst.Calc) inst).rs2);
                }
            }
            currentBlock.insts.add(new Calc(op,rd,rs1,rs2));
        }else if(inst instanceof IR.inst.Branch){
            if(inst.block.getCond() instanceof Cmp){
                Cmp cmp= (Cmp)inst.block.getCond();
                if(cmp.reg==((IR.inst.Branch) inst).val){
                    String op=switch (cmp.op){
                        case "slt" -> "bge";
                        case "sgt" -> "ble";
                        case "sle" -> "bgt";
                        case "sge" -> "blt";
                        case "eq" -> "bne";
                        case "ne" -> "beq";
                        default -> "error";
                    };
                    currentBlock.insts.add(new Branch(op,getReg(cmp.rs1),getReg(cmp.rs2),getBlock(((IR.inst.Branch) inst).falseDest)));
                    currentBlock.insts.add(new J(getBlock(((IR.inst.Branch) inst).trueDest)));
                }
            }else{
                currentBlock.insts.add(new Branch("beqz",getReg(((IR.inst.Branch) inst).val),null,getBlock(((IR.inst.Branch) inst).falseDest)));
                currentBlock.insts.add(new J(getBlock(((IR.inst.Branch) inst).trueDest)));
            }
        }else if(inst instanceof IR.inst.Call){
            for(int i = 0; i<((IR.inst.Call) inst).params.size(); ++i) {
                if (i < 8) {
                    currentBlock.insts.add(new Mv(ASMRoot.getPReg(10 + i), getReg(((IR.inst.Call) inst).params.get(i))));
                } else {
                    currentBlock.insts.add(new Store(getReg(((IR.inst.Call) inst).params.get(i)), ASMRoot.getPReg(2), new Imm((7 - i) * 4)));
                }
            }
            currentBlock.insts.add(new Call(((IR.inst.Call) inst).func,ASMRoot));
            if(inst.reg!=null){
                currentBlock.insts.add(new Mv(getReg(inst.reg),ASMRoot.getPReg(10)));
            }
        }else if(inst instanceof IR.inst.Cmp){
            Cmp xx=(Cmp) inst;
            if(xx.rs2 instanceof ConstInt){
                int val=((ConstInt) xx.rs2).val;
                if(val<=2047&&val>=-2048){
                    Register rs1=getReg(xx.rs1),tmp=getReg(inst.reg);
                    Imm rs2=new Imm(val);
                    switch (xx.op){
                        case "slt":
                            currentBlock.insts.add(new Calc("slti", tmp, rs1, rs2));
                            return;
                        case "sge":
                            currentBlock.insts.add(new Calc("slti", tmp, rs1, rs2));
                            currentBlock.insts.add(new Calc("xori", tmp,tmp,new Imm(1)));
                            return;
                        case "eq":
                            currentBlock.insts.add(new Calc("xori", tmp, rs1, rs2));
                            currentBlock.insts.add(new Calc("seqz", tmp,tmp,null));
                            return;
                        case "ne":
                            currentBlock.insts.add(new Calc("xori", tmp, rs1, rs2));
                            currentBlock.insts.add(new Calc("snez", tmp,tmp,null));
                            return;
                        default:
                            break;
                    }
                }
            }
            if(xx.rs1 instanceof ConstInt && xx.rs2!=null){
                int val=((ConstInt) xx.rs1).val;
                if(val<=2047&&val>=-2048){
                    Register rs1=getReg(xx.rs2),tmp=getReg(inst.reg);
                    Imm rs2=new Imm(val);
                    switch (xx.op){
                        case "sgt":
                            currentBlock.insts.add(new Calc("slti", tmp, rs1, rs2));
                            return;
                        case "sle":
                            currentBlock.insts.add(new Calc("slti", tmp, rs1, rs2));
                            currentBlock.insts.add(new Calc("xori", tmp,tmp,new Imm(1)));
                            return;
                        case "eq":
                            currentBlock.insts.add(new Calc("xori", tmp, rs1, rs2));
                            currentBlock.insts.add(new Calc("seqz", tmp,tmp,null));
                            return;
                        case "ne":
                            currentBlock.insts.add(new Calc("xori", tmp, rs1, rs2));
                            currentBlock.insts.add(new Calc("snez", tmp,tmp,null));
                            return;
                        default:
                            break;
                    }
                }
            }
            Register rs1=getReg(xx.rs1),rs2=getReg(xx.rs2),tmp=getReg(inst.reg);
            switch (xx.op){
                case "slt":
                    currentBlock.insts.add(new Calc("slt", tmp, rs1, rs2));
                    break;
                case "sgt":
                    currentBlock.insts.add(new Calc("sgt", tmp, rs1, rs2));
                    break;
                case "sle":
                    currentBlock.insts.add(new Calc("sgt", tmp, rs1, rs2));
                    currentBlock.insts.add(new Calc("xori", tmp,tmp,new Imm(1)));
                    break;
                case "sge":
                    currentBlock.insts.add(new Calc("slt", tmp, rs1, rs2));
                    currentBlock.insts.add(new Calc("xori", tmp,tmp,new Imm(1)));
                    break;
                case "eq":
                    currentBlock.insts.add(new Calc("xor", tmp, rs1, rs2));
                    currentBlock.insts.add(new Calc("seqz", tmp,tmp,null));
                    break;
                case "ne":
                    currentBlock.insts.add(new Calc("xor", tmp, rs1, rs2));
                    currentBlock.insts.add(new Calc("snez", tmp,tmp,null));
                    break;
                default:
                    break;
            }
        }else if(inst instanceof IR.inst.J){
            currentBlock.insts.add(new J(getBlock(((IR.inst.J) inst).dest)));
        }else if(inst instanceof IR.inst.Load){
            Register rd=getReg(inst.reg);
            if(((IR.inst.Load) inst).addr instanceof IR.operand.Symbol){
                currentBlock.insts.add(new Li(rd,new Symbol(((IR.operand.Symbol)((IR.inst.Load) inst).addr).name),"lw"));
            }else{
                currentBlock.insts.add(new Load(rd,getReg(((IR.inst.Load) inst).addr),new Imm(0)));
            }
        }else if(inst instanceof IR.inst.Store){
            Register rs=getReg(((IR.inst.Store) inst).rs);
            if(((IR.inst.Store) inst).addr instanceof IR.operand.Symbol){
                VReg tmp=new VReg("tmp");
                currentBlock.insts.add(new Sw(rs,tmp,new Symbol(((IR.operand.Symbol)((IR.inst.Store) inst).addr).name)));
            }else{
                currentBlock.insts.add(new Store(rs,getReg(((IR.inst.Store) inst).addr),new Imm(0)));
            }
        }else if(inst instanceof IR.inst.Return){
            if(((Return) inst).val!=null) assign(ASMRoot.getPReg(10),((Return) inst).val);
            for(int i=0;i<ASMRoot.getCalleeSave().size();++i){
                currentBlock.insts.add(new Mv(ASMRoot.getCalleeSave().get(i), currentFunc.calleeSaveReg.get(i)));
            }
            currentBlock.insts.add(new Mv(ASMRoot.getPReg(1),currentFunc.raSaveReg));
            currentBlock.insts.add(new Ret(ASMRoot));
            currentFunc.endBlock=currentBlock;
        }
    }
    public void doBlock(IR.Block block){
        currentBlock=getBlock(block);
        currentFunc.blocks.add(currentBlock);
        currentBlock.name=block.name;
        block.pred.forEach(x-> currentBlock.pred.add(getBlock(x)));
        block.succ.forEach(x-> currentBlock.succ.add(getBlock(x)));
        block.insts.forEach(this::doInst);
    }

    public void doFunc(IR.Function func){
        regMap=new LinkedHashMap<>();
        blockMap=new LinkedHashMap<>();
        currentFunc=getFunction(func);
        currentFunc.beginBlock=getBlock(func.beginBlock);
        currentBlock=currentFunc.beginBlock;
        ASMRoot.func.add(currentFunc);
        func.params.forEach(x->currentFunc.params.add(getReg(x)));
        for(int i=0;i<ASMRoot.getCalleeSave().size();++i){
            VReg tmp=new VReg("tmp");
            currentFunc.calleeSaveReg.add(tmp);
            currentBlock.insts.add(new Mv(tmp,ASMRoot.getCalleeSave().get(i)));
        }
        VReg tmp=new VReg("tmp");
        currentFunc.raSaveReg=tmp;
        currentBlock.insts.add(new Mv(tmp,ASMRoot.getPReg(1)));
        for(int i=0;i<currentFunc.params.size();++i){
            if(i<8) currentBlock.insts.add(new Mv(currentFunc.params.get(i),ASMRoot.getPReg(10+i)));
            else{
                Inst xx=new Load(currentFunc.params.get(i),ASMRoot.getPReg(2),new Imm((7-i)*4));
                xx.isOriginalOffset=true;
                currentBlock.insts.add(xx);
            }
        }
        func.blocks.forEach(this::doBlock);
        removeUselessInst();
    }

    public void removeUselessInst(){
        final boolean[] flag = {true};
        while(flag[0]){
            flag[0] =false;
            LinkedHashSet<Register> used=new LinkedHashSet<>();
            used.add(ASMRoot.getPReg(10));
            currentFunc.blocks.forEach(b->b.insts.forEach(x->used.addAll(x.getUse())));
            currentFunc.blocks.forEach(b->{
                for(int i=0;i<b.insts.size();++i){
                    Inst xx=b.insts.get(i);
                    if((xx instanceof Calc && !used.contains(((Calc) xx).rd))||
                        xx instanceof Li && !used.contains(((Li) xx).rd)||
                        xx instanceof Load && !used.contains(((Load) xx).rd)||
                        xx instanceof Mv && !used.contains(((Mv) xx).rd)){
                        b.insts.remove(i);
                        --i;
                        flag[0] =true;
                    }
                }
            });
        }
    }
    public void run(){
        ASMRoot.globals = IRRoot.globals;
        ASMRoot.strings = IRRoot.strings;
        IRRoot.func.forEach(this::doFunc);
    }
}
