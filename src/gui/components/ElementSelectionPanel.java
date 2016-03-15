/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.components;

import b3dElements.B3D_Element;
import components.BButton;
import general.CurrentData;
import gui.dialogs.SelectElementDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import javax.swing.JLabel;
import javax.swing.JPanel;
import other.Wizard;
import se.datadosen.component.RiverLayout;

/**
 *
 * @author User
 */
public abstract class ElementSelectionPanel extends JPanel implements ActionListener
{

    private JLabel idLabel, nameLabel;
    private BButton selectCurrentButton, browseButton, noneButton;
    public B3D_Element element;
    private Class[] types =
    {
        Object.class
    };

    public ElementSelectionPanel(UUID startValue)
    {
        element = Wizard.getObjects().getB3D_Element(startValue);
        setLayout(new RiverLayout());
        idLabel = new JLabel("ID: " + (element == null ? "" : element.getUUID().toString()));
        nameLabel = new JLabel("Name: " + (element == null ? "none" : element.getName()));
        selectCurrentButton = new BButton("Currently selected");
        browseButton = new BButton("Select...");
        noneButton = new BButton("NONE");
        add("hfill", idLabel);
        add("br hfill", nameLabel);
        add("br", selectCurrentButton);
        add(noneButton);
        add(browseButton);
        selectCurrentButton.addActionListener(this);
        browseButton.addActionListener(this);
        noneButton.addActionListener(this);
    }

    public ElementSelectionPanel(UUID startValue, Class[] allowed)
    {
        this(startValue);
        this.types = allowed;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() != noneButton)
        {
            if (e.getSource() == selectCurrentButton)
                element = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
            else if (e.getSource() == browseButton)
            {
                SelectElementDialog sed = new SelectElementDialog(getLocationOnScreen(), types);
                element = sed.getSelectedElement();
            }
            nameLabel.setText("Name: " + element.getName());
            idLabel.setText("ID: " + element.getUUID());
        } else
        {
            element = null;
            nameLabel.setText("Name: none");
            idLabel.setText("ID:");
        }
        System.out.println("Action Performed");
        onSelectionChange();
    }

    public abstract void onSelectionChange();
}
