package IR.inst;

import IR.operand.Operand;
import IR.operand.PReg;
import IR.operand.Symbol;
import IR.operand.VReg;

import java.io.PrintStream;

public class Li extends Inst{
    public Operand rd,imm;
    public Li(Operand rd,Operand imm){
        this.rd=rd;
        this.imm=imm;
    }

    @Override
    public void printASM(PrintStream prt) {
        if(rd instanceof VReg){
           rd.color=new PReg("t5");
        }
        if(imm instanceof Symbol){
            prt.println("\tla "+rd.toString()+", "+imm.toString());
        }else {
            prt.println("\tli "+rd.toString()+", "+imm.toString());
        }
        if(rd instanceof VReg){
            prt.println("\tsw t5, " + -(((VReg) rd).id + 1) * 4 + "(s0)");
        }
        rd.color=imm.color=null;
    }
}
