package AST;

import Util.position;

public class IntConstExpr extends ExprNode{
    public int val;
    public IntConstExpr(position pos,int val){
        super(pos);
        this.val=val;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}