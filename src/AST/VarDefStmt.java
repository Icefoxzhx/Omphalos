package AST;

import Util.position;
import java.util.ArrayList;

public class VarDefStmt extends StmtNode{
    public ArrayList<SingleVarDefStmt> varList=new ArrayList<>();
    public VarDefStmt(position pos){
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}