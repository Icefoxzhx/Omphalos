package AST;

import Util.position;

public class BoolConstExpr extends ExprNode{
    public boolean val;
    public BoolConstExpr(position pos,boolean val){
        super(pos);
        this.val=val;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}