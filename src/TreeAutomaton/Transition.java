package TreeAutomaton;

import java.util.ArrayList;

import Util.SortedList;

public class Transition {
    private ArrayList<Integer> LHS;
    private SortedList<Integer> label;
    private int RHS;

    public Transition(ArrayList<Integer> LHS, SortedList<Integer> label, int RHS) {
    	this.LHS = LHS;
    	this.label = label;
    	this.RHS = RHS;
    }
    public Transition(Transition c){
    	this.LHS = c.LHS;
    	this.label = c.label;
    	this.RHS = c.RHS;
    }
    
    public int hashCode() {
    	int hashFirst = LHS != null ? LHS.hashCode() : 0;
    	int hashSecond = label != null ? label.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond + RHS;
    }


    public String toString()
    { 
           return "(" + LHS + ", " + label + ", " + RHS + ")"; 
    }

    public ArrayList<Integer> getLHS() {
    	return LHS;
    }

    public void setLHS(ArrayList<Integer> LHS) {
    	this.LHS = LHS;
    }

    public SortedList<Integer> getLabel() {
    	return label;
    }

    public void setLabel(SortedList<Integer> label) {
    	this.label = label;
    }
    
    public int getRHS() {
    	return RHS;
    }

    public void setRHS(int RHS) {
    	this.RHS = RHS;
    }
    
}