package AST;

import Util.position;

public class SubscriptExpr extends ExprNode{
    public ExprNode base,offset;
    public SubscriptExpr(position pos,ExprNode base,ExprNode offset){
        super(pos,true);
        this.base=base;
        this.offset=offset;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}