package AST;

import Util.position;

public class UnaryExpr extends ExprNode{
    public ExprNode expr;
    public String op;
    public UnaryExpr(position pos,ExprNode expr,String op){
        super(pos);
        this.expr=expr;
        this.op=op;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}