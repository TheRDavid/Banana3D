package gui.dialogs;

import components.BButton;
import components.OKButton;
import dialogs.BasicDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import se.datadosen.component.RiverLayout;

/**
 * NO LONGER USED! (but who knows, might need it later...)
 * @author David
 */
public class LightControlDuplicateDialog extends BasicDialog
{

    private ButtonGroup choiceGroup = new ButtonGroup();
    private JRadioButton remainButton = new JRadioButton("LightControl remains at the original Object");
    private JRadioButton transferButton = new JRadioButton("Transfer LightControl to the new Object");
    private JRadioButton duplicateButton = new JRadioButton("Also duplicate Light and LightControl");
    private OKButton okButton = new OKButton("Ok");
    private String choice;

    public LightControlDuplicateDialog()
    {
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                choice = choiceGroup.getSelection().getActionCommand();
                dispose();
            }
        });
        addWindowListener(new WindowListener()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {
            }

            @Override
            public void windowClosing(WindowEvent e)
            {
                choice = choiceGroup.getSelection().getActionCommand();
            }

            @Override
            public void windowClosed(WindowEvent e)
            {
                choice = choiceGroup.getSelection().getActionCommand();
            }

            @Override
            public void windowIconified(WindowEvent e)
            {
            }

            @Override
            public void windowDeiconified(WindowEvent e)
            {
            }

            @Override
            public void windowActivated(WindowEvent e)
            {
            }

            @Override
            public void windowDeactivated(WindowEvent e)
            {
            }
        });
        remainButton.setSelected(true);
        choiceGroup.add(remainButton);
        choiceGroup.add(transferButton);
        remainButton.setActionCommand("Remain");
        transferButton.setActionCommand("Transfer");
        duplicateButton.setActionCommand("Duplicate");
        choiceGroup.add(duplicateButton);
        setLayout(new RiverLayout(10, 12));
        add(new JLabel("This Object has a Light Control. One Light Control can not depend on two Objects."));
        add("br", remainButton);
        add("br", transferButton);
        add("br", duplicateButton);
        add("br right", okButton);
        pack();
        setSize(getWidth(), getHeight() + 10);
        setTitle("Light Control found");
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public String getAction()
    {
        return choice;
    }
}