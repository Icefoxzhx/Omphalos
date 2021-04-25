package IR.operand;

public class ConstInt extends Operand{
    public int val;
    public ConstInt(int val){
        this.val=val;
    }

    @Override
    public String toString() {
        return val+"";
    }

    @Override
    public boolean equals(Operand x) {
        return (x instanceof ConstInt && ((ConstInt) x).val==val);
    }

}
