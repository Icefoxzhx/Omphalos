package IR;

import java.io.PrintStream;
import java.util.ArrayList;

public class IRBlockList{
    public ArrayList<IRBlock> blocks=new ArrayList<>();
    public ArrayList<String> strings=new ArrayList<>();
    public ArrayList<String> globals=new ArrayList<>();
    public IRBlockList(){}

    public void printASM(PrintStream prt){

        prt.println("\t.section\t.rodata");
        for(int i=0;i<strings.size();++i){
            prt.println(".LS"+ i+":");
            prt.println("\t.string\t"+strings.get(i));
            prt.println();
        }
        prt.println("\t.section\t.bss");
        for(String name:globals){
            prt.println("\t.globl\t"+name);
            prt.println("\t.type\t"+name+", @object");
            prt.println(name + ":");
            prt.println("\t.zero\t4");
            prt.println("\t.size\t"+name+", 4");
            prt.println();
        }
        prt.println("\t.text");
        blocks.forEach(x->x.printASM(prt));
    }
}