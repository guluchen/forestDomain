package TreeAutomaton;

import java.util.Collection;
import java.util.Iterator;

import Util.SortedList;

public class Label implements Iterable<Integer>{
	SortedList<Integer> label;
	
	public Label(){
		label=new SortedList<Integer>();
	}
	public Label(SortedList<Integer> label){
		this.label=label;
	}
	public Label(Label c) {
		this.label=c.label;
	}
	public int get(int index){
		return label.get(index);
	}
	public void add(Integer sublabel){
		label.add(sublabel);
	}
	public void addAll(Collection<Integer> c) {
		label.addAll(c);
	}

	public void addAll(Label c) {
		label.addAll(c.label);
	}
	public void remove(Integer sublabel){
		label.remove(sublabel);
	}
    public int hashCode() {
    	return label.hashCode();
    }
	public int indexOf(int sublabel) {
		return label.indexOf(sublabel);
	}
	public boolean equals(Object obj) {
        if (obj == null) return false;
        else if (!(obj instanceof Label)) return false;
        else if(this.size()!=((Label) obj).size()) return false;
        for(int i=0;i<this.size();i++){
        	if(this.get(i)!=((Label) obj).get(i)) return false;
        }
        return true;		
	}
	@Override
	public Iterator<Integer> iterator() {
		return label.iterator();
	}
	public int size() {
		return label.size();
	}
	public boolean contains(int sublabel) {
		return label.contains(sublabel);
	}

	
}
