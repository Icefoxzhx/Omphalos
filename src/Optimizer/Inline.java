package Optimizer;

import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.*;
import IR.operand.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Inline {
    public Root root;
    public LinkedHashMap<Function, ArrayList<Function>> calleeFunc = new LinkedHashMap<>();
    public LinkedHashMap<Function, ArrayList<Call>> callerInst = new LinkedHashMap<>();
    public LinkedHashMap<Function, ArrayList<Function>> callerFunc = new LinkedHashMap<>();
    public ArrayList<Call> newcallerInst=new ArrayList<>();
    public ArrayList<Function> newcallerFunc=new ArrayList<>();
    public LinkedHashSet<Function> canNotInline=new LinkedHashSet<>();
    public LinkedHashSet<Function> visited=new LinkedHashSet<>();
    public ArrayList<Function> stack=new ArrayList<>();
    public ArrayList<Function> canInline=new ArrayList<>();
    public LinkedHashMap<Block,Block> inlineBlock;
    public LinkedHashMap<Register,Register> inlineReg;

    public Inline(Root root){
        this.root=root;
    }

    public void dfsBlock(Function func,Block x){
        func.blocks.add(x);
        x.succ.forEach(y->{
            if(!func.blocks.contains(y)) dfsBlock(func,y);
        });
    }
    public void edgeCollect(){
        calleeFunc =new LinkedHashMap<>();
        root.func.forEach(x->{
            calleeFunc.put(x,new ArrayList<>());
            callerInst.put(x,new ArrayList<>());
            callerFunc.put(x,new ArrayList<>());
        });
        root.func.forEach(x->{
            x.blocks.forEach(block->block.insts.forEach(inst -> {
                if(inst instanceof Call && !((Call) inst).func.abs_name.startsWith("__Om_builtin_")){
                    calleeFunc.get(x).add(((Call) inst).func.func);
                    callerInst.get(((Call) inst).func.func).add((Call) inst);
                    callerFunc.get(((Call) inst).func.func).add(x);
                }
            }));
        });
    }

    public void dfs(Function func){
        visited.add(func);
        stack.add(func);
        boolean ring=false;
        for (Function x : stack) {
            if (calleeFunc.get(func).contains(x)) ring = true;
            if(ring) canNotInline.add(x);
        }
        calleeFunc.get(func).forEach(x->{
            if(!visited.contains(x)) dfs(x);
        });
        if(!canNotInline.contains(func)) canInline.add(func);
        stack.remove(stack.size()-1);
    }

    public Register getReg(Register x){
        if(!inlineReg.containsKey(x)) inlineReg.put(x,new Register(x.name));
        return inlineReg.get(x);
    }
    public Operand getOperand(Operand x){
        if(x instanceof Register) return getReg((Register) x);
        return x;
    }
    public Block getBlock(Block block){
        return inlineBlock.get(block);
    }

    public void inline(Call call,Function caller,String prefix){
        Function callee=call.func.func;
        int totalInstNum=callee.blocks.stream().mapToInt(b->b.insts.size()).sum();
        if(callee.blocks.size()>30 || totalInstNum>300) return;
        inlineBlock=new LinkedHashMap<>();
        inlineReg=new LinkedHashMap<>();
        callee.blocks.forEach(b->{
            Block nb=new Block(b.loopDepth,prefix+b.name);
            nb.terminated=b.terminated;
            inlineBlock.put(b,nb);
        });
        Block beginBlock=getBlock(callee.beginBlock),endBlock=getBlock(callee.endBlock);
        for (Block b : callee.blocks) {
            Block nb = getBlock(b);
            b.pred.forEach(x -> nb.pred.add(getBlock(x)));
            b.succ.forEach(x -> nb.succ.add(getBlock(x)));
            for (Inst inst : b.insts) {
                if (inst instanceof Assign) {
                    nb.insts.add(new Assign(nb, getReg(inst.reg), getOperand(((Assign) inst).val)));
                } else if (inst instanceof Calc) {
                    nb.insts.add(new Calc(nb, ((Calc) inst).op, getReg(inst.reg), getOperand(((Calc) inst).rs1), getOperand(((Calc) inst).rs2)));
                } else if (inst instanceof Branch) {
                    nb.insts.add(new Branch(nb, getOperand(((Branch) inst).val), getBlock(((Branch) inst).trueDest), getBlock(((Branch) inst).falseDest)));
                } else if (inst instanceof Cmp) {
                    nb.insts.add(new Cmp(nb, ((Cmp) inst).op, getReg(inst.reg), getOperand(((Cmp) inst).rs1), getOperand(((Cmp) inst).rs2)));
                } else if (inst instanceof Call) {
                    Call ninst = new Call(nb, ((Call) inst).func, getReg(inst.reg));
                    ((Call) inst).params.forEach(x -> ninst.params.add(getOperand(x)));
                    nb.insts.add(ninst);
                    if(!((Call) ninst).func.abs_name.startsWith("__Om_builtin_")){
                        callerInst.get(((Call) ninst).func.func).add((Call) ninst);
                        callerFunc.get(((Call) ninst).func.func).add(caller);
                    }
                } else if (inst instanceof J) {
                    nb.insts.add(new J(nb, getBlock(((J) inst).dest)));
                } else if (inst instanceof Load) {
                    nb.insts.add(new Load(nb, getReg(inst.reg), getOperand(((Load) inst).addr)));
                } else if (inst instanceof Store) {
                    nb.insts.add(new Store(nb, getOperand(((Store) inst).rs), getOperand(((Store) inst).addr)));
                } else if (inst instanceof Return) {
                    nb.insts.add(new Return(nb, getOperand(((Return) inst).val)));
                    endBlock = nb;
                }else if (inst instanceof Phi){
                    Phi ninst=new Phi(nb,getReg(inst.reg));
                    ninst.domPhi=((Phi) inst).domPhi;
                    for(int i=0;i<((Phi) inst).blocks.size();++i){
                        ninst.add(getBlock(((Phi) inst).blocks.get(i)),getOperand(((Phi) inst).vals.get(i)));
                    }
                    nb.insts.add(ninst);
                }
            }
        }
        Block callerBlock=call.block;
        int pos=callerBlock.insts.indexOf(call);
        Block b1=new Block(callerBlock.loopDepth,callerBlock.name+".inline1");
        b1.insts=new ArrayList<>(callerBlock.insts.subList(0,pos));
        for(int i=0;i<call.params.size();++i)
            b1.insts.add(new Assign(b1,getReg((Register) callee.params.get(i)),call.params.get(i)));
        b1.insts.addAll(beginBlock.insts);
        b1.pred=callerBlock.pred;
        b1.succ=beginBlock.succ;
        b1.terminated=true;
        b1.insts.forEach(x->x.block=b1);
        b1.pred.forEach(x->{
            if(x.getTerminator() instanceof J){
                if(((J) x.getTerminator()).dest==callerBlock) ((J) x.getTerminator()).dest=b1;
            }else if(x.getTerminator() instanceof Branch){
                if(((Branch) x.getTerminator()).trueDest==callerBlock) ((Branch) x.getTerminator()).trueDest=b1;
                if(((Branch) x.getTerminator()).falseDest==callerBlock) ((Branch) x.getTerminator()).falseDest=b1;
            }
            x.succ.remove(callerBlock);
            x.succ.add(b1);
            x.UpdateBranch(callerBlock,b1);
        });
        b1.succ.forEach(x->{
            x.pred.remove(beginBlock);
            x.pred.add(b1);
            x.UpdatePhi(beginBlock,b1);
        });
        if(caller.beginBlock==callerBlock) caller.beginBlock=b1;
        if(endBlock==beginBlock) endBlock=b1;

        Block b2=new Block(callerBlock.loopDepth,callerBlock.name+".inline2");
        Return ret=(Return)endBlock.getTerminator();
        endBlock.removeTerminator();
        b2.insts=new ArrayList<>(endBlock.insts);
        if(ret.val!=null) b2.insts.add(new Assign(b2,call.reg, ret.val));
        b2.insts.addAll(callerBlock.insts.subList(pos+1,callerBlock.insts.size()));
        b2.insts.forEach(x->x.block=b2);

        b2.pred=endBlock.pred;
        b2.succ=callerBlock.succ;
        b2.terminated=true;


        Block finalEndBlock = endBlock;
        b2.pred.forEach(x->{
            if(x.getTerminator() instanceof J){
                if(((J) x.getTerminator()).dest== finalEndBlock) ((J) x.getTerminator()).dest=b2;
            }else if(x.getTerminator() instanceof Branch){
                if(((Branch) x.getTerminator()).trueDest==finalEndBlock) ((Branch) x.getTerminator()).trueDest=b2;
                if(((Branch) x.getTerminator()).falseDest==finalEndBlock) ((Branch) x.getTerminator()).falseDest=b2;
            }
            x.succ.remove(finalEndBlock);
            x.succ.add(b2);
            x.UpdateBranch(finalEndBlock,b2);

        });
        b2.succ.forEach(x->{
            x.pred.remove(callerBlock);
            x.pred.add(b2);
            x.UpdatePhi(callerBlock,b2);
        });
        if(caller.beginBlock==endBlock) caller.beginBlock=b2;

        caller.blocks=new ArrayList<>();
        dfsBlock(caller,caller.beginBlock);
    }

    public void doInline(Function x){
        int totalInstNum=x.blocks.stream().mapToInt(b->b.insts.size()).sum();
        if(x.blocks.size()>30 || totalInstNum>300) return;
        for(int i=0;i<callerInst.get(x).size();++i){
            inline(callerInst.get(x).get(i),callerFunc.get(x).get(i),"I."+x.name+"."+i+".");
        }
        root.func.remove(x);
    }

    public void ForceInline(Function func){
        for(int ii=0;ii<4;++ii){
            int totalInstNum=func.blocks.stream().mapToInt(b->b.insts.size()).sum();
            if(func.blocks.size()>30 || totalInstNum>300) break;
            newcallerInst.clear();
            newcallerInst.addAll(callerInst.get(func));
            newcallerFunc.clear();
            newcallerFunc.addAll(callerFunc.get(func));
            callerInst.get(func).clear();
            callerFunc.get(func).clear();
            for(int i=0;i<newcallerInst.size();++i){
                inline(newcallerInst.get(i),newcallerFunc.get(i),"FI."+ii+"."+func.name+"."+i+".");
            }
        }
    }
    public void run(){
        edgeCollect();
        canNotInline.add(root.mainFunc);
        dfs(root.mainFunc);
        canInline.forEach(this::doInline);
        root.func.forEach(func->{
            if(callerFunc.get(func).contains(func)) ForceInline(func);
        });
    }
}
