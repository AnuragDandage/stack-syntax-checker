import java.util.Collections;import java.util.List;
/** Prepared simulation sequence consumed one character at a time. */
public final class Simulation {
 private List<SyntaxChecker.Step> steps=Collections.emptyList();private SyntaxChecker.Result result;private int cursor;
 // Analyze the code once, keep the resulting steps and reset playback to the beginning.
 public void prepare(String code){SyntaxChecker.Analysis a=new SyntaxChecker().analyze(code);steps=a.steps;result=a.result;cursor=0;}
 // Playback helpers expose the current position and safely stop at the end of the trace.
 public void reset(){cursor=0;}public boolean hasNext(){return cursor<steps.size();}public SyntaxChecker.Step next(){return hasNext()?steps.get(cursor++):null;}
 public int currentIndex(){return cursor;}public int totalSteps(){return steps.size();}public SyntaxChecker.Result result(){return result;}
}
