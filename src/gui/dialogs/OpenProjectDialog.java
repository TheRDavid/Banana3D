package gui.dialogs;

import components.BButton;
import components.CancelButton;
import dialogs.BasicDialog;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.VerticalLayout;
import other.B3D_Scene;
import other.Wizard;

/**
 *
 * @author David
 */
public class OpenProjectDialog extends BasicDialog
{

    private JPanel buttonPanel = new JPanel(new VerticalLayout(0));
    private JPanel mainPanel = new JPanel(new BorderLayout(20, 10));
    private BButton newButton = new BButton("New ...", new ImageIcon("dat//img//menu//new.png"));
    private BButton openButton = new BButton("Open", new ImageIcon("dat//img//menu//open.png"));
    private BButton renameButton = new BButton("Rename", new ImageIcon("dat//img//menu//edit.png"));
    private BButton deleteButton = new BButton("Delete", new ImageIcon("dat//img//menu//delete.png"));
    private JList<String> fileList;
    private File projectFile, sceneFile;
    private File[] sceneFiles;
    private Controller controller;

    public OpenProjectDialog(final File projectFile)
    {
        renameButton.setEnabled(false);
        deleteButton.setEnabled(false);
        this.projectFile = projectFile;
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);
        fileList = new JList<String>();
        fileList.setBorder(new EmptyBorder(10, 10, 10, 10));
        updateList();
        controller = new Controller();
        renameButton.setActionCommand("rename");
        renameButton.addActionListener(controller);
        JScrollPane scrollPane = new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        buttonPanel.add(newButton);
        buttonPanel.add(openButton);
        buttonPanel.add(renameButton);
        buttonPanel.add(deleteButton);
        newButton.addActionListener(controller);
        newButton.setActionCommand("new");
        openButton.addActionListener(controller);
        openButton.setActionCommand("open");
        deleteButton.addActionListener(controller);
        deleteButton.setActionCommand("delete");
        fileList.addListSelectionListener(controller);
        fileList.addMouseListener(controller);
        buttonPanel.add(new CancelButton(this));
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(new JLabel("Select a Scene-File or create a new one"), BorderLayout.NORTH);
        setTitle("Opening " + projectFile.getName());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateList()
    {
        sceneFiles = projectFile.getParentFile().listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getAbsolutePath().endsWith(".b3ds");
            }
        });
        String[] names = new String[sceneFiles.length];
        for (int i = 0; i < sceneFiles.length; i++)
            names[i] = sceneFiles[i].getName();
        fileList.setModel(new DefaultListModel<String>());
        for (String name : names)
            ((DefaultListModel) fileList.getModel()).addElement(name);
    }

    public File getSceneFile()
    {
        return sceneFile;
    }

    public File getProjectFile()
    {
        return projectFile;
    }

    private class Controller implements ActionListener, ListSelectionListener, MouseListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("new"))
            {
                String name = JOptionPane.showInputDialog("Scene Name:");
                if (name == null)
                    return;
                sceneFile = new File(projectFile.getParentFile().getAbsolutePath() + "//" + name + ".b3ds");
                if (sceneFile.exists()
                        && JOptionPane.showConfirmDialog(
                        rootPane,
                        "A file with that name already exists and will be overwirtten!\nDo you want to continue?",
                        "Warning",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                {
                    B3D_Scene newScene = new B3D_Scene(name);
                    Wizard.saveFile(sceneFile.getAbsolutePath(), newScene);
                    dispose();
                }
            } else if (e.getActionCommand().equals("rename"))
            {
                String newName = JOptionPane.showInputDialog("Rename to ...");
                if (newName != null)
                {
                    sceneFiles[fileList.getSelectedIndex()].renameTo(new File(sceneFiles[fileList.getSelectedIndex()].getParentFile().getAbsolutePath() + "//" + newName + ".b3ds"));
                    updateList();
                }
            } else if (e.getActionCommand().equals("open"))
            {
                if (fileList.getSelectedIndex() != -1)
                {
                    sceneFile = sceneFiles[fileList.getSelectedIndex()];
                    dispose();
                } else
                    JOptionPane.showMessageDialog(OpenProjectDialog.this, "Select a Scene!", "...", JOptionPane.WARNING_MESSAGE);
            } else if (e.getActionCommand().equals("delete"))
            {
                if (fileList.getSelectedIndex() != -1 && JOptionPane.showConfirmDialog(fileList, "Delete " + fileList.getSelectedValue() + "?") == JOptionPane.YES_OPTION)
                {
                    sceneFiles[fileList.getSelectedIndex()].delete();
                    updateList();
                }
            }
        }

        public void valueChanged(ListSelectionEvent e)
        {
            renameButton.setEnabled(fileList.getSelectedIndex() >= 0);
            deleteButton.setEnabled(fileList.getSelectedIndex() >= 0);
        }

        public void mouseClicked(MouseEvent e)
        {
            if (e.getClickCount() == 2)
                if (fileList.getSelectedIndex() != -1)
                {
                    sceneFile = sceneFiles[fileList.getSelectedIndex()];
                    dispose();
                }
        }

        public void mousePressed(MouseEvent e)
        {
        }

        public void mouseReleased(MouseEvent e)
        {
        }

        public void mouseEntered(MouseEvent e)
        {
        }

        public void mouseExited(MouseEvent e)
        {
        }
    }
}
