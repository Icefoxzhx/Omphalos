package IR.inst;

import IR.operand.Operand;
import IR.operand.PReg;
import IR.operand.Symbol;
import IR.operand.VReg;

import java.io.PrintStream;

public class Branch extends Inst{
    public String op;
    public Operand rs1, rs2;
    public int dest;

    public Branch(String op, Operand rs1, Operand rs2, int dest) {
        this.op = op;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.dest = dest;
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
        if(rs1.isptr){
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
        if(rs2!=null&&rs2.isptr){
            prt.println("\tlw "+rs2.toString()+", 0("+rs2.toString()+")");
        }
        prt.println("\t"+op+" "+rs1.toString()+(rs2==null?"":", "+rs2.toString())+", "+".L"+dest);
        rs1.color=null;
        if(rs2!=null) rs2.color=null;
    }
}
