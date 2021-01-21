package FrontEnd;

import AST.*;
import Util.symbol.Scope;
import Util.symbol.ClassType;
import Util.symbol.VarSymbol;

public class TypeCollector implements ASTVisitor{
    Scope global;
    String currentClass;

    public TypeCollector(Scope global){
        this.global=global;
    }
    @Override
    public void visit(ProgramNode it) {
        currentClass=null;
        it.body.forEach(x->x.accept(this));
    }

    @Override
    public void visit(SingleVarDefStmt it) {
        if(currentClass==null) global.varMap.get(it.name).type=global.getType(it.type);
        else ((ClassType)global.typeMap.get(currentClass)).varMap.get(it.name).type=global.getType(it.type);
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
        if(currentClass==null){
            global.funcMap.get(it.name).returnType=global.getType(it.type);
            it.paramList.forEach(x->global.funcMap.get(it.name).paramList.add(new VarSymbol(x.name,global.getType(x.type))));
        }else{
            ((ClassType)global.typeMap.get(currentClass)).funcMap.get(it.name).returnType=global.getType(it.type);
            it.paramList.forEach(x->((ClassType)global.typeMap.get(currentClass)).funcMap.get(it.name).paramList.add(new VarSymbol(x.name,global.getType(x.type))));
        }
    }

    @Override
    public void visit(ClassDefNode it) {
        currentClass=it.name;
        it.varList.forEach(x->x.accept(this));
        it.funcList.forEach(x->x.accept(this));
        currentClass=null;
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