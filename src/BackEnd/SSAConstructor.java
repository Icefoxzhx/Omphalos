package BackEnd;

import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.Inst;
import IR.inst.Phi;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class SSAConstructor {
    public Root root;
    public Function currentFunc;
    public LinkedHashMap<Block,Integer> dfn=new LinkedHashMap<>();
    public LinkedHashMap<Block,Block> iDom=new LinkedHashMap<>();
    public LinkedHashMap<Block, ArrayList<Block>> domSon=new LinkedHashMap<>();
    public LinkedHashMap<Block,ArrayList<Block>> domFr=new LinkedHashMap<>();
    public ArrayList<Block> topoBlocks=new ArrayList<>();

    public SSAConstructor(Root root){
        this.root=root;
    }

    public void dfsBlock(Block block){
        currentFunc.blocks.add(block);
        block.succ.forEach(x->{
            if(!currentFunc.blocks.contains(x)) dfsBlock(x);
        });
        topoBlocks.add(0,block);
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

    public void init(){
        currentFunc.params.forEach(x->currentFunc.vars.add((Register) x));
        currentFunc.blocks.forEach(b->b.insts.forEach(inst->{
            if(inst instanceof Phi){
                for(int i=0;i<((Phi) inst).blocks.size();++i){
                    if(currentFunc.blocks.contains(((Phi) inst).blocks.get(i))) continue;
                    ((Phi) inst).blocks.remove(i);
                    ((Phi) inst).vals.remove(i);
                    --i;

                }
            }
            if(inst.reg!=null&&!inst.reg.name.equals("tmp.")){
                currentFunc.vars.add(inst.reg);
                inst.reg.assign.add(inst);
            }
        }));
    }
    public Block Intersect(Block x, Block y){
        if(x==null) return y;
        if(y==null) return x;
        while(x!=y){
            while(dfn.get(x)>dfn.get(y)) x=iDom.get(x);
            while(dfn.get(x)<dfn.get(y)) y=iDom.get(y);
        }
        return x;
    }
    public void DomTree(){
        for(int i=0;i<topoBlocks.size();++i){
            dfn.put(topoBlocks.get(i),i);
            iDom.put(topoBlocks.get(i),null);
            domSon.put(topoBlocks.get(i),new ArrayList<>());
        }
        iDom.replace(currentFunc.beginBlock,currentFunc.beginBlock);
        boolean flag=true;
        while(flag){
            flag=false;
            for(Block x : topoBlocks){
                if(x==currentFunc.beginBlock) continue;
                Block new_iDom=null;
                for(Block y: x.pred){
                    if(iDom.get(y)!=null) new_iDom=Intersect(new_iDom,y);
                }
                if(iDom.get(x)!=new_iDom){
                    iDom.replace(x,new_iDom);
                    flag=true;
                }
            }
        }
        iDom.forEach((x,f)->{
            if(f!=null&&x!=f) domSon.get(f).add(x);
        });
    }

    public void DomFrontier(){
        topoBlocks.forEach(x->domFr.put(x, new ArrayList<>()));
        topoBlocks.forEach(x->{
            if(x.pred.size()>=2){
                x.pred.forEach(y->{
                    Block t=y;
                    while(t!=iDom.get(x)){
                        domFr.get(t).add(x);
                        t=iDom.get(t);
                    }
                });
            }
        });
    }

    public void GetPhi(){
        dfn=new LinkedHashMap<>();
        iDom=new LinkedHashMap<>();
        domSon=new LinkedHashMap<>();
        domFr=new LinkedHashMap<>();
        DomTree();
        DomFrontier();
        currentFunc.vars.forEach(x->{
            LinkedHashSet<Block> have=new LinkedHashSet<>();
            for(int i=0;i<x.assign.size();++i){
                Inst inst=x.assign.get(i);
                domFr.get(inst.block).forEach(b->{
                    if(!have.contains(b)){
                        Phi t=new Phi(b,x);
                        t.domPhi=true;
                        b.insts.add(0,t);
                        x.assign.add(t);
                        have.add(b);
                    }
                });
            }
        });
    }

    public void RenameVar(Register x, Block b){
        Register xx=x.rename_stack.peek();
        b.insts.forEach(inst->{
            if(!(inst instanceof Phi)||!(((Phi) inst).domPhi)){
                inst.replaceUse(x,x.rename_stack.peek());
            }
            if(inst.reg!=null&&inst.reg==x){
                x.rename_stack.push(new Register(x.name+"_"+(x.rename_id++)));
                inst.reg=x.rename_stack.peek();
            }
        });
        b.succ.forEach(y->y.insts.forEach(inst->{
            if(inst instanceof Phi&&((Phi) inst).phiReg==x){
                if(x.rename_stack.size()>1) ((Phi) inst).add(b,x.rename_stack.peek());
                else ((Phi) inst).add(b,null);//todo
            }
        }));
        domSon.get(b).forEach(y->RenameVar(x,y));
        while(x.rename_stack.peek()!=xx) x.rename_stack.pop();
    }

    public void CheckPhi(){
        currentFunc.blocks.forEach(block->{
            LinkedHashMap<Register,Phi> Phis=new LinkedHashMap<>();
            block.insts.forEach(inst->{
                if(inst instanceof Phi) Phis.put(inst.reg,(Phi)inst);
            });
            block.insts.forEach(inst->{
                if(inst instanceof Phi){
                    for(int i=0;i<((Phi) inst).vals.size();++i){
                        if(((Phi) inst).vals.get(i) instanceof Register && Phis.get(((Register) ((Phi) inst).vals.get(i)))!=null){
                            Phi inst2=Phis.get(((Register) ((Phi) inst).vals.get(i)));
                            for(int j=0;j<inst2.vals.size();++j){
                                if(inst2.blocks.get(j)==((Phi) inst).blocks.get(i)){
                                    ((Phi) inst).vals.set(i,inst2.vals.get(j));
                                }
                            }
                        }
                    }
                }
            });
        });


    }
    public void run(){
        root.func.forEach(func->{
            currentFunc=func;
            dfsBlock(func.beginBlock);
            removeDeadBlock();
            init();
            GetPhi();
            currentFunc.vars.forEach(v->{
                v.rename_stack.push(new Register(v.name+"_"+(v.rename_id++)));
                for(int i=0;i<currentFunc.params.size();++i){
                    if(currentFunc.params.get(i)==v){
                        v.rename_stack.push(new Register(v.name+"_"+(v.rename_id++)));
                        currentFunc.params.set(i,v.rename_stack.peek());
                    }
                }
                RenameVar(v,currentFunc.beginBlock);
            });
            CheckPhi();
        });
    }
}
