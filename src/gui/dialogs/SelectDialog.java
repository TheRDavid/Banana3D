package gui.dialogs;

import components.BButton;
import components.CancelButton;
import dialogs.BasicDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import se.datadosen.component.RiverLayout;

public class SelectDialog extends BasicDialog
{

    private JComboBox selectionComboBox;
    private CancelButton cancelButton;
    private BButton selectButton = new BButton("Select");
    private String selectedValue;
    private int selectedIndex;
    private boolean ok = false;

    /**
     *
     * @param title
     * @param values
     */
    public SelectDialog(String title, Object[] values)
    {
        setLayout(new RiverLayout(5, 10));
        selectionComboBox = new JComboBox(values);
        cancelButton = new CancelButton(this);
        selectButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ok = true;
                dispose();
                selectedValue = selectionComboBox.getSelectedItem().toString();
                selectedIndex = selectionComboBox.getSelectedIndex();
            }
        });
        add("tab hfill", selectionComboBox);
        add("br left", selectButton);
        add("right", cancelButton);
        setTitle(title);
        pack();
        setSize(getWidth(), getHeight() + 20);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public String getSelectedValue()
    {
        return selectedValue;
    }

    /**
     *
     * @param selectedValue
     */
    public void setSelectedValue(String selectedValue)
    {
        this.selectedValue = selectedValue;
    }

    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    /**
     *
     * @param selectedIndex
     */
    public void setSelectedIndex(int selectedIndex)
    {
        this.selectedIndex = selectedIndex;
    }

    public boolean isOk()
    {
        return ok;
    }
}
