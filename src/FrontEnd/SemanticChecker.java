package FrontEnd;

import AST.*;
import IR.operand.Address;
import IR.operand.Imm;
import Util.error.semanticError;
import Util.symbol.*;

public class SemanticChecker implements ASTVisitor{
    public Scope globalScope,currentScope;
    public Type currentReturnType;
    public ClassType currentClass;
    public boolean returnDone;
    public int loopDepth=0;

    public SemanticChecker(Scope global){
        this.globalScope=global;
    }

    @Override
    public void visit(ProgramNode it) {
        FuncSymbol main=globalScope.getFunction("main",false,it.pos);
        if(!main.returnType.isInt()) throw new semanticError("main function must return int",it.pos);
        if(main.paramList.size()!=0) throw new semanticError("main function should not have parameters",it.pos);
        currentScope=globalScope;
        it.body.forEach(x->x.accept(this));
    }

    @Override
    public void visit(SingleVarDefStmt it) {
        Type t=globalScope.getType(it.type);
        if(t.isVoid()) throw new semanticError("void variable",it.pos);
        if(it.expr!=null){
            it.expr.accept(this);
            if(!it.expr.type.equals(t)) throw new semanticError("variable init type error",it.pos);
        }
        it.var=new VarSymbol(it.name,t);
        if(currentScope==globalScope) it.var.isGlobal=true;
        currentScope.defineVariable(it.name,it.var,it.pos);
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
        it.stmtList.forEach(x->{
            if(x instanceof BlockStmt){
                currentScope=new Scope(currentScope);
                x.accept(this);
                currentScope=currentScope.parentScope;
            }else{
                x.accept(this);
            }
        });
    }

    @Override
    public void visit(FuncDefNode it) {
        if(it.type!=null) currentReturnType=globalScope.getType(it.type);
        else currentReturnType=new PrimitiveType("void");
        if(currentClass!=null){
           it.func.inClass=true;
           it.func.abs_name="_"+currentClass.name+"_"+it.func.name;
        }
        else it.func.abs_name=it.func.name;
        returnDone=false;
        currentScope=new Scope(currentScope);
        it.paramList.forEach(x->{
            x.var=new VarSymbol(x.name, globalScope.getType(x.type));
            currentScope.defineVariable(x.name,x.var,x.pos);
        });
        it.block.accept(this);
        currentScope=currentScope.parentScope;
        it.returnDone=returnDone;
        if(it.name.equals("main")) returnDone=true;
        if(!returnDone&&it.type!=null&&!it.type.Type.equals("void"))
            throw new semanticError("No return",it.pos);
    }

    @Override
    public void visit(ClassDefNode it) {
        currentClass=(ClassType) globalScope.typeMap.get(it.name);
        currentScope=new Scope(currentScope);
        for(int i=0;i<it.varList.size();++i) {
            it.varList.get(i).var.Vregid = new Imm(i);
            it.varList.get(i).var.isClassMember=true;
        }
        currentClass.varMap.forEach((key,val)->currentScope.defineVariable(key,val,it.pos));
        currentClass.funcMap.forEach((key,val)->currentScope.defineFunction(key,val,it.pos));
        it.funcList.forEach(x->x.accept(this));
        if(it.constructor!=null){
            if(!it.constructor.name.equals(it.name)) throw new semanticError("mismatched constructor name",it.pos);
            it.constructor.accept(this);
        }
        currentScope=currentScope.parentScope;
        currentClass=null;
    }

    @Override
    public void visit(BreakStmt it) {
        if(loopDepth==0) throw new semanticError("break not in loop",it.pos);
    }

    @Override
    public void visit(ContinueStmt it) {
        if(loopDepth==0) throw new semanticError("continue not in loop",it.pos);
    }

    @Override
    public void visit(EmptyStmt it) {

    }

    @Override
    public void visit(ForStmt it) {
        if(it.init!=null) it.init.accept(this);
        if(it.cond!=null){
            it.cond.accept(this);
            if(!it.cond.type.isBool())
                throw new semanticError("for condition not bool",it.pos);
        }
        if(it.incr!=null) it.incr.accept(this);
        loopDepth++;
        currentScope=new Scope(currentScope);
        it.body.accept(this);
        currentScope=currentScope.parentScope;
        loopDepth--;
    }

    @Override
    public void visit(IfStmt it) {
        it.cond.accept(this);
        if(!it.cond.type.isBool()) throw new semanticError("if condition not bool",it.pos);
        currentScope=new Scope(currentScope);
        it.trueStmt.accept(this);
        currentScope=currentScope.parentScope;
        if(it.falseStmt!=null){
            currentScope=new Scope(currentScope);
            it.falseStmt.accept(this);
            currentScope=currentScope.parentScope;
        }
    }

    @Override
    public void visit(PureExprStmt it) {
        it.expr.accept(this);
    }

    @Override
    public void visit(ReturnStmt it) {
        returnDone=true;
        if(it.returnValue!=null){
            it.returnValue.accept(this);
            if(!it.returnValue.type.equals(currentReturnType)) throw new semanticError("return type error", it.pos);
        }else{
            if(!currentReturnType.isVoid()) throw new semanticError("return type error",it.pos);
        }
    }

    @Override
    public void visit(WhileStmt it) {
        it.cond.accept(this);
        if(!it.cond.type.isBool()) throw new semanticError("while condition not bool",it.pos);
        loopDepth++;
        currentScope=new Scope(currentScope);
        it.body.accept(this);
        currentScope=currentScope.parentScope;
        loopDepth--;
    }

    @Override
    public void visit(NewExpr it) {
        if(it.exprList!=null){
            it.exprList.forEach(x->{
                x.accept(this);
                if(!x.type.isInt()) throw new semanticError("not int", x.pos);
            });
        }
        it.type=globalScope.getType(it.typeNode);
    }

    @Override
    public void visit(BoolConstExpr it) {
        it.type = new PrimitiveType("bool");
    }

    @Override
    public void visit(IntConstExpr it) {
        it.type = new PrimitiveType("int");
    }

    @Override
    public void visit(NullConstExpr it) {
        it.type = new PrimitiveType("null");
    }

    @Override
    public void visit(StrConstExpr it) {
        it.type = new PrimitiveType("string");
    }

    @Override
    public void visit(ExprListExpr it) {

    }

    @Override
    public void visit(BinaryExpr it) {
        it.expr1.accept(this);
        it.expr2.accept(this);
        switch (it.op) {
            case "*":
            case "/":
            case "%":
            case "-":
            case "<<":
            case ">>":
            case "&":
            case "^":
            case "|":
                if (!(it.expr1.type.isInt() && it.expr2.type.isInt())) throw new semanticError("not int", it.pos);
                it.type = new PrimitiveType("int");
                break;
            case "+":
                if (!((it.expr1.type.isInt() && it.expr2.type.isInt()) || (it.expr1.type.isString() && it.expr2.type.isString())))
                    throw new semanticError("not int or string", it.pos);
                it.type = it.expr1.type;
                break;
            case "<":
            case ">":
            case "<=":
            case ">=":
                if (!((it.expr1.type.isInt() && it.expr2.type.isInt()) || (it.expr1.type.isString() && it.expr2.type.isString())))
                    throw new semanticError("not int or string", it.pos);
                it.type = new PrimitiveType("bool");
                break;
            case "&&":
            case "||":
                if (!(it.expr1.type.isBool() && it.expr2.type.isBool())) throw new semanticError("not bool", it.pos);
                it.type = new PrimitiveType("bool");
                break;
            case "==":
            case "!=":
                if (!it.expr1.type.equals(it.expr2.type)) throw new semanticError("not same type", it.pos);
                it.type = new PrimitiveType("bool");
                break;
            case "=":
                if (!it.expr1.type.equals(it.expr2.type)) throw new semanticError("not same type", it.pos);
                if (!it.expr1.assignable) throw new semanticError("not assignable", it.pos);
                it.type = it.expr1.type;
                it.assignable = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(FuncCallExpr it) {
        if(it.base instanceof VarExpr){
            it.base.type=currentScope.getFunction(((VarExpr)it.base).name,true,it.pos);
        }else it.base.accept(this);
        if(!(it.base.type instanceof FuncSymbol)) throw new semanticError("not a function",it.pos);
        FuncSymbol func=(FuncSymbol)it.base.type;
        it.exprList.forEach(x->x.accept(this));
        if(func.paramList.size()!=it.exprList.size()) throw new semanticError("parameter number error", it.pos);
        for(int i=0;i<func.paramList.size();i++){
            if(!func.paramList.get(i).type.equals(it.exprList.get(i).type))
                throw new semanticError("parameter type error",it.pos);
        }
        it.type=func.returnType;
    }

    @Override
    public void visit(MemberAccessExpr it) {
        it.base.accept(this);
        if (it.base.type instanceof ArrayType && it.isFunc && it.name.equals("size")) {
            FuncSymbol func = new FuncSymbol("size");
            func.returnType = new PrimitiveType("int");
            it.type = func;
            return;
        }
        if (it.base.type.isString() && it.isFunc && it.name.equals("length")) {
            FuncSymbol func = new FuncSymbol("length");
            func.returnType = new PrimitiveType("int");
            func.abs_name="__Om_builtin_str_length";
            it.type = func;
            return;
        }
        if (it.base.type.isString() && it.isFunc && it.name.equals("substring")) {
            FuncSymbol func = new FuncSymbol("substring");
            func.returnType = new PrimitiveType("string");
            func.abs_name="__Om_builtin_str_substring";
            func.paramList.add(new VarSymbol("left", new PrimitiveType("int")));
            func.paramList.add(new VarSymbol("right", new PrimitiveType("int")));
            it.type = func;
            return;
        }
        if (it.base.type.isString() && it.isFunc && it.name.equals("parseInt")) {
            FuncSymbol func = new FuncSymbol("parseInt");
            func.returnType = new PrimitiveType("int");
            func.abs_name="__Om_builtin_str_parseInt";
            it.type = func;
            return;
        }
        if (it.base.type.isString() && it.isFunc && it.name.equals("ord")) {
            FuncSymbol func = new FuncSymbol("ord");
            func.returnType = new PrimitiveType("int");
            func.abs_name="__Om_builtin_str_ord";
            func.paramList.add(new VarSymbol("pos", new PrimitiveType("int")));
            it.type = func;
            return;
        }
        if (!(it.base.type instanceof ClassType)) throw new semanticError("not a class", it.pos);
        ClassType tmp = (ClassType) it.base.type;
        if (it.isFunc) {
            if (tmp.funcMap.containsKey(it.name)) it.type = tmp.funcMap.get(it.name);
            else throw new semanticError("no such symbol", it.pos);
        } else {
            if (tmp.varMap.containsKey(it.name)){
                it.var=tmp.varMap.get(it.name);
                it.type = it.var.type;
            }
            else throw new semanticError("no such symbol", it.pos);
        }
    }

    @Override
    public void visit(SubscriptExpr it) {
        it.base.accept(this);
        it.offset.accept(this);
        if(!(it.base.type instanceof ArrayType)) throw new semanticError("not an array", it.pos);
        if(!it.offset.type.isInt()) throw new semanticError("subscript not int", it.pos);
        ArrayType tmp = (ArrayType) it.base.type;
        if(tmp.dim - 1 == 0) it.type = tmp.basicType;
        else it.type = new ArrayType(tmp.basicType, tmp.dim - 1);
    }

    @Override
    public void visit(SuffixExpr it) {
        it.expr.accept(this);
        if(!it.expr.type.isInt())  throw new semanticError("not int", it.pos);
        if(!it.expr.assignable) throw new semanticError("not assignable", it.pos);
        it.type = it.expr.type;
    }

    @Override
    public void visit(ThisExpr it) {
        if(currentClass==null) throw new semanticError("this not in class", it.pos);
        it.type=currentClass;
    }

    @Override
    public void visit(UnaryExpr it) {
        it.expr.accept(this);
        switch (it.op) {
            case "++":
            case "--":
                if (!it.expr.assignable) throw new semanticError("not assignable", it.pos);
                it.assignable = true;
            case "+":
            case "-":
            case "~":
                if (!it.expr.type.isInt()) throw new semanticError("not int", it.pos);
                break;
            case "!":
                if (!it.expr.type.isBool()) throw new semanticError("not bool", it.pos);
                break;
            default:
                break;
        }
        it.type = it.expr.type;
    }

    @Override
    public void visit(VarExpr it) {
        it.var=currentScope.getVariable(it.name,true,it.pos);
        it.type=it.var.type;
    }
}