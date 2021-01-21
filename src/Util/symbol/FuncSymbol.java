package Util.symbol;

import java.util.ArrayList;

public class FuncSymbol extends Type{
    public Type returnType=null;
    public String name;
    public ArrayList<VarSymbol> paramList=new ArrayList<>();
    public FuncSymbol(String name){
        this.name=name;
    }
}