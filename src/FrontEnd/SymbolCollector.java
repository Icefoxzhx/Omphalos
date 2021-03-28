package FrontEnd;

import AST.*;
import Util.symbol.*;

//to support forwarding reference

public class SymbolCollector implements ASTVisitor{
    Scope global,current;

    public SymbolCollector(Scope global){
        this.global=global;
        this.global.typeMap.put("int",new PrimitiveType("int"));
        this.global.typeMap.put("bool",new PrimitiveType("bool"));
        this.global.typeMap.put("string",new PrimitiveType("string"));
        this.global.typeMap.put("void",new PrimitiveType("void"));
        this.global.typeMap.put("null",new PrimitiveType("null"));
        {
            FuncSymbol func=new FuncSymbol("print");
            func.returnType=new PrimitiveType("void");
            func.abs_name="__Om_builtin_print";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            this.global.funcMap.put("print",func);
        }
        {
            FuncSymbol func=new FuncSymbol("println");
            func.returnType=new PrimitiveType("void");
            func.abs_name="__Om_builtin_println";
            func.paramList.add(new VarSymbol("str",new PrimitiveType("string")));
            this.global.funcMap.put("println",func);
        }
        {
            FuncSymbol func=new FuncSymbol("printInt");
            func.returnType=new PrimitiveType("void");
            func.abs_name="__Om_builtin_printInt";
            func.paramList.add(new VarSymbol("n",new PrimitiveType("int")));
            this.global.funcMap.put("printInt",func);
        }
        {
            FuncSymbol func=new FuncSymbol("printlnInt");
            func.returnType=new PrimitiveType("void");
            func.abs_name="__Om_builtin_printlnInt";
            func.paramList.add(new VarSymbol("n",new PrimitiveType("int")));
            this.global.funcMap.put("printlnInt",func);
        }
        {
            FuncSymbol func=new FuncSymbol("getString");
            func.returnType=new PrimitiveType("string");
            func.abs_name="__Om_builtin_getString";
            this.global.funcMap.put("getString",func);
        }
        {
            FuncSymbol func=new FuncSymbol("getInt");
            func.returnType=new PrimitiveType("int");
            func.abs_name="__Om_builtin_getInt";
            this.global.funcMap.put("getInt",func);
        }
        {
            FuncSymbol func=new FuncSymbol("toString");
            func.returnType=new PrimitiveType("string");
            func.abs_name="__Om_builtin_toString";
            func.paramList.add(new VarSymbol("i",new PrimitiveType("int")));
            this.global.funcMap.put("toString",func);
        }
    }
    @Override
    public void visit(ProgramNode it) {
        current=global;
        it.body.forEach(x->x.accept(this));
    }

    @Override
    public void visit(SingleVarDefStmt it) {
        it.var=new VarSymbol(it.name);
        current.defineVariable(it.name,it.var,it.pos);
    }

    @Override
    public void visit(TypeNode it) {

    }

    @Override
    public void visit(VarDefStmt it) {

    }

    @Override
    public void visit(BlockStmt it) {

    }

    @Override
    public void visit(FuncDefNode it) {
        it.func=new FuncSymbol(it.name);
        current.defineFunction(it.name,it.func,it.pos);
    }

    @Override
    public void visit(ClassDefNode it) {
        current=new Scope(current);
        ClassType tmp=new ClassType(it.name);
        it.varList.forEach(x->x.accept(this));
        it.funcList.forEach(x->x.accept(this));
        if(it.constructor!=null) tmp.constructor=it.constructor.func=new FuncSymbol(it.constructor.name);
        tmp.varMap= current.varMap;
        tmp.funcMap= current.funcMap;
        current=current.parentScope;
        current.defineType(it.name,tmp,it.pos);
    }

    @Override
    public void visit(BreakStmt it) {

    }

    @Override
    public void visit(ContinueStmt it) {

    }

    @Override
    public void visit(EmptyStmt it) {

    }

    @Override
    public void visit(ForStmt it) {

    }

    @Override
    public void visit(IfStmt it) {

    }

    @Override
    public void visit(PureExprStmt it) {

    }

    @Override
    public void visit(ReturnStmt it) {

    }

    @Override
    public void visit(WhileStmt it) {

    }

    @Override
    public void visit(NewExpr it) {

    }

    @Override
    public void visit(BoolConstExpr it) {

    }

    @Override
    public void visit(IntConstExpr it) {

    }

    @Override
    public void visit(NullConstExpr it) {

    }

    @Override
    public void visit(StrConstExpr it) {

    }

    @Override
    public void visit(ExprListExpr it) {

    }

    @Override
    public void visit(BinaryExpr it) {

    }

    @Override
    public void visit(FuncCallExpr it) {

    }

    @Override
    public void visit(MemberAccessExpr it) {

    }

    @Override
    public void visit(SubscriptExpr it) {

    }

    @Override
    public void visit(SuffixExpr it) {

    }

    @Override
    public void visit(ThisExpr it) {

    }

    @Override
    public void visit(UnaryExpr it) {

    }

    @Override
    public void visit(VarExpr it) {

    }
}