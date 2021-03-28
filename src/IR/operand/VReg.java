package IR.operand;

public class VReg extends Register{
    public int id;
    public VReg(int id){
        this.id=id;
        this.isptr=false;
    }
    @Override
    public String toString() {
        return color.name;
    }
}
