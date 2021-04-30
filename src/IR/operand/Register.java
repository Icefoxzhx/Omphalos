package IR.operand;

import IR.inst.Inst;

import java.util.ArrayList;
import java.util.Stack;

public class Register extends Operand{
    public String name;

    public ArrayList<Inst> assign=new ArrayList<>();
    public Stack<Register> rename_stack=new Stack<>();
    public int rename_id=0;
    public Register(String name){
        this.name=name;
    }


    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Operand x) {
        return this==x;
    }
}
