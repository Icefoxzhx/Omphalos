package BackEnd;

import ASM.Block;
import ASM.Function;
import ASM.Root;

public class ASMPrinter {
    public Root root;

    public ASMPrinter(Root root){
        this.root=root;
    }

    public void prtBlock(Block block){
        System.out.println(block.name + ":");
        block.insts.forEach(inst -> System.out.println("\t" + inst.toString()));
    }
    public void prtFunc(Function func){
        System.out.println();
        System.out.println("\t.globl\t" + func.name);
        System.out.println("\t.type\t" + func.name + ", @function");
        System.out.println(func.name + ":");
        func.blocks.forEach(this::prtBlock);
        System.out.println("\t.size\t" + func.name + ", .-" + func.name);

    }
    public void run(){
        System.out.println("\t.section\t.rodata");
        root.strings.forEach((key,val)->{
            System.out.println(val.name+":");
            System.out.println("\t.string\t" + val.val);
            System.out.println();
        });
        System.out.println("\t.section\t.bss");
        for(String name : root.globals){
            System.out.println("\t.globl\t"+name);
            System.out.println("\t.type\t"+name+", @object");
            System.out.println(name + ":");
            System.out.println("\t.zero\t4");
            System.out.println("\t.size\t"+name+", 4");
            System.out.println();
        }
        System.out.println("\t.text");
        root.func.forEach(this::prtFunc);
    }
}