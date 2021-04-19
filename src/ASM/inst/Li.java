package ASM.inst;

import ASM.operand.*;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;

public class Li extends Inst{
    public String op;
    public Register rd;
    public Operand imm;
    public Li(Register rd,Operand imm,String op){
        this.rd=rd;
        this.imm=imm;
        this.op=op;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>();
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(Collections.singletonList(rd));
    }

    @Override
    public void replaceUse(Register x, Register y) {

    }

    @Override
    public void replaceDef(Register x, Register y) {
        if(rd==x) rd=y;
    }

    @Override
    public String toString() {
        return op+" "+rd+", "+imm.toString();
    }
    /*
    @Override
    public void printASM(PrintStream prt) {
        if((rd instanceof VReg || rd instanceof Symbol) && rd.color==null){
           rd.color=new PReg("t5");
        }
        if(imm instanceof Symbol){
            prt.println("\tla "+rd.toString()+", "+imm.toString());
        }else {
            prt.println("\tli "+rd.toString()+", "+imm.toString());
        }
        if(rd instanceof Address){
            prt.println("\tlw t6," + printVReg(-(((VReg) rd).id + 1) * 4));
            prt.println("\tsw " + rd.toString()+",  0(t6)");
        }else if(rd instanceof VReg){
            prt.println("\tsw " + rd.toString()+", " + printVReg(-(((VReg) rd).id + 1) * 4));
        }else if(rd instanceof Symbol){
            prt.println("\tsw " + rd.toString() + ", " + ((Symbol) rd).name + ", t6");
        }
        rd.color=imm.color=null;
    }

     */
}
