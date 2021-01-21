package AST;

import Util.position;

public class SuffixExpr extends ExprNode{
    public ExprNode expr;
    public String op;
    public SuffixExpr(position pos,ExprNode expr,String op){
        super(pos);
        this.expr=expr;
        this.op=op;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}