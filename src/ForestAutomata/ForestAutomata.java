package ForestAutomata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import TreeAutomata.Transition;
import TreeAutomata.TreeAutomata;
import Util.Pair;
import Util.SortedList;

public class ForestAutomata {

	static HashMap<Integer, Box> boxes=new HashMap<Integer, Box>();
	static public void addBox(Box b, int sublabel){
    	boxes.put(sublabel,b);
	}
	
	private ArrayList<TreeAutomata> lt; 
	private HashMap<String,Integer> pointers;

	//constructors
	ForestAutomata(){
		lt=new ArrayList<TreeAutomata>();
	    pointers=new HashMap<String,Integer>();
	}
	
	ForestAutomata(ForestAutomata c){
		lt=new ArrayList<TreeAutomata>();
		for(TreeAutomata ta:c.lt)
			lt.add(new TreeAutomata(ta));
	    pointers=new HashMap<String,Integer>(c.pointers);
	}

	ForestAutomata(ForestAutomata c, HashMap<Integer,Integer> stMapping) throws Exception{
		lt=new ArrayList<TreeAutomata>();
		for(TreeAutomata ta:c.lt)
			lt.add(new TreeAutomata(ta,stMapping));
	    pointers=new HashMap<String,Integer>();
	    for(String var:c.pointers.keySet()){
	    	pointers.put(var, stMapping.get(pointers.get(var)));
	    }
	}	
	
	//program operations (transformers)
    public void newNode(String x) throws Exception {//add a new tree automata to all forest automata
    	pointers.remove(x);
    	if(noOtherReferenceTo(pointers.get(x),x)){
    		throw new Exception("Error: a memory leak detected on "+x+" = malloc()\n");
    	}
    	TreeAutomata n=new TreeAutomata();
    	int newNodeNumber=TreeAutomata.getNewNodeNumber();
    	pointers.put(x, newNodeNumber);
    	n.setFinal(newNodeNumber);
    	addTreeAutomata(n);
    }
    
    //x = null
    public void assignNull(String x) throws Exception {
    	if(noOtherReferenceTo(pointers.get(x), x)){
    		throw new Exception("Error: a memory leak detected on an assignment to "+x+"\n");
    	}
    	pointers.put(x,0);
    }

    //x->z = null
    public HashSet<ForestAutomata> assignNull(String x, int z) throws Exception {
    	TreeAutomata ta_x=this.getTreeAutomataWithRoot(pointers.get(x));
    	if(ta_x==null){
    		throw new Exception("Error: variable "+x+" == null\n");
    	}
    	int tgtNode=pointers.get(x);
		HashSet<ForestAutomata> ret=new HashSet<ForestAutomata>();
		for(ForestAutomata fa_:unfold(tgtNode)){
			assert tgtNode==fa_.pointers.get(x);
			for(ForestAutomata fa:onlyOneFinalRule(fa_,fa_.getTreeAutomataWithRoot(tgtNode))){
				assert tgtNode==fa.pointers.get(x);
				TreeAutomata ta=fa.getTreeAutomataWithRoot(tgtNode);
				assert ta.getTransTo(tgtNode).size()==1;
				Transition tran=getFirst(ta.getTransTo(tgtNode));
				ArrayList<Integer> LHS=tran.getLHS();
				SortedList<Integer> label=tran.getLabel();
				if(!label.contains(z)){
					throw new Exception("Error: "+x+" does not have the selector "+z+"\n");
				}else{
					int z_ref=LHS.get(ta.getStartLoc(label, z));
					int r=ta.referenceTo(z_ref);
					if(r==-1){//r is not a root reference
						ta.delTrans(tran);
						int new_z_ref=TreeAutomata.getNewNodeNumber();
						LHS.set(ta.getStartLoc(label, z), new_z_ref);
						ta.addTrans(LHS, label, tgtNode);
						
						SortedList<Integer> nullRef=new SortedList<Integer>();
						nullRef.add(0);
						
						ta.addTrans(new ArrayList<Integer>(),nullRef,new_z_ref);
					}else{
						for(String y:fa.pointers.keySet()){
							if(fa.pointers.get(y)==r)
								fa.pointers.put(y, 0);
						}
					}
				}
			}
		}
    	return ret;
    }
    
    // x = y
    public void assign(String x, String y) throws Exception {
    	if(noOtherReferenceTo(pointers.get(x), x)){
    		throw new Exception("Error: a memory leak detected on an assignment to "+x+"\n");
    	}
		pointers.put(x, pointers.get(y));
    }

    //x->z = null TODO
    public HashSet<ForestAutomata> assign(String x, int z, String y) throws Exception {
    	TreeAutomata ta_x=this.getTreeAutomataWithRoot(pointers.get(x));
    	if(ta_x==null){
    		throw new Exception("Error: variable "+x+" == null\n");
    	}
    	int tgtNode=pointers.get(x);
		HashSet<ForestAutomata> ret=new HashSet<ForestAutomata>();
		for(ForestAutomata fa_:unfold(tgtNode)){
			assert tgtNode==fa_.pointers.get(x);
			for(ForestAutomata fa:onlyOneFinalRule(fa_,fa_.getTreeAutomataWithRoot(tgtNode))){
				assert tgtNode==fa.pointers.get(x);
				TreeAutomata ta=fa.getTreeAutomataWithRoot(tgtNode);
				assert ta.getTransTo(tgtNode).size()==1;
				Transition tran=getFirst(ta.getTransTo(tgtNode));
				ArrayList<Integer> LHS=tran.getLHS();
				SortedList<Integer> label=tran.getLabel();
				if(!label.contains(z)){
					throw new Exception("Error: "+x+" does not have the selector "+z+"\n");
				}else{
					int z_ref=LHS.get(ta.getStartLoc(label, z));
					int r=ta.referenceTo(z_ref);
					if(r==-1){//r is not a root reference
						ta.delTrans(tran);
						int new_z_ref=TreeAutomata.getNewNodeNumber();
						LHS.set(ta.getStartLoc(label, z), new_z_ref);
						ta.addTrans(LHS, label, tgtNode);
						
						SortedList<Integer> nullRef=new SortedList<Integer>();
						nullRef.add(pointers.get(y));
						
						ta.addTrans(new ArrayList<Integer>(),nullRef,new_z_ref);
					}else{
						for(String x_:fa.pointers.keySet()){
							if(fa.pointers.get(x_)==r)
								fa.pointers.put(y, pointers.get(y));
						}
					}
				}
			}
		}
    	return ret;
    }    
    
    // x = y->z
    public HashSet<ForestAutomata> assign(String x, String y, int z) throws Exception {// the type of y is ``label'' and z is a selector in ``label''
    	pointers.remove(x);//This step should be done when assign a new value to a variable
    	if(noOtherReferenceTo(pointers.get(x), x)){
    		throw new Exception("Error: a memory leak detected on an assignment to "+x+"\n");
    	}
    	
		int tgtNode=pointers.get(y);
		HashSet<ForestAutomata> ret=new HashSet<ForestAutomata>();
		
		for(ForestAutomata fa_:unfold(tgtNode)){
			assert tgtNode==fa_.pointers.get(y);
			for(ForestAutomata fa:onlyOneFinalRule(fa_,fa_.getTreeAutomataWithRoot(tgtNode))){
				assert tgtNode==fa_.pointers.get(y);
				TreeAutomata ta=fa.getTreeAutomataWithRoot(tgtNode);
				assert ta.getTransTo(tgtNode).size()==1;
				Transition tran=getFirst(ta.getTransTo(tgtNode));
				ArrayList<Integer> LHS=tran.getLHS();
				SortedList<Integer> label=tran.getLabel();
				 
				if(!label.contains(z)){
					throw new Exception("Error: "+y+" does not have the selector "+z+"\n");
				}else{
					int x_ref=LHS.get(ta.getStartLoc(label, z));
					int r=ta.referenceTo(x_ref);
					if(r==-1){//r is not a root reference
						ta.delTrans(tran);
						int new_x_ref=TreeAutomata.getNewNodeNumber();
						LHS.set(ta.getStartLoc(label, z), new_x_ref);
						ta.addTrans(LHS, label, tgtNode);

						for(Transition tran_to_x:ta.getTransTo(x_ref)){
							ArrayList<Integer> from_x=tran_to_x.getLHS();
							SortedList<Integer> label_x=tran_to_x.getLabel();
							ta.addTrans(new ArrayList<Integer>(from_x), new SortedList<Integer>(label_x), new_x_ref);
						}
						TreeAutomata ta_x=split(ta, new_x_ref);
						pointers.put(x, ta_x.getFinal());
						fa.addTreeAutomata(ta_x);
						
					}else{
						pointers.put(x, r);
					}
				}
			}
		}
    	return ret;
    }
    
    
    
    
    
    
	//private functions for FA transformation
    private boolean isJoint(int j) throws Exception{
		SortedList<Integer> label=new SortedList<Integer>();
		label.add(-j);
		int referenceCnt=0;
    	for(TreeAutomata ta:lt){
    		if(ta.getTo(new ArrayList<Integer>(), label).size()!=0){
    			referenceCnt++;
    			if(referenceCnt>=2)
    				return true;
    		}
    	}
    	return false;
    	
    }
    private boolean noOtherReferenceTo(int j, String curRef) throws Exception{
		if(isJoint(j))//if j is a joint, then it is safe to remove x
			return false;
		boolean last_to_j=true;
		for(String y:pointers.keySet()){
				
			if(pointers.get(y)==j && y.compareToIgnoreCase(curRef)!=0){
			//these exists y!=x that also points to j, no memory leak occurs
				last_to_j=false;
				break;
			}
		}
		if(last_to_j){
			return true;
		}
			
    	return false;
    }	
    
    public HashSet<ForestAutomata> unfold(int tgtNode) throws Exception{
    	HashSet<ForestAutomata> ret=unfoldPreprocess(tgtNode);
    	//open boxes
    	for(ForestAutomata fa:ret){
    		//backward
			for(Pair<TreeAutomata,Transition> ta_tran: getBackwardBoxTransWithRefOnLHS(tgtNode,fa)){
				openBox(fa, ta_tran.getFirst(), ta_tran.getSecond());
			}
    		//forward
			TreeAutomata tgtTA=fa.getTreeAutomataWithRoot(tgtNode);
			for(Transition tran: tgtTA.getTransTo(tgtNode)){
				openBox(fa, tgtTA, tran);
			}
    	}
    	
    	return ret;
    }

    //private functions for folding
    private HashSet<ForestAutomata> unfoldPreprocess(int tgtNode) throws Exception{
    	HashSet<ForestAutomata> ret=new HashSet<ForestAutomata>();
    	ret.add(this);
    	
    	//split the TA with box reference to tgtNode
    	ArrayList<TreeAutomata> toAdd=new ArrayList<TreeAutomata>();
    	ArrayList<Integer> rootsToLift=new ArrayList<Integer>();
    	rootsToLift.add(tgtNode);
    	
		for(Pair<TreeAutomata,Transition> ta_tran:getBackwardBoxTransWithRefOnLHS(tgtNode,this)){
			TreeAutomata cta=split(ta_tran.getFirst(), ta_tran.getSecond().getRHS());
			toAdd.add(cta);
	    	rootsToLift.add(cta.getFinal());
		}
    	lt.addAll(toAdd);
    	
    	return finalRuleLHSToTA(rootsToLift, ret);
    }
    private HashSet<Pair<TreeAutomata, Transition>> getBackwardBoxTransWithRefOnLHS(int root, ForestAutomata fa){
    	HashSet<Pair<TreeAutomata, Transition>> ret=new HashSet<Pair<TreeAutomata, Transition>>();
	  	for(TreeAutomata ta:fa.lt)
    		for(int s:ta.getStates())
        		if(ta.isReferenceTo(s, root))
        			for(Transition tran:ta.getTransFrom(s)){
        				SortedList<Integer> label=tran.getLabel();
        				ArrayList<Integer> from=tran.getLHS();
        				int startLoc=0;
        				check_backtran:
        				for(int i=0;i<label.size();i++){
        					int sublabel=label.get(i);
        					if(boxes.get(sublabel)!=null){
        						Box box=boxes.get(sublabel);
        						for(int pos:box.getBackPorts()){
        							if(from.get(startLoc+pos)==s){
        								ret.add(new Pair<TreeAutomata, Transition>(ta, tran));
        								break check_backtran;
        							}
        						}
        					}
        					startLoc+=ta.getSubLabelRank(sublabel);
        				}
        				
        			}
    	return ret;
    }
    private void openBox(ForestAutomata fa, TreeAutomata ta, Transition tran) throws Exception {
    	for(int sublabel:tran.getLabel()){
	    	Box box=boxes.get(sublabel);
	    	if(box==null)
	    		continue;
	    	HashMap<Integer,Integer> stMapping=new HashMap<Integer,Integer>();
			for(int s:box.getStates()){
				if(s==box.inPort){
					stMapping.put(s, tran.getRHS());
				}else if(box.outPorts.contains(s)){
					int i=box.outPorts.indexOf(s);
					int startPositionOfSublabel=ta.getStartLoc(tran.getLabel(), sublabel);
					int rootReferenceState=tran.getLHS().get(startPositionOfSublabel+i);
					int rootRef=ta.referenceTo(rootReferenceState);
					assert rootRef!=-1;
					stMapping.put(s, rootRef);
				}else
					stMapping.put(s, TreeAutomata.getNewNodeNumber());
			}
	    	tran=ta.removeSubTransition(tran, sublabel);
			Box boxFA=new Box(boxes.get(sublabel),stMapping);
			//inport
			attachTA(ta, boxFA.getTreeAutomataWithRoot(tran.getRHS()));
			//outports
			for(int o:boxFA.outPorts)
				attachTA(fa.getTreeAutomataWithRoot(o), boxFA.getTreeAutomataWithRoot(o));
			//internal
			for(TreeAutomata internalTA:boxFA.getTreeAutomata()){
				int f=internalTA.getFinal();
				if(f!=boxFA.inPort&&!boxFA.outPorts.contains(f)){
					fa.addTreeAutomata(internalTA);
				}
			}		
    	}
    }
	private void attachTA(TreeAutomata oriTa,
			TreeAutomata boxTa) throws Exception {
		assert oriTa.getFinal()==boxTa.getFinal();
		int root=oriTa.getFinal();
		assert oriTa.getTransTo(root).size()==1;
		Transition oriTran=getFirst(oriTa.getTransTo(root));
		
		for(Transition boxTran:boxTa.getTrans()){
			if(boxTran.getRHS()==root){
				oriTa.addSubTransition(oriTran, boxTa.getRankMapping(), boxTran);
			}else
				oriTa.addTrans(boxTran);
		}
		
	}

	private TreeAutomata split(TreeAutomata ta, int to) throws Exception {
    	
    	//ret is a copy of the current TA with all nodes renamed and with the only final state newSt.get(to) 
		HashMap<Integer,Integer> newSt=new HashMap<Integer, Integer>();
		for(int id:ta.getStates())
			newSt.put(id, TreeAutomata.getNewNodeNumber());
		TreeAutomata ret=new TreeAutomata(ta, newSt);
		ret.setFinal(newSt.get(to));

		//remove the transition and replace it with a reference
		for(Transition tran:ta.getTransTo(to))
			ta.delTrans(tran);
    	SortedList<Integer> portLabel=new SortedList<Integer>();
    	portLabel.add(-newSt.get(to));
    	ta.addSubLabel(-newSt.get(to), 0);
    	ta.addTrans(new ArrayList<Integer>(), portLabel, to);
		
		return ret;
	}  

	private HashSet<ForestAutomata> finalRuleLHSToTA(ArrayList<Integer> rootsToLift, HashSet<ForestAutomata> sfa) throws Exception {
		int tgtNode=rootsToLift.remove(0);
		HashSet<ForestAutomata> ret=new HashSet<ForestAutomata>();
		for(ForestAutomata fa:sfa) //make sure there is only one rule with tgtNode on the RHS
			ret.addAll(onlyOneFinalRule(fa, getTreeAutomataWithRoot(tgtNode)));

		for(ForestAutomata fa:ret){ //make a new TA for each LHS state of the only final rule
    		TreeAutomata srcTA=fa.getTreeAutomataWithRoot(tgtNode);
    		HashSet<Transition> finalTrans=srcTA.getTransTo(tgtNode);
    		assert finalTrans.size()==1;
    		Transition finalTran=getFirst(finalTrans);
    		
    		ArrayList<Integer> from=finalTran.getLHS();
    		SortedList<Integer> label=finalTran.getLabel();
    		int to=finalTran.getRHS();
    		
    		srcTA.delTrans(finalTran);
    		ArrayList<Integer> newfrom=new ArrayList<Integer>();
    		//let n=from.size(), create TAs A1...An, create states q1'...qn' and add to newfrom
    		for(int i=0;i<from.size();i++){
    			newfrom.add(i,TreeAutomata.getNewNodeNumber());
    			HashMap<Integer,Integer> stMapping=new HashMap<Integer,Integer>();
    			for(int s:srcTA.getStates()){
    				stMapping.put(s, TreeAutomata.getNewNodeNumber());
    			}
    			TreeAutomata Ai=new TreeAutomata(srcTA,stMapping);
    			Ai.setFinal(stMapping.get(from.get(i)));
    			addTreeAutomata(Ai);
    			
    			SortedList<Integer> reflabel=new SortedList<Integer>();
    			reflabel.add(-Ai.getFinal());
        		srcTA.addTrans(new ArrayList<Integer>(), reflabel, newfrom.get(i));
    		}
    		srcTA.addTrans(newfrom, label, to);
    	}
		if(rootsToLift.size()!=0)
			return finalRuleLHSToTA(rootsToLift, ret);
		else
			return ret;
    }

    
    private HashSet<ForestAutomata> onlyOneFinalRule(ForestAutomata srcFa, TreeAutomata srcTA) throws Exception {
    	//make sure tgtNode is not on the LHS of all rules
    	finalNotInLHS(srcTA);
    	
    	HashSet<ForestAutomata> ret=new HashSet<ForestAutomata>();
    	HashSet<Transition> trans=srcTA.getTransTo(srcTA.getFinal());
    	for(Transition tran:trans){
    		srcTA.delTrans(tran);
    	}
    	for(Transition tran:trans){
			ForestAutomata fa=new ForestAutomata(srcFa);
			fa.getTreeAutomataWithRoot(srcTA.getFinal()).addTrans(tran);
			ret.add(fa);
		}
    	return ret;       
    }

    private void finalNotInLHS(TreeAutomata srcTA) throws Exception {
		int newRoot=TreeAutomata.getNewNodeNumber();
		for(Transition finalTran:srcTA.getTransTo(srcTA.getFinal())){
			ArrayList<Integer> from=finalTran.getLHS();
			SortedList<Integer> label=finalTran.getLabel();
			srcTA.addTrans(new ArrayList<Integer>(from), new SortedList<Integer>(label), newRoot);
		}
		srcTA.setFinal(newRoot);
		srcTA.swapNamesOfStates(newRoot, srcTA.getFinal());
	}

	//state operations
    
    public HashSet<Integer> getStates(){
    	HashSet<Integer> states=new HashSet<Integer>();
    	for(TreeAutomata ta:lt){
    		states.addAll(ta.getStates());
    	}
    	return states;
    }

    
    //tree automata operations
	public void addTreeAutomata(TreeAutomata n) {
		lt.add(n);
	}    
	public HashSet<Integer> getRoots(){
		HashSet<Integer> ret=new HashSet<Integer>();
		for(TreeAutomata ta:lt){
			ret.add(ta.getFinal());
		}
		return ret;
		
	}
	
	public TreeAutomata getTreeAutomataWithRoot(int state){
		for(TreeAutomata ta:lt){
			if(ta.getFinal()==state)
				return ta;
		}
		return null;
	}

	public ArrayList<TreeAutomata> getTreeAutomata() {
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

		for(TreeAutomata ta:lt){
			ret+=(ta+"\n");
		}
		return ret;
	}	
	
}
