package AST;

import Util.position;

public class ReturnStmt extends StmtNode{
    public ExprNode returnValue;
    public ReturnStmt(position pos,ExprNode returnValue){
        super(pos);
        this.returnValue=returnValue;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}