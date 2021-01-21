package AST;

import Util.position;

public class NullConstExpr extends ExprNode{
    public NullConstExpr(position pos){
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}