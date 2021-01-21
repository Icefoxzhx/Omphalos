package AST;

import Util.position;
import java.util.ArrayList;

public class ClassDefNode extends ASTNode{
    public String name;
    public ArrayList<SingleVarDefStmt> varList=new ArrayList<>();
    public ArrayList<FuncDefNode> funcList=new ArrayList<>();
    public FuncDefNode constructor=null;
    public ClassDefNode(position pos,String name){
        super(pos);
        this.name=name;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}