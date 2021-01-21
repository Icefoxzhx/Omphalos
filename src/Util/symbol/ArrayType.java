package Util.symbol;

import java.util.ArrayList;

public class ArrayType extends Type{
    public Type basicType;
    public int dim;
    public ArrayType(Type basicType,int dim){
        this.basicType=basicType;
        this.dim=dim;
    }
    @Override
    public boolean equals(Type t) {
        return t.isNull()||((t instanceof ArrayType)&&(this.basicType.equals( ((ArrayType)t).basicType )&&this.dim==((ArrayType)t).dim));
    }
}