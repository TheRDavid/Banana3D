package gui.menu;

import gui.dialogs.AutosaveOptionsDialog;
import gui.dialogs.SettingsDialog;
import general.CurrentData;
import dialogs.ObserverDialog;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import other.Wizard;

public class ExtrasMenu extends JMenu implements ActionListener
{

    private JMenuItem openFolderItem = new JMenuItem("Open Assets-Folder", new ImageIcon("dat//img//menu//open.png"));
    private JMenu autosaveMenu = new JMenu("Autosave");
    private JCheckBoxMenuItem autosaveEnabledItem = new JCheckBoxMenuItem("Enabled", new ImageIcon("dat//img//menu//autosaveEnabled.png"));
    private JMenuItem autosaveOptionsItem = new JMenuItem("Options", new ImageIcon("dat//img//menu//settings.png"));
    private JMenuItem screenshotItem = new JMenuItem("Screenshot", new ImageIcon("dat//img//menu//screenshot.png"));
    private JMenuItem recordItem = new JMenuItem("Record", new ImageIcon("dat//img//menu//record.png"));
    private JMenuItem configItem = new JMenuItem("Settings", new ImageIcon("dat//img//menu//settings.png"));
    private ArrayList<AppItem> appItems = new ArrayList<AppItem>();

    public ExtrasMenu()
    {
        autosaveMenu.setIcon(new ImageIcon("dat//img//menu//autosave.png"));
        initShortcuts();
        setText("Extras");
        add(openFolderItem);
        add(autosaveMenu);
        autosaveMenu.add(autosaveEnabledItem);
        autosaveMenu.add(autosaveOptionsItem);
        add(screenshotItem);
        add(recordItem);
        add(configItem);
        add(new JSeparator());
        File apps = new File("dat//apps");
        if (!apps.exists())
            apps.mkdir();
        for (File f : apps.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        }))
        {
            AppItem appItem = new AppItem(f.getName());
            add(appItem);
            appItems.add(appItem);
        }
        initActions();
    }

    private void initActions()
    {
        configItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new SettingsDialog();
            }
        });
        autosaveEnabledItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getProject().getAutosaveOptions().setEnabled(autosaveEnabledItem.isSelected());
                Wizard.saveFile(CurrentData.getProject().getMainFolder().getAbsolutePath() + "//" + CurrentData.getProject().getMainFolder().getName() + ".b3dp", CurrentData.getProject());
            }
        });
        autosaveOptionsItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new AutosaveOptionsDialog();
            }
        });
        screenshotItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execScreenshot();
            }
        });
        recordItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execRecord();
            }
        });
        openFolderItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Desktop.getDesktop().open(CurrentData.getProject().getAssetsFolder());
                } catch (IOException ex)
                {
                    ObserverDialog.getObserverDialog().printError("Error opening Asset-Folder in File Manager", ex);
                }
            }
        });
    }

    private void initShortcuts()
    {
        screenshotItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        recordItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().startsWith("app"))
        {
            try
            {
                ObserverDialog.getObserverDialog().printMessage("Starting app: " + e.getActionCommand().substring(3));
                String command1 = "dat//apps//" + e.getActionCommand().substring(3);                                                    // app
                String command2 = CurrentData.getProject().getAssetsFolder().getAbsolutePath();                                         // assetsFolder
                Runtime.getRuntime().exec("java -jar " + command1 + " " + command2);
            } catch (IOException ex)
            {
                ObserverDialog.getObserverDialog().printError("Failed to execute " + e.getActionCommand().substring(3), ex);
                Logger.getLogger(ExtrasMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    class AppItem extends JMenuItem
    {

        public AppItem(String appName)
        {
            setActionCommand("app" + appName + "//" + appName + ".jar");
            setText(appName);
            setIcon(new ImageIcon("dat//apps//" + appName + "//" + appName + ".png"));
            addActionListener(ExtrasMenu.this);
        }
    }

    public JMenuItem getScreenshotItem()
    {
        return screenshotItem;
    }

    public JMenuItem getRecordItem()
    {
        return recordItem;
    }

    public JMenu getAutosaveMenu()
    {
        return autosaveMenu;
    }

    public JCheckBoxMenuItem getAutosaveEnabledItem()
    {
        return autosaveEnabledItem;
    }

    public JMenuItem getOpenFolderItem()
    {
        return openFolderItem;
    }

    public ArrayList<AppItem> getAppItems()
    {
        return appItems;
    }
}
