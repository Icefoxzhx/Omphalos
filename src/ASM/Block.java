package ASM;

import ASM.inst.Inst;

import java.util.ArrayList;

public class Block {
    public String name;
    public ArrayList<Inst> insts=new ArrayList<>();
    public ArrayList<Block> pred=new ArrayList<>();
    public ArrayList<Block> succ=new ArrayList<>();
    public int loopDepth;
    public boolean terminated=false;

    public Block(int loopDepth, String name){
        this.loopDepth=loopDepth;
        this.name="."+name;
    }
}
