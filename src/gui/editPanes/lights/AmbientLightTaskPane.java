package gui.editPanes.lights;

import gui.components.BColorButton;
import com.jme3.light.AmbientLight;
import components.EditTaskPane;
import general.UAManager;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class AmbientLightTaskPane extends EditTaskPane
{

    private BColorButton colorButton;

    /**
     *
     * @param ambientLight
     */
    public AmbientLightTaskPane(final AmbientLight ambientLight)
    {
        colorButton = new BColorButton(Wizard.makeColor(ambientLight.getColor()));
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Ambient Light");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ambientLight.setColor(Wizard.makeColorRGBA(colorButton.getColor()));
                UAManager.add(ambientLight, "Edit " + ambientLight.getName());
            }
        });
        taskPane.add("br", new JLabel("Color:"));
        taskPane.add("hfill", colorButton);
        taskPane.add("br right", applyButton);
    }
}
