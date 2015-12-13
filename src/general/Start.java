package general;

import gui.editor.EditorWindow;
import dialogs.ObserverDialog;
import dialogs.SplashDialog;
import java.awt.Color;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Start
{

    /**
     * Creates an Observer-Dialog to Display Messages to the user. Also
     * checking for a configuration-file, loading all Material files found,
     * displaying the Splash-Dialog and setting the LookAndFeel. Atl last,
     * opening the Editor Window.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        de.muntjak.tinylookandfeel.Theme.loadTheme(new File("dat//dark.theme"));
        try
        {
            UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
            UIManager.getDefaults().put("TaskPane.titleBackgroundGradientStart", Color.gray);
            UIManager.getDefaults().put("TaskPane.titleBackgroundGradientEnd", Color.lightGray);
        } catch (ClassNotFoundException ex)
        {
            ObserverDialog.getObserverDialog().printError("Failed to modify LookAndFeel", ex);
        } catch (InstantiationException ex)
        {
            ObserverDialog.getObserverDialog().printError("Failed to modify LookAndFeel", ex);
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            ObserverDialog.getObserverDialog().printError("Failed to modify LookAndFeel", ex);
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex)
        {
            ObserverDialog.getObserverDialog().printError("Failed to modify LookAndFeel", ex);
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        CurrentData.setSplashDialog(new SplashDialog(new ImageIcon("dat//img//other//splash.png")));

        CurrentData.loadPreferences();

        CurrentData.findMaterials();
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new EditorWindow();
            }
        });
    }
}
