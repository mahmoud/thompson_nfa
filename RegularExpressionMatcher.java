import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

//The class that does almost all of the work
public class RegularExpressionMatcher {
	
	//constant for denoting epsilon (the empty string)
	public static final String E = "\\e";
	
	//Constant for denoting concatenation
	public static final char CONCAT = '&';
	
	//Map of operators to their precedence value
	private static final Map<Character, Integer> OPERATORS;
	static {
		Map<Character, Integer> ops = new HashMap<Character, Integer>();
		ops.put(')', 1);
		ops.put('*', 2);
		ops.put(CONCAT, 3);
		ops.put('|', 4);
		ops.put('(', 5);
		OPERATORS = ops;
	}
	
	private Stack<Character> _operators;
	private Stack<ArrayList> _inputs;
	
	//The following two variables are just for naming states
	private String _stateNamePrefix = "q";
	private int _nextStateNum = 0;
	
	// todo: add generic
	public HashSet<String> inputSet;
	
	//A list of States that are connected by Transitions. Initialized by the constructor.
	public ArrayList<State> nfa;
	
	//Constructor initializes NFA
	public RegularExpressionMatcher(String regex)
	{
		nfa = _createNFA(regex);
	}
	
	// The main parser and NFA builder. The input string is parsed
	// one character at a time.
	private ArrayList<State> _createNFA(String input)
	{
		_operators = new Stack<Character>();
		_inputs = new Stack<ArrayList>();
		_nextStateNum = 0;
		inputSet = new HashSet<String>();
		
		boolean prevConcat = false;
		
		for(int i=0; i<input.length(); ++i)
		{
			char curChar = input.charAt(i);
			
			if(_isLiteral(curChar))
			{
				if(prevConcat)
				{
					if(_operators.isEmpty())
					{
						_operators.push(CONCAT);
					} else {
						char curOperator = _operators.peek();
						while(_compareOperators(CONCAT, curOperator) <= 0)
						{
							_evaluateOperator(_operators.pop());
							if(_operators.isEmpty())
								break;
							curOperator = _operators.peek();
						}
						_operators.push(CONCAT);
					}
				}
				
				_addLiteral(Character.toString(curChar));
				prevConcat = true;
			} else if (_operators.isEmpty()) 
			{
				if(prevConcat && curChar == '(')
				{
					_operators.push(CONCAT);
				}
				prevConcat = (curChar == '*' || curChar == ')');
				_operators.push(curChar);
			} else if (curChar == '(')
			{
				if(prevConcat)
				{
					char curOperator = _operators.peek();
					while(_compareOperators(CONCAT, curOperator) <= 0)
					{
						_evaluateOperator(_operators.pop());
						if(_operators.isEmpty())
							break;
						curOperator = _operators.peek();
					}
					_operators.push(CONCAT);
				}
				_operators.push(curChar);
				prevConcat = false;
			} else if (curChar == ')')
			{
				char curOperator = _operators.peek();
				while(curOperator != '(')
				{
					_evaluateOperator(_operators.pop());
					if(_operators.isEmpty())
						break;
					curOperator = _operators.peek();
				}
				_operators.pop();
				prevConcat = true;
			} else
			{
				char curOperator = _operators.peek();
				while(_compareOperators(curChar, curOperator) <= 0)
				{
					_evaluateOperator(_operators.pop());
					if(_operators.isEmpty())
						break;
					curOperator = _operators.peek();
				}
				_operators.push(curChar);
				
				prevConcat = (curChar == '*');
			}
		
		}
		
		while(!_operators.isEmpty()){
			_evaluateOperator(_operators.pop());
		}
		
		return _inputs.pop();
	}
	
	//Compare operator precedence
	private int _compareOperators(char op1, char op2)
	{
		if(OPERATORS.get(op1) == OPERATORS.get(op2))
			return 0;
		else if(OPERATORS.get(op1) < OPERATORS.get(op2))
			return 1;
		else
			return -1;
	}
	
	//quick check whether or not I'm looking at an operator
	private boolean _isLiteral(char c)
	{
		return c!='(' && c!=')' && c!='*' && c!='|' && c!=CONCAT;
	}
	
	//Switch that allows access to the specific operator functions
	private void _evaluateOperator(Character operator)
	{
		switch(operator)
		{
		case CONCAT:
			_addConcatenate();
			break;
		case '*':
			_addStar();
			break;
		case '|':
			_addAlternate();
			break;
		default:
			break;
		}
	}

	//Private method used for adding anything that isn't an operator (aka characters)
	private void _addLiteral(String transitionString)
	{
		ArrayList<State> fragment = new ArrayList<State>();
		
		State s1 = new State(_stateNamePrefix+_nextStateNum++);
		State s2 = new State(_stateNamePrefix+_nextStateNum++);
		
		Transition t = new Transition(transitionString);
		t.addState(s2);
		s1.addTransition(t);
		
		fragment.add(s1);
		fragment.add(s2);
		
		_inputs.push(fragment);
		inputSet.add(transitionString);
	}
	
	//Private method used for denoting a concatenation in the NFA
	private void _addConcatenate()
	{
		//Order matters here, and because stacks are LIFO, first one popped
		//is actually the second fragment
		ArrayList<State> fragment2 = _inputs.pop();
		ArrayList<State> fragment1 = _inputs.pop();
		
		Transition t = new Transition(E);
		t.addState(fragment2.get(0));
		
		State s = fragment1.get(fragment1.size()-1);
		s.addTransition(t);
		
		ArrayList<State> newFragment = new ArrayList<State>();
		newFragment.addAll(fragment1);
		newFragment.addAll(fragment2);
		
		_inputs.push(newFragment);
	}
	
	//Private method used for adding a union (or alternate '|') to the NFA
	private void _addAlternate()
	{
		ArrayList<State> fragment2 = _inputs.pop();
		ArrayList<State> fragment1 = _inputs.pop();
		
		State s1 = new State(_stateNamePrefix+_nextStateNum++);
		State s2 = new State(_stateNamePrefix+_nextStateNum++);
		
		Transition t1 = new Transition(E);
		Transition t2 = new Transition(E);
		
		t1.addState(fragment1.get(0));
		t2.addState(fragment2.get(0));
		
		s1.addTransition(t1);
		s1.addTransition(t2);
		
		t1 = new Transition(E);
		t2 = new Transition(E);
		t1.addState(s2);
		t2.addState(s2);
		
		fragment1.get(fragment1.size()-1).addTransition(t1);
		fragment2.get(fragment2.size()-1).addTransition(t2);
		
		ArrayList<State> newFragment = new ArrayList<State>();
		newFragment.add(s1);
		newFragment.addAll(fragment1);
		newFragment.addAll(fragment2);
		newFragment.add(s2);
		
		_inputs.push(newFragment);
	}
	
	//Private method used for adding a star to the NFA
	private void _addStar()
	{
		ArrayList<State> fragment = _inputs.pop();
		
		State s1 = new State(_stateNamePrefix+_nextStateNum++);
		State s2 = new State(_stateNamePrefix+_nextStateNum++);
		
		Transition t1 = new Transition(E);
		Transition t2 = new Transition(E);
		Transition t3 = new Transition(E);
		
		t1.addState(fragment.get(0));
		t1.addState(s2);
		s1.addTransition(t1);
		
		t2.addState(s2);
		fragment.get(fragment.size()-1).addTransition(t2);
		
		t3.addState(fragment.get(0));
		fragment.get(fragment.size()-1).addTransition(t3);
		
		ArrayList<State> newFragment = new ArrayList<State>();
		newFragment.add(s1);
		newFragment.addAll(fragment);
		newFragment.add(s2);
		
		_inputs.push(newFragment);
	}
	
	//matches the provided string to the NFA set by a regular expression
	//in the constructor. Traverses the NFA in a breadth-first fashion, not
	//recursively, not using backtracking. This method should provide linear
	//time.
	public boolean match(String toMatch)
	{
		HashSet<State> curList = new HashSet<State>();
		HashSet<State> nextList = new HashSet<State>();
		HashSet<State> swapList;
		ArrayList<State> tempNextStates;
		
		State startState = nfa.get(0);
		State matchState = nfa.get(nfa.size()-1);
		
		//This assumes the nfa's start state is the first state in the list.
		curList.add(startState);
		curList.addAll(startState.getEpsilonStates());
		for(int i = 0; i<toMatch.length(); ++i)
		{
			//step
			for(State curState:curList)
			{
				tempNextStates = curState.getNextStates(toMatch.charAt(i));
				if(tempNextStates.size() > 0)
				{
					nextList.addAll(tempNextStates);
				}
			}
			
			//swap lists
			swapList = curList;
			curList = nextList;
			nextList = swapList;
			nextList.clear();
			swapList = null;
			
			//System.out.println(curList);
		}
		
		return curList.contains(matchState);
	}
	
	public static void main(String[] args)
	{
		Scanner console = new Scanner(System.in);
		
		String regex = console.nextLine();
		RegularExpressionMatcher regexMatcher = new RegularExpressionMatcher(regex);
		String string = console.nextLine();
		while(string != null && string.trim().compareTo("")!=0)
		{
			string = removeE(string);
			if(regexMatcher.match(string))
				System.out.println("yes");
			else
				System.out.println("no");
			
			if(!console.hasNextLine())
				break;
			
			string = console.nextLine();
		}
		/*
		RegularExpressionMatcher regexMatcher = new RegularExpressionMatcher("((a|b)(a|b))*");
		System.out.println(regexMatcher.match("aaaaa"));
		System.out.println(regexMatcher.nfa);*/
	}
	
	//It's unnatural to explicitly state 'e'
	//empty strings should be empty!
	//So I'm sanitizing it!
	public static String removeE(String in)
	{
		return in.replaceAll("e", "");
	}
	
}
