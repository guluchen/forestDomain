package ForestAutomaton;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import TreeAutomaton.Label;
import TreeAutomaton.States;
import TreeAutomaton.Transition;
import TreeAutomaton.TreeAutomaton;


public class ForestAutomatonTester {
	private ForestAutomaton fa;
	private static ArrayList<String> DLL;
	private HashSet<ForestAutomaton> cur;
	private static Box dll;

	@BeforeClass
	static public void setUpBeforeClass() throws Exception {
		DLL=new ArrayList<String>();
		DLL.add("next");
		DLL.add("prev");
		
		dll=new Box();
		TreeAutomaton ta1=new TreeAutomaton();
		TreeAutomaton ta2=new TreeAutomaton();
		int s1=TreeAutomaton.getNewNodeNumber();
		int s2=TreeAutomaton.getNewNodeNumber();
		int s3=TreeAutomaton.getNewNodeNumber();
		int s4=TreeAutomaton.getNewNodeNumber();
		int n=TreeAutomaton.getNewSymNumber();
		int p=TreeAutomaton.getNewSymNumber();
		ForestAutomaton.setSymbolMap("next", n);
		ForestAutomaton.setSymbolMap("prev", p);
		
		ta1.setFinal(s1);
		ta1.addSubLabel(n, 1);
		ta1.addSubLabel(-s3, 0);

		Label label1=new Label();
		label1.add(n,1);
		States lhs1=new States();
		lhs1.add(s2);
		ta1.addTrans(new Transition(lhs1, label1, s1));
		
		Label ref1=new Label();
		ref1.add(-s3,0);
		ta1.addTrans(new Transition(new States(), ref1, s2));

		ta2.setFinal(s3);
		ta2.addSubLabel(p, 1);
		ta2.addSubLabel(-s1, 0);

		Label label2=new Label();
		label2.add(p,1);
		States lhs2=new States();
		lhs2.add(s4);
		ta2.addTrans(new Transition(lhs2, label2, s3));

		Label ref2=new Label();
		ref2.add(-s1,0);
		ta2.addTrans(new Transition(new States(), ref2, s4));
		dll.addTreeAutomata(ta1);
		dll.addTreeAutomata(ta2);
		dll.addOutPort(s3);
		dll.setInPort(s1);
		dll.setPortConnections(s1, s3);
		dll.setPortConnections(s3, s1);
		
	}
	@Before
	public void setUp() throws Exception {
		fa=new ForestAutomaton();
		ForestAutomaton.addBox(dll, "dll");
	}
	@After
	public void tearDown() throws Exception {
	}

//	@Test
//	public void testAssignNull_x() {
//		try {
//			cur=fa.assignNull("x");
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testNewNode_x() {
//		try {
//			cur=fa.assignNull("x");
//			HashSet<ForestAutomaton> sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.newNode("x",DLL));
//			}
//			cur=sfa_new;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testAssign_x_y() {
//		try {
//			cur=fa.assignNull("x");
//			HashSet<ForestAutomaton> sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.assign("y", "x"));
//			}
//			cur=sfa_new;
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
//
//
//	
//	@Test
//	public void testAssignNull_x_z() {
//		try {
//			cur=fa.newNode("x",DLL);
//			HashSet<ForestAutomaton> sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.assignNull("x", "next"));
//			}
// 
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testAssignLeftPointer_x_z_y() {
//		try {
//			cur=fa.newNode("x",DLL);
//			HashSet<ForestAutomaton> sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.newNode("y",DLL));
//			}
//			cur=sfa_new;
//			sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.assignLeftPointer("x", "next","y"));
//			}
//			cur=sfa_new;
//				
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testAssignRightPointer_x_y_z() {
//		try {
//			cur=fa.newNode("y",DLL);
//			HashSet<ForestAutomaton> sfa_new=new HashSet<ForestAutomaton>();
//			sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.assignRightPointer("x", "y","next"));
//			}
//			cur=sfa_new;
//				
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testFormADLLCell() {
//		try {
//			cur=fa.newNode("x",DLL);
//			HashSet<ForestAutomaton> sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.newNode("y",DLL));
//			}
//			cur=sfa_new;
//			sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.assignLeftPointer("x", "next","y"));
//			}
//			cur=sfa_new;
//			sfa_new=new HashSet<ForestAutomaton>();
//			for(ForestAutomaton fa: cur){
//				sfa_new.addAll(fa.assignLeftPointer("y", "prev","x"));
//			}
//
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}

	@Test
	public void testCreateADLLofArbitaryLength() {
		try {

			TreeAutomaton ta_X=new TreeAutomaton();
			TreeAutomaton ta_I=new TreeAutomaton();
			TreeAutomaton ta_Y=new TreeAutomaton();
			int s1=TreeAutomaton.getNewNodeNumber();
			int s2=TreeAutomaton.getNewNodeNumber();
			int s3=TreeAutomaton.getNewNodeNumber();
			int s4=TreeAutomaton.getNewNodeNumber();
			int s6=TreeAutomaton.getNewNodeNumber();
			int s7=TreeAutomaton.getNewNodeNumber();
			int s8=TreeAutomaton.getNewNodeNumber();

			int n=ForestAutomaton.getSymbolMap("next");
			int p=ForestAutomaton.getSymbolMap("prev");
			int dll=ForestAutomaton.getSymbolMap("dll");

			ta_X.setFinal(s1);
			ta_X.addSubLabel(n, 1);
			ta_X.addSubLabel(-ForestAutomaton.NULL, 0);
			//([2])-[next]->1
			Label label_s1_s2=new Label();
			label_s1_s2.add(n, 1);
			States lhs_s2=new States();
			lhs_s2.add(s2);
			ta_X.addTrans(new Transition(lhs_s2, label_s1_s2, s1));
			//([])-[NULL]->2
			Label nullRef=new Label();
			nullRef.add(-ForestAutomaton.NULL,0);
			ta_X.addTrans(new Transition(new States(), nullRef, s2));

			ta_I.setFinal(s3);
			ta_I.addSubLabel(dll,1);
			ta_I.addSubLabel(-s1,0);
			ta_I.addSubLabel(-s3,0);
			//([4])-[dll]->3
			Label label_s3_s4=new Label();
			label_s3_s4.add(dll,1);
			States lhs_s4=new States();
			lhs_s4.add(s4);
			ta_I.addTrans(new Transition(lhs_s4, label_s3_s4, s3));
			//([3])-[dll]->3
			Label label_s3_s3=new Label();
			label_s3_s3.add(dll,1);
			States lhs_s3=new States();
			lhs_s3.add(s3);
			ta_I.addTrans(new Transition(lhs_s3, label_s3_s3, s3));
			//([])-[REF 1]->4
			Label ref_s1=new Label();
			ref_s1.add(-s1,0);
			ta_I.addTrans(new Transition(new States(), ref_s1, s4));
			
			ta_Y.setFinal(s6);
			ta_Y.addSubLabel(p, 1);
			ta_Y.addSubLabel(-s3, 0);
			ta_Y.addSubLabel(dll, 1);
			
			//([7, 8])-[prev dll]->6
			Label label_s6_s7s8=new Label();
			label_s6_s7s8.add(p,1);
			label_s6_s7s8.add(dll,1);
			States lhs_s7s8=new States();
			lhs_s7s8.add(s7);
			lhs_s7s8.add(s8);
			ta_Y.addTrans(new Transition(lhs_s7s8, label_s6_s7s8, s6));

			//([])-[null]->7
			ta_Y.addTrans(new Transition(new States(), new Label(nullRef), s7));
			//([])-[REF 3]->8
			Label ref_s3=new Label();
			ref_s3.add(-s3,0);
			ta_Y.addTrans(new Transition(new States(), ref_s3, s8));

			fa.addTreeAutomata(ta_X);
			fa.addTreeAutomata(ta_Y);
			fa.addTreeAutomata(ta_I);
			
			fa.pointers.put("x", s1);
			fa.pointers.put("y", s6);
			
			System.out.println(fa);
			
			HashSet<ForestAutomaton> sfa_new=new HashSet<ForestAutomaton>();
			sfa_new.addAll(fa.assignRightPointer("x","x","prev"));
			
			
//			cur=fa.newNode("x",DLL);
//			HashSet<ForestAutomata> sfa_new=new HashSet<ForestAutomata>();
//			for(ForestAutomata fa: cur){
//				sfa_new.addAll(fa.newNode("y",DLL));
//			}
//			cur=sfa_new;
//			sfa_new=new HashSet<ForestAutomata>();
//			for(ForestAutomata fa: cur){
//				sfa_new.addAll(fa.assignLeftPointer("x", "next","y"));
//			}
//			cur=sfa_new;
//			sfa_new=new HashSet<ForestAutomata>();
//			for(ForestAutomata fa: cur){
//				sfa_new.addAll(fa.assignLeftPointer("y", "prev","x"));
//			}

			cur=sfa_new;
			for(ForestAutomaton fa: cur){
				fa.removeDeadTransitions();
				System.out.println("===============");
				System.out.println(fa.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}	
	
}
