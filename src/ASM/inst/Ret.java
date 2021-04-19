package ASM.inst;

import ASM.Root;
import ASM.operand.Register;

import java.util.Collections;
import java.util.HashSet;

public class Ret extends Inst{
    public Root root;

    public Ret(Root root){
        this.root=root;
    }
    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>(Collections.singletonList(root.getPReg(1)));
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>();
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
