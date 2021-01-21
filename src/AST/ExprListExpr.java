package AST;

import Util.position;
import java.util.ArrayList;

public class ExprListExpr extends ExprNode{
    public ArrayList<ExprNode> exprList=new ArrayList<>();
    public ExprListExpr(position pos){
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}