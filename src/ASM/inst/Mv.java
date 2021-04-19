package ASM.inst;

import ASM.operand.*;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;

public class Mv extends Inst{
    public Register rd,rs;
    public Mv(Register rd, Register rs){
        this.rd=rd;
        this.rs=rs;
    }

    @Override
    public HashSet<Register> getUse() {
        return new HashSet<>(Collections.singletonList(rs));
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(Collections.singletonList(rd));
    }

    @Override
    public void replaceUse(Register x, Register y) {
        if(rs==x) rs=y;
    }

    @Override
    public void replaceDef(Register x, Register y) {
        if(rd==x) rd=y;
    }

    @Override
    public String toString() {
        return "mv "+rd.toString()+", "+rs.toString();
    }
    /*
    @Override
    public void printASM(PrintStream prt) {
        if(rs instanceof VReg && rs.color==null){
            prt.println("\tlw t3, " + printVReg(-(((VReg) rs).id + 1) * 4));
            rs.color=new PReg("t3");
        }
        if(rs instanceof Symbol && rs.color==null){
            prt.println("\tlw t3, " + rs.toString());
            rs.color=new PReg("t3");
        }
        if(rs instanceof Address){
            prt.println("\tlw "+rs.toString()+", 0("+rs.toString()+")");
        }
        if((rd instanceof VReg || rd instanceof Symbol) && rd.color==null ){
            rd.color=new PReg("t5");
        }

        prt.println("\tmv "+rd.toString()+","+rs.toString());

        if(rd instanceof Address){
            prt.println("\tlw t6," + printVReg(-(((VReg) rd).id + 1) * 4));
            prt.println("\tsw " + rd.toString()+",  0(t6)");
        }else if(rd instanceof VReg){
            prt.println("\tsw " + rd.toString()+", " + printVReg(-(((VReg) rd).id + 1) * 4));
        }else if(rd instanceof Symbol){
            prt.println("\tsw " + rd.toString() + ", " + ((Symbol) rd).name + ", t6");
        }
        rd.color=null;
        rs.color=null;
    }

     */
}
