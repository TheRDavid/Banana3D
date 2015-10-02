package gui.editPanes;

import components.BButton;
import components.OKButton;
import java.awt.Color;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public abstract class EditTaskPane extends JXTaskPaneContainer
{

    protected JXTaskPane taskPane = new JXTaskPane();
    protected OKButton applyButton = new OKButton("Apply");

    public EditTaskPane()
    {
        setBackground(new Color(53, 50, 50));
    }

    public void updateData(boolean urgent)
    {
    }

    public void lastAction(){};
}
