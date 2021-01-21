package AST;

import Util.position;

public class ThisExpr extends ExprNode{
    public ThisExpr(position pos){
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}
