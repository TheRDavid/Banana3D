package general;

import files.Project;
import dialogs.ObserverDialog;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David
 */
public class AutosaveRunnable implements Runnable
{

    private int wait = 0;
    private boolean interrupted = false;

    @Override
    public void run()
    {
        while (true)
        {
            wait = 0;
            while (!interrupted && wait < CurrentData.getProject().getAutosaveOptions().getMinutesPerSave())
            {
                try
                {
                    Thread.sleep(60000);
                } catch (InterruptedException ex)
                {
                    ObserverDialog.getObserverDialog().printError("Thread.sleep in AutoSaveRunnable interrupted", ex);
                    Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                }
                wait++;
            }
            if (CurrentData.getProject().getAutosaveOptions().isEnabled())
            {
                ObserverDialog.getObserverDialog().printMessage("Autosave at " + CurrentData.getProject().getAutosaveOptions().getPath());
                System.out.println("autosaving");
                CurrentData.execSaveScene(CurrentData.getProject().getAutosaveOptions().getPath());
            }
            interrupted = false;
        }
    }

    public void interrupt()
    {
        interrupted = true;
    }
};