import java.util.ArrayDeque;
import java.util.ArrayList;


public class State {
	//name of the state
	public String name;
	
	//transitions exiting the state
	public ArrayList<Transition> transitions = new ArrayList<Transition>();
	
	//Basic constructor, sets the name of the state
	public State(String n)
	{
		name = n;
	}
	
	//Adds a transition to the state
	public boolean addTransition(Transition t)
	{
		return transitions.add(t);
	}
	
	//Debug stuff. Outputs the string version of this state.
	public String toString()
	{
		StringBuilder transString = new StringBuilder();
		transString.append("(");
		for(int i = 0; i < transitions.size(); ++i)
		{
			transString.append(transitions.get(i).transString);
			transString.append("[");
			for(State s:transitions.get(i).states)
			{
				transString.append(s.name+"|");
			}
			transString.append("];");
		}
		transString.append(")");
		return name+": "+transitions.size()+" "+transString;
	}
	
	//Gets all the states implied by this state because they're connected by
	//epsilon (empty string) transitions
	public ArrayList<State> getEpsilonStates()
	{
		ArrayList<State> ret = new ArrayList<State>();
		ArrayDeque<Transition> tempEpsilon = new ArrayDeque<Transition>();
		Transition tempTransition;

		for(Transition t:transitions)
		{
			if(t.transString.compareTo(RegularExpressionMatcher.E) == 0)
			{
				tempEpsilon.add(t);
				while(!tempEpsilon.isEmpty())
				{
					tempTransition = tempEpsilon.removeFirst();
					if(tempTransition.transString.compareTo(RegularExpressionMatcher.E) == 0)
					{
						for(State s:tempTransition.states)
						{
							ret.add(s);
							tempEpsilon.addAll(s.transitions);
						}
					}
				}
			}
		}
		
		return ret;
	}
	
	//This method gets all the states connected by a certain transitionString
	//plus all of the epsilon-connected states
	public ArrayList<State> getNextStates(String transitionString)
	{
		ArrayDeque<Transition> tempEpsilon = new ArrayDeque<Transition>();
		Transition tempTransition;
		
		ArrayList<State> ret = new ArrayList<State>();
		for(Transition t:transitions)
		{
			if(t.transString.compareTo(transitionString) == 0)
			{
				ret.addAll(t.states);
			}
		}
		ret.addAll(getEpsilonStates());
		return ret;
	}
	
	//A wrapper to autoconvert a character to a string
	public ArrayList<State> getNextStates(char c)
	{
		return getNextStates(Character.toString(c));
	}
	
}
