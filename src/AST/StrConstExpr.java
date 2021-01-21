package AST;

import Util.position;

public class StrConstExpr extends ExprNode{
    public String val;
    public StrConstExpr(position pos,String val){
        super(pos);
        this.val=val;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}