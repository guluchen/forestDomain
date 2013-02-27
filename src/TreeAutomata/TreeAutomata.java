package TreeAutomata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import Util.ManyToMany;
import Util.Pair;
import Util.SortedList;

public class TreeAutomata{

	static private int fresh=0;	
	private HashMap<Integer,Integer> rank=new HashMap<Integer,Integer>();
	private ManyToMany<ArrayList<Integer>, Integer> trans=new ManyToMany<ArrayList<Integer>, Integer>();	
	private int finalSt;
	private HashSet<Integer> states=new HashSet<Integer>();
    
    //Constructors
    public TreeAutomata(){
    	rank=new HashMap<Integer,Integer>();
    	trans=new ManyToMany<ArrayList<Integer>, Integer>();	
        states=new HashSet<Integer>();
    }
    public TreeAutomata(TreeAutomata c){
    	rank=new HashMap<Integer,Integer>(c.rank);
    	trans=new ManyToMany<ArrayList<Integer>, Integer>(c.trans);	
        states=new HashSet<Integer>(c.states);
    }

    public TreeAutomata(TreeAutomata c, HashMap<Integer, Integer> stMapping) throws Exception {
    	rank=new HashMap<Integer,Integer>(c.rank);
    	trans=new ManyToMany<ArrayList<Integer>, Integer>();
    	for(int to:c.trans.rightKeySet()){
    		for(ArrayList<Integer> from_label:c.trans.leftSetFromRightKey(to)){
    			Pair<ArrayList<Integer>,SortedList<Integer>> from_label_pair=c.seprateFromLabel(from_label);
    			ArrayList<Integer> from=from_label_pair.getFirst();
    			SortedList<Integer> label=from_label_pair.getSecond();
    			
    			ArrayList<Integer> new_from=new ArrayList<Integer>();
    			for(int i=0;i<from.size();i++){
    				new_from.set(i, stMapping.get(from.get(i)));
    			}
    			trans.put(mergeFromLabel(new_from, label), stMapping.get(to));
    		}
    		
    	}
        states=new HashSet<Integer>();
        for(int state:c.getStates()){
        	states.add(stMapping.get(state));
        }
	}
	//alphabet label operations
    public void addSubLabel(int sublabel, int rank){
    	this.rank.put(sublabel,rank);
    }
    public void delSubLabel(int sublabel){
    	this.rank.remove(sublabel);
    	assert checkSubLabelNotExistsInAllTransitions(sublabel);
    }
    public Set<Integer> getSubLabels(){
    	return rank.keySet();
    }
    public int getSubLabelRank(int sublabel){
    	return rank.get(sublabel);
    }
    public HashSet<SortedList<Integer>> getLabels(){
    	HashSet<SortedList<Integer>> ret=new HashSet<SortedList<Integer>>();
    	for(int to:trans.rightKeySet()){
			for(ArrayList<Integer> from_label:trans.leftSetFromRightKey(to)){
	    		Pair<ArrayList<Integer>, SortedList<Integer>> from_label_pair=seprateFromLabel(from_label);
	        	SortedList<Integer> label=from_label_pair.getSecond();
	        	ret.add(label);
			}
		}
    	return ret;
    }
    public HashMap<Integer,Integer> getRankMapping(){
    	return rank;
    }
    
    //return the start location of the states correspond to the sublabel
    public int getStartLoc(SortedList<Integer> label, int sublabel){
    	int startLoc=0;
    	for(int i=0;i<label.indexOf(sublabel);i++){
        	startLoc+=rank.get(label.get(i));
        }
    	return startLoc;
    } 
    
    //state operations
    public void setFinal(int st){
    	finalSt=st;
    }
    public int getFinal(){
    	return finalSt;
    }
    public HashSet<Integer> getStates(){
    	return states;
    }
	public boolean isState(Integer to) {
		return states.contains(to);
	}

	public void swapNamesOfStates(int srcSt, int tgtSt) throws Exception{
		renameState(srcSt,0);
		renameState(tgtSt,srcSt);
		renameState(0,tgtSt);
	}	
	public boolean isReferenceTo(int to,int rootRef){
		SortedList<Integer> label=new SortedList<Integer>();
		label.add(-rootRef);
		if(this.getFrom(to, label).size()==0)
			return false;
		else
			return true;
	}
	
	//returns -1 if it is not a root reference
	public int referenceTo(int rootRef){
		
		for(Transition tran:getTransTo(rootRef)){
			if(tran.getLHS().size()!=0)
				return -1;
			else{
				SortedList<Integer> label=tran.getLabel();
				if(label.size()!=1)
					return -1;
				else if(label.iterator().next()>0)
					return -1;
				else 
					return -label.iterator().next();
			}
		}
		
		return -1;
	}
	
	public void renameState(int oriSt, int newSt) throws Exception{
		if(finalSt==oriSt) finalSt=newSt;
		if(states.contains(oriSt)){
			states.remove(oriSt);
			states.add(newSt);
		}
		HashSet<Pair<ArrayList<Integer>, Integer>> toAdd=new HashSet<Pair<ArrayList<Integer>, Integer>>();
		HashSet<Pair<ArrayList<Integer>, Integer>> toRemove=new HashSet<Pair<ArrayList<Integer>, Integer>>();
    	for(int to:trans.rightKeySet()){
			for(ArrayList<Integer> from_label:trans.leftSetFromRightKey(to)){
	    		Pair<ArrayList<Integer>, SortedList<Integer>> from_label_pair=seprateFromLabel(from_label);
	        	if(to==oriSt || from_label_pair.getFirst().contains(oriSt)){
	        		toRemove.add(new Pair<ArrayList<Integer>, Integer>(from_label,to));
	        		to=(to!=oriSt)?to:newSt;
	        		if(from_label_pair.getFirst().contains(oriSt)){
	        			ArrayList<Integer> new_from=new ArrayList<Integer>(from_label_pair.getFirst());
	        			for(int i=0;i<new_from.size();i++){
	        				if(new_from.get(i)==oriSt)
	        					new_from.set(i, newSt);
	        			}
		        		toAdd.add(new Pair<ArrayList<Integer>, Integer>(this.mergeFromLabel(new_from, from_label_pair.getSecond()),to));
	        		}
	        	}
			}
    	}
    	for(Pair<ArrayList<Integer>, Integer> cur:toRemove){
    		trans.removePair(cur.getFirst(), cur.getSecond());
    	}
    	for(Pair<ArrayList<Integer>, Integer> cur:toAdd){
    		trans.put(cur.getFirst(), cur.getSecond());
    	}
	}
    //transition operations
	public void addTrans(ArrayList<Integer> from, SortedList<Integer> label, int to) throws Exception{
    	states.addAll(from);
    	states.add(to);
    	trans.put(mergeFromLabel(from,label), to);
    }
	public void addTrans(Transition tran) throws Exception {
    	states.addAll(tran.getLHS());
    	states.add(tran.getRHS());
    	trans.put(mergeFromLabel(tran.getLHS(),tran.getLabel()), tran.getRHS());
	}

	public void delTrans(ArrayList<Integer> from, SortedList<Integer> label, int to) throws Exception{
    	trans.removePair(mergeFromLabel(from,label), to);
    }
    public void delTrans(Transition tran) throws Exception{
    	trans.removePair(mergeFromLabel(tran.getLHS(),tran.getLabel()), tran.getRHS());
    }    
    public HashSet<Transition> getTrans(){
    	HashSet<Transition> ret=new HashSet<Transition>();	
    	for(int to:trans.rightKeySet()){
			for(ArrayList<Integer> from_label:trans.leftSetFromRightKey(to)){
	    		Pair<ArrayList<Integer>, SortedList<Integer>> from_label_pair=seprateFromLabel(from_label);
	    		ArrayList<Integer> from=from_label_pair.getFirst();
	    		SortedList<Integer> label=from_label_pair.getSecond();
	    		ret.add(new Transition(from,label,to));
			}
    	}    	
    	return ret;
    }

    public HashSet<Transition> getTransFrom(int from_state){
    	HashSet<Transition> ret=new HashSet<Transition>();	
    	for(int to:trans.rightKeySet()){
			for(ArrayList<Integer> from_label:trans.leftSetFromRightKey(to)){
	    		Pair<ArrayList<Integer>, SortedList<Integer>> from_label_pair=seprateFromLabel(from_label);
	    		ArrayList<Integer> from=from_label_pair.getFirst();
	    		SortedList<Integer> label=from_label_pair.getSecond();
	    		if(from.contains(from_state))
	    			ret.add(new Transition(from,label,to));
			}
    	}    	
    	return ret;
    }

    public HashSet<Transition> getTransTo(int to_state){
    	HashSet<Transition> ret=new HashSet<Transition>();	
    	for(int to:trans.rightKeySet()){
    		if(to==to_state)
				for(ArrayList<Integer> from_label:trans.leftSetFromRightKey(to)){
		    		Pair<ArrayList<Integer>, SortedList<Integer>> from_label_pair=seprateFromLabel(from_label);
		    		ArrayList<Integer> from=from_label_pair.getFirst();
		    		SortedList<Integer> label=from_label_pair.getSecond();
	    			ret.add(new Transition(from,label,to));
				}
    	}    	
    	return ret;
    }        
    
    public HashSet<ArrayList<Integer>> getFrom(int to, SortedList<Integer> tgtLabel){
    	HashSet<ArrayList<Integer>> ret= new HashSet<ArrayList<Integer>>();
    	if(trans.leftSetFromRightKey(to)!=null)
	    	for(ArrayList<Integer> from_label:trans.leftSetFromRightKey(to)){
	    		Pair<ArrayList<Integer>, SortedList<Integer>> from_label_pair=seprateFromLabel(from_label);
	    		ArrayList<Integer> from=from_label_pair.getFirst();
	        	SortedList<Integer> label=from_label_pair.getSecond();
	        	if(label.equals(tgtLabel))
	    		ret.add(from);
	    	}
    	return ret;
    }
    public HashSet<Integer> getTo(ArrayList<Integer> from, SortedList<Integer> label) throws Exception{
    	HashSet<Integer> ret =new HashSet<Integer>();
    	ArrayList<Integer> from_label=mergeFromLabel(from,label);
		if(trans.rightSetFromLeftKey(from_label)!=null)
    		for(int to:trans.rightSetFromLeftKey(from_label)){
    			ret.add(to);
    		}
    	return ret;
    }
    
    public Transition removeSubTransition(Transition srcTran, 
    		int srcSubLabel) throws Exception{

    	ArrayList<Integer> from=new ArrayList<Integer>(srcTran.getRHS());
    	SortedList<Integer> label=new SortedList<Integer>(srcTran.getLabel());
    	int to=srcTran.getRHS();
    	
    	int startLoc=getStartLoc(label,srcSubLabel);
    	for(int i=startLoc;i<startLoc+rank.get(srcSubLabel);i++)
        	from.remove(startLoc);
       	label.remove(new Integer(srcSubLabel));
	
    	this.delTrans(srcTran);
    	this.addTrans(from,label,to);
    	return new Transition(from,label,to);
    }

    public Transition addSubTransition(Transition srcTran, 
    		HashMap<Integer,Integer> tgtRank, Transition tgtTran) throws Exception{

    	for(int tgtSublabel:tgtTran.getLabel()){
    		this.addSubLabel(tgtSublabel, tgtRank.get(tgtSublabel));
    	}
    	
    	ArrayList<Integer> from=new ArrayList<Integer>(srcTran.getRHS());
    	SortedList<Integer> label=new SortedList<Integer>(srcTran.getLabel());
    	int to=srcTran.getRHS();

    	label.addAll(tgtTran.getLabel());
    	int j=0;
    	for(int tgtSubLabel:tgtTran.getLabel()){
	    	int startLoc=getStartLoc(label,tgtSubLabel);
	    	for(int i=0;i<rank.get(tgtSubLabel);i++){
	        	from.add(startLoc+i,tgtTran.getLHS().get(j));
	        	j++;
	    	}
    	}
    	this.delTrans(srcTran);
    	this.addTrans(from,label,to);
    	return new Transition(from,label,to);
    }
    

    
    //automata operations
    public void union(TreeAutomata tgt){
    	if(this.finalSt!=tgt.finalSt){
    		System.out.println("Only union between automata with the same root is allowed");
    		System.exit(0);
    	}
    	rank.putAll(tgt.rank);
    	trans.putAll(tgt.trans);
    	states.addAll(tgt.states);
    }
    
    public void removeLoopToRoot() throws Exception{
    	int oriRoot=getFinal();
    	if(this.getTransFrom(oriRoot).size()==0)
    		return;
    	int newRoot=TreeAutomata.getNewNodeNumber();
    	
    	for(SortedList<Integer> label:this.getLabels())
    		for(ArrayList<Integer> from:this.getFrom(oriRoot, label)){
    			this.addTrans(from, label, newRoot);
    		}
    	swapNamesOfStates(oriRoot, newRoot);
    	setFinal(oriRoot);
    }
  

	static public int getNewNodeNumber(){
		return fresh++;
	}
	
	
	@Override
	public String toString() {
		String ret="Ops ";
		for(int i:rank.keySet()){
			ret+=("a"+i+":"+rank.get(i)+" ");
		}
		ret+="\n\nAutomaton \nStates ";
		for(int i:states){
			ret+=("q"+i+" ");
		}
		ret+="\nFinal States ";
		ret+=("q"+finalSt+" ");

		ret+="\nTransitions\n";
		for(Transition tran: this.getTrans()){
    		ArrayList<Integer> from=tran.getLHS();
    		SortedList<Integer> label=tran.getLabel();
    		int to=tran.getRHS();

			ret+=("("+from+") -"+ label +"-> q"+to+"\n");
		}
		return ret;
	}
	
	//used in an assertion only, can be deleted in the final version
    private boolean checkSubLabelNotExistsInAllTransitions(int sublabel){
    	for(Transition tran:this.getTrans()){
    		if(tran.getLabel().contains(sublabel))
    			return false;
    	}
    	return true;
    }
	
    private ArrayList<Integer> mergeFromLabel(ArrayList<Integer> from, SortedList<Integer> label) throws Exception{
    	ArrayList<Integer> from_label=new ArrayList<Integer>();
    	int fromIndex=0;
    	for(int sublabel:label){
			if(!rank.containsKey(sublabel)){
				throw new Exception("Insert a new transition with unknown sublabel "+sublabel);
			}
	    	from_label.add(sublabel);
	    	from_label.addAll(from.subList(fromIndex, fromIndex+rank.get(sublabel)));
	    	fromIndex+=rank.get(sublabel);
		}
    	return from_label;
    }
    
    private Pair<ArrayList<Integer>, SortedList<Integer>> seprateFromLabel(ArrayList<Integer> from_label){
    	ArrayList<Integer> from=new ArrayList<Integer>();
    	SortedList<Integer> label=new SortedList<Integer>();
    	int subLabelIndex=0;
    	while(subLabelIndex<from_label.size()){
    		int sublabel=from_label.get(subLabelIndex);
    		label.add(sublabel);
    		from.addAll(from_label.subList(subLabelIndex+1, subLabelIndex+1+rank.get(sublabel)));
    		subLabelIndex+=(rank.get(sublabel)+1);
    	}
    	return new Pair<ArrayList<Integer>, SortedList<Integer>>(from,label);
    }	
      
}
