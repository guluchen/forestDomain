package ForestAutomata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Util.ManyToMany;

public class Box extends ForestAutomata{
    //for boxes only
	int inPort;
	ArrayList<Integer> outPorts;
	ManyToMany<Integer, Integer> portConnections;

	Box(){
		super();
		outPorts =new ArrayList<Integer>();
		portConnections=new ManyToMany<Integer, Integer>();
	}

	Box(Box c){
		super(c);
		inPort=c.inPort;
		outPorts =new ArrayList<Integer>(c.outPorts);
		portConnections=new ManyToMany<Integer, Integer>(c.portConnections);
	}

	Box(Box c, HashMap<Integer,Integer> stMapping) throws Exception{
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
	
	HashSet<Integer> getBackPorts(){
		HashSet<Integer> ret=new HashSet<Integer>();
		for(int i=0;i<outPorts.size();i++)
			if(portConnections.leftSetFromRightKey(outPorts.get(i)).contains(inPort))
				ret.add(i);
		return ret;
	}
	
}
