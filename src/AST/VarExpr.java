package AST;

import Util.position;

public class VarExpr extends ExprNode{
    public String name;
    public VarExpr(position pos,String name){
        super(pos,true);
        this.name=name;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}
