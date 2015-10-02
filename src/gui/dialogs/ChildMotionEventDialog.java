package gui.dialogs;

import general.CurrentData;
import components.CancelButton;
import dialogs.BasicDialog;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

public class ChildMotionEventDialog extends BasicDialog
{

    private CancelButton okButton;
    private JCheckBox dontBotherMeAgainCheckBox = new JCheckBox("Don't bother me again");

    public ChildMotionEventDialog()
    {
        setTitle("Just so you know");
        setLayout(new BorderLayout(0, 10));
        okButton = new CancelButton(this, "Got it");
        add(new JLabel("<html><body><p>While MotionPaths are using absolute coordinates, the selected Spatial appears to be the child of a node,</p><p>"
                + "so it's location, even while the Motion Event is playing, will depend on the node's location.</p></body></html>"),
                BorderLayout.NORTH);
        add(dontBotherMeAgainCheckBox, BorderLayout.CENTER);
        add(okButton, BorderLayout.SOUTH);
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getConfiguration().setRemindOfNodeChildrenAsMotionEventSpatial(!dontBotherMeAgainCheckBox.isSelected());
            }
        });
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
