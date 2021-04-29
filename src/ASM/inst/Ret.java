package ASM.inst;

import ASM.Root;
import ASM.operand.Register;

import java.util.Collections;
import java.util.LinkedHashSet;

public class Ret extends Inst{
    public Root root;

    public Ret(Root root){
        this.root=root;
    }
    @Override
    public LinkedHashSet<Register> getUse() {
        return new LinkedHashSet<>(Collections.singletonList(root.getPReg(1)));
    }

    @Override
    public LinkedHashSet<Register> getDef() {
        return new LinkedHashSet<>();
    }

    @Override
    public void replaceUse(Register x, Register y) {

    }

    @Override
    public void replaceDef(Register x, Register y) {

    }

    @Override
    public String toString() {
        return "ret";
    }
}
