import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import ui.LoginFrame;
import utils.AppLogger;
import utils.AppPaths;

public class App {

    public static void main(String[] args) {
        AppPaths.ensureApplicationFolders();
        AppLogger.configure();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }

            new LoginFrame();
            // new Dashboard();
        });
    }
}