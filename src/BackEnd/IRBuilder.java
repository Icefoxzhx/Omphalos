package BackEnd;

import AST.*;
import IR.*;
import IR.inst.*;
import IR.operand.*;
import Util.error.semanticError;
import Util.symbol.*;
import org.antlr.v4.runtime.atn.SemanticContext;

import java.util.ArrayList;

public class IRBuilder implements ASTVisitor {
    public Type currentReturnType;
    private int loopend,loopcond;
    private int Label=0;
    private boolean MainInited=false,returnDone=false;
    private ArrayList<SingleVarDefStmt> globals=new ArrayList<>();
    private IRBlock currentBlock;
    private IRBlockList Root;
    public IRBuilder(IRBlockList Root){
        this.Root=Root;
    }
    @Override
    public void visit(ProgramNode it) {
        it.body.forEach(x->x.accept(this));
    }

    @Override
    public void visit(SingleVarDefStmt it) {
        if (it.var.isGlobal) {
            if (!MainInited) globals.add(it);
            else {
                Root.globals.add(it.name);
                it.var.Vregid = new Symbol(it.name);
                if (it.expr != null) {
                    it.expr.accept(this);
                    currentBlock.insts.add(new Store(it.expr.Vregid, it.var.Vregid));
                }
            }
        } else {
            it.var.Vregid = new VReg(++currentBlock.Vregnum);
            if (it.expr != null) {
                it.expr.accept(this);
                currentBlock.insts.add(new Store(it.expr.Vregid, it.var.Vregid));
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
        it.stmtList.forEach(x->x.accept(this));
    }

    @Override
    public void visit(FuncDefNode it) {
        currentBlock=new IRBlock(it.name);
        currentBlock.returnlabel=++Label;
        Root.blocks.add(currentBlock);

        if(it.inClass==false){
            for(int i=0;i<it.paramList.size();++i){
                if(i<8) it.paramList.get(i).var.Vregid=new PReg("a"+i);
                else it.paramList.get(i).var.Vregid=new VReg(++currentBlock.Vregnum);
            }
            if(it.name.equals("main")){
                MainInited=true;
                globals.forEach(x->x.accept(this));
            }
            it.block.accept(this);

            if(it.name.equals("main")&&!it.returnDone){
                currentBlock.insts.add(new Mv(new PReg("a0"),new PReg("zero")));
                currentBlock.insts.add(new J(currentBlock.returnlabel));
            }
        }else{
            for(int i=0;i<it.paramList.size();++i){
                if(i+1<8) it.paramList.get(i).var.Vregid=new PReg("a"+(i+1));
                else it.paramList.get(i).var.Vregid=new VReg(++currentBlock.Vregnum);
            }
            it.block.accept(this);
        }
    }

    @Override
    public void visit(ClassDefNode it) {
        // no default assign...?
        it.funcList.forEach(x->x.accept(this));
    }

    @Override
    public void visit(BreakStmt it) {
        currentBlock.insts.add(new J(loopend));
    }

    @Override
    public void visit(ContinueStmt it) {
        currentBlock.insts.add(new J(loopcond));
    }

    @Override
    public void visit(EmptyStmt it) {

    }

    @Override
    public void visit(ForStmt it) {
        int _loopend=loopend,_loopcond=loopcond;
        loopcond=++Label;loopend=++Label;

        if(it.init!=null) it.init.accept(this);

        currentBlock.insts.add(new Label(loopcond));
        if(it.cond!=null) it.cond.accept(this);
        currentBlock.insts.add(new Branch("beqz",it.cond.Vregid,null,loopend));

        it.body.accept(this);
        if(it.incr!=null) it.incr.accept(this);
        currentBlock.insts.add(new J(loopcond));

        currentBlock.insts.add(new Label(loopend));

        loopend=_loopend;loopcond=_loopcond;
    }

    @Override
    public void visit(IfStmt it) {
        int ifend=++Label,iftrue=++Label,iffalse=ifend;
        if(it.falseStmt!=null) iffalse=++Label;

        it.cond.accept(this);
        currentBlock.insts.add(new Branch("beqz",it.cond.Vregid,null,iffalse));

        currentBlock.insts.add(new Label(iftrue));
        it.trueStmt.accept(this);
        if(it.falseStmt!=null){
            currentBlock.insts.add(new J(ifend));

            currentBlock.insts.add(new Label(iffalse));
            it.falseStmt.accept(this);
        }

        currentBlock.insts.add(new Label(ifend));
    }

    @Override
    public void visit(PureExprStmt it) {
        it.expr.accept(this);
    }

    @Override
    public void visit(ReturnStmt it) {
        if(it.returnValue!=null){
            it.returnValue.accept(this);
            currentBlock.insts.add(new Mv(new PReg("a0"),it.returnValue.Vregid));
        }
        currentBlock.insts.add(new J(currentBlock.returnlabel));
    }

    @Override
    public void visit(WhileStmt it) {
        int _loopend=loopend,_loopcond=loopcond;
        loopcond=++Label;loopend=++Label;

        currentBlock.insts.add(new Label(loopcond));
        it.cond.accept(this);
        currentBlock.insts.add(new Branch("beqz",it.cond.Vregid,null,loopend));

        it.body.accept(this);
        currentBlock.insts.add(new J(loopcond));

        currentBlock.insts.add(new Label(loopend));

        loopend=_loopend;loopcond=_loopcond;
    }

    public Operand newArray(int i,NewExpr it){
        Operand nowreg=new VReg(++currentBlock.Vregnum);
        Operand sz=it.exprList.get(i).Vregid;
        currentBlock.insts.add(new Calc("addi",new PReg("a0"),sz,new Imm(1)));
        currentBlock.insts.add(new Call("__Om_builtin_malloc"));
        currentBlock.insts.add(new Store(new PReg("a0"),nowreg));
        nowreg.isptr=true;
        currentBlock.insts.add(new Store(sz,nowreg));
        if(i<it.exprList.size()-1){
            Operand iter=new VReg(++currentBlock.Vregnum);
            currentBlock.insts.add(new Mv(iter,sz));

            int loopcond=++Label,loopend=++Label;

            currentBlock.insts.add(new Label(loopcond));
            currentBlock.insts.add(new Branch("beqz",iter,null,loopend));

            Operand res=new VReg(++currentBlock.Vregnum);
            currentBlock.insts.add(new Calc("slli",res,iter,new Imm(2)));
            currentBlock.insts.add(new Calc("add",res,nowreg,res));
            res.isptr=true;

            currentBlock.insts.add(new Store(newArray(i+1,it),res));

            currentBlock.insts.add(new Calc("addi",iter,iter,new Imm(-1)));
            currentBlock.insts.add(new J(loopcond));

            currentBlock.insts.add(new Label(loopend));
        }
        return nowreg;
    }
    @Override
    public void visit(NewExpr it) {
        if(it.exprList!=null) it.exprList.forEach(x->x.accept(this));
        if(it.type instanceof ArrayType){
            it.Vregid=newArray(0,it);
        }else{
            currentBlock.insts.add(new Li(new PReg("a0"),new Imm(((ClassType)it.type).size())));
            currentBlock.insts.add(new Call("__Om_builtin_malloc"));
            it.Vregid=new VReg(++currentBlock.Vregnum);
            currentBlock.insts.add(new Store(new PReg("a0"),it.Vregid));
            if(((ClassType)it.type).constructor!=null) currentBlock.insts.add(new Call(((ClassType)it.type).constructor.abs_name));
        }
    }

    @Override
    public void visit(BoolConstExpr it) {
        it.Vregid=new VReg(++currentBlock.Vregnum);
        currentBlock.insts.add(new Li(it.Vregid,new Imm(it.val?1:0)));
    }

    @Override
    public void visit(IntConstExpr it) {
        it.Vregid=new VReg(++currentBlock.Vregnum);
        currentBlock.insts.add(new Li(it.Vregid,new Imm(it.val)));
    }

    @Override
    public void visit(NullConstExpr it) {
        it.Vregid=new PReg("zero");
    }

    @Override
    public void visit(StrConstExpr it) {
        Root.strings.add(it.val);
        it.Vregid=new VReg(++currentBlock.Vregnum);
        currentBlock.insts.add(new Li(it.Vregid,new Symbol(".LS"+String.valueOf(Root.strings.size()-1))));
    }

    @Override
    public void visit(ExprListExpr it) {

    }

    @Override
    public void visit(BinaryExpr it) {
        if(it.op.equals("=")){
            it.expr1.accept(this);
            it.expr2.accept(this);
            currentBlock.insts.add(new Mv(it.expr1.Vregid,it.expr2.Vregid));
            it.Vregid=it.expr1.Vregid;
            return;
        }
        it.Vregid=new VReg(++currentBlock.Vregnum);
        switch (it.op) {
            case "&&":
                int setfalse = ++Label, end = ++Label;
                //short-circuit
                it.expr1.accept(this);
                currentBlock.insts.add(new Branch("beqz", it.expr1.Vregid, null, setfalse));
                it.expr2.accept(this);
                currentBlock.insts.add(new Branch("beqz", it.expr2.Vregid, null, setfalse));
                currentBlock.insts.add(new Li(it.Vregid, new Imm(1)));
                currentBlock.insts.add(new J(end));

                currentBlock.insts.add(new Label(setfalse));
                currentBlock.insts.add(new Li(it.Vregid, new Imm(0)));

                currentBlock.insts.add(new Label(end));
                return;
            case "||":
                int settrue = ++Label;
                end = ++Label;
                //short-circuit
                it.expr1.accept(this);
                currentBlock.insts.add(new Branch("bnez", it.expr1.Vregid, null, settrue));
                it.expr2.accept(this);
                currentBlock.insts.add(new Branch("bnez", it.expr2.Vregid, null, settrue));
                currentBlock.insts.add(new Li(it.Vregid,new Imm(0)));
                currentBlock.insts.add(new J(end));

                currentBlock.insts.add(new Label(settrue));
                currentBlock.insts.add(new Li(it.Vregid, new Imm(1)));

                currentBlock.insts.add(new Label(end));
                return;
        }
        it.expr1.accept(this);
        it.expr2.accept(this);
        if(it.expr1.type.isString()){
            currentBlock.insts.add(new Load(new PReg("a0"),it.expr1.Vregid));
            currentBlock.insts.add(new Load(new PReg("a1"),it.expr2.Vregid));
            switch(it.op) {
                case "+":
                    currentBlock.insts.add(new Call("__Om_builtin_str_add"));
                    break;
                case "<":
                    currentBlock.insts.add(new Call("__Om_builtin_str_lt"));
                    break;
                case ">":
                    currentBlock.insts.add(new Call("__Om_builtin_str_gt"));
                    break;
                case "<=":
                    currentBlock.insts.add(new Call("__Om_builtin_str_le"));
                    break;
                case ">=":
                    currentBlock.insts.add(new Call("__Om_builtin_str_ge"));
                    break;
                case "==":
                    currentBlock.insts.add(new Call("__Om_builtin_str_eq"));
                    break;
                case "!=":
                    currentBlock.insts.add(new Call("__Om_builtin_str_ne"));
                    break;
            }
            currentBlock.insts.add(new Store(new PReg("a0"),it.Vregid));
            return;
        }
        switch(it.op){
            case "*":
                currentBlock.insts.add(new Calc("mul", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "/":
                currentBlock.insts.add(new Calc("div", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "%":
                currentBlock.insts.add(new Calc("rem", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "-":
                currentBlock.insts.add(new Calc("sub", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "<<":
                currentBlock.insts.add(new Calc("sll", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case ">>":
                currentBlock.insts.add(new Calc("sra", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "&":
                currentBlock.insts.add(new Calc("and", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "^":
                currentBlock.insts.add(new Calc("xor", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "|":
                currentBlock.insts.add(new Calc("or", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "+":
                currentBlock.insts.add(new Calc("add", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "<":
                currentBlock.insts.add(new Calc("slt", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case ">":
                currentBlock.insts.add(new Calc("sgt", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                break;
            case "<=":
                currentBlock.insts.add(new Calc("sgt", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                currentBlock.insts.add(new Calc("xori", it.Vregid,it.Vregid,new Imm(1)));
                break;
            case ">=":
                currentBlock.insts.add(new Calc("slt", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                currentBlock.insts.add(new Calc("xori", it.Vregid,it.Vregid,new Imm(1)));
                break;
            case "==":
                currentBlock.insts.add(new Calc("xor", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                currentBlock.insts.add(new Calc("seqz",it.Vregid,it.Vregid,null));
                break;
            case "!=":
                currentBlock.insts.add(new Calc("xor", it.Vregid,it.expr1.Vregid,it.expr2.Vregid));
                currentBlock.insts.add(new Calc("snez",it.Vregid,it.Vregid,null));
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
            currentBlock.insts.add(new Mv(new PReg("a0"),it.base.Vregid));
            for(int i=0;i<it.exprList.size();++i){
                if(i+1<8){
                    currentBlock.insts.add(new Mv(new PReg("a"+(i+1)),it.exprList.get(i).Vregid));
                }else{
                    //todo:more parameters
                }
            }
        }else{
            for(int i=0;i<it.exprList.size();++i){
                if(i<8){
                    currentBlock.insts.add(new Mv(new PReg("a"+i),it.exprList.get(i).Vregid));
                }else{
                    //todo:more parameters
                }
            }
        }
        currentBlock.insts.add(new Call(((FuncSymbol)it.base.type).abs_name));

         it.Vregid=new VReg(++currentBlock.Vregnum);
        currentBlock.insts.add(new Mv(it.Vregid,new PReg("a0")));
    }

    @Override
    public void visit(MemberAccessExpr it) {
        it.base.accept(this);
        if(it.isFunc){
            it.Vregid=it.base.Vregid;
        }else{
            it.Vregid=new VReg(++currentBlock.Vregnum);
            currentBlock.insts.add(new Calc("addi",it.Vregid,it.base.Vregid,new Imm(((Imm)it.var.Vregid).val*4)));
            it.Vregid.isptr=true;
        }

    }

    @Override
    public void visit(SubscriptExpr it) {
        it.base.accept(this);
        it.offset.accept(this);
        it.Vregid=new VReg(++currentBlock.Vregnum);
        currentBlock.insts.add(new Calc("addi",it.Vregid,it.offset.Vregid,new Imm(1)));
        currentBlock.insts.add(new Calc("slli",it.Vregid,it.Vregid,new Imm(2)));
        currentBlock.insts.add(new Calc("add",it.Vregid,it.base.Vregid,it.Vregid));
        it.Vregid.isptr=true;
    }

    @Override
    public void visit(SuffixExpr it) {
        it.expr.accept(this);
        it.Vregid=new VReg(++currentBlock.Vregnum);
        currentBlock.insts.add(new Mv(it.Vregid,it.expr.Vregid));
        switch (it.op) {
            case "++":
                currentBlock.insts.add(new Calc("addi", it.expr.Vregid, it.expr.Vregid, new Imm(1)));
                break;
            case "--":
                currentBlock.insts.add(new Calc("addi", it.expr.Vregid, it.expr.Vregid, new Imm(-1)));
                break;
        }
    }

    @Override
    public void visit(ThisExpr it) {

    }

    @Override
    public void visit(UnaryExpr it) {
        it.expr.accept(this);
        switch (it.op) {
            case "++":
                it.Vregid=it.expr.Vregid;
                currentBlock.insts.add(new Calc("addi",it.Vregid,it.expr.Vregid,new Imm(1)));
                break;
            case "--":
                it.Vregid=it.expr.Vregid;
                currentBlock.insts.add(new Calc("addi",it.Vregid,it.expr.Vregid,new Imm(-1)));
                break;
            case "+":
                it.Vregid=it.expr.Vregid;
                break;
            case "-":
                it.Vregid=new VReg(++currentBlock.Vregnum);
                currentBlock.insts.add(new Calc("neg",it.Vregid,it.expr.Vregid,null));
                break;
            case "~":
                it.Vregid=new VReg(++currentBlock.Vregnum);
                currentBlock.insts.add(new Calc("not",it.Vregid,it.expr.Vregid,null));
                break;
            case "!":
                it.Vregid=new VReg(++currentBlock.Vregnum);
                currentBlock.insts.add(new Calc("seqz",it.Vregid,it.expr.Vregid,null));
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(VarExpr it) {
        it.Vregid=it.var.Vregid;
    }
}