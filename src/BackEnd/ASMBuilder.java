package BackEnd;

import AST.*;
import ASM.*;
import ASM.inst.*;
import ASM.operand.*;
import Util.symbol.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ASMBuilder implements ASTVisitor {
    public Type currentReturnType;
    private Block currentloopend,currentloopcond;
    private int Label=0;
    private int loopDepth=0;
    private boolean MainInited=false,returnDone=false;
    private ArrayList<SingleVarDefStmt> globals=new ArrayList<>();
    private Function currentFunc;
    private Block currentBlock;
    private Register thisptr;
    private ASM.Root root;
    private HashMap<String, FuncSymbol> funcMap=new HashMap<>();
    public ASMBuilder(ASM.Root root){
        this.root = root;
        {
            FuncSymbol func=new FuncSymbol("__Om_builtin_str_add");
            func.returnType=new PrimitiveType("string");
            func.abs_name="__Om_builtin_str_add";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            funcMap.put("__Om_builtin_str_add",func);
        }
        {
            FuncSymbol func=new FuncSymbol("__Om_builtin_str_lt");
            func.returnType=new PrimitiveType("bool");
            func.abs_name="__Om_builtin_str_lt";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            funcMap.put("__Om_builtin_str_lt",func);
        }
        {
            FuncSymbol func=new FuncSymbol("__Om_builtin_str_gt");
            func.returnType=new PrimitiveType("bool");
            func.abs_name="__Om_builtin_str_gt";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            funcMap.put("__Om_builtin_str_gt",func);
        }
        {
            FuncSymbol func=new FuncSymbol("__Om_builtin_str_le");
            func.returnType=new PrimitiveType("bool");
            func.abs_name="__Om_builtin_str_le";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            funcMap.put("__Om_builtin_str_le",func);
        }
        {
            FuncSymbol func=new FuncSymbol("__Om_builtin_str_ge");
            func.returnType=new PrimitiveType("bool");
            func.abs_name="__Om_builtin_str_ge";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            funcMap.put("__Om_builtin_str_ge",func);
        }
        {
            FuncSymbol func=new FuncSymbol("__Om_builtin_str_eq");
            func.returnType=new PrimitiveType("bool");
            func.abs_name="__Om_builtin_str_eq";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            funcMap.put("__Om_builtin_str_eq",func);
        }
        {
            FuncSymbol func=new FuncSymbol("__Om_builtin_str_ne");
            func.returnType=new PrimitiveType("bool");
            func.abs_name="__Om_builtin_str_ne";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            funcMap.put("__Om_builtin_str_ne",func);
        }
        {
            FuncSymbol func=new FuncSymbol("__Om_builtin_malloc");
            func.returnType=new PrimitiveType("int");
            func.abs_name="__Om_builtin_malloc";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("int")));
            funcMap.put("__Om_builtin_malloc",func);
        }
    }

    public Register getReg(Operand x){
        VReg tmp=new VReg("tmp");
        if(x instanceof Symbol){
            currentBlock.insts.add(new Li(tmp,x,"lw"));
        }else if(x instanceof Imm){
            currentBlock.insts.add(new Li(tmp,x,"li"));
        }else if(x instanceof Register && ((Register)x).isptr){
            currentBlock.insts.add(new Load(tmp,(Register) x,new Imm(0)));
        }else if(x instanceof Register) return (Register) x;
        return tmp;
    }

    public void assign(Operand rd, Register rs){
        VReg tmp=new VReg("tmp");
        if(rd instanceof Symbol){
            currentBlock.insts.add(new Sw(rs,tmp,(Symbol) rd));
        }else if (rd instanceof Register && ((Register) rd).isptr){
            currentBlock.insts.add(new Store(rs,(Register) rd,new Imm(0)));
        }else if(rd instanceof Register) currentBlock.insts.add(new Mv((Register) rd,rs));
    }
    @Override
    public void visit(ProgramNode it) {
        it.body.forEach(x->x.accept(this));
    }

    @Override
    public void visit(SingleVarDefStmt it) {
        if (it.var.isGlobal) {
            if(!MainInited){
                it.var.operand = new Symbol(it.name);
                root.globals.add(it.name);
                globals.add(it);
            }else {
                if (it.expr != null) {
                    it.expr.accept(this);
                    assign(it.var.operand, getReg(it.expr.operand));
                }
            }
        } else {
            it.var.operand = new VReg(it.var.name);
            if (it.expr != null) {
                it.expr.accept(this);
                assign(it.var.operand, getReg(it.expr.operand));
            }
        }
    }

    @Override
    public void visit(TypeNode it) {

    }

    @Override
    public void visit(VarDefStmt it) {
        it.varList.forEach(x->x.accept(this));
    }

    @Override
    public void visit(BlockStmt it) {
        for(StmtNode x: it.stmtList){
            x.accept(this);
            if(currentBlock.terminated) break;
        }
    }

    @Override
    public void visit(FuncDefNode it) {
        currentFunc=it.func.func;
        currentFunc.StackSpace=Integer.max(0,it.paramList.size()+(it.func.inClass?1:0)-8)*4;
        currentBlock=currentFunc.beginBlock=new Block(loopDepth,currentFunc.name+"."+currentFunc.blocks.size());
        currentFunc.blocks.add(currentBlock);
        root.func.add(currentFunc);

        for(int i=0;i<root.getCalleeSave().size();++i){
            VReg tmp=new VReg("tmp");
            currentFunc.calleeSaveReg.add(tmp);
            currentBlock.insts.add(new Mv(tmp,root.getCalleeSave().get(i)));
        }
        VReg tmp=new VReg("tmp");
        currentFunc.raSaveReg=tmp;
        currentBlock.insts.add(new Mv(tmp,root.getPReg(1)));

        if(it.returnNum>1){
            currentFunc.endBlock=new Block(loopDepth,"Returnof" + currentFunc.name);
        }

        if(it.func.inClass==false){
            for(int i=0;i<it.paramList.size();++i){
                tmp=new VReg(it.paramList.get(i).name);
                it.paramList.get(i).var.operand=tmp;
                if(i<8) currentBlock.insts.add(new Mv(tmp,root.getPReg(10+i)));
                else{
                    Inst xx=new Load(tmp,root.getPReg(2),new Imm((7-i)*4));
                    xx.isOriginalOffset=true;
                    currentBlock.insts.add(xx);
                }
            }
            if(it.name.equals("main")){
                MainInited=true;
                globals.forEach(x->x.accept(this));
            }
            it.block.accept(this);

            if(it.name.equals("main") && it.returnNum==0 ){
                currentBlock.insts.add(new Mv(root.getPReg(10),root.getPReg(0)));
                it.returnNum=1;
            }
        }else{
            thisptr=new VReg("this");
            currentBlock.insts.add(new Mv(thisptr,root.getPReg(10)));
            for(int i=0;i<it.paramList.size();++i){
                tmp=new VReg(it.paramList.get(i).name);
                it.paramList.get(i).var.operand=tmp;
                if(i+1<8) currentBlock.insts.add(new Mv(tmp,root.getPReg(10+i+1)));
                else{
                    Inst xx=new Load(tmp,root.getPReg(2),new Imm((7-(i+1))*4));
                    xx.isOriginalOffset=true;
                    currentBlock.insts.add(xx);
                }
            }
            it.block.accept(this);
        }
        if(it.returnNum<2){
            currentFunc.endBlock=currentBlock;
        }else{
            currentBlock.succ.add(currentFunc.endBlock);
            currentFunc.endBlock.pred.add(currentBlock);
            currentBlock=currentFunc.endBlock;
            currentFunc.blocks.add(currentBlock);
        }
        for(int i=0;i<root.getCalleeSave().size();++i){
            currentBlock.insts.add(new Mv(root.getCalleeSave().get(i), currentFunc.calleeSaveReg.get(i)));
        }
        currentBlock.insts.add(new Mv(root.getPReg(1),currentFunc.raSaveReg));
        currentBlock.insts.add(new Ret(root));
    }

    @Override
    public void visit(ClassDefNode it) {
        // no default assign...?
        it.funcList.forEach(x->x.accept(this));
        if(it.constructor!=null) it.constructor.accept(this);
    }

    @Override
    public void visit(BreakStmt it) {
        currentBlock.insts.add(new J(currentloopend));
        currentBlock.succ.add(currentloopend);
        currentloopend.pred.add(currentBlock);
        currentBlock.terminated=true;
    }

    @Override
    public void visit(ContinueStmt it) {
        currentBlock.insts.add(new J(currentloopcond));
        currentBlock.succ.add(currentloopcond);
        currentloopcond.pred.add(currentBlock);
        currentBlock.terminated=true;
    }

    @Override
    public void visit(EmptyStmt it) {

    }

    @Override
    public void visit(ForStmt it) {
        ++loopDepth;
        ++Label;
        Block loopincr=new Block(loopDepth,"loopincr"+Label), loopcond=new Block(loopDepth,"loopcond"+Label), loopend=new Block(loopDepth,"loopend"+Label), loopbody=new Block(loopDepth,"loopbody"+Label);
        Block _loopend=currentloopend,_loopcond=currentloopcond;
        currentloopcond=(it.incr==null)?(it.cond==null?loopbody:loopcond):loopincr;
        currentloopend=loopend;

        if(it.init!=null) it.init.accept(this);

        if(it.cond!=null){
            currentBlock.succ.add(loopcond);
            loopcond.pred.add(currentBlock);
            currentBlock=loopcond;
            currentFunc.blocks.add(currentBlock);
            it.cond.accept(this);
            currentBlock.insts.add(new Branch("beqz", getReg(it.cond.operand),null, loopend));
            currentBlock.succ.add(loopend);
            loopend.pred.add(currentBlock);
        }
        currentBlock.succ.add(loopbody);
        loopbody.pred.add(currentBlock);
        currentBlock=loopbody;
        currentFunc.blocks.add(currentBlock);
        it.body.accept(this);
        if(it.incr!=null){
            currentBlock.succ.add(loopincr);
            loopincr.pred.add(currentBlock);

            currentBlock=loopincr;
            currentFunc.blocks.add(currentBlock);
            it.incr.accept(this);
        }
        currentBlock.insts.add(new J(loopcond));
        currentBlock.succ.add(loopcond);
        loopcond.pred.add(currentBlock);

        currentBlock=loopend;
        currentFunc.blocks.add(currentBlock);

        currentloopend=_loopend;
        currentloopcond=_loopcond;
        --loopDepth;
    }

    @Override
    public void visit(IfStmt it) {
        ++Label;
        Block ifend= new Block(loopDepth,"ifend"+Label), iftrue= new Block(loopDepth,"iftrue"+Label),iffalse=ifend;


        if(it.falseStmt!=null) iffalse=new Block(loopDepth,"iffalse"+Label);

        it.cond.accept(this);
        currentBlock.insts.add(new Branch("beqz",getReg(it.cond.operand),null, iffalse));
        currentBlock.succ.add(iffalse);
        iffalse.pred.add(currentBlock);
        currentBlock.succ.add(iftrue);
        iftrue.pred.add(currentBlock);

        currentBlock=iftrue;
        currentFunc.blocks.add(currentBlock);
        it.trueStmt.accept(this);
        if(it.falseStmt!=null){
            currentBlock.insts.add(new J(ifend));
            currentBlock.succ.add(ifend);
            ifend.pred.add(currentBlock);

            currentBlock=iffalse;
            currentFunc.blocks.add(currentBlock);
            it.falseStmt.accept(this);
        }
        currentBlock.succ.add(ifend);
        ifend.pred.add(currentBlock);

        currentBlock=ifend;
        currentFunc.blocks.add(currentBlock);
    }

    @Override
    public void visit(PureExprStmt it) {
        it.expr.accept(this);
    }

    @Override
    public void visit(ReturnStmt it) {
        if(it.returnValue!=null){
            it.returnValue.accept(this);
            currentBlock.insts.add(new Mv(root.getPReg(10), getReg(it.returnValue.operand)));
        }
        if(currentFunc.endBlock!=null){
            currentBlock.insts.add(new J(currentFunc.endBlock));
            currentBlock.succ.add(currentFunc.endBlock);
            currentFunc.endBlock.pred.add(currentBlock);
        }
        currentBlock.terminated=true;
    }

    @Override
    public void visit(WhileStmt it) {
        ++loopDepth;
        ++Label;
        Block _loopend=currentloopend,_loopcond=currentloopcond;
        Block loopcond=new Block(loopDepth,"loopcond"+Label),loopend=new Block(loopDepth,"loopend"+Label),loopbody=new Block(loopDepth,"loopbody"+Label);
        currentloopcond=loopcond;
        currentloopend=loopend;

        currentBlock.succ.add(loopcond);
        loopcond.pred.add(currentBlock);
        currentBlock=loopcond;
        currentFunc.blocks.add(currentBlock);
        it.cond.accept(this);
        currentBlock.insts.add(new Branch("beqz",getReg(it.cond.operand),null,loopend));
        currentBlock.succ.add(loopend);
        loopend.pred.add(currentBlock);
        currentBlock.succ.add(loopbody);
        loopbody.pred.add(currentBlock);

        currentBlock=loopbody;
        currentFunc.blocks.add(currentBlock);
        it.body.accept(this);
        currentBlock.insts.add(new J(loopcond));
        currentBlock.succ.add(loopcond);
        loopcond.pred.add(currentBlock);

        currentBlock=loopend;
        currentFunc.blocks.add(currentBlock);

        currentloopend=_loopend;
        currentloopcond=_loopcond;
        --loopDepth;
    }

    public VReg newArray(int i,NewExpr it){
        VReg nowreg=new VReg("tmp");
        Register sz=getReg(it.exprList.get(i).operand);
        currentBlock.insts.add(new Calc("addi", root.getPReg(10), sz, new Imm(1)));
        currentBlock.insts.add(new Calc("slli", root.getPReg(10), root.getPReg(10),new Imm(2)));
        currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_malloc"),root));
        currentBlock.insts.add(new Mv(nowreg, root.getPReg(10)));
        currentBlock.insts.add(new Store(sz,nowreg,new Imm(0)));
        if(i<it.exprList.size()-1){

            VReg iter=new VReg("tmp");
            currentBlock.insts.add(new Mv(iter,sz));

            ++Label;
            ++loopDepth;
            Block _loopend= new Block(loopDepth,"newloopend"+Label),_loopcond= new Block(loopDepth,"newloopcond"+Label),_loopbody= new Block(loopDepth,"newloopbody"+Label);
            currentBlock.succ.add(_loopcond);
            _loopcond.pred.add(currentBlock);


            currentBlock=_loopcond;
            currentFunc.blocks.add(currentBlock);
            currentBlock.insts.add(new Branch("beqz",iter,null,_loopend));
            currentBlock.succ.add(_loopend);
            _loopend.pred.add(currentBlock);
            currentBlock.succ.add(_loopbody);
            _loopbody.pred.add(currentBlock);

            currentBlock=_loopbody;
            currentFunc.blocks.add(currentBlock);
            VReg res=new VReg("tmp");
            currentBlock.insts.add(new Calc("slli", res, iter, new Imm(2)));
            currentBlock.insts.add(new Calc("add", res, nowreg, res));

            currentBlock.insts.add(new Store(newArray(i+1,it),res,new Imm(0)));

            currentBlock.insts.add(new Calc("addi", iter, iter, new Imm(-1)));
            currentBlock.insts.add(new J(_loopcond));
            currentBlock.succ.add(_loopcond);
            _loopcond.pred.add(currentBlock);

            currentBlock=_loopend;
            currentFunc.blocks.add(currentBlock);
            --loopDepth;
        }
        return nowreg;
    }
    @Override
    public void visit(NewExpr it) {
        if(it.exprList!=null) it.exprList.forEach(x->x.accept(this));
        if(it.type instanceof ArrayType){
            it.operand = newArray(0,it);
        }else{
            currentBlock.insts.add(new Li(root.getPReg(10),new Imm(((ClassType)it.type).size()),"li"));
            currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_malloc"),root));
            it.operand = new VReg("NewAddress");
            currentBlock.insts.add(new Mv((Register) it.operand, root.getPReg(10)));
            if(((ClassType)it.type).constructor!=null) currentBlock.insts.add(new Call(((ClassType)it.type).constructor,root));
        }
    }

    @Override
    public void visit(BoolConstExpr it) {
        it.operand = new Imm(it.val?1:0);
    }

    @Override
    public void visit(IntConstExpr it) {
        it.operand = new Imm(it.val);
    }

    @Override
    public void visit(NullConstExpr it) {
        it.operand = root.getPReg(0);
    }

    @Override
    public void visit(StrConstExpr it) {
        root.strings.add(it.val);
        VReg tmp = new VReg("tmp");
        it.operand = tmp;
        currentBlock.insts.add(new Li(tmp,new Symbol(".LS"+String.valueOf(root.strings.size()-1)),"la"));
    }

    @Override
    public void visit(ExprListExpr it) {

    }

    @Override
    public void visit(BinaryExpr it) {
        if(it.op.equals("=")){
            it.expr1.accept(this);
            it.expr2.accept(this);
            assign(it.expr1.operand,getReg(it.expr2.operand));
            it.operand = it.expr1.operand;
            return;
        }
        VReg tmp =new VReg("tmp");
        it.operand=tmp;
        switch (it.op) {
            case "&&":
                ++Label;
                Block setfalse = new Block(loopDepth,"setfalse"+Label), settrue = new Block(loopDepth,"settrue"+Label), end = new Block(loopDepth,"setend"+Label);
                //short-circuit
                it.expr1.accept(this);
                currentBlock.insts.add(new Branch("beqz", getReg(it.expr1.operand), null, setfalse));
                currentBlock.succ.add(setfalse);
                setfalse.pred.add(currentBlock);
                currentBlock.succ.add(settrue);
                settrue.pred.add(currentBlock);

                currentBlock=settrue;
                currentFunc.blocks.add(currentBlock);
                it.expr2.accept(this);
                currentBlock.insts.add(new Branch("beqz", getReg(it.expr2.operand), null, setfalse));
                currentBlock.succ.add(setfalse);
                setfalse.pred.add(currentBlock);

                currentBlock.insts.add(new Li(tmp, new Imm(1), "li"));
                currentBlock.insts.add(new J(end));
                currentBlock.succ.add(end);
                end.pred.add(currentBlock);

                currentBlock=setfalse;
                currentFunc.blocks.add(currentBlock);
                currentBlock.insts.add(new Li(tmp, new Imm(0),"li"));
                currentBlock.succ.add(end);
                end.pred.add(currentBlock);

                currentBlock=end;
                currentFunc.blocks.add(currentBlock);
                return;
            case "||":
                ++Label;
                settrue = new Block(loopDepth,"settrue"+Label);
                setfalse = new Block(loopDepth,"setfalse"+Label);
                end = new Block(loopDepth,"setend"+Label);
                //short-circuit
                it.expr1.accept(this);
                currentBlock.insts.add(new Branch("bnez", getReg(it.expr1.operand), null, settrue));
                currentBlock.succ.add(settrue);
                settrue.pred.add(currentBlock);
                currentBlock.succ.add(setfalse);
                setfalse.pred.add(currentBlock);

                currentBlock=setfalse;
                currentFunc.blocks.add(currentBlock);
                it.expr2.accept(this);
                currentBlock.insts.add(new Branch("bnez", getReg(it.expr2.operand), null, settrue));
                currentBlock.succ.add(settrue);
                settrue.pred.add(currentBlock);

                currentBlock.insts.add(new Li(tmp,new Imm(0),"li"));
                currentBlock.insts.add(new J(end));
                currentBlock.succ.add(end);
                end.pred.add(currentBlock);

                currentBlock=settrue;
                currentFunc.blocks.add(currentBlock);
                currentBlock.insts.add(new Li(tmp, new Imm(1),"li"));
                currentBlock.succ.add(end);
                end.pred.add(currentBlock);

                currentBlock=end;
                currentFunc.blocks.add(currentBlock);
                return;
        }
        it.expr1.accept(this);
        it.expr2.accept(this);
        if(it.expr1.type.isString()){
            currentBlock.insts.add(new Mv(root.getPReg(10),getReg(it.expr1.operand)));
            currentBlock.insts.add(new Mv(root.getPReg(11),getReg(it.expr2.operand)));
            switch(it.op) {
                case "+":
                    currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_str_add"),root));
                    break;
                case "<":
                    currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_str_lt"),root));
                    break;
                case ">":
                    currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_str_gt"),root));
                    break;
                case "<=":
                    currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_str_le"),root));
                    break;
                case ">=":
                    currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_str_ge"),root));
                    break;
                case "==":
                    currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_str_eq"),root));
                    break;
                case "!=":
                    currentBlock.insts.add(new Call(funcMap.get("__Om_builtin_str_ne"),root));
                    break;
            }
            currentBlock.insts.add(new Mv(tmp,root.getPReg(10)));
            return;
        }
        Register rs1=getReg(it.expr1.operand), rs2=getReg(it.expr2.operand);
        switch(it.op){
            case "*":
                currentBlock.insts.add(new Calc("mul", tmp, rs1, rs2));
                break;
            case "/":
                currentBlock.insts.add(new Calc("div", tmp, rs1, rs2));
                break;
            case "%":
                currentBlock.insts.add(new Calc("rem", tmp, rs1, rs2));
                break;
            case "-":
                currentBlock.insts.add(new Calc("sub", tmp, rs1, rs2));
                break;
            case "<<":
                currentBlock.insts.add(new Calc("sll", tmp, rs1, rs2));
                break;
            case ">>":
                currentBlock.insts.add(new Calc("sra", tmp, rs1, rs2));
                break;
            case "&":
                currentBlock.insts.add(new Calc("and", tmp, rs1, rs2));
                break;
            case "^":
                currentBlock.insts.add(new Calc("xor", tmp, rs1, rs2));
                break;
            case "|":
                currentBlock.insts.add(new Calc("or", tmp, rs1, rs2));
                break;
            case "+":
                currentBlock.insts.add(new Calc("add", tmp, rs1, rs2));
                break;
            case "<":
                currentBlock.insts.add(new Calc("slt", tmp, rs1, rs2));
                break;
            case ">":
                currentBlock.insts.add(new Calc("sgt", tmp, rs1, rs2));
                break;
            case "<=":
                currentBlock.insts.add(new Calc("sgt", tmp, rs1, rs2));
                currentBlock.insts.add(new Calc("xori", tmp,tmp,new Imm(1)));
                break;
            case ">=":
                currentBlock.insts.add(new Calc("slt", tmp, rs1, rs2));
                currentBlock.insts.add(new Calc("xori", tmp,tmp,new Imm(1)));
                break;
            case "==":
                currentBlock.insts.add(new Calc("xor", tmp, rs1, rs2));
                currentBlock.insts.add(new Calc("seqz", tmp,tmp,null));
                break;
            case "!=":
                currentBlock.insts.add(new Calc("xor", tmp, rs1, rs2));
                currentBlock.insts.add(new Calc("snez", tmp,tmp,null));
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(FuncCallExpr it) {
        it.exprList.forEach(x->x.accept(this));
        if(it.base instanceof MemberAccessExpr){
            it.base.accept(this);
            if (((MemberAccessExpr) it.base).base.type instanceof ArrayType && ((FuncSymbol)it.base.type).name.equals("size")) {
                it.operand =new VReg("ArraySize");
                currentBlock.insts.add(new Load((Register) it.operand, getReg(it.base.operand),new Imm(0)));
                return;
            }
            currentBlock.insts.add(new Mv(root.getPReg(10),getReg(it.base.operand) ));
            for(int i=0;i<it.exprList.size();++i){
                if(i+1<8){
                    currentBlock.insts.add(new Mv(root.getPReg(10+(i+1)),getReg(it.exprList.get(i).operand)));
                }else{
                    currentBlock.insts.add(new Store(getReg(it.exprList.get(i).operand),root.getPReg(2),new Imm((8-(i+1))*4)));
                }
            }
        }else if(((FuncSymbol)it.base.type).inClass){
            currentBlock.insts.add(new Mv(root.getPReg(10),thisptr));
            for(int i=0;i<it.exprList.size();++i){
                if(i+1<8){
                    currentBlock.insts.add(new Mv(root.getPReg(10+(i+1)),getReg(it.exprList.get(i).operand)));
                }else{
                    currentBlock.insts.add(new Store(getReg(it.exprList.get(i).operand),root.getPReg(2),new Imm((7-(i+1))*4)));
                }
            }
        }else{
            for(int i=0;i<it.exprList.size();++i){
                if(i<8){
                    currentBlock.insts.add(new Mv(root.getPReg(10+i),getReg(it.exprList.get(i).operand)));
                }else{
                    currentBlock.insts.add(new Store(getReg(it.exprList.get(i).operand),root.getPReg(2),new Imm((7-i)*4)));
                }
            }
        }
        currentBlock.insts.add(new Call((FuncSymbol)it.base.type,root));

        it.operand =new VReg("ReturnValue");
        currentBlock.insts.add(new Mv((Register) it.operand,root.getPReg(10)));
    }

    @Override
    public void visit(MemberAccessExpr it) {
        it.base.accept(this);
        if(it.isFunc){
            it.operand = it.base.operand;
        }else{
            VReg tmp=new VReg("tmp");
            if(((Imm)it.var.operand).val*4>2047){
                //todo:
            }
            currentBlock.insts.add(new Calc("addi",tmp, getReg(it.base.operand),new Imm(((Imm)it.var.operand).val*4)));
            tmp.isptr=true;
            it.operand = tmp;
        }
    }

    @Override
    public void visit(SubscriptExpr it) {
        it.base.accept(this);
        it.offset.accept(this);
        VReg tmp=new VReg("tmp");
        currentBlock.insts.add(new Calc("addi", tmp, getReg(it.offset.operand), new Imm(1)));
        currentBlock.insts.add(new Calc("slli", tmp, tmp, new Imm(2)));
        currentBlock.insts.add(new Calc("add", tmp , getReg(it.base.operand), tmp));
        tmp.isptr=true;
        it.operand = tmp;
    }

    @Override
    public void visit(SuffixExpr it) {
        it.expr.accept(this);
        VReg tmp = new VReg("tmp");
        it.operand = new VReg("tmp");
        Register rs = getReg(it.expr.operand);
        currentBlock.insts.add(new Mv((Register) it.operand,rs));
        switch (it.op) {
            case "++":
                currentBlock.insts.add(new Calc("addi", tmp, rs, new Imm(1)));
                break;
            case "--":
                currentBlock.insts.add(new Calc("addi", tmp, rs, new Imm(-1)));
                break;
        }
        assign(it.expr.operand,tmp);
    }

    @Override
    public void visit(ThisExpr it) {
        it.operand = thisptr;
    }

    @Override
    public void visit(UnaryExpr it) {
        it.expr.accept(this);
        Register rs = getReg(it.expr.operand);
        VReg tmp = new VReg("tmp");
        it.operand=tmp;
        switch (it.op) {
            case "++":
                currentBlock.insts.add(new Calc("addi", tmp , rs, new Imm(1)));
                assign(it.expr.operand,tmp);
                break;
            case "--":
                currentBlock.insts.add(new Calc("addi", tmp, rs, new Imm(-1)));
                assign(it.expr.operand,tmp);
                break;
            case "+":
                it.operand = it.expr.operand;
                break;
            case "-":
                currentBlock.insts.add(new Calc("neg", tmp, rs,null));
                break;
            case "~":
                currentBlock.insts.add(new Calc("not", tmp, rs,null));
                break;
            case "!":
                currentBlock.insts.add(new Calc("seqz", tmp, rs,null));
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(VarExpr it) {
        if(it.var.isClassMember){
            VReg tmp=new VReg("tmp");
            currentBlock.insts.add(new Calc("addi",tmp, thisptr,new Imm(((Imm)it.var.operand).val*4)));
            tmp.isptr = true;
            it.operand = tmp;
        }else it.operand =it.var.operand;
    }
}