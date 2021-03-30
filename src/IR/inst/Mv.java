package IR.inst;

import IR.operand.*;

import java.io.PrintStream;

public class Mv extends Inst{
    public Operand rd,rs;
    public Mv(Operand rd,Operand rs){
        this.rd=rd;
        this.rs=rs;
    }

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
}
