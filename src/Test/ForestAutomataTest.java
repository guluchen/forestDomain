package Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ForestAutomata.ForestAutomata;

public class ForestAutomataTest {
	private ForestAutomata fa;
	private ArrayList<String> DLL;
	private HashSet<ForestAutomata> cur;
	@Before
	public void setUp() throws Exception {
		DLL=new ArrayList<String>();
		DLL.add("next");
		DLL.add("prev");
		fa=new ForestAutomata();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAssignNull_x() {
		try {
			cur=fa.assignNull("x");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	@Test
	public void testNewNode_x() {
		try {
			cur=fa.assignNull("x");
			HashSet<ForestAutomata> sfa_new=new HashSet<ForestAutomata>();
			for(ForestAutomata fa: cur){
				sfa_new.addAll(fa.newNode("x",DLL));
			}
			cur=sfa_new;
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	@Test
	public void testAssign_x_y() {
		try {
			cur=fa.assignNull("x");
			HashSet<ForestAutomata> sfa_new=new HashSet<ForestAutomata>();
			for(ForestAutomata fa: cur){
				sfa_new.addAll(fa.assign("y", "x"));
			}
			cur=sfa_new;
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testAssignNull_x_z() {
		try {
			cur=fa.newNode("x",DLL);
			HashSet<ForestAutomata> sfa_new=new HashSet<ForestAutomata>();
			for(ForestAutomata fa: cur){
				sfa_new.addAll(fa.assignNull("x", "next"));
			}
			cur=sfa_new;
			for(ForestAutomata fa: cur){
				System.out.println("===============");
				System.out.println(fa.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}	
}
