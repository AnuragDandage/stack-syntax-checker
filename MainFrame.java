import java.awt.*;import javax.swing.*;import javax.swing.event.*;import java.util.StringJoiner;
/** Educational dark-themed Swing interface for stack based delimiter checking. */
public final class MainFrame extends JFrame {
 // Shared color palette keeps the editor, panels, status text, and results visually consistent.
 private static final Color BG=new Color(15,23,42),P=new Color(24,35,58),P2=new Color(30,43,69),FG=new Color(231,238,250),M=new Color(151,166,191),T=new Color(45,212,191),B=new Color(96,165,250),R=new Color(248,113,113);
 // These components belong to the code checker, custom checker, shared visualization, and footer.
 private final JTextArea editor=new JTextArea(),log=new JTextArea(),detail=new JTextArea(),customExplanation=new JTextArea();
 private final JTextField customInput=new JTextField();
 private final JLabel status=new JLabel("Ready"),progress=new JLabel("Character 0 / 0"),result=new JLabel("Awaiting analysis"),customResult=new JLabel("Enter a bracket sequence");
 // Separate simulation models and saved display state prevent one checker from overwriting the other's progress.
 private final StackView stack=new StackView();private final Simulation sim=new Simulation();private final JSlider speed=new JSlider(1,10,6);private final Timer timer;
 private final CustomBracketChecker customChecker=new CustomBracketChecker();
 private CustomBracketChecker.Analysis customAnalysis;private int customCursor;private boolean customMode;
 private final StringBuilder codeLog=new StringBuilder(),customLog=new StringBuilder();
 private char[] codeStackState=new char[0],customStackState=new char[0];
 // Built-in source examples let the learner try balanced, unmatched, and mismatched code.
 private static final String[] N={"Choose a sample…","Balanced example","Missing closing brace","Mismatched delimiters"};
 private static final String[] S={"","public class Main {\n  public static void main(String[] args) {\n    int a = (10 + 20);\n    System.out.println(a);\n  }\n}","public class Main {\n  public static void main(String[] args) {\n    int a = (10 + 20);\n  }","public class Main {\n  public static void main(String[] args) {\n    int[] arr = {10, 20, 30);\n  }\n}"};
 // Create the window, timer routing, panels, and initial editor contents.
 public MainFrame(){super("Stack Syntax Checker");setDefaultCloseOperation(EXIT_ON_CLOSE);setMinimumSize(new Dimension(1020,700));setSize(1240,820);setLocationRelativeTo(null);getContentPane().setBackground(BG);setLayout(new BorderLayout());timer=new Timer(delay(),e->{if(customMode)advanceCustom();else advance();});add(header(),BorderLayout.NORTH);add(content(),BorderLayout.CENTER);add(footer(),BorderLayout.SOUTH);editor.setText(S[1]);}
 // Build the title, purpose line, and Java/Swing badge.
 private JPanel header(){JPanel p=new JPanel(new BorderLayout());p.setBackground(BG);p.setBorder(BorderFactory.createEmptyBorder(18,22,14,22));JLabel t=new JLabel("STACK SYNTAX CHECKER");t.setForeground(FG);t.setFont(new Font("SansSerif",1,22));JLabel s=new JLabel("Interactive stack based delimiter matching visualizer");s.setForeground(M);JPanel a=new JPanel(new GridLayout(2,1));a.setOpaque(false);a.add(t);a.add(s);JLabel badge=new JLabel("JAVA 17  •  SWING");badge.setForeground(T);p.add(a);p.add(badge,BorderLayout.EAST);return p;}
 // Arrange the editor and code controls on the left, checker/stack panels on the right, and operation log below.
 private JPanel content(){
  JPanel root=new JPanel(new BorderLayout(12,12));root.setBackground(BG);root.setBorder(BorderFactory.createEmptyBorder(0,18,12,18));
  JPanel left=new JPanel(new BorderLayout(10,10));left.setOpaque(false);left.add(editorCard());left.add(controls(),BorderLayout.SOUTH);
  JPanel right=new JPanel(new BorderLayout(10,10));right.setOpaque(false);right.add(stackCard());JPanel lowerRight=new JPanel(new GridLayout(2,1,8,8));lowerRight.setOpaque(false);lowerRight.add(resultCard());lowerRight.add(customCard());right.add(lowerRight,BorderLayout.SOUTH);
  JSplitPane top=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,right);top.setResizeWeight(.62);top.setBorder(null);
  JPanel lc=card("OPERATION LOG  ·  Character / operation / reason / stack");log.setEditable(false);log.setFont(new Font(Font.MONOSPACED,0,12));log.setBackground(new Color(12,19,33));log.setForeground(FG);lc.add(new JScrollPane(log));JSplitPane all=new JSplitPane(JSplitPane.VERTICAL_SPLIT,top,lc);all.setResizeWeight(.68);all.setBorder(null);root.add(all);return root;
 }
 // Build the code editor, line-number gutter, sample selector, and editor clear button.
 private JPanel editorCard(){
  JPanel p=card("CODE EDITOR  ·  Paste code or choose a sample");editor.setFont(new Font(Font.MONOSPACED,0,14));editor.setTabSize(4);editor.setBackground(new Color(12,19,33));editor.setForeground(FG);editor.setCaretColor(T);editor.setMargin(new Insets(10,10,10,10));JScrollPane sc=new JScrollPane(editor);sc.setRowHeaderView(new Lines(editor));p.add(sc);
  JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0));row.setOpaque(false);JComboBox<String> samples=new JComboBox<>(N);samples.addActionListener(e->{int i=samples.getSelectedIndex();if(i>0)editor.setText(S[i]);});JButton clear=button("CLEAR");clear.addActionListener(e->editor.setText(""));row.add(samples);row.add(clear);p.add(row,BorderLayout.SOUTH);return p;
 }
 // Connect code playback buttons and the shared speed slider to the code simulation timer.
 private JPanel controls(){
  JPanel p=card("SIMULATION CONTROLS  ·  Observe each character");JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0));row.setOpaque(false);JButton play=button("▶ PLAY"),pause=button("Ⅱ PAUSE"),next=button("NEXT STEP →"),reset=button("↺ RESET");
  play.addActionListener(e->start());pause.addActionListener(e->{timer.stop();status.setText("Paused");});next.addActionListener(e->{timer.stop();advance();});reset.addActionListener(e->prepare());row.add(play);row.add(pause);row.add(next);row.add(reset);p.add(row);
  speed.setOpaque(false);speed.setPaintTicks(true);speed.setMajorTickSpacing(3);speed.addChangeListener(e->timer.setDelay(delay()));JPanel sr=new JPanel(new BorderLayout());sr.setOpaque(false);sr.add(new JLabel("SPEED"),BorderLayout.WEST);sr.add(speed);p.add(sr,BorderLayout.SOUTH);return p;
 }
 // Show the active stack, result panels, and custom bracket entry controls.
 private JPanel stackCard(){JPanel p=card("STACK VISUALIZATION  ·  Top element shown first");stack.setPreferredSize(new Dimension(280,330));p.add(stack);JLabel legend=new JLabel("PUSH  •  MATCH  •  ERROR");legend.setForeground(T);p.add(legend,BorderLayout.SOUTH);return p;}
 // Display the code checker's independent final result and diagnostic text.
 private JPanel resultCard(){JPanel p=card("ANALYSIS RESULT  ·  Delimiter validation only");result.setForeground(M);result.setFont(new Font("SansSerif",1,15));detail.setEditable(false);detail.setOpaque(false);detail.setForeground(M);detail.setLineWrap(true);detail.setWrapStyleWord(true);JPanel body=new JPanel(new BorderLayout(0,4));body.setOpaque(false);body.add(result,BorderLayout.NORTH);body.add(detail,BorderLayout.CENTER);p.add(body,BorderLayout.CENTER);return p;}
 // Build custom input, validation actions, independent playback controls, and custom result explanation.
 private JPanel customCard(){
  JPanel p=card("CUSTOM BRACKET CHECKER");
  // Style the single-line input and add CHECK/CLEAR actions.
  customInput.setFont(new Font(Font.MONOSPACED,0,14));customInput.setBackground(new Color(12,19,33));customInput.setForeground(FG);customInput.setCaretColor(T);customInput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(51,65,91)),BorderFactory.createEmptyBorder(5,8,5,8)));customInput.setToolTipText("Enter only brackets: (), {}, []");
  JPanel top=new JPanel(new BorderLayout(6,0));top.setOpaque(false);top.add(customInput,BorderLayout.CENTER);JButton check=button("CHECK"),clear=button("CLEAR");JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));actions.setOpaque(false);actions.add(check);actions.add(clear);top.add(actions,BorderLayout.EAST);
  // Wire the custom controls to custom playback while sharing only the timer and visible stack panel.
  JPanel playback=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));playback.setOpaque(false);JButton play=button("▶ PLAY"),pause=button("Ⅱ PAUSE"),next=button("NEXT STEP →"),reset=button("↺ RESET");play.addActionListener(e->startCustom());pause.addActionListener(e->{timer.stop();status.setText("Custom check paused");});next.addActionListener(e->{timer.stop();advanceCustom();});reset.addActionListener(e->resetCustom());check.addActionListener(e->prepareCustom(true));clear.addActionListener(e->clearCustom());customInput.addActionListener(e->prepareCustom(true));playback.add(play);playback.add(pause);playback.add(next);playback.add(reset);
  customResult.setForeground(M);customResult.setFont(new Font("SansSerif",1,13));customExplanation.setEditable(false);customExplanation.setOpaque(false);customExplanation.setForeground(M);customExplanation.setLineWrap(true);customExplanation.setWrapStyleWord(true);customExplanation.setRows(2);
  JPanel body=new JPanel(new BorderLayout(4,3));body.setOpaque(false);body.add(top,BorderLayout.NORTH);body.add(playback,BorderLayout.CENTER);JPanel summary=new JPanel(new BorderLayout(0,2));summary.setOpaque(false);summary.add(customResult,BorderLayout.NORTH);summary.add(customExplanation,BorderLayout.CENTER);body.add(summary,BorderLayout.SOUTH);p.add(body,BorderLayout.CENTER);return p;
 }
 // Build the footer status, reusable card frame, and consistent buttons.
 private JPanel footer(){JPanel p=new JPanel(new BorderLayout());p.setBackground(new Color(10,16,29));p.setBorder(BorderFactory.createEmptyBorder(8,22,8,22));status.setForeground(M);progress.setForeground(T);p.add(status);p.add(progress,BorderLayout.EAST);return p;}
 // Create a reusable, consistently bordered panel with a styled section heading.
 private JPanel card(String title){JPanel p=new JPanel(new BorderLayout(0,9));p.setBackground(P);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(46,61,86)),BorderFactory.createEmptyBorder(12,13,13,13)));JLabel h=new JLabel(title);h.setForeground(FG);h.setFont(new Font("SansSerif",1,11));p.add(h,BorderLayout.NORTH);return p;}
 // Apply the common foreground, background, and focus style to buttons.
 private JButton button(String s){JButton b=new JButton(s);b.setFocusPainted(false);b.setBackground(P2);b.setForeground(FG);return b;}
 // Restore the code trace's saved log, stack snapshot, and progress when returning from custom mode.
 private void showCodeState(){if(customMode){customMode=false;log.setText(codeLog.toString());log.setCaretPosition(log.getDocument().getLength());stack.setItems(codeStackState);update(sim.currentIndex(),sim.totalSteps());}}
 // Restore the custom trace's saved log, stack snapshot, and progress when entering custom mode.
 private void showCustomState(){if(!customMode){customMode=true;log.setText(customLog.toString());log.setCaretPosition(log.getDocument().getLength());stack.setItems(customStackState);update(customCursor,customAnalysis==null?0:customAnalysis.steps.size());}}
 // Prepare the Java code checker and reset only its saved display and result state.
 private void prepare(){timer.stop();customMode=false;sim.prepare(editor.getText());codeLog.setLength(0);codeStackState=new char[0];log.setText("");stack.setItems(codeStackState);result.setText("SIMULATION READY");result.setForeground(B);detail.setText("Press PLAY or NEXT STEP to begin.");update(0,sim.totalSteps());status.setText("Prepared "+sim.totalSteps()+" character steps");}
 // Start or restart timed playback for the Java code checker.
 private void start(){showCodeState();if(sim.totalSteps()==0||!sim.hasNext())prepare();if(sim.hasNext()){timer.setDelay(delay());timer.start();status.setText("Playing code simulation");}else finish();}
 // Advance one code character, save its stack state and log entry, then finish when exhausted.
 private void advance(){showCodeState();if(sim.totalSteps()==0)prepare();SyntaxChecker.Step s=sim.next();if(s==null){timer.stop();finish();return;}codeStackState=s.stack.clone();stack.setItems(codeStackState);String row=String.format("%4d %-6s %-6s %s  Stack: %s%n",s.index+1,s.visible(),s.operation,s.reason,format(s.stack));codeLog.append(row);log.append(row);log.setCaretPosition(log.getDocument().getLength());update(sim.currentIndex(),sim.totalSteps());status.setText(s.reason);if(!sim.hasNext()){timer.stop();finish();}}
 // Validate custom input and reset only the custom trace before optional autoplay.
 private void prepareCustom(boolean autoplay){
  timer.stop();customMode=true;customAnalysis=customChecker.analyze(customInput.getText());customCursor=0;customLog.setLength(0);customStackState=new char[0];log.setText("");stack.setItems(customStackState);update(0,customAnalysis.steps.size());
  if(!customAnalysis.result.valid){customResult.setText(customAnalysis.result.message);customResult.setForeground(R);customExplanation.setText(customAnalysis.result.explanation);status.setText(customAnalysis.result.message);return;}
  customResult.setText("CHECK IN PROGRESS");customResult.setForeground(B);customExplanation.setText("Use PLAY or NEXT STEP to watch the custom stack.");status.setText("Custom sequence prepared");
  if(autoplay)startCustom();
 }
 // Start the prepared custom trace using the shared timer's custom-mode route.
 private void startCustom(){
  if(customAnalysis==null){prepareCustom(false);if(customAnalysis==null||!customAnalysis.result.valid)return;}
  showCustomState();if(customCursor>=customAnalysis.steps.size()){finishCustom();return;}timer.setDelay(delay());timer.start();status.setText("Playing custom bracket check");
 }
 // Advance one custom character and update the visible stack, operation log, and explanation.
 private void advanceCustom(){
  showCustomState();if(customAnalysis==null||!customAnalysis.result.valid)return;
  if(customCursor>=customAnalysis.steps.size()){timer.stop();finishCustom();return;}
  CustomBracketChecker.Step s=customAnalysis.steps.get(customCursor++);customStackState=s.stack.clone();stack.setItems(customStackState);
  String row=String.format("%4d '%s' %-6s %s  Stack: %s%n",s.index+1,s.character,s.operation,s.reason,format(s.stack));customLog.append(row);log.append(row);
  log.setCaretPosition(log.getDocument().getLength());customExplanation.setText("Character '"+s.character+"'  •  "+s.operation+"\n"+s.reason);
  update(customCursor,customAnalysis.steps.size());status.setText(s.reason);
  if(!customAnalysis.result.balanced&&s.error){timer.stop();finishCustom();}
  else if(customCursor>=customAnalysis.steps.size()){timer.stop();finishCustom();}
 }
 // Present the custom check's final balance status and explanation.
 private void finishCustom(){
  if(customAnalysis==null||!customAnalysis.result.valid)return;CustomBracketChecker.Result r=customAnalysis.result;
  customResult.setText(r.message);customResult.setForeground(r.balanced?T:R);customExplanation.setText(r.explanation);
  customStackState=r.finalStack.clone();stack.setItems(customStackState);status.setText("Custom check complete — "+r.message);
 }
 // Reanalyze and replay the current custom sequence, or clear it if empty.
 private void resetCustom(){if(customInput.getText().trim().isEmpty()){clearCustom();return;}prepareCustom(true);}
 // Clear only custom input, results, trace, and saved custom display state.
 private void clearCustom(){timer.stop();customMode=true;customAnalysis=null;customCursor=0;customLog.setLength(0);customStackState=new char[0];customInput.setText("");customResult.setText("Enter a bracket sequence");customResult.setForeground(M);customExplanation.setText("");log.setText("");stack.setItems(customStackState);update(0,0);status.setText("Custom checker cleared");}
 // Show the code checker's stored balanced result or delimiter diagnostic.
 private void finish(){SyntaxChecker.Result r=sim.result();if(r==null)return;if(r.balanced){result.setText("✓ BALANCED");result.setForeground(T);detail.setText("All delimiters are correctly matched.\nFinal stack: EMPTY");status.setText("Analysis complete — balanced");}else{result.setText("✕ NOT BALANCED");result.setForeground(R);detail.setText(r.message+"\nCharacter: "+(r.incorrect==null?"end of input":"'"+r.incorrect+"'")+" • line "+r.line+"\n"+(r.expected==null?"No opening delimiter available.":"Expected closing delimiter: '"+r.expected+"'.")+"\nStack: "+format(r.finalStack));status.setText("Analysis complete — mismatch found");}}
 // Convert slider position to a timer delay, update the footer counter, and format stack contents.
 private int delay(){return 1050-speed.getValue()*95;}private void update(int n,int total){progress.setText("Character "+n+" / "+total);}
 // Format a bottom-to-top stack snapshot for display in the operation log.
 private String format(char[] a){if(a==null||a.length==0)return "EMPTY";StringJoiner j=new StringJoiner(", ","[","]");for(char c:a)j.add(""+c);return j.toString();}
 // Keep the editor's line-number gutter synchronized with document changes.
 private static final class Lines extends JTextArea{
  Lines(JTextArea src){setEditable(false);setBackground(new Color(17,26,43));setForeground(new Color(100,125,158));setFont(new Font(Font.MONOSPACED,0,14));setMargin(new Insets(10,8,10,8));setPreferredSize(new Dimension(42,80));src.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){refresh();}public void removeUpdate(DocumentEvent e){refresh();}public void changedUpdate(DocumentEvent e){refresh();}void refresh(){StringBuilder b=new StringBuilder();for(int i=1;i<=Math.max(1,src.getLineCount());i++)b.append(i).append('\n');setText(b.toString());}});setText("1\n");}
 }
 // Paint the stack from top to bottom, including its empty state and overflow indicator.
 private static final class StackView extends JPanel{
  char[] items=new char[0];StackView(){setBackground(new Color(12,19,33));setBorder(BorderFactory.createLineBorder(new Color(51,65,91)));}
  // Copy each stack snapshot so later stack operations cannot alter the current drawing.
  void setItems(char[] a){items=a.clone();repaint();}
  // Draw only as many stack elements as fit in the panel; element zero is the bottom of the stack.
  protected void paintComponent(Graphics g){super.paintComponent(g);int w=getWidth(),h=getHeight();g.setColor(M);g.setFont(new Font("SansSerif",1,12));g.drawString("TOP",14,23);g.drawLine(14,30,w-14,30);if(items.length==0){g.setFont(new Font("SansSerif",0,15));g.drawString("EMPTY",w/2-25,h/2);return;}int bh=Math.min(42,Math.max(27,(h-48)/Math.min(items.length,7))),bw=Math.min(150,w-50),n=Math.min(items.length,Math.max(1,(h-42)/bh));for(int k=0;k<n;k++){int i=items.length-1-k,x=(w-bw)/2,y=39+k*bh;g.setColor(k==0?new Color(15,91,94):P2);g.fillRoundRect(x,y,bw,bh-5,9,9);g.setColor(k==0?T:new Color(74,96,129));g.drawRoundRect(x,y,bw,bh-5,9,9);g.setFont(new Font(Font.MONOSPACED,1,18));g.drawString(""+items[i],x+16,y+bh/2+4);if(k==0){g.setFont(new Font("SansSerif",1,10));g.drawString("LATEST",x+bw-55,y+bh/2+4);}}if(items.length>n){g.setColor(M);g.drawString("+"+(items.length-n)+" below",14,h-10);}}
 }
}
