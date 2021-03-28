package IR.operand;

public class Imm extends Operand{
    public int val;
    public Imm(int val){
        this.val=val;
        this.isptr=false;
    }

    @Override
    public String toString() {
        return val+"";
    }
}
