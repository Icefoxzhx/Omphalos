package Util.symbol;

import IR.Function;

import java.util.ArrayList;

public class FuncSymbol extends Type{
    public Type returnType=null;
    public String name;
    public String abs_name;
    public boolean inClass=false;
    public Function func;
    public ArrayList<VarSymbol> paramList=new ArrayList<>();
    public FuncSymbol(String name){
        this.name=name;
    }
}