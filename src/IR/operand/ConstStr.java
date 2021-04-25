package IR.operand;

public class ConstStr extends Operand{
    public String name;
    public String val;
    public ConstStr(String name,String val){
        this.name=name;
        this.val=val;
    }
    @Override
    public String toString() {
        return val;
    }

    @Override
    public boolean equals(Operand x) {
        return (x instanceof ConstStr && ((ConstStr) x).val.equals(val));
    }
}
