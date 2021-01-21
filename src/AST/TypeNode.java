package AST;

import Util.position;

public class TypeNode extends ASTNode{
    public String Type;
    public int dim;

    public TypeNode(position pos, String Type, int dim){
        super(pos);
        this.Type=Type;
        this.dim=dim;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}