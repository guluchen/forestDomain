package TreeAutomaton;

public class Term {
	Label label;
	States bottom;
	
	public Term(){
		label=new Label();
		bottom=new States();
	}
	public Term(Label label, States bottom){
		this.label=label;
		this.bottom=bottom;
	}
	public Label getLabel(){
		return label;
	}
	public void setLabel(Label label){
		this.label=label;
	}
	public States getStates(){
		return bottom;
	}
	public void setStates(States bottom){
		this.bottom=bottom;
	}
    public int hashCode() {
    	int hashFirst = bottom != null ? bottom.hashCode() : 0;
    	int hashSecond = label != null ? label.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond;
    }
	
}
