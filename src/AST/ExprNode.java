package AST;

import IR.operand.Operand;
import Util.position;
import Util.symbol.Type;

public abstract class ExprNode extends ASTNode{
    public Type type;
    public Operand Vregid;
    public boolean assignable=false;
    public ExprNode(position pos){
        super(pos);
    }
    public ExprNode(position pos,boolean assignable){
        super(pos);
        this.assignable=assignable;
    }
}