package Test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import TreeAutomata.TreeAutomata;
import Util.SortedList;

public class TreeAutomataTest {
	private TreeAutomata t;
	@SuppressWarnings("serial")
	@Before
	public void setUp() throws Exception {
		t=new TreeAutomata();
    	
    	t.setFinal(1);
    	t.addSubLabel(1, 2);
    	t.addSubLabel(2, 2);

    	t.addTrans(
    	new ArrayList<Integer>() {{add(2);add(3);add(4);add(3);}}, 
    	new SortedList<Integer>() {{add(1);add(2);}}, 
    	1);
    	
    	t.addTrans(new ArrayList<Integer>() {{add(1);add(2);}}, 
    	new SortedList<Integer>() {{add(2);}}, 
    	0);
    	t.addTrans(new ArrayList<Integer>() {{add(1);add(1);}}, 
    	new SortedList<Integer>() {{add(1);}}, 
    	1);
    	/*
    	 * Ops a1:2 a2:2 
    	 * States q0 q1 q2 q3 q4 
    	 * Final States q1 
    	 * Transitions
    	 * ([1, 2]) -[2]-> 0
    	 * ([2, 3, 4, 3]) -[1, 2]-> 1
    	 * ([1, 1]) -[1]-> 1
    	 */
    	
    	
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSwapNamesOfStates() {
		try {
			t.swapNamesOfStates(1,2);
			if(t.getFinal()!=2){
				fail("swap did not correctly handle the final state");
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
