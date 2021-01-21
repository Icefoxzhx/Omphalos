package AST;

import Util.position;
import java.util.ArrayList;
public class FuncCallExpr extends ExprNode{
    public ExprNode base;
    public ArrayList<ExprNode> exprList=new ArrayList<>();
    public FuncCallExpr(position pos,ExprNode base){
        super(pos);
        this.base=base;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}