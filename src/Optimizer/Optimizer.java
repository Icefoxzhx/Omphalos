package Optimizer;

import IR.Root;

public class Optimizer {
    public Root root;

    public Optimizer(Root root){
        this.root=root;
    }

    public void run(){
        new Simplify(root).run();
        for(int i=0;i<10;++i){
            new SCCP(root).run();
            new Simplify(root).run();
            new CSE(root).run();
            new Simplify(root).run();
            new ADCE(root).run();
            new Simplify(root).run();
            new Inline(root,i).run();
            new Simplify(root).run();
            new LICM(root).run();
            new Simplify(root).run();
        }
    }
}
