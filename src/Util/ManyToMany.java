package Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ManyToMany<leftClass,rightClass> {

	@Override
	public String toString() {
    	String ret="";
    	for(leftClass l:to.keySet()){
    		for(rightClass r:to.get(l))
    			ret+=(l+"<==>"+r+"\n");
    	}
		return ret;
	}
	HashMap<leftClass, HashSet<rightClass>> to;
    HashMap<rightClass, HashSet<leftClass>> from;

    public ManyToMany() {
    	to=new HashMap<leftClass, HashSet<rightClass>>();
    	from=new HashMap<rightClass, HashSet<leftClass>>();
    }
    public ManyToMany(ManyToMany<leftClass, rightClass> c) {
    	to=new HashMap<leftClass, HashSet<rightClass>>(c.to);
    	from=new HashMap<rightClass, HashSet<leftClass>>(c.from);
    }
    
    
    
    public void put(leftClass left, rightClass right){
    	if(to.get(left)==null)
    		to.put(left, new HashSet<rightClass>());
    	if(from.get(right)==null)
    		from.put(right, new HashSet<leftClass>());    	
    	to.get(left).add(right);
    	from.get(right).add(left);
    }

    public void putAll(ManyToMany<leftClass,rightClass> other){
    	for(leftClass left:other.leftKeySet()){
    		for(rightClass right:other.rightSetFromLeftKey(left)){
		    	if(to.get(left)==null)
		    		to.put(left, new HashSet<rightClass>());
		    	if(from.get(right)==null)
		    		from.put(right, new HashSet<leftClass>());    	
		    	to.get(left).add(right);
		    	from.get(right).add(left);
    		}
    	}
    }
    
    
    public void removePair(leftClass left, rightClass right){
    	if(to.get(left)!=null){
    		to.get(left).remove(right);
    	}
    	if(from.get(right)!=null){
    		from.get(right).remove(left);
    	}
    }
    public void removeLeft(leftClass left){
    	if(to.get(left)!=null){
    		for(rightClass r:to.get(left)){
    			from.get(r).remove(left);
       			if(from.get(r).size()==0){
    				from.remove(r);
    			}
 
    		}
    		to.remove(left);
    	}
    }
    public void removeRight(rightClass right){
    	if(from.get(right)!=null){
    		for(leftClass l:from.get(right)){
    			to.get(l).remove(right);
    			if(to.get(l).size()==0){
    				to.remove(l);
    			}
    		}
    		from.remove(right);
    	}
    }
    
    public HashSet<rightClass> rightSetFromLeftKey(leftClass left){
    	return to.get(left);
    }

    public HashSet<leftClass> leftSetFromRightKey(rightClass right){
    	return from.get(right);
    }
    public Set<leftClass> leftKeySet(){
    	return to.keySet();
    }
    public Set<rightClass> rightKeySet(){
    	return from.keySet();
    }
    public static void main(String[] args) {

    	ManyToMany<Integer,Integer> pointer=new ManyToMany<Integer, Integer>();
    	pointer.put(10, 10);
    	pointer.put(10, 11);
    	pointer.put(10, 12);   	
    	pointer.put(11, 10);
    	pointer.put(11, 11);
    	pointer.put(12, 10);
    	pointer.put(12, 11);
    	for(int i:pointer.leftKeySet())
    		System.out.println("Node #"+i+" => node(s) #"+pointer.rightSetFromLeftKey(i));
    	for(int s:pointer.rightKeySet())
    		System.out.println("Node #"+s+" <= node(s) #"+pointer.leftSetFromRightKey(s));
    	System.out.println("");
    	pointer.removeLeft(10);
    	for(int i:pointer.leftKeySet())
    		System.out.println("Node #"+i+" => node(s) #"+pointer.rightSetFromLeftKey(i));
    	for(int s:pointer.rightKeySet())
    		System.out.println("Node #"+s+" <= node(s) #"+pointer.leftSetFromRightKey(s));
    	
    }

}
