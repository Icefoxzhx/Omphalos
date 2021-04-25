package IR.operand;

public abstract class Operand {
    public boolean isptr=false;
    public abstract String toString();
    public abstract boolean equals(Operand x);
}
