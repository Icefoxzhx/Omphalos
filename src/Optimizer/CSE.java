package Optimizer;

import IR.Block;
import IR.Root;

public class CSE {
    Root root;

    public CSE(Root root){
        this.root=root;
    }

    public boolean same(){
return false;
    }

    public void doBlock(Block block){

    }
    public void run(){
        root.func.forEach(func->func.blocks.forEach(this::doBlock));
    }
}
