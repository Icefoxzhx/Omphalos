package AST;

import Util.position;

public class IfStmt extends StmtNode{
    public ExprNode cond;
    public StmtNode trueStmt,falseStmt;
    public IfStmt(position pos,ExprNode cond,StmtNode trueStmt,StmtNode falseStmt){
        super(pos);
        this.cond=cond;
        this.trueStmt=trueStmt;
        this.falseStmt=falseStmt;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}