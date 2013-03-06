package TreeAutomaton;

public class Transition {
    private States bottom;
    private Label label;
    private int top;

    public Transition(States bottom, Label label, int top) {
    	this.bottom = bottom;
    	this.label = label;
    	this.top = top;
    }
    public Transition(Transition c){
    	this.bottom = c.bottom;
    	this.label = c.label;
    	this.top = c.top;
    }
    
    public int hashCode() {
    	int hashFirst = bottom != null ? bottom.hashCode() : 0;
    	int hashSecond = label != null ? label.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond + top;
    }


    public String toString()
    { 
           return "(" + bottom + ", " + label + ", " + top + ")"; 
    }

    public States getBottom() {
    	return bottom;
    }

    public void setBottom(States LHS) {
    	this.bottom = LHS;
    }

    public Label getLabel() {
    	return label;
    }

    public void setLabel(Label label) {
    	this.label = label;
    }
    
    public int getTop() {
    	return top;
    }

    public void setTop(int top) {
    	this.top = top;
    }
    
}