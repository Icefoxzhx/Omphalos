package ASM;

import ASM.operand.PReg;
import IR.operand.ConstStr;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Root {
    public ArrayList<Function> func=new ArrayList<>();
    public LinkedHashMap<String, ConstStr> strings=new LinkedHashMap<>();
    public ArrayList<String> globals=new ArrayList<>();
    public LinkedHashMap<Integer,PReg> PRegMap=new LinkedHashMap<>();
    public String []regname=new String[]{"zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"};

    public Root(){
        for(int i=0;i<32;++i){
            PReg a=new PReg(regname[i]);
            PRegMap.put(i,a);
        }
    }

    public PReg getPReg(int id){
        return PRegMap.get(id);
    }

    public ArrayList<PReg> getCallerSave(){
        ArrayList<PReg> res=new ArrayList<>();
        res.add(getPReg(1));
        for(int i=5;i<=7;++i) res.add(getPReg(i));
        for(int i=10;i<=17;++i) res.add(getPReg(i));
        for(int i=28;i<=31;++i) res.add(getPReg(i));
        return res;
    }
    public ArrayList<PReg> getCalleeSave(){
        ArrayList<PReg> res=new ArrayList<>();
        for(int i=8;i<=9;++i) res.add(getPReg(i));
        for(int i=18;i<=27;++i) res.add(getPReg(i));
        return res;
    }

    public ArrayList<PReg> getColors(){
        ArrayList<PReg> res=new ArrayList<>();
        for(int i=5;i<=7;++i) res.add(getPReg(i));
        for(int i=10;i<=17;++i) res.add(getPReg(i));
        for(int i=28;i<=31;++i) res.add(getPReg(i));
        for(int i=8;i<=9;++i) res.add(getPReg(i));
        for(int i=18;i<=27;++i) res.add(getPReg(i));
        res.add(getPReg(1));
        return res;
    }

    public ArrayList<PReg> getPRegs(){
        ArrayList<PReg> res=new ArrayList<>();
        for(int i=0;i<32;++i) res.add(getPReg(i));
        return res;
    }
}