package Util.symbol;

import ASM.operand.Register;

import java.util.HashMap;

public class ClassType extends Type{
    public String name;
    public HashMap<String,VarSymbol> varMap=new HashMap<>();
    public HashMap<String,FuncSymbol> funcMap=new HashMap<>();
    public FuncSymbol constructor=null;
    public ClassType(String name){
        this.name=name;
    }
    @Override
    public boolean equals(Type t){
        return t.isNull()||((t instanceof ClassType)&&(this.name.equals(((ClassType)t).name)));
    }
    public int size(){
        return varMap.size()*4;
    }
}