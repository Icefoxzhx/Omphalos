package Util.symbol;

public abstract class Type{
    public boolean equals(Type t){
        return false;
    }
    public boolean isBool(){
        return false;
    }
    public boolean isInt(){
        return false;
    }
    public boolean isString(){
        return false;
    }
    public boolean isVoid(){
        return false;
    }
    public boolean isNull(){
        return false;
    }
}