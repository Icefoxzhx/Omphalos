package AST;

import Util.position;
import Util.symbol.FuncSymbol;

import java.util.ArrayList;

public class FuncDefNode extends ASTNode{
    public String name;
    public FuncSymbol func;
    public boolean returnDone=false;
    public TypeNode type;
    public BlockStmt block;
    public ArrayList<SingleVarDefStmt> paramList=new ArrayList<>();

    public FuncDefNode(position pos,String name,TypeNode type,BlockStmt block){
        super(pos);
        this.name=name;
        this.type=type;
        this.block=block;
    }
    @Override
    public void accept(ASTVisitor visitor){
        visitor.visit(this);
    }
}