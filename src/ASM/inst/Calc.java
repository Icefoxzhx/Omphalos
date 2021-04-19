package ASM.inst;

import ASM.operand.*;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;

public class Calc extends Inst{
    public Register rd,rs1;
    public Operand rs2;
    public String op;
    public Calc(String op,Register rd,Register rs1,Operand rs2){
        this.op=op;
        this.rd=rd;
        this.rs1=rs1;
        this.rs2=rs2;
    }

    @Override
    public HashSet<Register> getUse() {
        HashSet<Register> res=new HashSet<>();
        res.add(rs1);
        if(rs2 instanceof Register) res.add((Register) rs2);
        return res;
    }

    @Override
    public HashSet<Register> getDef() {
        return new HashSet<>(Collections.singletonList(rd));
    }

    @Override
    public void replaceUse(Register x, Register y) {
        if(rs1==x) rs1=y;
        if(rs2==x) rs2=y;
    }

    @Override
    public void replaceDef(Register x, Register y) {
        if(rd==x) rd=y;
    }


    @Override
    public String toString() {
        return op+" "+rd.toString()+", "+rs1.toString()+ (rs2==null ? "" : ", "+rs2.toString());
    }
/*
    @Override
    public void printASM(PrintStream prt) {
        if(rs1 instanceof VReg && rs1.color==null){
            prt.println("\tlw t3, " + printVReg(-(((VReg) rs1).id + 1) * 4));
            rs1.color=new PReg("t3");
        }
        if(rs1 instanceof Symbol && rs1.color==null){
            prt.println("\tlw t3, " + rs1.toString());
            rs1.color=new PReg("t3");
        }
        if(rs1 instanceof Address){
            prt.println("\tlw "+rs1.toString()+", 0("+rs1.toString()+")");
        }
        if(rs2 instanceof VReg && rs2.color==null){
            prt.println("\tlw t4, " + printVReg(-(((VReg) rs2).id + 1) * 4));
            rs2.color=new PReg("t4");
        }
        if(rs2 instanceof Symbol && rs2.color==null){
            prt.println("\tlw t4, " + rs2.toString());
            rs2.color=new PReg("t4");
        }
        if(rs2 instanceof Address){
            prt.println("\tlw "+rs2.toString()+", 0("+rs2.toString()+")");
        }
        if((rd instanceof VReg|| rd instanceof Symbol) && rd.color==null){
            rd.color=new PReg("t5");
        }
        prt.println("\t"+op+" "+rd.toString()+", "+rs1.toString()+(rs2==null?"":", "+rs2.toString()));
        if(rd instanceof Address){
            prt.println("\tlw t6," + printVReg(-(((VReg) rd).id + 1) * 4));
            prt.println("\tsw " + rd.toString()+",  0(t6)");
        }else if(rd instanceof VReg){
            prt.println("\tsw " + rd.toString() + ", " + printVReg(-(((VReg) rd).id + 1) * 4));
        }else if(rd instanceof Symbol){
            prt.println("\tsw " + rd.toString() + ", " + ((Symbol) rd).name + ", t6");
        }
        rd.color=null;
        rs1.color=null;
        if(rs2!=null) rs2.color=null;
    }
*/
}
