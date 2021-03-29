package IR;

import IR.inst.Inst;

import java.io.PrintStream;
import java.util.ArrayList;

public class IRBlock{
    public ArrayList<Inst> insts=new ArrayList<>();
    public String name;
    public int Vregnum=1;
    public IRBlock(String name){
        this.name=name;
    }

    public void printASM(PrintStream prt) {
        int SMEM=(Vregnum+1)*4;

        prt.println();
        prt.println("\t.globl\t"+name);
        prt.println("\t.type\t"+name+", @function");
        prt.println(name + ":");

        System.out.println("\taddi\tsp,sp,-" + String.valueOf(SMEM));
        System.out.println("\tsw\ts0," + String.valueOf(SMEM-4) + "(sp)");
        System.out.println("\tsw\tra," + String.valueOf(SMEM- 8) + "(sp)");
        System.out.println("\taddi\ts0,sp," + String.valueOf(SMEM));

        insts.forEach(inst->inst.printASM(prt));

        System.out.println(".Returnof"+name+":");
        System.out.println("\tlw\ts0,"+(SMEM-4)+"(sp)");
        System.out.println("\tlw\tra," + String.valueOf(SMEM- 8) + "(sp)");
        System.out.println("\taddi\tsp,sp," + String.valueOf(SMEM));
        System.out.println("\tjr\tra");

        prt.println("\t.size\t"+name+ ", .-"+name);
    }
}