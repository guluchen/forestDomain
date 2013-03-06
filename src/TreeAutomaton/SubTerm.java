package TreeAutomaton;

public class SubTerm {
	int sublabel;
	States bottom;
	
	public SubTerm(){
		bottom=new States();
	}
	public SubTerm(int sublabel, States bottom){
		this.sublabel=sublabel;
		this.bottom=bottom;
	}
	public int getSubLabel(){
		return sublabel;
	}
	public void setSubLabel(int sublabel){
		this.sublabel=sublabel;
	}
	public States getStates(){
		return bottom;
	}
	public void setStates(States bottom){
		this.bottom=bottom;
	}
    public int hashCode() {
    	int hashFirst = bottom != null ? bottom.hashCode() : 0;
    	int hashSecond = sublabel;

    	return (hashFirst + hashSecond) * hashSecond;
    }
	
}
