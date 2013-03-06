//TODO Transition = top + Label + bottom, label + bottom = Term, Term = a set of SubTerms, SubTerm = subLabel + states



package TreeAutomaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ForestAutomaton.ForestAutomaton;
import Util.ManyToMany;

public class TreeAutomaton{


	static private int freshNodeNum=3;//state 1,2 are reserved for the roots of the null and undef TAs, resp.
	static private int freshSymNum=1;	
	private HashMap<Integer,Integer> rank=new HashMap<Integer,Integer>();
	private ManyToMany<Term, Integer> trans=new ManyToMany<Term, Integer>();	
	private int finalSt;
	private States states;
	//Constructors
	public TreeAutomaton(){
		rank=new HashMap<Integer,Integer>();
		trans=new ManyToMany<Term, Integer>();	
		states=new States();
		addSubLabel(-1,0);//for ref to null
		addSubLabel(-2,0);//for ref to undef
	}
	public TreeAutomaton(TreeAutomaton c){
		rank=new HashMap<Integer,Integer>(c.rank);
		trans=new ManyToMany<Term, Integer>(c.trans);	
		states=new States(c.states);
		finalSt=c.finalSt;
	}

	public TreeAutomaton(TreeAutomaton c, HashMap<Integer, Integer> stMapping) throws Exception {
		stMapping.put(1, 1);//for the final of null
		stMapping.put(2, 2);//for the final of undef
		rank=new HashMap<Integer,Integer>(c.rank);
		trans=new ManyToMany<Term, Integer>();
		for(int top:c.trans.rightKeySet()){
			for(Term term:c.trans.leftSetFromRightKey(top)){
				States bottom=term.getStates();
				Label label=term.label;
				States new_bottom=new States(term.getStates());
				for(int i=0;i<bottom.size();i++){
					new_bottom.set(i, stMapping.get(bottom.get(i)));
				}
				trans.put(new Term(label,new_bottom), stMapping.get(top));
			}

		}
		states=new States();
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
	public States getStates(){
		return states;
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
		label.add(-root);
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
				else if(label.iterator().next()>0)
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
		states.addAll(tran.getBottom());
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

	//TODO: subTransition or edgeTerm?
	public Transition removeSubTransition(Transition srcTran,
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

	//TODO make symmetric? (transition, symbol, lhs)
	public Transition addSubTransition(Transition srcTran,
			HashMap<Integer,Integer> tgtRank, Transition tgtTran) throws Exception{

		for(int tgtSublabel:tgtTran.getLabel()){
			this.addSubLabel(tgtSublabel, tgtRank.get(tgtSublabel));
		}

		States from=new States(srcTran.getBottom());
		Label label=new Label(srcTran.getLabel());
		int to=srcTran.getTop();

		label.addAll(tgtTran.getLabel());
		int j=0;
		for(int tgtSubLabel:tgtTran.getLabel()){
			int startLoc=getStartLoc(label,tgtSubLabel);
			for(int i=0;i<rank.get(tgtSubLabel);i++){
				from.add(startLoc+i,tgtTran.getBottom().get(j));
				j++;
			}
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

	public void removeLoopToRoot() throws Exception{
		int oriRoot=getFinal();
		if(this.getTransFrom(oriRoot).size()==0)
			return;
		int newRoot=TreeAutomaton.getNewNodeNumber();
		//TODO getNewNodeNumber() -> getNewStateNumber()? Node should rather be state.

		for(Label label:getLabels())
			for(States from:this.getFrom(oriRoot, label)){
				this.addTrans(new Transition(from, label, newRoot));
			}
		swapNamesOfStates(oriRoot, newRoot);//TODO why do we do this?
		setFinal(oriRoot);
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