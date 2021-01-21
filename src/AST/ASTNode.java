package AST;

import Util.position;

public abstract class ASTNode{
    public position pos;
    public ASTNode(position pos){
        this.pos=pos;
    }
    public abstract void accept(ASTVisitor visitor);
}