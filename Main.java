import javax.swing.SwingUtilities;

// Starts the application and schedules Swing window creation on its event thread.
public final class Main {private Main(){}public static void main(String[]a){SwingUtilities.invokeLater(()->new MainFrame().setVisible(true));}}
