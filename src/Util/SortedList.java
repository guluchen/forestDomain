package Util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;

public class SortedList<E> extends AbstractList<E> {

    private ArrayList<E> internalList;

    public SortedList() {
    	internalList=new ArrayList<E>();
    }
    
    public SortedList(SortedList<E> c) {
    	internalList=new ArrayList<E>(c);
    }

	// Note that add(E e) in AbstractList is calling this one
    @Override 
    public void add(int position, E e) {
        internalList.add(e);
        
        Collections.sort(internalList, null);
    }

    @Override
    public E get(int i) {
        return internalList.get(i);
    }

    @Override
    public int size() {
        return internalList.size();
    }

    @Override
    public E remove(int i) {
        return internalList.remove(i);
    }
    
    
    @SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
        if (obj == null) return false;
        else if (!(obj instanceof SortedList)) return false;
        else if(this.size()!=((SortedList<E>) obj).size()) return false;
        for(int i=0;i<this.size();i++){
        	if(this.get(i)!=((SortedList<E>) obj).get(i)) return false;
        }
        return true;
    }
}