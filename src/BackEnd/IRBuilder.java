package BackEnd;

import AST.*;
import IR.*;
import IR.inst.*;
import IR.operand.*;
import Util.symbol.*;

import java.util.ArrayList;
import java.util.HashMap;

public class IRBuilder implements ASTVisitor {
    public Type currentReturnType;
    private Block currentloopend,currentloopcond;
    private int Label=0;
    private int loopDepth=0;
    private boolean MainInited=false,returnDone=false;
    private ArrayList<SingleVarDefStmt> globals=new ArrayList<>();
    private Function currentFunc;
    private Block currentBlock;
    private Register thisptr;
    private IR.Root root;
    private HashMap<String, FuncSymbol> funcMap=new HashMap<>();
    public IRBuilder(IR.Root root){
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

    public Operand resolvePtr(Operand x){
        if(!x.isptr) return x;
        Register tmp=new Register("tmp");
        currentBlock.insts.add(new Load(currentBlock,tmp,x));
        return tmp;
    }

    public void assign(Operand rd, Operand rs){
        if(rd.isptr) currentBlock.insts.add(new Store(currentBlock,rs, rd));
        else currentBlock.insts.add(new Assign(currentBlock,(Register) rd,rs));
    }

    public void checkBranch(ExprNode it){
        if(it.trueBlock==null) return;
        if(it.operand instanceof ConstInt){
            currentBlock.addTerminator(new J(currentBlock,((ConstInt) it.operand).val==0?it.falseBlock:it.trueBlock));
        }
        else currentBlock.addTerminator(new Branch(currentBlock,resolvePtr(it.operand),it.trueBlock,it.falseBlock));
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
                    assign(it.var.operand, resolvePtr(it.expr.operand));
                }
            }
        } else {
            it.var.operand = new Register(it.var.name);
            if (it.expr != null) {
                it.expr.accept(this);
                assign(it.var.operand, resolvePtr(it.expr.operand));
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
        if(it.func.inClass){
            thisptr=new Register("this");
            currentFunc.params.add(thisptr);
        }
        currentFunc.StackSpace=Integer.max(0,it.paramList.size()+(it.func.inClass?1:0)-8)*4;
        currentBlock=currentFunc.beginBlock=new Block(loopDepth,currentFunc.name+"."+currentFunc.blocks.size());
        currentFunc.blocks.add(currentBlock);
        root.func.add(currentFunc);

        it.paramList.forEach(x->{
            x.var.operand=new Register(x.name);
            currentFunc.params.add(x.var.operand);
        });

        if(currentFunc.name.equals("main")){
            MainInited=true;
            globals.forEach(x->x.accept(this));
        }
        it.block.accept(this);

        if(!currentBlock.terminated){
            Inst xx;

            if(currentFunc.name.equals("main")){
                xx=new Return(currentBlock,new ConstInt(0));
            }else xx=new Return(currentBlock,null);
            currentFunc.returnBlocks.add(currentBlock);
            currentBlock.addTerminator(xx);
        }
        if(currentFunc.returnBlocks.size()>1){
            currentFunc.endBlock=currentBlock=new Block(loopDepth,"Returnof" + currentFunc.name);
            if(!it.func.returnType.isVoid()){
                Register tmp=new Register("tmp");
                currentFunc.returnBlocks.forEach(b-> {
                    if(((Return)b.insts.get(b.insts.size()-1)).val!=null) b.insts.add(b.insts.size()-1,new Assign(b,tmp,((Return)b.insts.get(b.insts.size()-1)).val));
                });
                currentBlock.addTerminator(new Return(currentBlock,tmp));
            }else currentBlock.addTerminator(new Return(currentBlock,null));
            currentFunc.returnBlocks.forEach(block->{
                block.removeTerminator();
                block.addTerminator(new J(block,currentBlock));
            });
            currentFunc.blocks.add(currentBlock);
        }else currentFunc.endBlock=currentBlock;
    }

    @Override
    public void visit(ClassDefNode it) {
        // no default assign...?
        it.funcList.forEach(x->x.accept(this));
        if(it.constructor!=null) it.constructor.accept(this);
    }

    @Override
    public void visit(BreakStmt it) {
        currentBlock.addTerminator(new J(currentBlock,currentloopend));
    }

    @Override
    public void visit(ContinueStmt it) {
        currentBlock.addTerminator(new J(currentBlock,currentloopcond));
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
            currentBlock.addTerminator(new J(currentBlock,loopcond));

            currentBlock=loopcond;
            currentFunc.blocks.add(currentBlock);
            it.cond.trueBlock=loopbody;
            it.cond.falseBlock=loopend;
            it.cond.accept(this);
        }else currentBlock.addTerminator(new J(currentBlock,loopbody));
        currentBlock=loopbody;
        currentFunc.blocks.add(currentBlock);
        it.body.accept(this);
        if(it.incr!=null){
            currentBlock.addTerminator(new J(currentBlock,loopincr));

            currentBlock=loopincr;
            currentFunc.blocks.add(currentBlock);
            it.incr.accept(this);
        }
        currentBlock.addTerminator(new J(currentBlock,it.cond==null?loopbody:loopcond));

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

        it.cond.trueBlock=iftrue;
        it.cond.falseBlock=iffalse;

        it.cond.accept(this);
        currentBlock=iftrue;
        currentFunc.blocks.add(currentBlock);
        it.trueStmt.accept(this);
        if(it.falseStmt!=null){
            currentBlock.addTerminator(new J(currentBlock,ifend));

            currentBlock=iffalse;
            currentFunc.blocks.add(currentBlock);
            it.falseStmt.accept(this);
        }
        currentBlock.addTerminator(new J(currentBlock,ifend));

        currentBlock=ifend;
        currentFunc.blocks.add(currentBlock);
    }

    @Override
    public void visit(PureExprStmt it) {
        it.expr.accept(this);
    }

    @Override
    public void visit(ReturnStmt it){
        Inst xx;
        if(it.returnValue!=null){
            it.returnValue.accept(this);
            xx=new Return(currentBlock,resolvePtr(it.returnValue.operand));
        }else xx=new Return(currentBlock,null);
        currentFunc.returnBlocks.add(currentBlock);
        currentBlock.addTerminator(xx);
    }

    @Override
    public void visit(WhileStmt it) {
        ++loopDepth;
        ++Label;
        Block _loopend=currentloopend,_loopcond=currentloopcond;
        Block loopcond=new Block(loopDepth,"loopcond"+Label),loopend=new Block(loopDepth,"loopend"+Label),loopbody=new Block(loopDepth,"loopbody"+Label);
        currentloopcond=loopcond;
        currentloopend=loopend;

        currentBlock.addTerminator(new J(currentBlock,loopcond));

        currentBlock=loopcond;
        currentFunc.blocks.add(currentBlock);

        it.cond.trueBlock=loopbody;
        it.cond.falseBlock=loopend;
        it.cond.accept(this);

        currentBlock=loopbody;
        currentFunc.blocks.add(currentBlock);

        it.body.accept(this);
        currentBlock.addTerminator(new J(currentBlock,loopcond));

        currentBlock=loopend;
        currentFunc.blocks.add(currentBlock);

        currentloopend=_loopend;
        currentloopcond=_loopcond;
        --loopDepth;
    }

    public Register newArray(int i,NewExpr it){
        Register nowreg=new Register("tmp");
        Operand sz=resolvePtr(it.exprList.get(i).operand);
        Call xx=new Call(currentBlock,funcMap.get("__Om_builtin_malloc"),nowreg);
        Register tmp=new Register("tmp");
        currentBlock.insts.add(new Calc(currentBlock,"add", tmp, sz, new ConstInt(1)));
        currentBlock.insts.add(new Calc(currentBlock,"sll", tmp, tmp, new ConstInt(2)));
        xx.params.add(tmp);
        currentBlock.insts.add(xx);

        currentBlock.insts.add(new Store(currentBlock,sz,nowreg));
        if(i<it.exprList.size()-1){

            Register iter=new Register("tmp");
            currentBlock.insts.add(new Assign(currentBlock,iter,sz));

            ++Label;
            ++loopDepth;
            Block _loopend= new Block(loopDepth,"newloopend"+Label),_loopcond= new Block(loopDepth,"newloopcond"+Label),_loopbody= new Block(loopDepth,"newloopbody"+Label);
            currentBlock.addTerminator(new J(currentBlock,_loopcond));

            currentBlock=_loopcond;
            currentFunc.blocks.add(currentBlock);
            currentBlock.addTerminator(new Branch(currentBlock,iter,_loopbody,_loopend));

            currentBlock=_loopbody;
            currentFunc.blocks.add(currentBlock);
            Register res=new Register("tmp");
            currentBlock.insts.add(new Calc(currentBlock,"sll", res, iter, new ConstInt(2)));
            currentBlock.insts.add(new Calc(currentBlock,"add", res, nowreg, res));

            currentBlock.insts.add(new Store(currentBlock,newArray(i+1,it),res));

            currentBlock.insts.add(new Calc(currentBlock,"add", iter, iter, new ConstInt(-1)));
            currentBlock.addTerminator(new J(currentBlock,_loopcond));

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
            it.operand = new Register("NewAddress");
            Call xx=new Call(currentBlock,funcMap.get("__Om_builtin_malloc"),(Register) it.operand);
            xx.params.add(new ConstInt(((ClassType)it.type).size()));
            currentBlock.insts.add(xx);
            if(((ClassType)it.type).constructor!=null){
                xx=new Call(currentBlock,((ClassType)it.type).constructor,null);
                xx.params.add(it.operand);
                currentBlock.insts.add(xx);
            }
        }
    }

    @Override
    public void visit(BoolConstExpr it) {
        it.operand = new ConstInt(it.val?1:0);
        checkBranch(it);
    }

    @Override
    public void visit(IntConstExpr it) {
        it.operand = new ConstInt(it.val);
    }

    @Override
    public void visit(NullConstExpr it) {
        it.operand = new ConstInt(0);
    }

    @Override
    public void visit(StrConstExpr it) {
        root.strings.add(it.val);
        it.operand = new ConstStr(".LS"+String.valueOf(root.strings.size()-1),it.val);
    }

    @Override
    public void visit(ExprListExpr it) {

    }

    @Override
    public void visit(BinaryExpr it) {
        if(it.op.equals("=")){
            it.expr1.accept(this);
            it.expr2.accept(this);
            assign(it.expr1.operand,resolvePtr(it.expr2.operand));
            it.operand = it.expr1.operand;
            return;
        }
        Register tmp =new Register("tmp");
        it.operand=tmp;
        switch (it.op) {
            case "&&":
                ++Label;
                if(it.trueBlock!=null){
                    Block newBlock=new Block(loopDepth,"andandtmp"+Label);
                    it.expr1.trueBlock=newBlock;
                    it.expr1.falseBlock=it.falseBlock;
                    it.expr2.trueBlock=it.trueBlock;
                    it.expr2.falseBlock=it.falseBlock;
                    it.expr1.accept(this);
                    currentBlock=newBlock;
                    currentFunc.blocks.add(currentBlock);
                    it.expr2.accept(this);
                }else{
                    Block setfalse = new Block(loopDepth,"setfalse"+Label), settrue = new Block(loopDepth,"settrue"+Label), end = new Block(loopDepth,"setend"+Label), settmp = new Block(loopDepth, "settmp"+Label);
                    //short-circuit
                    it.expr1.trueBlock=settmp;
                    it.expr1.falseBlock=setfalse;
                    it.expr1.accept(this);

                    currentBlock=settmp;
                    currentFunc.blocks.add(currentBlock);
                    it.expr2.trueBlock=settrue;
                    it.expr2.falseBlock=setfalse;
                    it.expr2.accept(this);

                    currentBlock=settrue;
                    currentFunc.blocks.add(currentBlock);
                    currentBlock.insts.add(new Assign(currentBlock,tmp, new ConstInt(1)));
                    currentBlock.addTerminator(new J(currentBlock,end));

                    currentBlock=setfalse;
                    currentFunc.blocks.add(currentBlock);
                    currentBlock.insts.add(new Assign(currentBlock,tmp, new ConstInt(0)));
                    currentBlock.addTerminator(new J(currentBlock,end));

                    currentBlock=end;
                    currentFunc.blocks.add(currentBlock);

                    if(it.expr1.operand instanceof ConstInt && it.expr2.operand instanceof ConstInt){
                        it.operand=new ConstInt(((ConstInt) it.expr2.operand).val&((ConstInt) it.expr2.operand).val);
                    }
                }

                return;
            case "||":
                ++Label;
                if(it.trueBlock!=null){
                    Block newBlock=new Block(loopDepth,"andandtmp"+Label);
                    it.expr1.trueBlock=it.trueBlock;
                    it.expr1.falseBlock=newBlock;
                    it.expr2.trueBlock=it.trueBlock;
                    it.expr2.falseBlock=it.falseBlock;
                    it.expr1.accept(this);
                    currentBlock=newBlock;
                    currentFunc.blocks.add(currentBlock);
                    it.expr2.accept(this);
                }else{
                    Block settrue = new Block(loopDepth,"settrue"+Label),
                    setfalse = new Block(loopDepth,"setfalse"+Label),
                    settmp = new Block(loopDepth,"settmp"+Label),
                    end = new Block(loopDepth,"setend"+Label);
                    //short-circuit
                    it.expr1.trueBlock=settrue;
                    it.expr1.falseBlock=settmp;
                    it.expr1.accept(this);

                    currentBlock=settmp;
                    currentFunc.blocks.add(currentBlock);
                    it.expr2.trueBlock=settrue;
                    it.expr2.falseBlock=setfalse;
                    it.expr2.accept(this);

                    currentBlock=setfalse;
                    currentFunc.blocks.add(currentBlock);
                    currentBlock.insts.add(new Assign(currentBlock,tmp,new ConstInt(0)));
                    currentBlock.addTerminator(new J(currentBlock,end));

                    currentBlock=settrue;
                    currentFunc.blocks.add(currentBlock);
                    currentBlock.insts.add(new Assign(currentBlock,tmp, new ConstInt(1)));
                    currentBlock.addTerminator(new J(currentBlock,end));

                    currentBlock=end;
                    currentFunc.blocks.add(currentBlock);

                    if(it.expr1.operand instanceof ConstInt && it.expr2.operand instanceof ConstInt){
                        it.operand=new ConstInt(((ConstInt) it.expr2.operand).val | ((ConstInt) it.expr2.operand).val);
                    }
                }
                return;
        }
        it.expr1.accept(this);
        it.expr2.accept(this);
        if(it.expr1.type.isString()){
            Call xx=new Call(currentBlock,null,tmp);
            xx.params.add(resolvePtr(it.expr1.operand));
            xx.params.add(resolvePtr(it.expr2.operand));
            switch(it.op) {
                case "+":
                    xx.func=funcMap.get("__Om_builtin_str_add");
                    break;
                case "<":
                    xx.func=funcMap.get("__Om_builtin_str_lt");
                    break;
                case ">":
                    xx.func=funcMap.get("__Om_builtin_str_gt");
                    break;
                case "<=":
                    xx.func=funcMap.get("__Om_builtin_str_le");
                    break;
                case ">=":
                    xx.func=funcMap.get("__Om_builtin_str_ge");
                    break;
                case "==":
                    xx.func=funcMap.get("__Om_builtin_str_eq");
                    break;
                case "!=":
                    xx.func=funcMap.get("__Om_builtin_str_ne");
                    break;
            }
            currentBlock.insts.add(xx);
            checkBranch(it);
            return;
        }
        //naive constant folding
        if(it.expr1.operand instanceof ConstInt && it.expr2.operand instanceof ConstInt){
            if(!((it.op.equals("/")||it.op.equals("%")) && ((ConstInt) it.expr2.operand).val==0)){
                int res= switch (it.op){
                    case "*" -> ((ConstInt) it.expr1.operand).val * ((ConstInt) it.expr2.operand).val;
                    case "/" -> ((ConstInt) it.expr1.operand).val / (((ConstInt) it.expr2.operand).val);
                    case "%" -> ((ConstInt) it.expr1.operand).val % ((ConstInt) it.expr2.operand).val;
                    case "-" -> ((ConstInt) it.expr1.operand).val - ((ConstInt) it.expr2.operand).val;
                    case "<<" -> ((ConstInt) it.expr1.operand).val << ((ConstInt) it.expr2.operand).val;
                    case ">>" -> ((ConstInt) it.expr1.operand).val >> ((ConstInt) it.expr2.operand).val;
                    case "&" -> ((ConstInt) it.expr1.operand).val & ((ConstInt) it.expr2.operand).val;
                    case "^" -> ((ConstInt) it.expr1.operand).val ^ ((ConstInt) it.expr2.operand).val;
                    case "|" -> ((ConstInt) it.expr1.operand).val | ((ConstInt) it.expr2.operand).val;
                    case "+" -> ((ConstInt) it.expr1.operand).val + ((ConstInt) it.expr2.operand).val;
                    case "<" -> ((ConstInt) it.expr1.operand).val < ((ConstInt) it.expr2.operand).val ? 1: 0;
                    case ">" -> ((ConstInt) it.expr1.operand).val > ((ConstInt) it.expr2.operand).val ? 1: 0;
                    case "<=" -> ((ConstInt) it.expr1.operand).val <= ((ConstInt) it.expr2.operand).val ? 1: 0;
                    case ">=" -> ((ConstInt) it.expr1.operand).val >= ((ConstInt) it.expr2.operand).val ? 1: 0;
                    case "==" -> ((ConstInt) it.expr1.operand).val == ((ConstInt) it.expr2.operand).val ? 1: 0;
                    case "!=" -> ((ConstInt) it.expr1.operand).val >= ((ConstInt) it.expr2.operand).val ? 1: 0;
                    default -> 0;
                };
                it.operand=new ConstInt(res);
                checkBranch(it);
                return;
            }
        }
        Operand rs1=resolvePtr(it.expr1.operand),rs2=resolvePtr(it.expr2.operand);
        switch(it.op){
            case "*":
                currentBlock.insts.add(new Calc(currentBlock,"mul", tmp, rs1, rs2));
                break;
            case "/":
                currentBlock.insts.add(new Calc(currentBlock,"div", tmp, rs1, rs2));
                break;
            case "%":
                currentBlock.insts.add(new Calc(currentBlock,"rem", tmp, rs1, rs2));
                break;
            case "-":
                currentBlock.insts.add(new Calc(currentBlock,"sub", tmp, rs1, rs2));
                break;
            case "<<":
                currentBlock.insts.add(new Calc(currentBlock,"sll", tmp, rs1, rs2));
                break;
            case ">>":
                currentBlock.insts.add(new Calc(currentBlock,"sra", tmp, rs1, rs2));
                break;
            case "&":
                currentBlock.insts.add(new Calc(currentBlock,"and", tmp, rs1, rs2));
                break;
            case "^":
                currentBlock.insts.add(new Calc(currentBlock,"xor", tmp, rs1, rs2));
                break;
            case "|":
                currentBlock.insts.add(new Calc(currentBlock,"or", tmp, rs1, rs2));
                break;
            case "+":
                currentBlock.insts.add(new Calc(currentBlock,"add", tmp, rs1, rs2));
                break;
            case "<":
                currentBlock.insts.add(new Cmp(currentBlock,"slt", tmp, rs1, rs2));
                break;
            case ">":
                currentBlock.insts.add(new Cmp(currentBlock,"sgt", tmp, rs1, rs2));
                break;
            case "<=":
                currentBlock.insts.add(new Cmp(currentBlock,"sle", tmp, rs1, rs2));
                break;
            case ">=":
                currentBlock.insts.add(new Cmp(currentBlock,"sge", tmp, rs1, rs2));
                break;
            case "==":
                currentBlock.insts.add(new Cmp(currentBlock,"eq", tmp, rs1, rs2));
                break;
            case "!=":
                currentBlock.insts.add(new Cmp(currentBlock,"ne", tmp, rs1, rs2));
                break;
            default:
                break;
        }
        checkBranch(it);
    }

    @Override
    public void visit(FuncCallExpr it) {
        it.exprList.forEach(x->x.accept(this));
        it.operand=new Register("Return Value");
        Call xx=new Call(currentBlock,((FuncSymbol)it.base.type),(Register) it.operand);
        if(it.base instanceof MemberAccessExpr){
            it.base.accept(this);
            if (((MemberAccessExpr) it.base).base.type instanceof ArrayType && ((FuncSymbol)it.base.type).name.equals("size")) {
                it.operand =new Register("ArraySize");
                currentBlock.insts.add(new Load(currentBlock,(Register) it.operand, resolvePtr(it.base.operand)));
                return;
            }
            xx.params.add(resolvePtr(it.base.operand));
        }else if(((FuncSymbol)it.base.type).inClass)  xx.params.add(thisptr);

        it.exprList.forEach(x->{
            xx.params.add(resolvePtr(x.operand));
        });
        currentBlock.insts.add(xx);
        checkBranch(it);
    }

    @Override
    public void visit(MemberAccessExpr it) {
        it.base.accept(this);
        if(it.isFunc){
            it.operand = it.base.operand;
        }else{
            Register tmp=new Register("tmp");
            if(((ConstInt)it.var.operand).val*4>2047){
                //todo:
            }
            currentBlock.insts.add(new Calc(currentBlock,"add",tmp, resolvePtr(it.base.operand),new ConstInt(((ConstInt)it.var.operand).val*4)));
            tmp.isptr=true;
            it.operand = tmp;
        }
        checkBranch(it);
    }

    @Override
    public void visit(SubscriptExpr it) {
        it.base.accept(this);
        it.offset.accept(this);
        Register tmp=new Register("tmp");
        currentBlock.insts.add(new Calc(currentBlock,"add", tmp, resolvePtr(it.offset.operand), new ConstInt(1)));
        currentBlock.insts.add(new Calc(currentBlock,"sll", tmp, tmp, new ConstInt(2)));
        currentBlock.insts.add(new Calc(currentBlock,"add", tmp , resolvePtr(it.base.operand), tmp));
        tmp.isptr=true;
        it.operand = tmp;
        checkBranch(it);
    }

    @Override
    public void visit(SuffixExpr it) {
        it.expr.accept(this);
        Register tmp = new Register("tmp");
        it.operand = new Register("tmp");
        Operand rs = resolvePtr(it.expr.operand);
        currentBlock.insts.add(new Assign(currentBlock,(Register) it.operand,rs));
        switch (it.op) {
            case "++":
                currentBlock.insts.add(new Calc(currentBlock,"add", tmp, rs, new ConstInt(1)));
                break;
            case "--":
                currentBlock.insts.add(new Calc(currentBlock,"add", tmp, rs, new ConstInt(-1)));
                break;
        }
        assign(it.expr.operand,tmp);
        checkBranch(it);
    }

    @Override
    public void visit(ThisExpr it) {
        it.operand = thisptr;
    }

    @Override
    public void visit(UnaryExpr it) {
        it.expr.accept(this);
        Operand rs = resolvePtr(it.expr.operand);
        Register tmp = new Register("tmp");
        it.operand=tmp;
        switch (it.op) {
            case "++":
                currentBlock.insts.add(new Calc(currentBlock,"add", tmp , rs, new ConstInt(1)));
                assign(it.expr.operand,tmp);
                break;
            case "--":
                currentBlock.insts.add(new Calc(currentBlock,"add", tmp, rs, new ConstInt(-1)));
                assign(it.expr.operand,tmp);
                break;
            case "+":
                it.operand = it.expr.operand;
                break;
            case "-":
                currentBlock.insts.add(new Calc(currentBlock,"neg", tmp, rs,null));
                break;
            case "~":
                currentBlock.insts.add(new Calc(currentBlock,"not", tmp, rs,null));
                break;
            case "!":
                currentBlock.insts.add(new Calc(currentBlock,"seqz", tmp, rs,null));
                break;
            default:
                break;
        }
        checkBranch(it);
    }

    @Override
    public void visit(VarExpr it) {
        if(it.var.isClassMember){
            Register tmp=new Register("tmp");
            currentBlock.insts.add(new Calc(currentBlock,"add",tmp, thisptr,new ConstInt(((ConstInt)it.var.operand).val*4)));
            tmp.isptr = true;
            it.operand = tmp;
        }else it.operand =it.var.operand;
        checkBranch(it);
    }
}