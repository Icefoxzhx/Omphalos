package BackEnd;

import ASM.Block;
import ASM.Function;
import ASM.Root;
import ASM.inst.*;
import ASM.operand.Imm;
import ASM.operand.PReg;
import ASM.operand.Register;
import ASM.operand.VReg;

import java.util.*;

public class RegAllocator{
    public Root root;
    public Function currentFunction=null;

    public RegAllocator(Root root){
        this.root=root;
    }

    public HashMap<Block, HashSet<Register>> buses=new HashMap<>(),bdefs=new HashMap<>(),blivein=new HashMap<>(),bliveout=new HashMap<>();

    public void LivenessAnalysis(){
        buses=new HashMap<>();
        bdefs=new HashMap<>();
        blivein=new HashMap<>();
        bliveout=new HashMap<>();
        currentFunction.blocks.forEach(b->{
            HashSet<Register> uses=new HashSet<>(),defs=new HashSet<>();
            b.insts.forEach(x->{
                HashSet<Register> t=x.getUse();
                t.removeAll(defs);
                uses.addAll(t);
                defs.addAll(x.getDef());
            });
            buses.put(b,uses);
            bdefs.put(b,defs);
            blivein.put(b,new HashSet<>());
            bliveout.put(b,new HashSet<>());
        });
        HashSet<Block> inq=new HashSet<>();
        Queue<Block> q=new LinkedList<>();
        currentFunction.blocks.forEach(b->{
            if(b.succ.isEmpty()){
                inq.add(b);
                q.add(b);
            }
        });
        while(!q.isEmpty()){
            Block x=q.poll();
            inq.remove(x);
            HashSet<Register> liveout=new HashSet<>();
            x.succ.forEach(a->liveout.addAll(blivein.get(a)));
            bliveout.replace(x,liveout);
            HashSet<Register> livein=new HashSet<>(liveout);
            livein.removeAll(bdefs.get(x));
            livein.addAll(buses.get(x));
            if(!livein.equals(blivein.get(x))){
                blivein.replace(x,livein);
                x.pred.forEach(a->{
                    if(!inq.contains(a)){
                        inq.add(a);
                        q.add(a);
                    }
                });
            }
        }
    }

    public static class edge{
        Register x,y;

        public edge(Register x, Register y){
            this.x=x;
            this.y=y;
        }

        @Override
        public int hashCode(){
            return x.hashCode()^y.hashCode();
        }

        @Override
        public boolean equals(Object obj){
            return (obj instanceof edge && ((edge)obj).x==x&&((edge)obj).y==y);
        }
    }

    public int spOffset=0;
    public HashMap<Register,HashSet<Mv>> moveList=new HashMap<>();
    public HashMap<Register,HashSet<Register>> adjList=new HashMap<>();
    public HashMap<Register,Double>weight=new HashMap<>();
    public HashMap<Register,Integer>degree=new HashMap<>();
    public HashMap<Register,Register> alias=new HashMap<>();
    public HashMap<Register,Integer> offset=new HashMap<>();
    public HashSet<edge> adjSet=new HashSet<>();

    public int K;
    public HashSet<Mv> workListMoves,activeMoves,coalescedMoves,constrainedMoves,frozenMoves;
    public HashSet<Register> preColored,initial,simplifyWorkList,freezeWorkList,spillWorkList,spilledNodes, coalescedNodes,coloredNodes,canNotSpillNodes;
    public Stack<Register> selectStack;
    public void Init(){
        K=root.getColors().size();
        workListMoves=new HashSet<>();
        activeMoves=new HashSet<>();
        coalescedMoves=new HashSet<>();
        constrainedMoves=new HashSet<>();
        frozenMoves=new HashSet<>();
        preColored=new HashSet<>(root.getPRegs());
        initial=new HashSet<>();
        simplifyWorkList=new HashSet<>();
        freezeWorkList=new HashSet<>();
        spillWorkList=new HashSet<>();
        spilledNodes=new HashSet<>();
        coalescedNodes =new HashSet<>();
        coloredNodes=new HashSet<>();
        canNotSpillNodes=new HashSet<>();
        moveList=new HashMap<>();
        adjList=new HashMap<>();
        weight=new HashMap<>();
        degree=new HashMap<>();
        alias=new HashMap<>();
        offset=new HashMap<>();
        adjSet=new HashSet<>();
        selectStack=new Stack<>();
        currentFunction.blocks.forEach(block->{
            block.insts.forEach(inst -> {
                initial.addAll(inst.getUse());
                initial.addAll(inst.getDef());
            });
        });
        for(Register x:initial){
            moveList.put(x,new HashSet<>());
            adjList.put(x,new HashSet<>());
            weight.put(x,0.0);
            degree.put(x,0);
            alias.put(x,x);
            x.color=null;
        }
        initial.removeAll(preColored);
        for(Register x:preColored){
            degree.put(x,20010122);
            x.color=(PReg) x;
        }
        currentFunction.blocks.forEach(block->{
            block.insts.forEach(inst->{
                inst.getUse().forEach(x->{
                   double t=weight.get(x)+Math.pow(10.0,block.loopDepth);
                   weight.replace(x,t);
                });
                inst.getDef().forEach(x->{
                    double t=weight.get(x)+Math.pow(10.0,block.loopDepth);
                    weight.replace(x,t);
                });
            });
        });
    }

    public void AddEdge(Register x, Register y){
        if(x != y && !adjSet.contains(new edge(x,y))){
            adjSet.add(new edge(x,y));
            adjSet.add(new edge(y,x));
            if(!preColored.contains(x)){
                adjList.get(x).add(y);
                int t=degree.get(x);
                degree.replace(x,t+1);
            }
            if(!preColored.contains(y)){
                adjList.get(y).add(x);
                int t=degree.get(y);
                degree.replace(y,t+1);
            }
        }
    }
    public void Build(){
        currentFunction.blocks.forEach(b->{
            HashSet<Register> live=new HashSet<>(bliveout.get(b));
            for(int i=b.insts.size()-1;i>=0;--i){
                Inst inst=b.insts.get(i);
                if(inst instanceof Mv){
                    live.removeAll(inst.getUse());
                    HashSet<Register> t=inst.getDef();
                    t.addAll(inst.getUse());
                    t.forEach(x->moveList.get(x).add((Mv)inst));
                    workListMoves.add((Mv) inst);
                }
                live.addAll(inst.getDef());
                inst.getDef().forEach(a->live.forEach(c-> AddEdge(a,c)));
                live.removeAll(inst.getDef());
                live.addAll(inst.getUse());
            }
        });
    }

    public HashSet<Mv> NodeMoves(Register x){
        HashSet<Mv> res=new HashSet<>(activeMoves);
        res.addAll(workListMoves);
        res.retainAll(moveList.get(x));
        return res;
    }
    public boolean MoveRelated(Register x){
        return !NodeMoves(x).isEmpty();
    }
    public HashSet<Register> Adjacent(Register x){
        HashSet<Register> res=new HashSet<>(adjList.get(x));
        HashSet<Register> tmp=new HashSet<>(selectStack);
        tmp.addAll(coalescedNodes);
        res.removeAll(tmp);
        return res;
    }
    public void EnableMoves(HashSet<Register> nodes){
        nodes.forEach(n-> NodeMoves(n).forEach(m->{
            if(activeMoves.contains(m)){
                activeMoves.remove(m);
                workListMoves.add(m);
            }
        }));
    }
    public void DecrementDegree(Register x){
        int d=degree.get(x);
        degree.replace(x,d-1);
        if(d==K){
            HashSet<Register> t = Adjacent(x);
            t.add(x);
            EnableMoves(t);
            spillWorkList.remove(x);
            if(MoveRelated(x)) freezeWorkList.add(x);
            else simplifyWorkList.add(x);
        }
    }
    public void AddWorkList(Register x){
        if(!preColored.contains(x) && !MoveRelated(x) && degree.get(x) < K){
            freezeWorkList.remove(x);
            simplifyWorkList.add(x);
        }
    }
    public Register GetAlias(Register x){
        if(coalescedNodes.contains(x)) return GetAlias(alias.get(x));
        return x;
    }
    public boolean OK(Register x, Register y){
        return degree.get(x)<K || preColored.contains(x) || adjSet.contains(new edge(x,y));
    }
    public boolean OK(ArrayList<Register> t, Register y){
        for(Register x : t){
            if(!OK(x,y)) return false;
        }
        return true;
    }
    public boolean Conservative(ArrayList<Register> nodes, ArrayList<Register> y){
        nodes.addAll(y);
        int k=0;
        for(Register node : nodes) if(degree.get(node) >= K) ++k;
        return (k<K);
    }
    public void Combine(Register x, Register y){
        if(freezeWorkList.contains(y)) freezeWorkList.remove(y);
        else spillWorkList.remove(y);
        coalescedNodes.add(y);
        alias.put(y,x);
        moveList.get(x).addAll(moveList.get(y));
        EnableMoves(new HashSet<>(Collections.singletonList(y)));
        Adjacent(y).forEach(t->{
            AddEdge(t,x);
            DecrementDegree(t);
        });
        if(degree.get(x)>=K&&freezeWorkList.contains(x)){
            freezeWorkList.remove(x);
            spillWorkList.add(x);
        }
    }
    public void FreezeMoves(Register x){
        NodeMoves(x).forEach(inst->{
            Register u = inst.rd, v = inst.rs, y;
            if(GetAlias(x)== GetAlias(v)) y= GetAlias(u);
            else y= GetAlias(v);
            activeMoves.remove(inst);
            frozenMoves.add(inst);
            if(NodeMoves(y).isEmpty()&&degree.get(y)<K){
                freezeWorkList.remove(y);
                simplifyWorkList.add(y);
            }
        });
    }
    public void MakeWorkList(){
        initial.forEach(x->{
            if(degree.get(x)>=K) spillWorkList.add(x);
            else if (MoveRelated(x)) freezeWorkList.add(x);
            else simplifyWorkList.add(x);
        });
    }

    public void Simplify(){
        Register x=simplifyWorkList.iterator().next();
        simplifyWorkList.remove(x);
        selectStack.push(x);
        Adjacent(x).forEach(this::DecrementDegree);
    }

    public void Coalesce(){
        Mv m=workListMoves.iterator().next();
        workListMoves.remove(m);
        Register x= GetAlias(m.rd),y= GetAlias(m.rs);
        if(preColored.contains(y)){
            Register t=x;
            x=y;
            y=t;
        }
        if(x==y){
            coalescedMoves.add(m);
            AddWorkList(x);
        }else if(preColored.contains(y)|| adjSet.contains(new edge(x,y))){
            constrainedMoves.add(m);
            AddWorkList(x);
            AddWorkList(y);
        }else if ((preColored.contains(x) && OK(new ArrayList<>(Adjacent(y)),x)) ||
        (!preColored.contains(x) && Conservative(new ArrayList<>(Adjacent(x)),new ArrayList<>(Adjacent(y))))){
            coalescedMoves.add(m);
            Combine(x,y);
            AddWorkList(x);
        }else{
            activeMoves.add(m);
        }
    }

    public void Freeze(){
        Register x = freezeWorkList.iterator().next();
        freezeWorkList.remove(x);
        simplifyWorkList.add(x);
        FreezeMoves(x);
    }
    public void SelectSpill(){
        Register res=null;
        double min=Double.POSITIVE_INFINITY;
        for(Register x : spillWorkList){
            if(canNotSpillNodes.contains(x) || preColored.contains(x)) continue;
            double t=weight.get(x)/degree.get(x);
            if(t<min){
                res=x;min=t;
            }
        }
        spillWorkList.remove(res);
        simplifyWorkList.add(res);
        FreezeMoves(res);
    }
    public void AssignColors(){
        while(!selectStack.isEmpty()){
            Register x=selectStack.pop();
            ArrayList<PReg> okColors = new ArrayList<>(root.getColors());
            HashSet<Register> colored = new HashSet<>(preColored);
            colored.addAll(coloredNodes);
            adjList.get(x).forEach(y->{
                if(colored.contains(GetAlias(y))) okColors.remove(GetAlias(y).color);
            });
            if(okColors.isEmpty()) spilledNodes.add(x);
            else{
                coloredNodes.add(x);
                x.color=okColors.get(0);
            }
        }
        for(Register x : coalescedNodes){
            x.color= GetAlias(x).color;
        }
    }

    public void RewriteProgram(){
        spilledNodes.forEach(x->{
            offset.put(x,spOffset);
            spOffset+=4;
        });
        for(Block block : currentFunction.blocks){
            for(int i=0;i<block.insts.size();++i){
                Inst inst=block.insts.get(i);
                if(inst instanceof Mv && spilledNodes.contains(((Mv) inst).rd) && spilledNodes.contains(((Mv) inst).rs)){
                    VReg tmp=new VReg("tmp");
                    block.insts.set(i,new Load(tmp,root.getPReg(2),new Imm(offset.get(((Mv)inst).rs))));
                    block.insts.set(i+1,new Store(tmp,root.getPReg(2),new Imm(offset.get(((Mv)inst).rd))));
                    ++i;
                    continue;
                }
                for (Register x : inst.getUse()){
                    if(!spilledNodes.contains(x)) continue;
                    if(inst instanceof Mv)
                        block.insts.set(i, new Load(((Mv)inst).rd,root.getPReg(2),new Imm(offset.get(x))));
                    else{
                        VReg tmp=new VReg("tmp");
                        canNotSpillNodes.add(tmp);
                        inst.replaceUse(x,tmp);
                        block.insts.add(i, new Load(tmp,root.getPReg(2),new Imm(offset.get(x))));
                        ++i;
                    }
                }
                for (Register x : inst.getDef()){
                    if(!spilledNodes.contains(x)) continue;
                    if(inst instanceof Mv)
                        block.insts.set(i, new Store(((Mv)inst).rs,root.getPReg(2),new Imm(offset.get(x))));
                    else{
                        VReg tmp=new VReg("tmp");
                        canNotSpillNodes.add(tmp);
                        inst.replaceDef(x,tmp);
                        block.insts.add(i+1, new Store(tmp,root.getPReg(2),new Imm(offset.get(x))));
                        ++i;
                    }
                }
            }
        }
    }

    public void AddSp(){
        int realOffset=spOffset+currentFunction.StackSpace;
        if(realOffset>0){
            currentFunction.beginBlock.insts.add(0,new Calc("addi",root.getPReg(2), root.getPReg(2), new Imm(-realOffset)));
            currentFunction.endBlock.insts.add(currentFunction.endBlock.insts.size()-1,new Calc("addi", root.getPReg(2), root.getPReg(2),new Imm(realOffset) ));
        }
        for(Inst inst : currentFunction.beginBlock.insts){
            if(inst.isOriginalOffset){
                ((Load)inst).offset.val+=spOffset;
            }
        }
    }

    public void RemoveDeadMv(){
        for(Block block : currentFunction.blocks){
            for(int i=0;i<block.insts.size();++i){
                Inst inst=block.insts.get(i);
                if(inst instanceof Mv && ((Mv)inst).rd.color==((Mv)inst).rs.color){
                    block.insts.remove(i);
                    --i;
                }
            }
        }
    }

    public void RemoveUselessBlock(){
        for (int i=0;i<currentFunction.blocks.size();++i){
            Block block=currentFunction.blocks.get(i);
            if(block.insts.isEmpty()){
                Block dest=block.succ.get(0);
                dest.pred.retainAll(block.pred);
                for(Block b : currentFunction.blocks){
                    for(int ii=0;ii<b.succ.size();++ii) if (b.succ.get(ii)==block) b.succ.set(ii,dest);
                    for(Inst inst : b.insts){
                        if(inst instanceof J && ((J)inst).dest==block) ((J)inst).dest=dest;
                        if(inst instanceof Branch && ((Branch)inst).dest==block) ((Branch)inst).dest=dest;
                    }
                }
                currentFunction.blocks.remove(i);
                --i;
            }
        }
    }
    public void RunFunc(Function func){
        currentFunction=func;
        Init();
        LivenessAnalysis();
        Build();
        MakeWorkList();
        while(!simplifyWorkList.isEmpty() || !workListMoves.isEmpty() || !freezeWorkList.isEmpty() || !spillWorkList.isEmpty()){
            if(!simplifyWorkList.isEmpty()) Simplify();
            else if (!workListMoves.isEmpty()) Coalesce();
            else if (!freezeWorkList.isEmpty()) Freeze();
            else SelectSpill();
        }
        AssignColors();
        if(!spilledNodes.isEmpty()){
            RewriteProgram();
            RunFunc(func);
        }else{
            AddSp();
            RemoveDeadMv();
            RemoveUselessBlock();
        }
        currentFunction=null;
    }

    public void Run(){
        root.func.forEach(func->{
            spOffset=0;
            RunFunc(func);
        });
    }
}