package AST;

import Util.position;
import java.util.ArrayList;

public class NewExpr extends ExprNode{
    public TypeNode typeNode;
    public ArrayList<ExprNode> exprList=new ArrayList<>();
    public NewExpr(position pos,TypeNode typeNode,int dim){
        super(pos);
        this.typeNode=typeNode;
        this.typeNode.dim=dim;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}