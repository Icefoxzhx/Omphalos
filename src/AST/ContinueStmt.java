package AST;
import Util.position;

public class ContinueStmt extends StmtNode{
    public ContinueStmt(position pos){
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}