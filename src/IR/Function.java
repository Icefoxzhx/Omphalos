package IR;

import IR.operand.Operand;
import IR.operand.Register;

import java.util.ArrayList;
import java.util.HashSet;

public class Function {
    public String name;
    public Block beginBlock=null,endBlock=null;
    public int StackSpace=0;
    public ArrayList<Block> blocks=new ArrayList<>();
    public ArrayList<Register> calleeSaveReg=new ArrayList<>();
    public ArrayList<Operand> params=new ArrayList<>();
    public Register raSaveReg;
    public ArrayList<Block> returnBlocks=new ArrayList<>();
    public HashSet<Register> vars=new HashSet<>();
    public Function(String name){
        this.name=name;
    }
    public String toString(){
        return name;
    }

}