package Optimizer;

import IR.Block;
import IR.Function;
import IR.Root;
import IR.inst.*;
import IR.operand.*;

import java.util.*;

public class LICM {
    public Root root;
    public Function currentFunc=null;
    public LinkedHashSet<Block> visited;
    public ArrayList<Block> topoBlocks;
    public LinkedHashMap<Block,Integer> dfn;
    public LinkedHashMap<Block,Block> iDom;
    public LinkedHashMap<Block, ArrayList<Block>> domSon;
    public LinkedHashMap<Block, LinkedHashSet<Block>> domSubTree;
    public ArrayList<Block> nodes;
    public LinkedHashMap<Register, Inst> regDef;
    public LinkedHashMap<String, ArrayList<Inst>> globalDef;

    public LICM(Root root){
        this.root=root;
    }

    public void dfsBlock(Block x){
        visited.add(x);
        x.succ.forEach(y->{
            if(!visited.contains(y)) dfsBlock(y);
        });
        topoBlocks.add(0,x);
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

    public void dfsTree(Block x){
        LinkedHashSet<Block> sub=new LinkedHashSet<>();
        domSon.get(x).forEach(y->{
            dfsTree(y);
            sub.add(y);
            sub.addAll(domSubTree.get(y));
        });
        nodes.add(x);
        domSubTree.put(x,sub);
    }

    public void RegDefCollect(){
        regDef=new LinkedHashMap<>();
        globalDef =new LinkedHashMap<>();
        root.globals.forEach(x->globalDef.put(x,new ArrayList<>()));
        currentFunc.blocks.forEach(block->block.insts.forEach(inst->{
            if(inst.reg!=null) regDef.put(inst.reg, inst);
            if(inst instanceof Store && ((Store) inst).addr instanceof Symbol){
                globalDef.get(((Symbol) ((Store) inst).addr).name).add(inst);
            }
        }));
    }

    public void doFunc(){
        for (Block block : nodes){
            if(!(block.getTerminator() instanceof J)) continue;
            Block head=((J) block.getTerminator()).dest;
            ArrayList<Block> tails=new ArrayList<>();
            LinkedHashSet<Block> sub=domSubTree.get(block);

            for(Block b : sub){
                if(b.getTerminator() instanceof J && ((J) b.getTerminator()).dest==head){
                    tails.add(b);
                }
            }
            if(tails.isEmpty()) continue;
            LinkedHashSet<Block> loopBlock=new LinkedHashSet<>();
            loopBlock.add(head);
            loopBlock.addAll(tails);
            Queue<Block> q=new LinkedList<>(tails);
            while(!q.isEmpty()){
                Block x=q.poll();
                x.pred.forEach(y->{
                    if(!loopBlock.contains(y)){
                        loopBlock.add(y);
                        q.add(y);
                    }
                });
            }

            boolean flag=true;
            for(Block b : loopBlock){
                if(!sub.contains(b)) flag=false;
                for(Inst inst : b.insts){
                    if(inst instanceof Call){
                        flag=false;
                        break;
                    }
                }
            }
            if(!flag) continue;

            for(Block b : loopBlock){
                for(int i=0;i<b.insts.size();++i){
                    Inst inst=b.insts.get(i);
                    if(inst instanceof Calc || inst instanceof Cmp){
                        flag=true;
                        for(Operand x : inst.getUse()){
                            if(x instanceof Register && regDef.containsKey(x) && loopBlock.contains(regDef.get(x).block)){
                                flag=false;
                                break;
                            }
                        }
                        if(flag){
                            inst.block=block;
                            block.insts.add(block.insts.size()-1,inst);
                            b.insts.remove(i);
                            --i;
                        }
                    }else if(inst instanceof Load && ((Load) inst).addr instanceof Symbol){
                        flag=true;
                        for(Inst def : globalDef.get(((Symbol) ((Load) inst).addr).name)){
                            if(loopBlock.contains(def.block)){
                                flag=false;
                                break;
                            }
                        }
                        if(flag){
                            inst.block=block;
                            block.insts.add(block.insts.size()-1,inst);
                            b.insts.remove(i);
                            --i;
                        }
                    }
                }
            }
        }
    }
    public void run(){
        root.func.forEach(func->{
            currentFunc=func;
            visited=new LinkedHashSet<>();
            topoBlocks=new ArrayList<>();
            dfsBlock(currentFunc.beginBlock);
            dfn=new LinkedHashMap<>();
            iDom=new LinkedHashMap<>();
            domSon=new LinkedHashMap<>();
            DomTree();
            nodes=new ArrayList<>();
            domSubTree=new LinkedHashMap<>();
            dfsTree(currentFunc.beginBlock);
            RegDefCollect();
            doFunc();
        });
    }


}
