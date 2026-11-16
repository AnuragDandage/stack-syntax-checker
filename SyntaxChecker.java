import java.util.ArrayList;import java.util.List;
/** Matches delimiters, ignoring Java string/character literals and comments. */
public final class SyntaxChecker {
 // A single scan event, including the stack snapshot needed by the animation and log.
 public static final class Step {
  public final int index,line;public final char character;public final String operation,reason;public final char[] stack;public final boolean error;
  Step(int i,int l,char c,String o,String r,char[] s,boolean e){index=i;line=l;character=c;operation=o;reason=r;stack=s;error=e;}
  public String visible(){return character=='\n'?"\\n":character=='\t'?"\\t":"'"+character+"'";}
 }
 // Final scan status and first diagnostic, when an error was found.
 public static final class Result {
  public final boolean balanced;public final String message;public final int errorIndex,line;public final Character incorrect,expected;public final char[] finalStack;
  Result(boolean b,String m,int i,int l,Character c,Character x,char[] s){balanced=b;message=m;errorIndex=i;line=l;incorrect=c;expected=x;finalStack=s;}
 }
 // The complete trace and final result returned to Simulation.
 public static final class Analysis {public final List<Step> steps;public final Result result;Analysis(List<Step>s,Result r){steps=s;result=r;}}
 // Scanner modes prevent bracket characters inside non-code text from affecting the stack.
 private enum Mode{CODE,STRING,CHAR,LINE,BLOCK}
 // Scan once from left to right while tracking lexical context, line number, and stack state.
 public Analysis analyze(String input){
  String code=input==null?"":input;List<Step> steps=new ArrayList<>();Stack st=new Stack(Math.max(16,code.length()/4));
  Mode mode=Mode.CODE;boolean escaped=false;int line=1,err=-1,errLine=-1;Character wrong=null,expected=null;String message=null;
  for(int i=0;i<code.length();i++){
   char c=code.charAt(i);String op="SKIP",why="Ordinary character; stack unchanged.";boolean bad=false;
   // In comments and literals, ignore delimiters; escaped quotes do not end a literal.
   if(mode==Mode.LINE){why="Line comment; delimiter ignored.";if(c=='\n'){mode=Mode.CODE;why="End of line comment.";}}
   else if(mode==Mode.BLOCK){why="Block comment; delimiter ignored.";if(c=='*'&&i+1<code.length()&&code.charAt(i+1)=='/'){mode=Mode.CODE;why="End of block comment.";}}
   else if(mode==Mode.STRING||mode==Mode.CHAR){why="Literal; delimiter ignored.";char q=mode==Mode.STRING?'"':'\'';if(escaped)escaped=false;else if(c=='\\')escaped=true;else if(c==q){mode=Mode.CODE;why="Literal closed; contained delimiters ignored.";}}
   // In code mode, detect comments and quoted literals before interpreting bracket characters.
   else if(c=='/'&&i+1<code.length()&&code.charAt(i+1)=='/'){mode=Mode.LINE;why="Line comment begins.";}
   else if(c=='/'&&i+1<code.length()&&code.charAt(i+1)=='*'){mode=Mode.BLOCK;why="Block comment begins.";}
   else if(c=='"'){mode=Mode.STRING;why="String literal begins.";}
   else if(c=='\''){mode=Mode.CHAR;why="Character literal begins.";}
   // Openers are pushed; closers require a non-empty stack and a matching top element.
   else if(open(c)){st.push(c,i);op="PUSH";why="Opening delimiter pushed onto stack.";}
   else if(close(c)){
    if(st.isEmpty()){op="ERROR";bad=true;why="Closing delimiter found with empty stack.";if(message==null){err=i;errLine=line;wrong=c;message="Unmatched closing delimiter.";}}
    else{char top=st.peek(),match=matching(top);if(c==match){st.pop();op="POP";why="Matching close; popped "+top+".";}else{op="ERROR";bad=true;why="Mismatch; expected "+match+" for "+top+".";if(message==null){err=i;errLine=line;wrong=c;expected=match;message="Mismatched closing delimiter; expected '"+match+"'.";}}}
   }
   // Save the result after this character so playback can reproduce each visible stack state.
   steps.add(new Step(i,line,c,op,why,st.display(),bad));if(c=='\n')line++;
  }
  // If scanning ended without an earlier error, any remaining opener is unmatched.
  if(message==null&&!st.isEmpty()){err=st.peekPosition();errLine=1;for(int j=0;j<err;j++)if(code.charAt(j)=='\n')errLine++;wrong=st.peek();expected=matching(wrong);message="Unmatched opening delimiter; expected '"+expected+"'.";}
  // Return either a balanced result or the first diagnostic and the final stack snapshot.
  Result r=message==null?new Result(true,"All delimiters are correctly matched.",-1,-1,null,null,st.display()):new Result(false,message,err,errLine,wrong,expected,st.display());
  return new Analysis(steps,r);
 }
 // Helpers recognize the supported bracket types and map each opener to its closer.
 public static boolean open(char c){return c=='('||c=='['||c=='{';}public static boolean close(char c){return c==')'||c==']'||c=='}';}
 public static char matching(char c){return c=='('?')':c=='['?']':'}';}
}
