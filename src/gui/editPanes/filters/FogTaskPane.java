package gui.editPanes.filters;

import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import com.jme3.post.filters.FogFilter;
import components.BTextField;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class FogTaskPane extends EditTaskPane
{

    private BColorButton colorButton;
    private BTextField densityField = new BTextField("Float");
    private BTextField distanceField = new BTextField("Float");

    /**
     *
     * @param fogFilter
     */
    public FogTaskPane(final FogFilter fogFilter)
    {
        densityField.setText("" + fogFilter.getFogDensity());
        distanceField.setText("" + fogFilter.getFogDistance());
        colorButton = new BColorButton(Wizard.makeColor(fogFilter.getFogColor()));
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Depth Of Field Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fogFilter.setFogDensity(Float.parseFloat(densityField.getText()));
                fogFilter.setFogDistance(Float.parseFloat(distanceField.getText()));
                fogFilter.setFogColor(Wizard.makeColorRGBA(colorButton.getColor()));
            }
        });
        taskPane.add("br left", new JLabel("Color:"));
        taskPane.add("tab hfill", colorButton);
        taskPane.add("br left", new JLabel("Density:"));
        taskPane.add("tab hfill", densityField);
        taskPane.add("br left", new JLabel("Distance:"));
        taskPane.add("tab hfill", distanceField);
        taskPane.add("br right", applyButton);
    }
}
