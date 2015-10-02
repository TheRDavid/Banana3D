package gui.dialogs;

import files.Project;
import general.CurrentData;
import static general.CurrentData.execReset;
import static general.CurrentData.getProject;
import components.CancelButton;
import components.OKButton;
import dialogs.BasicDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import other.Wizard;
import sun.swing.FilePane;

public class CreateProjectDialog extends BasicDialog
{

    private OKButton okButton = new OKButton("OK", true);
    private CancelButton cancelButton;
    private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    private JFileChooser jfc = new JFileChooser();
    private JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private JTextField nameField = new JTextField("Weeee", 30);

    /**
     * Initializes the dialog and makes it visible
     */
    public CreateProjectDialog()
    {
        nameField.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                okButton.setEnabled(nameField.getText().length() != 0);
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    create();
                }
            }
        });
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                create();
            }
        });
        setTitle("Create Project");
        cancelButton = new CancelButton(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        namePanel.add(new JLabel("Project Name:"));
        namePanel.add(nameField);
        jfc.setMultiSelectionEnabled(false);
        jfc.setControlButtonsAreShown(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        add(namePanel, BorderLayout.NORTH);
        add(jfc, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        jfc.remove(jfc.getComponent(3));
        setVisible(true);
    }

    /**
     * Executed if the user is done chosing a project name and wants to
     * continue.
     */
    private void create()
    {
        boolean confirmed = true;
        //Warn the user if a folder of the same name already exists
        if (new File(jfc.getCurrentDirectory() + "//" + nameField.getText()).exists())
        {
            confirmed = JOptionPane.showConfirmDialog(
                    CreateProjectDialog.this, "A directory with this name already exists! This could lead to a loss of data!", "Warning!",
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
        }
        if (confirmed)
        {
            //Create a new project and set it as the main project
            Project project = new Project(jfc.getCurrentDirectory().getAbsolutePath(), nameField.getText());
            project.create();
            if (CurrentData.getEditorWindow().getB3DApp() != null)
            {
                execReset(false);
                CurrentData.buildSceneIntoEditor(project.getScene());
            } else
            {
                //Rearrange the 3D-B3D_Scene
                CurrentData.getEditorWindow().initNewScene(project.getScene());
            }
            CreateProjectDialog.this.dispose();
            //Save the project file
            Wizard.saveFile(getProject().getMainFolder().getAbsolutePath() + "//" + getProject().getMainFolder().getName() + ".b3dp", project);
        }
    }
}
