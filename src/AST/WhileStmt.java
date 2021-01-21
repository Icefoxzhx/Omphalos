package AST;

import Util.position;

public class WhileStmt extends StmtNode{
    public ExprNode cond;
    public StmtNode body;
    public WhileStmt(position pos,ExprNode cond,StmtNode body){
        super(pos);
        this.cond=cond;
        this.body=body;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}