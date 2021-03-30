package IR.inst;

import IR.operand.*;

import java.io.PrintStream;

public class Li extends Inst{
    public Operand rd,imm;
    public Li(Operand rd,Operand imm){
        this.rd=rd;
        this.imm=imm;
    }

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
}
