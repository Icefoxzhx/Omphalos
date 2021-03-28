package IR.operand;

public class Symbol extends Operand{
    public String name;
    public Symbol(String name){
        this.name=name;
        this.isptr=false;
    }
    @Override
    public String toString() {
        if(color!=null) return color.name;
        return name;
    }
}
