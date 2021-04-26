package BackEnd;

import IR.Block;
import IR.Function;
import IR.Root;

public class IRPrinter {
    public Root root;

    public IRPrinter(Root root){
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
        root.func.forEach(this::prtFunc);
    }
}
