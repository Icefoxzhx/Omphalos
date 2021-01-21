package AST;

import Util.position;

public class MemberAccessExpr extends ExprNode{
    public ExprNode base;
    public String name;
    public boolean isFunc=false;
    public MemberAccessExpr(position pos,ExprNode base,String name){
        super(pos,true);
        this.base=base;
        this.name=name;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}