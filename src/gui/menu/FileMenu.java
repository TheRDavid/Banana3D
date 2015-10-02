package gui.menu;

import files.Project;
import gui.dialogs.OpenProjectDialog;
import general.CurrentData;
import static general.CurrentData.execReset;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;

public class FileMenu extends JMenu
{

    private JMenu newMenu = new JMenu("New");
    private JMenuItem newProjectItem = new JMenuItem("Project", new ImageIcon("dat//img//menu//newProject.png"));
    private JMenuItem newSceneItem = new JMenuItem("Scene", new ImageIcon("dat//img//menu//newScene.png"));
    private JMenuItem switchSceneItem = new JMenuItem("Switch Scene", new ImageIcon("dat//img//menu//switch.png"));
    private JMenuItem openItem = new JMenuItem("Open Project", new ImageIcon("dat//img//menu//open.png"));
    private JMenu openRecentMenu = new JMenu("Open Recent");
    private JMenuItem clearRecentItemsItem = new JMenuItem("Clear", new ImageIcon("dat//img//menu//clear.png"));
    private JMenuItem saveItem = new JMenuItem("Save", new ImageIcon("dat//img//menu//save.png"));
    private JMenuItem saveAsItem = new JMenuItem("Save as...", new ImageIcon("dat//img//menu//saveAs.png"));
    private JMenuItem quitItem = new JMenuItem("Quit", new ImageIcon("dat//img//menu//quit.png"));

    public FileMenu()
    {
        newMenu.setIcon(new ImageIcon("dat//img//menu//new.png"));
        openRecentMenu.setIcon(new ImageIcon("dat//img//menu//openRecent.png"));
        initShortcuts();
        setText("File");
        add(newMenu);
        newMenu.add(newProjectItem);
        newMenu.add(newSceneItem);
        add(switchSceneItem);
        add(openItem);
        add(openRecentMenu);
        add(saveItem);
        add(saveAsItem);
        add(new JSeparator());
        add(quitItem);
        initActions();
    }

    private void initActions()
    {
        addMenuListener(new MenuListener()
        {
            @Override
            public void menuSelected(MenuEvent e)
            {
                openRecentMenu.removeAll();
                for (String s : CurrentData.getConfiguration().recentProjectPaths)
                {
                    openRecentMenu.add(new PathMenuItem(s));
                }
                openRecentMenu.add(new JSeparator());
                openRecentMenu.add(clearRecentItemsItem);
                clearRecentItemsItem.setEnabled(CurrentData.getConfiguration().recentProjectPaths.size() > 0);
            }

            @Override
            public void menuDeselected(MenuEvent e)
            {
            }

            @Override
            public void menuCanceled(MenuEvent e)
            {
            }
        });
        switchSceneItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                OpenProjectDialog ssd = new OpenProjectDialog(CurrentData.getProject().getProjectFile());
                if (ssd.getSceneFile() != null)
                {
                    Project pr = new Project(ssd.getProjectFile(), ssd.getSceneFile());
                    if (CurrentData.getEditorWindow().getB3DApp() != null)
                    {
                        //Reset B3D_Scene if there is one and load the content of the new (or loaded) one
                        execReset(false);
                        CurrentData.buildSceneIntoEditor(pr.getScene());
                    } else
                    {
                        //Of no B3D_Scene was opened before, initialize the 3D-Context
                        CurrentData.getEditorWindow().initNewScene(pr.getScene());
                    }
                }
            }
        });
        clearRecentItemsItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getConfiguration().recentProjectPaths.clear();
            }
        });
        newProjectItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execNewProject();
            }
        });
        newSceneItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execNewScene();
            }
        });
        openItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execOpenProject(null);
            }
        });
        saveItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execSaveScene(CurrentData.getProject().getMainFolder().getAbsolutePath() + "/" + CurrentData.getProject().getScene().getName() + ".b3ds");
            }
        });
        saveAsItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileFilter(new FileFilter()
                {
                    @Override
                    public boolean accept(File f)
                    {
                        return (f.isDirectory() || f.getName().endsWith(".b3ds"));
                    }

                    @Override
                    public String getDescription()
                    {
                        return "B3D Scene";
                    }
                });
                jfc.showSaveDialog(CurrentData.getEditorWindow());
                if (jfc.getSelectedFile() != null)
                {
                    String path = jfc.getSelectedFile().getAbsolutePath();
                    if (!path.endsWith(".b3ds"))
                    {
                        path += ".b3ds";
                    }
                    CurrentData.execSaveScene(path);
                }
            }
        });
        quitItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execQuit();
            }
        });
    }

    public JMenuItem getSaveItem()
    {
        return saveItem;
    }

    public JMenuItem getSaveAsItem()
    {
        return saveAsItem;
    }

    private void initShortcuts()
    {
        newSceneItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        quitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
    }

    private class PathMenuItem extends JMenuItem
    {

        public PathMenuItem(String text)
        {
            super(text);
            setIcon(new ImageIcon("dat//img//menu//recent.png"));
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    File file = new File(getText());
                    if (file.exists())
                        CurrentData.execOpenProject(getText());
                    else
                    {
                        if (JOptionPane.showConfirmDialog(openRecentMenu, "File could not be found\nDelete entry?", "Whops", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION)
                            CurrentData.getConfiguration().recentProjectPaths.remove(file.getAbsolutePath());
                    }
                }
            });
        }
    }

    public JMenuItem getSwitchSceneItem()
    {
        return switchSceneItem;
    }
}