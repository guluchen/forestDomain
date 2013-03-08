package ForestAutomaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import TreeAutomaton.Transition;
import TreeAutomaton.TreeAutomaton;
import Util.ManyToMany;

public class Box extends ForestAutomaton{
    //for boxes only
	int inPort;
	ArrayList<Integer> outPorts;
	//which port can reach which port in the semantics
	ManyToMany<Integer, Integer> portConnections;

	
	public void setInPort(int inPort){
		this.inPort=inPort;
	}
	public void addOutPort(int outPort){
		this.outPorts.add(outPort);
	}
	public void setPortConnections(int from, int to){
		this.portConnections.put(from, to);
	}
	public boolean checkPortConnections(int from, int to){
		return portConnections.rightSetFromLeftKey(from).contains(to);
	}
	public Box(){
		super();
		outPorts =new ArrayList<Integer>();
		portConnections=new ManyToMany<Integer, Integer>();
	}

	public Box(Box c){
		super(c);
		inPort=c.inPort;
		outPorts =new ArrayList<Integer>(c.outPorts);
		portConnections=new ManyToMany<Integer, Integer>(c.portConnections);
	}

	public Box(Box c, HashMap<Integer,Integer> stMapping) throws Exception{
		super(c,stMapping);
		inPort=stMapping.get(c.inPort);
		outPorts =new ArrayList<Integer>();
		for(int state:c.outPorts){
			outPorts.add(stMapping.get(state));
		}
		portConnections=new ManyToMany<Integer, Integer>(c.portConnections);
	    for(int stateFrom:c.portConnections.leftKeySet()){
	    	for(int stateTo:c.portConnections.rightSetFromLeftKey(stateFrom))
	    		portConnections.put(stMapping.get(stateFrom), stMapping.get(stateTo));
	    }		
	}	
	
	HashSet<Integer> getBackPorts(int selectorNum){
		HashSet<Integer> ret=new HashSet<Integer>();
		for(int i=0;i<outPorts.size();i++){
			TreeAutomaton out_i=getTreeAutomataWithRoot(outPorts.get(i));
			for(Transition tran:out_i.getTransTo(out_i.getFinal())){
				if(tran.getLabel().contains(selectorNum))
					ret.add(i);
			}
		}
		return ret;
	}
	
}
