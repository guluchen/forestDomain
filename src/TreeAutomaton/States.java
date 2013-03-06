package TreeAutomaton;

import java.util.ArrayList;
import java.util.Iterator;

public class States implements Iterable<Integer>{
	ArrayList<Integer> states;
	
	public States(){
		states=new ArrayList<Integer>();
	}
	public States(ArrayList<Integer> states){
		this.states=states;
	}
	public States(States s) {
		states=new ArrayList<Integer>(s.states);
	}
	public int get(int index){
		return states.get(index);
	}
	public void add(Integer state){
		states.add(state);
	}
	public void add(int index, int s) {
		states.add(index,s);
	}
	public void remove(Integer state){
		states.remove(state);
	}
	public void remove(int index){
		states.remove(index);
	}

	public void addAll(States bottom) {
		states.addAll(bottom.states);
	}
	
	public void set(int index, Integer state){
		states.set(index, state);
	}
    public int hashCode() {
    	return states.hashCode();
    }
	public int size() {
		return states.size();
	}
	public boolean contains(Integer s) {
		return states.contains(s);
	}
	@Override
	public Iterator<Integer> iterator() {
		return states.iterator();
	}
    public String toString()
    { 
           return states.toString(); 
    }	
	public boolean equals(Object obj) {
        if (obj == null) return false;
        else if (!(obj instanceof States)) return false;
        else {
        	States o=(States)obj;
        	return this.states.equals(o.states);
        }
    }    
}
