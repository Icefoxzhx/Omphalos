package Util.symbol;

import ASM.operand.Register;

import java.util.LinkedHashMap;

public class ClassType extends Type{
    public String name;
    public LinkedHashMap<String,VarSymbol> varMap=new LinkedHashMap<>();
    public LinkedHashMap<String,FuncSymbol> funcMap=new LinkedHashMap<>();
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