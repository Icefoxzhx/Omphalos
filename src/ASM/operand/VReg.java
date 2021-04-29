package ASM.operand;

public class VReg extends Register{
    public String name;
    public VReg(String name){
        this.name=name;
    }
    @Override
    public String toString() {
        return color==null? name : color.toString();
    }
}
