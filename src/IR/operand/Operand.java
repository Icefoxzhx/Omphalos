package IR.operand;

public abstract class Operand {
    public PReg color=null;
    public abstract String toString();
    public boolean isptr=false;
}
