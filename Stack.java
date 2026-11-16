import java.util.Arrays;
/** Manual array-backed stack. */
public final class Stack {
 // These arrays keep delimiter values and their source positions in matching slots.
 private char[] stackArray; private int[] positions; private int top=-1,capacity;
 // Initialize a small stack; a requested capacity is clamped to at least one slot.
 public Stack(){this(16);} public Stack(int n){capacity=Math.max(1,n);stackArray=new char[capacity];positions=new int[capacity];}
 // Push onto the top, growing the arrays first if there is no free slot.
 public void push(char c){push(c,-1);} public void push(char c,int p){if(isFull())grow();stackArray[++top]=c;positions[top]=p;}
 // Remove and return the top value; popping an empty stack is an underflow error.
 public char pop(){if(isEmpty())throw new IllegalStateException("Stack underflow");return stackArray[top--];}
 // Read the top value or its saved input position without changing the stack.
 public char peek(){if(isEmpty())throw new IllegalStateException("Stack empty");return stackArray[top];}
 public int peekPosition(){if(isEmpty())throw new IllegalStateException("Stack empty");return positions[top];}
 // Report stack state for the checker and visualizer.
 public boolean isEmpty(){return top<0;} public boolean isFull(){return top==capacity-1;} public int size(){return top+1;}
 // Return a copy from bottom to top so callers can display a stable snapshot.
 public char[] display(){return Arrays.copyOf(stackArray,top+1);}
 // Double capacity and preserve existing values and saved positions.
 private void grow(){capacity*=2;stackArray=Arrays.copyOf(stackArray,capacity);positions=Arrays.copyOf(positions,capacity);}
}
