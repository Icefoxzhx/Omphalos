package AST;

import Util.position;
import Util.symbol.VarSymbol;

public class VarExpr extends ExprNode{
    public String name;
    public VarSymbol var;
    public VarExpr(position pos,String name){
        super(pos,true);
        this.name=name;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}
