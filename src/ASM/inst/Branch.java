package ASM.inst;

import ASM.Block;
import ASM.operand.*;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class Branch extends Inst{
    public String op;
    public Register rs1, rs2;
    public Block dest;

    public Branch(String op, Register rs1, Register rs2, Block dest) {
        this.op = op;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.dest = dest;
    }

    @Override
    public LinkedHashSet<Register> getUse() {
        LinkedHashSet<Register> res=new LinkedHashSet<>();
        res.add(rs1);
        if(rs2!=null) res.add(rs2);
        return res;
    }

    @Override
    public LinkedHashSet<Register> getDef() {
        return new LinkedHashSet<>();
    }

    @Override
    public void replaceUse(Register x, Register y) {
        if(rs1==x) rs1=y;
        if(rs2==x) rs2=y;
    }

    @Override
    public void replaceDef(Register x, Register y) {

    }

    @Override
    public String toString() {
        return op+" "+rs1.toString()+(rs2==null ? "" : ", "+rs2.toString() )+", "+dest.name;
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
        prt.println("\t"+op+" "+rs1.toString()+(rs2==null?"":", "+rs2.toString())+", ."+dest);
        rs1.color=null;
        if(rs2!=null) rs2.color=null;
    }*/
}
