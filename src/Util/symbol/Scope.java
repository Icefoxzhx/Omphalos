package Util.symbol;

import AST.TypeNode;
import Util.error.semanticError;
import Util.position;
import java.util.HashMap;

public class Scope{
    public HashMap<String,VarSymbol>varMap=new HashMap<>();
    public HashMap<String, FuncSymbol>funcMap=new HashMap<>();
    public HashMap<String,Type> typeMap=new HashMap<>();
    public Scope parentScope;

    public Scope(Scope parentScope){
        this.parentScope=parentScope;
    }
    public void defineType(String name, Type val, position pos){
        if(typeMap.containsKey(name)) throw new semanticError("class redefine",pos);//maybe wrong a little
        typeMap.put(name,val);
    }
    public boolean containsType(String name,boolean lookUpon,position pos){
        if(typeMap.containsKey(name)) return true;
        if(parentScope!=null&&lookUpon) return parentScope.containsType(name,true,pos);
        return false;
    }
    public Type getType(String name,boolean lookUpon,position pos){
        if(typeMap.containsKey(name)) return typeMap.get(name);
        if(parentScope!=null&&lookUpon) return parentScope.getType(name,true,pos);
        throw new semanticError("type node defined", pos);
    }
    public Type getType(TypeNode it){
        if(it.dim==0) return typeMap.get(it.Type);
        return new ArrayType(typeMap.get(it.Type),it.dim);

    }
    public void defineVariable(String name, VarSymbol Val, position pos){
        if(this.containsType(name,true,pos)) throw new semanticError("duplicated with type name",pos);
        if(varMap.containsKey(name)) throw new semanticError("variable redefine", pos);
        varMap.put(name,Val);
    }
    public VarSymbol getVariable(String name,boolean lookUpon,position pos){
        if(varMap.containsKey(name)) return varMap.get(name);
        if(parentScope!=null&&lookUpon) return parentScope.getVariable(name,true,pos);
        throw new semanticError("variable not defined", pos);
    }
    public void defineFunction(String name, FuncSymbol Val, position pos){
        if(this.containsType(name,true,pos)) throw new semanticError("duplicated with type name",pos);
        if(funcMap.containsKey(name)) throw new semanticError("function redefine",pos);
        funcMap.put(name,Val);
    }
    public FuncSymbol getFunction(String name,boolean lookUpon,position pos){
        if(funcMap.containsKey(name)) return funcMap.get(name);
        if(parentScope!=null&&lookUpon) return parentScope.getFunction(name,true,pos);
        throw new semanticError("function not defined", pos);
    }
}