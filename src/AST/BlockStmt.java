package AST;

import Util.position;
import java.util.ArrayList;

public class BlockStmt extends StmtNode{
    public ArrayList<StmtNode> stmtList=new ArrayList<>();
    public BlockStmt(position pos){
        super(pos);
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}