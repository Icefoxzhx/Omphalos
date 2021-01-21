package Util.symbol;

public class PrimitiveType extends Type{
    public String name;
    public PrimitiveType(String name){
        this.name=name;
    }
    @Override
    public boolean equals(Type t){
        return (this.isNull()&&(t instanceof  ArrayType||t instanceof ClassType)) || ((t instanceof PrimitiveType)&&(this.name.equals( ((PrimitiveType)t).name)));
    }
    public boolean isBool(){
        return name.equals("bool");
    }
    public boolean isInt(){
        return name.equals("int");
    }
    public boolean isString(){
        return name.equals("string");
    }
    public boolean isVoid(){
        return name.equals("void");
    }
    public boolean isNull(){
        return name.equals("null");
    }
}