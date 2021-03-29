package IR.inst;

import IR.operand.*;

import java.io.PrintStream;

public class Calc extends Inst{
    public Operand rs1,rs2,rd;
    public String op;
    public Calc(String op,Operand rd,Operand rs1,Operand rs2){
        this.op=op;
        this.rd=rd;
        this.rs1=rs1;
        this.rs2=rs2;
    }

    @Override
    public String toString() {
        return op+" "+rd.toString()+", "+rs1.toString()+", "+rs2.toString();
    }

    @Override
    public void printASM(PrintStream prt) {
        if(rs1 instanceof VReg && rs1.color==null){
            prt.println("\tlw t3, " + -(((VReg) rs1).id + 1) * 4 + "(s0)");
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
            prt.println("\tlw t4, " + -(((VReg) rs2).id + 1) * 4 + "(s0)");
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
            prt.println("\tlw t6," + -(((VReg) rd).id + 1) * 4 + "(s0)");
            prt.println("\tsw " + rd.toString()+",  0(t6)");
        }else if(rd instanceof VReg){
            prt.println("\tsw " + rd.toString() + ", " + -(((VReg) rd).id + 1) * 4 + "(s0)");
        }else if(rd instanceof Symbol){
            prt.println("\tsw " + rd.toString() + ", " + ((Symbol) rd).name + ", t6");
        }
        rd.color=null;
        rs1.color=null;
        if(rs2!=null) rs2.color=null;
    }
}
