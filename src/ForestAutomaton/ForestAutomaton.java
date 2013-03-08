package ForestAutomaton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import TreeAutomaton.Label;
import TreeAutomaton.States;
import TreeAutomaton.SubTerm;
import TreeAutomaton.Transition;
import TreeAutomaton.TreeAutomaton;
import Util.Pair;
import Util.Triple;

public class ForestAutomaton {
	final static int NULL=1;
	final static int UNDEF=2;
	
	//a sub label is a string and it has a unique number
	static HashMap<String, Integer> symNum=new HashMap<String, Integer>();
	//some sublables (their numbers) are boxes
	static HashMap<Integer, Box> boxes=new HashMap<Integer, Box>();
	static public void addBox(Box b, String selector){
		if(symNum.get(selector)==null)
			symNum.put(selector, TreeAutomaton.getNewSymNumber());
    	boxes.put(symNum.get(selector),b);
	}
	
	//a type has a name and it is a set of sublables (selectors)
	static HashMap<String, ArrayList<String>> varType=new HashMap<String, ArrayList<String>>();

	ArrayList<TreeAutomaton> lt;
	//pointers (program variables) are mapped to the root states  
	HashMap<String,Integer> pointers;
	static public void setSymbolMap(String sym, int number){
		symNum.put(sym, number);
	}
	static public int getSymbolMap(String sym){
		if(symNum.get(sym)==null)
			symNum.put(sym, TreeAutomaton.getNewSymNumber());
		return symNum.get(sym);
	}
	static public Set<String> getSymbols(){
		return symNum.keySet();
	}
	
	//constructors
	public ForestAutomaton(){
		lt=new ArrayList<TreeAutomaton>();
	    pointers=new HashMap<String,Integer>();
	}
	
	public ForestAutomaton(ForestAutomaton c){
		lt=new ArrayList<TreeAutomaton>();
		for(TreeAutomaton ta:c.lt)
			lt.add(new TreeAutomaton(ta));
	    pointers=new HashMap<String,Integer>(c.pointers);
	}

	ForestAutomaton(ForestAutomaton c, HashMap<Integer,Integer> stMapping) throws Exception{
		lt=new ArrayList<TreeAutomaton>();
		for(TreeAutomaton ta:c.lt)
			lt.add(new TreeAutomaton(ta,stMapping));
	    pointers=new HashMap<String,Integer>();
	    for(Entry<String, Integer> e:c.pointers.entrySet()){
	    	pointers.put(e.getKey(), stMapping.get(e.getValue()));
	    }
	}		
	
	//program operations (transformers)
    public HashSet<ForestAutomaton> newNode(String x, ArrayList<String> type) throws Exception {//add a new tree automata to all forest automata
    	removeDeadTransitions();
    	HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
    	varType.put(x, type);
    	
    	TreeAutomaton n=new TreeAutomaton();
    	int newNodeNumber=TreeAutomaton.getNewNodeNumber();//TODO node -> state
    	pointers.put(x, newNodeNumber);
    	n.setFinal(newNodeNumber);
    	addTreeAutomata(n);


    	Label label=new Label();
    	Label label_to_undef=new Label();
    	label_to_undef.add(-UNDEF,0);
    	States refs_to_undef=new States();
    	for(String selector:type){
    		if(symNum.get(selector)==null)
    			symNum.put(selector, TreeAutomaton.getNewSymNumber());
    		n.addSubLabel(symNum.get(selector), 1);
    		label.add(symNum.get(selector),1);
    		refs_to_undef.add(TreeAutomaton.getNewNodeNumber());//TODO maybe it could be just one state common for all new refs to undef (created outside the for loop)
    	}
    	n.addTrans(new Transition(refs_to_undef, label, newNodeNumber));
    	for(int ref:refs_to_undef)
        	n.addTrans(new Transition(new States(), label_to_undef, ref));
    	if(removeDeadTransitions()){//assumes that there are no useless states
    		throw new Exception("Error: a memory leak detected on "+x+" = malloc()\n");
    	}
    	ret.add(this);
    	return ret;
    }
    
    //x = null
    public HashSet<ForestAutomaton> assignNull(String x) throws Exception {
    	this.removeDeadTransitions();
    	HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
    	
    	TreeAutomaton n=new TreeAutomaton();
    	int newNodeNumber=TreeAutomaton.getNewNodeNumber();
    	pointers.put(x, newNodeNumber);
    	n.setFinal(newNodeNumber);
    	Label nullRef=new Label();
		nullRef.add(-NULL,0);
		n.addTrans(new Transition(new States(),nullRef,newNodeNumber));
    	addTreeAutomata(n);
    	if(removeDeadTransitions()){
    		throw new Exception("Error: a memory leak detected on an assignment to "+x+"\n");
    	}		
    	ret.add(this);
    	return ret;
    }

    //x->z = null //TODO some different names for selectors, 'z' looks like a variable 
    public HashSet<ForestAutomaton> assignNull(String x, String z) throws Exception {
    	removeDeadTransitions();
    	int tgtNode=pointers.get(x);
    	TreeAutomaton ta_x=this.getTreeAutomataWithRoot(tgtNode);
    	if(ta_x==null){
    		throw new Exception("Error: variable "+x+" == null\n");//TODO change the error message
    	}
		HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
		for(ForestAutomaton fa_:unfold(tgtNode,z)){
	    	assert tgtNode==fa_.pointers.get(x);
			for(ForestAutomaton fa:onlyOneFinalRule(fa_,fa_.getTreeAutomataWithRoot(tgtNode))){

				assert tgtNode==fa.pointers.get(x);
				TreeAutomaton ta=fa.getTreeAutomataWithRoot(tgtNode);
				assert ta.getTransTo(tgtNode).size()==1;
				Transition tran=getFirst(ta.getTransTo(tgtNode));
				States LHS=tran.getBottom();
				Label label=tran.getLabel();
				if(!label.contains(symNum.get(z))){
					throw new Exception("Error: "+x+" does not have the selector "+z+"\n");
				}else{
					ta.delTrans(tran);
					int new_z_ref=TreeAutomaton.getNewNodeNumber();
					LHS.set(ta.getStartLoc(label, symNum.get(z)), new_z_ref);//TODO Prepare a new shirt, sweating too much
					ta.addTrans(new Transition(LHS, label, tgtNode));
					
					Label nullRef=new Label();
					nullRef.add(-NULL,0);
					ta.addTrans(new Transition(new States(),nullRef,new_z_ref));
					//TODO States() are ordered ... What about creating class State (one)?
				}
		    
		    	if(fa.removeDeadTransitions()){//assumes that there are no useless states
		    		throw new Exception("Error: a memory leak detected on an assignment to "+x+"->"+z+"\n");
		    	}
		    	ret.add(fa);
			}
		}
    	return ret;
    }
    
    // x = y
    public HashSet<ForestAutomaton> assign(String x, String y) throws Exception {
    	this.removeDeadTransitions();
    	HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
		pointers.put(x, pointers.get(y));
    	if(removeDeadTransitions()){
    		throw new Exception("Error: a memory leak detected on an assignment to "+x+"\n");
    	}
		ret.add(this);
		return ret;
    }

    //x->z = y
    public HashSet<ForestAutomaton> assignLeftPointer(String x, String z, String y) throws Exception {
    	this.removeDeadTransitions();
    	TreeAutomaton ta_x=this.getTreeAutomataWithRoot(pointers.get(x));
    	if(ta_x==null){
    		throw new Exception("Error: variable "+x+" == null\n");
    	}
    	int tgtNode=pointers.get(x);
		HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
		for(ForestAutomaton fa_:unfold(tgtNode,z)){
			assert tgtNode==fa_.pointers.get(x);
			for(ForestAutomaton fa:onlyOneFinalRule(fa_,fa_.getTreeAutomataWithRoot(tgtNode))){
				assert tgtNode==fa.pointers.get(x);
				TreeAutomaton ta=fa.getTreeAutomataWithRoot(tgtNode);
				assert ta.getTransTo(tgtNode).size()==1;
				Transition tran=getFirst(ta.getTransTo(tgtNode));
				States LHS=tran.getBottom();
				Label label=tran.getLabel();
				if(!label.contains(symNum.get(z))){
					throw new Exception("Error: "+x+" does not have the selector "+z+"\n");
				}else{
					ta.delTrans(tran);
					int new_z_ref=TreeAutomaton.getNewNodeNumber();
					LHS.set(ta.getStartLoc(label, symNum.get(z)), new_z_ref);
					ta.addTrans(new Transition(LHS, label, tgtNode));
					
					Label ref=new Label();
					ref.add(-pointers.get(y),0);
					ta.addSubLabel(-pointers.get(y), 0);
					ta.addTrans(new Transition(new States(),ref,new_z_ref));
				}
		    	if(fa.removeDeadTransitions()){
		    		throw new Exception("Error: a memory leak detected on an assignment to "+x+"->"+z+"\n");
		    	}
				ret.add(fa);
			}
		}
    	return ret;
    }    
    
    // x = y->z
    public HashSet<ForestAutomaton> assignRightPointer(String x, String y, String z) throws Exception {// the type of y is ``label'' and z is a selector in ``label''
    	this.removeDeadTransitions();
 		int tgtNode=pointers.get(y);
		HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
		
		for(ForestAutomaton fa_:unfold(tgtNode,z)){
			assert tgtNode==fa_.pointers.get(y);
			for(ForestAutomaton fa:onlyOneFinalRule(fa_,fa_.getTreeAutomataWithRoot(tgtNode))){
				assert tgtNode==fa_.pointers.get(y);
				TreeAutomaton ta=fa.getTreeAutomataWithRoot(tgtNode);
				assert ta.getTransTo(tgtNode).size()==1;
				Transition tran=getFirst(ta.getTransTo(tgtNode));
				States LHS=tran.getBottom();
				Label label=tran.getLabel();
				 
				if(!label.contains(symNum.get(z))){
					throw new Exception("Error: "+y+" does not have the selector "+z+"\n");
				}else{
					int x_ref=LHS.get(ta.getStartLoc(label, symNum.get(z)));
					int r=ta.referenceTo(x_ref);
					if(r==-1){//r is not a root reference
						ta.delTrans(tran);
						int new_x_ref=TreeAutomaton.getNewNodeNumber();
						LHS.set(ta.getStartLoc(label, symNum.get(z)), new_x_ref);
						ta.addTrans(new Transition(LHS, label, tgtNode));

						for(Transition tran_to_x:ta.getTransTo(x_ref)){
							States from_x=tran_to_x.getBottom();
							Label label_x=tran_to_x.getLabel();
							ta.addTrans(new Transition(new States(from_x), new Label(label_x), new_x_ref));
						}
						TreeAutomaton ta_x=split(ta, new_x_ref);
						fa.pointers.put(x, ta_x.getFinal());
						fa.addTreeAutomata(ta_x);
						
					}else{
						fa.pointers.put(x, r);
					}
				}
		    	if(fa.removeDeadTransitions()){
		    		throw new Exception("Error: a memory leak detected on an assignment to "+x+"\n");
		    	}
		    	ret.add(fa);
			}
		}
    	return ret;
    }
    
    
    
    
    
    
	//private functions for FA transformation
    private boolean isJoint(int j) throws Exception{
		Label label=new Label();
		label.add(-j,0);
		int referenceCnt=0;
    	for(TreeAutomaton ta:lt){
    		if(ta.getTo(new States(), label).size()!=0){
    			referenceCnt++;
    			if(referenceCnt>=2)
    				return true;
    		}
    	}
    	return false;
    	
    }

    
    //return a 2d array canReach[][] such that canReach[i][j]==true iff st.get(i) can reach st.get(j) 
    private boolean [][] buildConnectionMatrix(ArrayList<Integer> st) throws Exception{
    	boolean [][] canReach=new boolean[st.size()][st.size()]; //initial values of boolean is false in JAVA
		for(int i=0;i<st.size();i++) canReach[i][i]=true;
		//build connection matrix
    	for(TreeAutomaton ta:lt)
    		for(Transition tran:ta.getTrans()){
    			int topLoc=st.indexOf(tran.getTop());
    			for(SubTerm t:tran.getSubTerms()){
    				if(boxes.containsKey(t.getSubLabel())){//the case of a box transition
    					Box box=boxes.get(t.getSubLabel());
    					for(int i=0;i<box.outPorts.size();i++){
							int botLoc_i=st.indexOf(t.getStates().get(i));
        					for(int j=0;j<box.outPorts.size();j++){
    							int botLoc_j=st.indexOf(t.getStates().get(j));
        						if(box.checkPortConnections(box.outPorts.get(i), box.outPorts.get(j)))//handle outport->outport
        							canReach[botLoc_i][botLoc_j]=true;
        					}
    						if(box.checkPortConnections(box.outPorts.get(i), box.inPort))//handle outport->inport
    							canReach[botLoc_i][topLoc]=true;
       						if(box.checkPortConnections(box.inPort, box.outPorts.get(i)))//handle inport->outport
    							canReach[topLoc][botLoc_i]=true;
    					}
    				}else{ 
    					int rootRef=ta.referenceTo(tran.getTop());
    					if(rootRef!=-1){//root reference
    		    			int refLoc=st.indexOf(rootRef);
    						canReach[topLoc][refLoc]=true;
	    				}else{//normal transition
	    				
	    					for(int bot:t.getStates()){
	    		    			int botLoc=st.indexOf(bot);
	    						canReach[topLoc][botLoc]=true;
	    					}
	    				}
    				}
    			}
    		}
    	return canReach;
    }
    
    //Dead = unreachable from the input port. Removes also dead states.
    //if there are not useless states, then returns true iff there is a garbage
    public boolean removeDeadTransitions() throws Exception{
    	ArrayList<Integer> st=new ArrayList<Integer>(getStates());//st fixes some arbitrary order of states
    	boolean [][] canReach=buildConnectionMatrix(st);

    	//collect reachable states
    	Stack<Integer> worklist=new Stack<Integer>();
    	HashSet<Integer> reachableStates=new HashSet<Integer> ();
    	for(String var:pointers.keySet())
    		worklist.push(st.indexOf(pointers.get(var)));
    	while(!worklist.empty()){
    		int cur=worklist.pop();
    		reachableStates.add(st.get(cur));
    		for(int i=0;i<st.size();i++)
    			if(canReach[cur][i]&&!worklist.contains(i)&&!reachableStates.contains(st.get(i)))
    				worklist.push(i);
    	}
    	//remove unreachable states
    	boolean hasUnreachableNonReferenceStates=false;//might still be a useless state
    	for(TreeAutomaton ta:lt){
        	HashSet<Integer> toRemove=new HashSet<Integer>();
    		for(int s:ta.getStates())
    			if(!reachableStates.contains(s)){
    				toRemove.add(s);
    			}
        	for(int s:toRemove){
				if(!ta.isReferenceTo(s, NULL)&&!ta.isReferenceTo(s, UNDEF)&&ta.referenceTo(s)==-1)
					hasUnreachableNonReferenceStates=true;
    			ta.removeState(s);
        	}
    	}
    	return hasUnreachableNonReferenceStates;
    }
    
    public HashSet<ForestAutomaton> unfold(int tgtNode, String z) throws Exception{
		System.out.println("\n=======before unfold======\n"+this+"\n=========\n");

    	HashSet<ForestAutomaton> ret=unfoldPreprocess(tgtNode,z);
    	//open boxes
    	for(ForestAutomaton fa:ret){
    		//backward   
    		fa.removeDeadTransitions();
			System.out.println("\n=======after preprocess======\n"+fa+"\n=========\n");
			for(Triple<Integer,Transition,Integer> root_tran_ref: getBackwardBoxTransWithRefOnBottom(tgtNode,fa,symNum.get(z))){
				openBox(fa, fa.getTreeAutomataWithRoot(root_tran_ref.getFirst()), root_tran_ref.getSecond());
			}
    		//forward
			TreeAutomaton tgtTA=fa.getTreeAutomataWithRoot(tgtNode);
			for(Transition tran: tgtTA.getTransTo(tgtNode)){
				openBox(fa, tgtTA, tran);
			}
			System.out.println("\n=======after open box======\n"+fa+"\n=========\n");
    	}
    	
    	return ret;
    }

    //private functions for folding
    private HashSet<ForestAutomaton> unfoldPreprocess(int tgtNode, String z) throws Exception{
    	int selectorNum=ForestAutomaton.getSymbolMap(z);
    	HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
    	
    	//split the TA with box reference to tgtNode
    	this.removeDeadTransitions();
		for(Triple<Integer,Transition, Integer> root_tran_ref:getBackwardBoxTransWithRefOnBottom(tgtNode,this,selectorNum)){
			ForestAutomaton fa=new ForestAutomaton(this);
			TreeAutomaton parTA=fa.getTreeAutomataWithRoot(root_tran_ref.getFirst());
			Transition tran=root_tran_ref.getSecond();
			int ref=root_tran_ref.getThird();

			HashSet<Pair<Integer,TreeAutomaton>> unwindedTAs=parTA.unwindTA_fromLeaf(tran,ref);
			for(Pair<Integer,TreeAutomaton> splitpoint_ta:unwindedTAs){
				TreeAutomaton cta=split(parTA, split_point);
				fa.addTreeAutomata(cta);
		    	finalRuleBottomToTA(cta.getFinal(), fa);	
			}
			
			
	    	
		}
    	
    	return ret;
    }
    
    //return a set of triples in the form of (final,tran,reference_state) where "final" is the final state of the TA of "tran" and 
    //"reference_state" is the reference state in the bottom of tran 
    private HashSet<Triple<Integer, Transition, Integer>> getBackwardBoxTransWithRefOnBottom(int root, ForestAutomaton fa, int selectorNum){
    	HashSet<Triple<Integer, Transition, Integer>> ret=new HashSet<Triple<Integer, Transition, Integer>>();
	  	for(TreeAutomaton ta:fa.lt)
    		for(int s:ta.getStates())
        		if(ta.isReferenceTo(s, root))
        			for(Transition tran:ta.getTransFrom(s)){
        				Label label=tran.getLabel();
        				States from=tran.getBottom();
        				int startLoc=0;
        				check_backtran:
        				for(int i=0;i<label.size();i++){
        					int sublabel=label.get(i);
        					if(boxes.get(sublabel)!=null){
        						Box box=boxes.get(sublabel);
        						for(int pos:box.getBackPorts(selectorNum)){
        							if(from.get(startLoc+pos)==s){
        								ret.add(new Triple<Integer, Transition, Integer>(ta.getFinal(), tran, s));
        								break check_backtran;
        							}
        						}
        					}
        					startLoc+=ta.getSubLabelRank(sublabel);
        				}
        				
        			}
    	return ret;
    }
    private void openBox(ForestAutomaton fa, TreeAutomaton ta, Transition tran) throws Exception {
    	for(int sublabel:tran.getLabel()){
	    	Box box=boxes.get(sublabel);
	    	if(box==null)
	    		continue;
	    	HashMap<Integer,Integer> stMapping=new HashMap<Integer,Integer>();
			for(int s:box.getStates()){
				if(s==box.inPort){
					stMapping.put(s, tran.getTop());
				}else if(box.outPorts.contains(s)){
					int i=box.outPorts.indexOf(s);
					int startPositionOfSublabel=ta.getStartLoc(tran.getLabel(), sublabel);
					int rootReferenceState=tran.getBottom().get(startPositionOfSublabel+i);
					int rootRef=ta.referenceTo(rootReferenceState);
					assert rootRef!=-1;
					stMapping.put(s, rootRef);
				}else
					stMapping.put(s, TreeAutomaton.getNewNodeNumber());
			}
	    	tran=ta.removeSubTerm(tran, sublabel);
			Box boxFA=new Box(boxes.get(sublabel),stMapping);
			//inport
			attachTA(ta, boxFA.getTreeAutomataWithRoot(tran.getTop()));
			//outports
			for(int o:boxFA.outPorts)
				attachTA(fa.getTreeAutomataWithRoot(o), boxFA.getTreeAutomataWithRoot(o));
			//internal
			for(TreeAutomaton internalTA:boxFA.getTreeAutomata()){
				int f=internalTA.getFinal();
				if(f!=boxFA.inPort&&!boxFA.outPorts.contains(f)){
					fa.addTreeAutomata(internalTA);
				}
			}		
    	}
    }
	private void attachTA(TreeAutomaton oriTa,
			TreeAutomaton boxTa) throws Exception {
		assert oriTa.getFinal()==boxTa.getFinal();
		int root=oriTa.getFinal();
		assert oriTa.getTransTo(root).size()==1;
		Transition oriTran=getFirst(oriTa.getTransTo(root));
		
		for(Transition boxTran:boxTa.getTrans()){
			if(boxTran.getTop()==root){
				for(SubTerm t:boxTran.getSubTerms()){
					oriTran=oriTa.addSubTerm(oriTran, t.getSubLabel(),t.getStates()); 
				}
			}else
				oriTa.addTrans(boxTran);
		}
		
	}

	private TreeAutomaton split(TreeAutomaton ta, int to) throws Exception {
    	
    	//ret is a copy of the current TA with all nodes renamed and with the only final state newSt.get(to) 
		HashMap<Integer,Integer> newSt=new HashMap<Integer, Integer>();
		for(int id:ta.getStates())
			newSt.put(id, TreeAutomaton.getNewNodeNumber());
		TreeAutomaton ret=new TreeAutomaton(ta, newSt);
		ret.setFinal(newSt.get(to));

		//remove the transitions and replace it with a reference
		for(Transition tran:ta.getTransTo(to))
			ta.delTrans(tran);
    	Label portLabel=new Label();
    	portLabel.add(-newSt.get(to),0);
    	ta.addSubLabel(-newSt.get(to), 0);
    	ta.addTrans(new Transition(new States(), portLabel, to));
		
		return ret;
	}  

	private HashSet<ForestAutomaton> finalRuleBottomToTA(ArrayList<Integer> rootsToLift, HashSet<ForestAutomaton> sfa) throws Exception {
		int tgtNode=rootsToLift.remove(0);
		HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
		for(ForestAutomaton fa:sfa) //make sure there is only one rule with tgtNode on the top 
			ret.addAll(onlyOneFinalRule(fa, fa.getTreeAutomataWithRoot(tgtNode)));

		for(ForestAutomaton fa:ret){ //make a new TA for each bottom state of the only final rule
    		TreeAutomaton srcTA=fa.getTreeAutomataWithRoot(tgtNode);
    		HashSet<Transition> finalTrans=srcTA.getTransTo(tgtNode);
    		assert finalTrans.size()==1;
    		Transition finalTran=getFirst(finalTrans);
    		
    		States bottom=finalTran.getBottom();
    		Label label=finalTran.getLabel();
    		int top=finalTran.getTop();
    		
    		srcTA.delTrans(finalTran);
    		States newbottom=new States(bottom);
    		//let n=from.size(), create TAs A1...An, create states q1'...qn' and add to newfrom
    		for(int i=0;i<bottom.size();i++){
    			newbottom.set(i,TreeAutomaton.getNewNodeNumber());
    			HashMap<Integer,Integer> stMapping=new HashMap<Integer,Integer>();
    			for(int s:srcTA.getStates()){
    				stMapping.put(s, TreeAutomaton.getNewNodeNumber());
    			}
    			TreeAutomaton Ai=new TreeAutomaton(srcTA,stMapping);
    			Ai.setFinal(stMapping.get(bottom.get(i)));
    			fa.addTreeAutomata(Ai);
    			
    			Label reflabel=new Label();
    			reflabel.add(-Ai.getFinal(),0);
    			srcTA.addSubLabel(-Ai.getFinal(), 0);
        		srcTA.addTrans(new Transition(new States(), reflabel, newbottom.get(i)));
    		}
    		srcTA.addTrans(new Transition(newbottom, label, top));
    	}
		if(rootsToLift.size()!=0)
			return finalRuleBottomToTA(rootsToLift, ret);
		else
			return ret;
    }

    
    private HashSet<ForestAutomaton> onlyOneFinalRule(ForestAutomaton srcFa, TreeAutomaton srcTA) throws Exception {
    	//make sure tgtNode is not on the bottom of all rules
    	srcTA.unwindTA_fromRoot();
    	srcFa.removeDeadTransitions();
    	HashSet<ForestAutomaton> ret=new HashSet<ForestAutomaton>();
    	HashSet<Transition> trans=srcTA.getTransTo(srcTA.getFinal());
    	for(Transition tran:trans){
    		srcTA.delTrans(tran);
    	}
    	for(Transition tran:trans){
			ForestAutomaton fa=new ForestAutomaton(srcFa);
			TreeAutomaton ta=fa.getTreeAutomataWithRoot(srcTA.getFinal());
			ta.addTrans(tran);
			fa.removeDeadTransitions();
			ret.add(fa);
    	}
    	return ret;       
    }



	//state operations
    
    public HashSet<Integer> getStates(){
    	HashSet<Integer> states=new HashSet<Integer>();
    	for(TreeAutomaton ta:lt){
    		states.addAll(ta.getStates());
    	}
    	return states;
    }

    
    //tree automata operations
	public void addTreeAutomata(TreeAutomaton n) {
		lt.add(n);
	}    
	public HashSet<Integer> getRoots(){
		HashSet<Integer> ret=new HashSet<Integer>();
		for(TreeAutomaton ta:lt){
			ret.add(ta.getFinal());
		}
		return ret;
		
	}
	
	public TreeAutomaton getTreeAutomataWithRoot(int state){
		for(TreeAutomaton ta:lt){
			if(ta.getFinal()==state)
				return ta;
		}
		return null;
	}

	public ArrayList<TreeAutomaton> getTreeAutomata() {
		return lt;
	}

	
	//some helper methods
	private <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.<T>emptyList() : iterable;
    }
	
	private <T> T getFirst(Iterable<T> iterable) {
        return iterable == null ? null : iterable.iterator().next();
    }
	@Override
	public String toString() {
		String ret="";
		for(String var:pointers.keySet()){
			ret+=(var+"->"+pointers.get(var)+"\n");
		}
		for(TreeAutomaton ta:lt){
			ret+=(ta.toString()+"\n");
		}
		return ret;
	}	
	
}
