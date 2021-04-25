package IR.operand;

public class Symbol extends Operand{
    public String name;
    public Symbol(String name){
        this.name=name;
        this.isptr=true;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Operand x) {
        return (x instanceof Symbol && ((Symbol) x).name.equals(name));
    }
}
