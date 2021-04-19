package Util.symbol;

import ASM.operand.Operand;

public class VarSymbol{
    public String name;
    public Type type;
    public Operand operand;
    public boolean isGlobal=false,isClassMember=false;
    public VarSymbol(String name){
        this.name=name;
    }
    public VarSymbol(String name,Type type){
        this.name=name;
        this.type=type;
    }
}