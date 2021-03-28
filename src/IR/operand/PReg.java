package IR.operand;

public class PReg extends Register{
    public String name;

    public PReg(String name){
        this.name=name;
        this.isptr=false;
    }

    @Override
    public String toString() {
        return name;
    }
}
