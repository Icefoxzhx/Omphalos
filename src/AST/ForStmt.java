package AST;

import Util.position;

public class ForStmt extends StmtNode{
    public ExprNode init,cond,incr;
    public StmtNode body;
    public ForStmt(position pos,ExprNode init,ExprNode cond,ExprNode incr,StmtNode body){
        super(pos);
        this.init=init;
        this.cond=cond;
        this.incr=incr;
        this.body=body;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}