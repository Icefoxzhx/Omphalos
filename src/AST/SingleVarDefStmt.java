package AST;

import Util.position;
import Util.symbol.VarSymbol;

public class SingleVarDefStmt extends StmtNode{
    public TypeNode type;
    public String name;
    public ExprNode expr;
    public VarSymbol var;
    public SingleVarDefStmt(position pos,String name,ExprNode expr){
        super(pos);
        this.name=name;
        this.expr=expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}