package BackEnd;

import ASM.Block;
import ASM.Function;
import ASM.Root;
import ASM.inst.*;
import ASM.operand.*;

import java.util.*;

public class RegAllocator{
    public Root root;
    public Function currentFunction=null;

    public RegAllocator(Root root){
        this.root=root;
    }

    public LinkedHashMap<Block, LinkedHashSet<Register>> buses=new LinkedHashMap<>(),bdefs=new LinkedHashMap<>(),blivein=new LinkedHashMap<>(),bliveout=new LinkedHashMap<>();

    public void LivenessAnalysis(){
        buses=new LinkedHashMap<>();
        bdefs=new LinkedHashMap<>();
        blivein=new LinkedHashMap<>();
        bliveout=new LinkedHashMap<>();
        for (Block block : currentFunction.blocks) {
            LinkedHashSet<Register> uses = new LinkedHashSet<>(), defs = new LinkedHashSet<>();
            for (Inst x : block.insts) {
                LinkedHashSet<Register> t = x.getUse();
                t.removeAll(defs);
                uses.addAll(t);
                defs.addAll(x.getDef());
            }
            buses.put(block, uses);
            bdefs.put(block, defs);
            blivein.put(block, new LinkedHashSet<>());
            bliveout.put(block, new LinkedHashSet<>());
        }
        LinkedHashSet<Block> inq=new LinkedHashSet<>();
        Queue<Block> q=new LinkedList<>();
        for (Block b : currentFunction.blocks) {
            if (b.succ.isEmpty()) {
                inq.add(b);
                q.add(b);
            }
        }
        while(!q.isEmpty()){
            Block x=q.poll();
            inq.remove(x);
            LinkedHashSet<Register> liveout=new LinkedHashSet<>();
            for (Block block : x.succ) {
                liveout.addAll(blivein.get(block));
            }
            bliveout.replace(x,liveout);
            LinkedHashSet<Register> livein=new LinkedHashSet<>(liveout);
            livein.removeAll(bdefs.get(x));
            livein.addAll(buses.get(x));
            if(!livein.equals(blivein.get(x))){
                blivein.replace(x,livein);
                for (Block a : x.pred) {
                    if (!inq.contains(a)) {
                        inq.add(a);
                        q.add(a);
                    }
                }
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
    public LinkedHashMap<Register,LinkedHashSet<Mv>> moveList=new LinkedHashMap<>();
    public LinkedHashMap<Register,LinkedHashSet<Register>> adjList=new LinkedHashMap<>();
    public LinkedHashMap<Register,Double>weight=new LinkedHashMap<>();
    public LinkedHashMap<Register,Integer>degree=new LinkedHashMap<>();
    public LinkedHashMap<Register,Register> alias=new LinkedHashMap<>();
    public LinkedHashMap<Register,Integer> offset=new LinkedHashMap<>();
    public LinkedHashSet<edge> adjSet=new LinkedHashSet<>();

    public int K;
    public LinkedHashSet<Mv> workListMoves,activeMoves,coalescedMoves,constrainedMoves,frozenMoves;
    public LinkedHashSet<Register> preColored,initial,simplifyWorkList,freezeWorkList,spillWorkList,spilledNodes, coalescedNodes,coloredNodes,canNotSpillNodes;
    public Stack<Register> selectStack;
    public void Init(){
        K=root.getColors().size();
        workListMoves=new LinkedHashSet<>();
        activeMoves=new LinkedHashSet<>();
        coalescedMoves=new LinkedHashSet<>();
        constrainedMoves=new LinkedHashSet<>();
        frozenMoves=new LinkedHashSet<>();
        preColored=new LinkedHashSet<>(root.getPRegs());
        initial=new LinkedHashSet<>();
        simplifyWorkList=new LinkedHashSet<>();
        freezeWorkList=new LinkedHashSet<>();
        spillWorkList=new LinkedHashSet<>();
        spilledNodes=new LinkedHashSet<>();
        coalescedNodes =new LinkedHashSet<>();
        coloredNodes=new LinkedHashSet<>();
        canNotSpillNodes=new LinkedHashSet<>();
        moveList=new LinkedHashMap<>();
        adjList=new LinkedHashMap<>();
        weight=new LinkedHashMap<>();
        degree=new LinkedHashMap<>();
        alias=new LinkedHashMap<>();
        offset=new LinkedHashMap<>();
        adjSet=new LinkedHashSet<>();
        selectStack=new Stack<>();
        for (Block block1 : currentFunction.blocks) {
            for (Inst inst : block1.insts) {
                initial.addAll(inst.getUse());
                initial.addAll(inst.getDef());
            }
        }
        for(Register x:initial){
            moveList.put(x,new LinkedHashSet<>());
            adjList.put(x,new LinkedHashSet<>());
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
        for (Block block : currentFunction.blocks) {
            for (Inst inst : block.insts) {
                for (Register register : inst.getUse()) {
                    double t = weight.get(register) + Math.pow(10.0, block.loopDepth);
                    weight.replace(register, t);
                }
                for (Register x : inst.getDef()) {
                    double t = weight.get(x) + Math.pow(10.0, block.loopDepth);
                    weight.replace(x, t);
                }
            }
        }
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
        for (Block b : currentFunction.blocks) {
            LinkedHashSet<Register> live = new LinkedHashSet<>(bliveout.get(b));
            for (int i = b.insts.size() - 1; i >= 0; --i) {
                Inst inst = b.insts.get(i);
                if (inst instanceof Mv) {
                    live.removeAll(inst.getUse());
                    LinkedHashSet<Register> t = inst.getDef();
                    t.addAll(inst.getUse());
                    for (Register x : t) {
                        moveList.get(x).add((Mv) inst);
                    }
                    workListMoves.add((Mv) inst);
                }
                if (inst instanceof Sw) AddEdge(((Sw) inst).rt, ((Sw) inst).rd);
                live.addAll(inst.getDef());
                for (Register a : inst.getDef()) {
                    for (Register c : live) {
                        AddEdge(a, c);
                    }
                }
                live.removeAll(inst.getDef());
                live.addAll(inst.getUse());
            }
        }
    }

    public LinkedHashSet<Mv> NodeMoves(Register x){
        LinkedHashSet<Mv> res=new LinkedHashSet<>(activeMoves);
        res.addAll(workListMoves);
        res.retainAll(moveList.get(x));
        return res;
    }
    public boolean MoveRelated(Register x){
        return !NodeMoves(x).isEmpty();
    }
    public LinkedHashSet<Register> Adjacent(Register x){
        LinkedHashSet<Register> res=new LinkedHashSet<>(adjList.get(x));
        LinkedHashSet<Register> tmp=new LinkedHashSet<>(selectStack);
        tmp.addAll(coalescedNodes);
        res.removeAll(tmp);
        return res;
    }
    public void EnableMoves(LinkedHashSet<Register> nodes){
        for (Register n : nodes) {
            for (Mv m : NodeMoves(n)) {
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m);
                    workListMoves.add(m);
                }
            }
        }
    }
    public void DecrementDegree(Register x){
        int d=degree.get(x);
        degree.replace(x,d-1);
        if(d==K){
            LinkedHashSet<Register> t = Adjacent(x);
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
        nodes.removeAll(y);
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
        EnableMoves(new LinkedHashSet<>(Collections.singletonList(y)));
        for (Register t : Adjacent(y)) {
            AddEdge(t, x);
            DecrementDegree(t);
        }
        if(degree.get(x)>=K&&freezeWorkList.contains(x)){
            freezeWorkList.remove(x);
            spillWorkList.add(x);
        }
    }
    public void FreezeMoves(Register x){
        for (Mv inst : NodeMoves(x)) {
            Register u = inst.rd, v = inst.rs, y;
            if (GetAlias(x) == GetAlias(v)) y = GetAlias(u);
            else y = GetAlias(v);
            activeMoves.remove(inst);
            frozenMoves.add(inst);
            if (NodeMoves(y).isEmpty() && degree.get(y) < K) {
                freezeWorkList.remove(y);
                simplifyWorkList.add(y);
            }
        }
    }
    public void MakeWorkList(){
        for (Register x : initial) {
            if (degree.get(x) >= K) spillWorkList.add(x);
            else if (MoveRelated(x)) freezeWorkList.add(x);
            else simplifyWorkList.add(x);
        }
    }

    public void Simplify(){
        Register x=simplifyWorkList.iterator().next();
        simplifyWorkList.remove(x);
        selectStack.push(x);
        for (Register register : Adjacent(x)) {
            DecrementDegree(register);
        }
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
            LinkedHashSet<Register> colored = new LinkedHashSet<>(preColored);
            colored.addAll(coloredNodes);
            for (Register y : adjList.get(x)) {
                if (colored.contains(GetAlias(y))) okColors.remove(GetAlias(y).color);
            }
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
        for (Register spilledNode : spilledNodes) {
            offset.put(spilledNode, spOffset);
            spOffset += 4;
        }
        for(Block block : currentFunction.blocks){
            //System.err.println(block);
            for(int i=0;i<block.insts.size();++i){
                Inst inst=block.insts.get(i);
                if(inst instanceof Mv && spilledNodes.contains(((Mv) inst).rd) && spilledNodes.contains(((Mv) inst).rs)){
                    VReg tmp=new VReg("tmp");
                    block.insts.set(i,new Load(tmp,root.getPReg(2),new Imm(offset.get(((Mv)inst).rs))));
                    block.insts.add(i+1,new Store(tmp,root.getPReg(2),new Imm(offset.get(((Mv)inst).rd))));
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
    public void RemoveUselessJ(){
        for(int i=0;i<currentFunction.blocks.size()-1;++i){
            Block block=currentFunction.blocks.get(i),dest;
            if(block.insts.get(block.insts.size()-1) instanceof J){
                dest=((J) block.insts.get(block.insts.size()-1)).dest;
            }else continue;
            if(dest==currentFunction.blocks.get(i+1)) block.insts.remove(block.insts.size()-1);
        }
    }
    public void RemoveUselessBlock(){
        for (int i=0;i<currentFunction.blocks.size();++i){
            Block block=currentFunction.blocks.get(i);
            if(i!=0&&block.pred.isEmpty()){
                for (Block b : block.succ) {
                    b.pred.remove(block);
                }
                currentFunction.blocks.remove(i);
                --i;
                continue;
            }
            if(block.insts.isEmpty()){
                Block dest=block.succ.get(0);
                dest.pred.removeAll(block.pred);
                dest.pred.addAll(block.pred);
                for(Block b : currentFunction.blocks){
                    for(int ii=0;ii<b.succ.size();++ii) if (b.succ.get(ii)==block) b.succ.set(ii,dest);
                    for(Inst inst : b.insts){
                        if(inst instanceof J && ((J)inst).dest==block) ((J)inst).dest=dest;
                        if(inst instanceof Branch && ((Branch)inst).dest==block) ((Branch)inst).dest=dest;
                    }
                }
                currentFunction.blocks.remove(i);
                --i;
                continue;
            }
            if (i!=0&&block.insts.get(0) instanceof J) {
                Block dest = ((J) block.insts.get(0)).dest;
                for (Block b1 : block.succ) {
                    b1.pred.removeAll(Collections.singletonList(block));
                }
                for (Block b1 : block.pred) {
                    b1.succ.remove(block);
                    b1.succ.add(dest);
                    dest.pred.add(b1);
                }
                for (Block b : currentFunction.blocks) {
                    for (Inst inst : b.insts) {
                        if (inst instanceof J && ((J) inst).dest == block)
                            ((J) inst).dest = dest;
                        if (inst instanceof Branch && ((Branch) inst).dest == block)
                            ((Branch) inst).dest = dest;
                    }
                }
                currentFunction.blocks.remove(i);
                --i;
                continue;
            }if(i!=0&&block.pred.size()==1&&block.pred.get(0).getTerminator() instanceof J && ((J)block.pred.get(0).getTerminator()).dest==block){
                Block b=block.pred.get(0);
                b.removeTerminator();
                b.insts.addAll(block.insts);
                for (Block succ : block.succ) {
                    succ.pred.remove(block);
                    succ.pred.add(b);
                    b.succ.remove(succ);
                    b.succ.add(succ);
                }
                currentFunction.blocks.remove(i);
                --i;
            }
        }
    }
    public void RunFunc(){
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
            RunFunc();
        }else{
            AddSp();
            RemoveDeadMv();
            RemoveUselessBlock();
            RemoveUselessJ();
        }
        currentFunction=null;
    }

    public void Run(){
        for (Function func : root.func) {
            spOffset = 0;
            currentFunction = func;
            RunFunc();
        }
    }
}