package gui.dialogs;

import general.CurrentData;
import components.BButton;
import components.CancelButton;
import dialogs.BasicDialog;
import general.Preference;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ExitDialog extends BasicDialog
{

    private JCheckBox dontBotherCheckBox = new JCheckBox("Don't bother me again");
    private JPanel buttonPanel = new JPanel();
    private BButton okButton = new BButton("Exit", new ImageIcon("dat//img//menu//quit.png"));
    private CancelButton cancelButton;

    public ExitDialog()
    {
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getPrefs().set(Preference.EXIT_WITHOUT_PROMPT, dontBotherCheckBox.isSelected());
                CurrentData.getPrefs().save();
                System.exit(0);
            }
        });
        dontBotherCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
        cancelButton = new CancelButton(this);
        setTitle("Exit");
        JLabel msgLabel = new JLabel("Do you want to exit? All unsaved data will be lost!", JLabel.CENTER);
        msgLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(msgLabel, BorderLayout.NORTH);
        add(dontBotherCheckBox);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
