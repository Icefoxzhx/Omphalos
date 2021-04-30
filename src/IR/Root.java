package IR;

import IR.operand.ConstStr;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Root {
    public ArrayList<Function> func=new ArrayList<>();
    public LinkedHashMap<String, ConstStr> strings=new LinkedHashMap<>();
    public ArrayList<String> globals=new ArrayList<>();
    public Function mainFunc;
    public Root(){
    }


}