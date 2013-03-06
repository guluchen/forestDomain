package TreeAutomaton;

import java.util.HashSet;

import Util.SortedList;

public class Term {
	Label label;
	States bottom;
	
	public Term(){
		label=new Label();
		bottom=new States();
	}
	public Term(Label label, States bottom){
		this.label=label;
		this.bottom=bottom;
	}
	public Label getLabel(){
		return label;
	}
	public void setLabel(Label label){
		this.label=label;
	}
	public States getStates(){
		return bottom;
	}
	public void setStates(States bottom){
		this.bottom=bottom;
	}
	public HashSet<SubTerm> getSubTerms() throws Exception{
		HashSet<SubTerm> ret=new HashSet<SubTerm>();
		int startLoc=0;
		for(int i=0;i<label.size();i++){
			int sublabel=label.get(i);
			int sublabelrank=label.getRank(sublabel);
			States states=new States();
			for(int j=startLoc;j<(startLoc+sublabelrank);j++){
				states.add(bottom.get(j));
			}
			ret.add(new SubTerm(sublabel,states));
			startLoc+=sublabelrank;
		}
		return ret;
	}
    public int hashCode() {
    	int hashFirst = bottom != null ? bottom.hashCode() : 0;
    	int hashSecond = label != null ? label.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond;
    }
	
	public boolean equals(Object obj) {
        if (obj == null) return false;
        else if (!(obj instanceof Term)) return false;
        else {
        	Term o=(Term)obj;
        	return this.bottom.equals(o.bottom) && this.label.equals(o.label);
        }
    }    
}
