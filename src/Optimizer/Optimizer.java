package Optimizer;

import IR.Root;

public class Optimizer {
    public Root root;

    public Optimizer(Root root){
        this.root=root;
    }

    public void run(){
        for(int i=0;i<5;++i){
            new Simplify(root).run();
            new ADCE(root).run();
        }
        new Simplify(root).run();
    }
}
