import java.util.ArrayList;


public class Transition {

	//trigger string
	public String transString;
	
	//states that this transition leads to
	public ArrayList<State> states = new ArrayList<State>();
	
	//Basic constructor, sets the string that triggers this transition
	public Transition(String t)
	{
		transString = t;
	}
	
	//Adds a state that this transition leads to
	public boolean addState(State s)
	{
		return states.add(s);
	}
}
