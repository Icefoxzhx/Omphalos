package AST;

import Util.position;

public class BinaryExpr extends ExprNode{
    public ExprNode expr1,expr2;
    public String op;
    public BinaryExpr(position pos,ExprNode expr1,ExprNode expr2,String op){
        super(pos);
        this.expr1=expr1;
        this.expr2=expr2;
        this.op=op;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}