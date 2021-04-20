package ASM.inst;

import ASM.operand.Imm;
import ASM.operand.Register;
import ASM.operand.Symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class Sw extends Inst{
    public Register rd,rt;
    public Symbol symbol;
    public Sw(Register rd, Register rt, Symbol symbol){
        this.rd=rd;
        this.rt=rt;
        this.symbol=symbol;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>(Collections.singletonList(rd));
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(Collections.singletonList(rt));
    }

    @Override
    public void replaceUse(Register x, Register y) {
        if(rd==x) rd=y;
        if(rt==x) rt=y;
    }

    @Override
    public void replaceDef(Register x, Register y) {
        if(rt==x) rt=y;
    }

    @Override
    public String toString() {
        return "sw " + rd.toString() + ", " + symbol.toString() + ", "+rt.toString();
    }
}
