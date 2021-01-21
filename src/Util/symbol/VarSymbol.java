package Util.symbol;

public class VarSymbol{
    public String name;
    public Type type;
    public VarSymbol(String name){
        this.name=name;
    }
    public VarSymbol(String name,Type type){
        this.name=name;
        this.type=type;
    }
}