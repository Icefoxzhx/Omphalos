package ASM;

import ASM.operand.Register;

import java.util.ArrayList;

public class Function {
    public String name;
    public Block beginBlock=null,endBlock=null;
    public int StackSpace=0;
    public ArrayList<Register> params=new ArrayList<>();
    public ArrayList<Block> blocks=new ArrayList<>();
    public ArrayList<Register> calleeSaveReg=new ArrayList<>();
    public Register raSaveReg;
    public ArrayList<Block> returnBlocks=new ArrayList<>();
    public Function(String name){
        this.name=name;
    }
    public String toString(){
        return name;
    }


}