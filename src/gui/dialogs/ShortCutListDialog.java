package gui.dialogs;

import dialogs.BasicDialog;
import java.awt.BorderLayout;
import javax.swing.JLabel;

public class ShortCutListDialog extends BasicDialog
{

    private JLabel list = new JLabel("<html><table cellspacing=\"4\" cellpadding=\"4\"><tr><td></td><td><h3>Description</h3></td></tr>"
            + "<tr><td>Ctrl+N</td><td>Create a new Project / Scene</td></tr>"
            + "<tr><td>Ctrl+0</td><td>Open Project / Scene</td></tr>"
            + "<tr><td>Ctrl+S</td><td>Save</td></tr>"
            + "<tr><td>Ctrl+Q</td><td>Quit</td></tr>"
            + "<tr><td>Ctrl+T</td><td>Duplicate</td></tr>"
            + "<tr><td>Ctrl+F</td><td>Find</td></tr>"
            + "<tr><td>Ctrl+R</td><td>Rename</td></tr>"
            + "<tr><td>Ctrl+L</td><td>Look at the selected object</td></tr>"
            + "<tr><td>Ctrl+M</td><td>MotionPath play / pause</td></tr>"
            + "<tr><td>DEL</td><td>Delete</td></tr>"
            + "<tr><td>F1</td><td>Screenshot</td></tr>"
            + "<tr><td>F2</td><td>Recorder</td></tr>"
            + "<tr><td>F6</td><td>Start Physics</td></tr>"
            + "<tr><td>F7</td><td>Fullscreen on / off</td></tr>"
            + "<tr><td>Left Shift</td><td>Speed camera up</td></tr>"
            + "<tr><td>Space</td><td>Navigate without picking</td></tr>"
            + "<tr><td>X</td><td>Select X-Axis</td></tr>"
            + "<tr><td>Y</td><td>Select Y-Axis</td></tr>"
            + "<tr><td>C</td><td>Select Z-Axis</td></tr></table></html>");

    public ShortCutListDialog()
    {
        setModal(false);
        setAlwaysOnTop(true);
        setResizable(false);
        setTitle("Shortcuts");
        add(list, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
