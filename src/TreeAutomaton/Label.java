package TreeAutomaton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import Util.SortedList;

public class Label implements Iterable<Integer>{
	SortedList<Integer> label;
	HashMap<Integer,Integer> rank;
	
	public Label(){
		label=new SortedList<Integer>();
		rank=new  HashMap<Integer,Integer>();
	}
	public Label(SortedList<Integer> label, HashMap<Integer,Integer> rank){
		this.label=label;
		this.rank=rank;
	}
	public Label(Label c) {
		this.label=new SortedList<Integer>(c.label);
		this.rank=new HashMap<Integer,Integer>(c.rank);
	}
	public int get(int index){
		return label.get(index);
	}
	public void add(Integer sublabel, int rank){
		label.add(sublabel);
		this.rank.put(sublabel, rank);
	}
	public void addAll(Collection<Integer> c, HashMap<Integer,Integer> rank) {
		label.addAll(c);
		this.rank.putAll(rank);
	}
	public int getRank(int sublabel) throws Exception{
		if(!rank.containsKey(sublabel))
			throw new Exception("The rank of the sublabel "+sublabel+" is not defined");
		return rank.get(sublabel);
	}
	public void addAll(Label c) {
		label.addAll(c.label);
		rank.putAll(c.rank);
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
