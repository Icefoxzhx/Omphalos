package AST;
import Util.position;

public class BreakStmt extends StmtNode{
    public BreakStmt(position pos){
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}