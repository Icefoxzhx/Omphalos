package AST;
import Util.position;

public class PureExprStmt extends StmtNode{
    public ExprNode expr;
    public PureExprStmt(position pos,ExprNode expr){
        super(pos);
        this.expr=expr;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}