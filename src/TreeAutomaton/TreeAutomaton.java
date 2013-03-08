//TODO Transition = top + Label + bottom, label + bottom = Term, Term = a set of SubTerms, SubTerm = subLabel + states



package TreeAutomaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ForestAutomaton.ForestAutomaton;
import Util.ManyToMany;
import Util.Pair;

public class TreeAutomaton{


	static private int freshNodeNum=3;//state 1,2 are reserved for the roots of the null and undef TAs, resp.
	static private int freshSymNum=1;	
	private HashMap<Integer,Integer> rank;
	private ManyToMany<Term, Integer> trans;	
	private int finalSt;
	private HashSet<Integer> states;
	//Constructors
	public TreeAutomaton(){
		rank=new HashMap<Integer,Integer>();//TODO should be static?
		trans=new ManyToMany<Term, Integer>();	
		states=new HashSet<Integer>();
		addSubLabel(-1,0);//for ref to null
		addSubLabel(-2,0);//for ref to undef
	}
	public TreeAutomaton(TreeAutomaton c){
		rank=new HashMap<Integer,Integer>(c.rank);
		trans=new ManyToMany<Term, Integer>(c.trans);	
		states=new HashSet<Integer>(c.states);
		finalSt=c.finalSt;
	}

	public TreeAutomaton(TreeAutomaton c, HashMap<Integer, Integer> stMapping) throws Exception {
		rank=new HashMap<Integer,Integer>(c.rank);
		trans=new ManyToMany<Term, Integer>();
		
		for(int top:c.trans.rightKeySet()){
			for(Term term:c.trans.leftSetFromRightKey(top)){
				Term newTerm=new Term();
				for(SubTerm st:term.getSubTerms()){
					int sublabel;
					if(stMapping.get(-st.getSubLabel())!=null){
						if(st.getSubLabel()<0)
							sublabel=-stMapping.get(-st.getSubLabel());
						else
							sublabel=stMapping.get(st.getSubLabel());
					}else{
						sublabel=st.getSubLabel();
					}
					States new_bottom=new States(term.getStates());
					for(int i=0;i<term.getStates().size();i++){
						new_bottom.set(i, stMapping.get(term.getStates().get(i)));
					}
					newTerm.addSubTerm(new SubTerm(sublabel,new_bottom));
				}
				trans.put(newTerm, stMapping.get(top));
			}

		}
		states=new HashSet<Integer>();
		for(int state:c.getStates()){
			states.add(stMapping.get(state));
		}
		finalSt=stMapping.get(c.finalSt);
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
	public HashSet<Label> getLabels(){
		HashSet<Label> ret=new HashSet<Label>();
		for(int to:trans.rightKeySet()){
			for(Term term:trans.leftSetFromRightKey(to)){
				Label label=term.getLabel();
				ret.add(label);
			}
		}
		return ret;
	}
	public HashMap<Integer,Integer> getRankMapping(){
		return rank;
	}

	//return the start location of the states correspond to the sublabel
	public int getStartLoc(Label label, int sublabel){
		int startLoc=0;
		for(int i=0;i<label.indexOf(sublabel);i++){
			startLoc+=rank.get(label.get(i));
		}
		return startLoc;
	}

	//state operations
	public void setFinal(int st){
		finalSt=st;
		this.states.add(st);
	}
	public int getFinal(){
		return finalSt;
	}
	public HashSet<Integer> getStates(){
		return states;
	}
	public void removeState(Integer s) throws Exception{
		states.remove(s);
		for(Transition tran:getTransFrom(s))
			delTrans(tran);
		for(Transition tran:getTransTo(s))
			delTrans(tran);
	}

	public boolean isState(Integer s) {
		return states.contains(s);
	}

	public void swapNamesOfStates(int srcSt, int tgtSt) throws Exception{
		renameState(srcSt,0);
		renameState(tgtSt,srcSt);
		renameState(0,tgtSt);
	}
	
	//check if state s is a root reference to TA_root
	public boolean isReferenceTo(int s,int root){
		Label label=new Label();
		label.add(-root,0);
		if(this.getFrom(s, label).size()==0)
			return false;
		else
			return true;
	}

	//returns -1 if it is not a root reference, otherwise, return the tree root it references to
	public int referenceTo(int rootRef){

		for(Transition tran:getTransTo(rootRef)){
			if(tran.getBottom().size()!=0)
				return -1;
			else{
				Label label=tran.getLabel();
				if(label.size()!=1)
					return -1;
				else if(label.iterator().next()>-3)
					return -1;
				else
					return -label.iterator().next();
			}
		}

		return -1;
	}

	public void renameState(Integer oriSt, Integer newSt) throws Exception{
		if(finalSt==oriSt) finalSt=newSt;
		if(states.contains(oriSt)){
			states.remove(oriSt);
			states.add(newSt);
		}else{
			return;
		}
		HashSet<Transition> toAdd=new HashSet<Transition>();
		for(Transition tran:this.getTrans()){
			int top=tran.getTop();
			Label label = tran.getLabel();
			States bottom=tran.getBottom();

			States new_bottom=new States(bottom);
			for(int i=0;i<new_bottom.size();i++){
				if(new_bottom.get(i)==oriSt)
					new_bottom.set(i, newSt);
			}
			top=(top!=oriSt)?top:newSt;
			toAdd.add(new Transition(new_bottom, label,top));	
		}
		trans.clear();

		for(Transition cur:toAdd){
			this.addTrans(cur);
		}
	}
	//transition operations
	public void addTrans(Transition tran) throws Exception {
		States s=tran.getBottom();
		for(int i=0;i<s.size();i++)
			states.add(s.get(i));
		states.add(tran.getTop());
		trans.put(new Term(tran.getLabel(),tran.getBottom()), tran.getTop());
	}

	public void delTrans(Transition tran) throws Exception{
		trans.removePair(new Term(tran.getLabel(),tran.getBottom()), tran.getTop());
	}
	public HashSet<Transition> getTrans(){
		HashSet<Transition> ret=new HashSet<Transition>();	
		for(int to:trans.rightKeySet()){
			for(Term term:trans.leftSetFromRightKey(to)){
				States from=term.getStates();
				Label label=term.getLabel();
				ret.add(new Transition(from,label,to));
			}
		}
		return ret;
	}

	public HashSet<Transition> getTransFrom(int s){
		HashSet<Transition> ret=new HashSet<Transition>();	
		for(int top:trans.rightKeySet()){
			for(Term term:trans.leftSetFromRightKey(top)){
				States bottom=term.getStates();
				Label label=term.getLabel();
				if(bottom.contains(s))
					ret.add(new Transition(bottom,label,top));
			}
		}
		return ret;
	}

	public HashSet<Transition> getTransTo(int s){
		HashSet<Transition> ret=new HashSet<Transition>();	
		for(int top:trans.rightKeySet()){
			if(top==s)
				for(Term term:trans.leftSetFromRightKey(top)){
					States bottom=term.getStates();
					Label label=term.getLabel();
					ret.add(new Transition(bottom,label,top));
				}
		}
		return ret;
	}

	public HashSet<States> getFrom(int top, Label tgtLabel){
		HashSet<States> ret= new HashSet<States>();
		if(trans.leftSetFromRightKey(top)!=null)
			for(Term term:trans.leftSetFromRightKey(top)){
				States bottom=term.getStates();
				Label label=term.getLabel();
				if(label.equals(tgtLabel))
					ret.add(bottom);
			}
		return ret;
	}
	public HashSet<Integer> getTo(States from, Label label) throws Exception{
		HashSet<Integer> ret =new HashSet<Integer>();
		if(trans.rightSetFromLeftKey(new Term(label,from))!=null)
			for(int to:trans.rightSetFromLeftKey(new Term(label,from))){
				ret.add(to);
			}
		return ret;
	}

	public Transition removeSubTerm(Transition srcTran,
			int srcSubLabel) throws Exception{

		States from=new States(srcTran.getBottom());
		Label label=new Label(srcTran.getLabel());
		int to=srcTran.getTop();

		int startLoc=getStartLoc(label,srcSubLabel);
		for(int i=startLoc;i<startLoc+rank.get(srcSubLabel);i++)
			from.remove(startLoc);
		label.remove(srcSubLabel);

		this.delTrans(srcTran);
		this.addTrans(new Transition(from,label,to));
		return new Transition(from,label,to);
	}

	public Transition addSubTerm(Transition srcTran,
			int srcSubLabel, States states) throws Exception{
		
		addSubLabel(srcSubLabel, states.size());
		States from=new States(srcTran.getBottom());
		Label label=new Label(srcTran.getLabel());
		int to=srcTran.getTop();

		label.add(srcSubLabel,states.size());
		int j=0;
		int startLoc=getStartLoc(label,srcSubLabel);
		for(int i=0;i<states.size();i++){
			from.add(startLoc+i,states.get(j));
			j++;
		}
		this.delTrans(srcTran);
		this.addTrans(new Transition(from,label,to));		
		return new Transition(from,label,to);
	}	
	


	//automata operations
	//TODO shouldn't we check some more things before doing union? Like that the sets of states are disjoint?
	public void union(TreeAutomaton tgt){
		if(this.finalSt!=tgt.finalSt){
			System.out.println("Only union between automata with the same root is allowed");
			System.exit(0);
		}
		rank.putAll(tgt.rank);
		trans.putAll(tgt.trans);
		states.addAll(tgt.states);
	}

	//Unwind the TA from root. The tree language and final state are remain unchanged after this operation.
	//For each tree accepted by the TA, the root appears only once after the transformation
    public void unwindTA_fromRoot() throws Exception {
		int newRoot=TreeAutomaton.getNewNodeNumber();
		for(Transition finalTran:getTransTo(getFinal())){
			States from=finalTran.getBottom();
			Label label=finalTran.getLabel();
			addTrans(new Transition(new States(from), new Label(label), newRoot));
		}
		int oriRoot=getFinal();
		swapNamesOfStates(newRoot, oriRoot);
		setFinal(oriRoot);
	}

	//Unwind the TA from a given leaf state l. The tree language, the leaf 
    //state, and final state are remain unchanged after this operation. 
    //NOTICE:this function makes use of several assumptions on the input
    //1. state l is a leaf state
    //2. For every run of the tree automata, the label of the transition tran
    //   followed by the state referenced by l appears at most once 
    //Then this function guarantees that the top state of tran (renamed to 
    //the return value ret) appears at most once at all trees accepted by this TA.
    public HashSet<Pair<Integer,TreeAutomaton>> unwindTA_fromLeaf(Transition tran, int l) throws Exception {
    	HashSet<Pair<Integer,TreeAutomaton>> ret=new HashSet<Pair<Integer,TreeAutomaton>>();

    	int new_top=TreeAutomaton.getNewNodeNumber();
		int top=tran.getTop();
		States bottom=tran.getBottom();

		Label label=tran.getLabel();
		addTrans(new Transition(new States(bottom), new Label(label), new_top));
		delTrans(new Transition(new States(bottom), new Label(label), top));
		for(Transition tran2:getTransFrom(top)){
			int top2=tran2.getTop();
			States bottom2=new States(tran2.getBottom());
			Label label2=new Label(tran2.getLabel());
			//replace all occurrences of top in bottom2 with new_top
			for(int i=0;i<bottom2.size();i++){
				if(bottom2.get(i)==top)
					bottom2.set(i, new_top);
			}
			addTrans(new Transition(bottom2, label2, top2));
		}
		
		ret.add(new Pair<Integer,TreeAutomaton>(new_top, this));
		if(top==this.getFinal()){
			TreeAutomaton new_ta=new TreeAutomaton(this);
			new_ta.setFinal(new_top);
			ret.add(new Pair<Integer,TreeAutomaton>(new_top, new_ta));
		}
		
    	return ret;
    }
    

    
    
    
	static public int getNewNodeNumber(){//Node -> State everywhere?
		return freshNodeNum++;
	}

	static public int getNewSymNumber(){
		return freshSymNum++;
	}	


	@Override
	public String toString() {
		HashMap<Integer,String> numSym=new HashMap<Integer,String>();
		for(String sym:ForestAutomaton.getSymbols()){
			numSym.put(ForestAutomaton.getSymbolMap(sym), sym);
		}

		String ret="Ops ";
		for(int i:rank.keySet()){
			if(i>0)
				ret+=((numSym.get(i))+":"+rank.get(i)+" ");
		}
		ret+="\n\nAutomaton \nStates ";
		for(int i:states){
			ret+=(" "+i+" ");
		}
		ret+="\nFinal States ";
		ret+=(" "+finalSt+" ");

		ret+="\nTransitions\n";
		for(Transition tran: this.getTrans()){
			States from=tran.getBottom();
			Label label=tran.getLabel();
			int to=tran.getTop();
			String label_prettyprint="[ ";
			for(int sublabel:label){
				if(sublabel==-1)
					label_prettyprint+="NULL ";
				else if(sublabel==-2)
					label_prettyprint+="UNDEF ";
				else if(sublabel<0)
					label_prettyprint+=("REF "+(-sublabel)+" ");
				else
					label_prettyprint+=(numSym.get(sublabel)+" ");
			}
			label_prettyprint+="]";
			ret+=("("+from+") -"+ label_prettyprint +"-> "+to+"\n");
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


}