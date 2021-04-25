package IR.operand;

public class Register extends Operand{
    public String name;

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
