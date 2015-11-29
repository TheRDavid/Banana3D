package gui.editPanes.filters;

import gui.editPanes.EditTaskPane;
import com.jme3.post.ssao.SSAOFilter;
import components.BTextField;
import general.UserActionManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class SSAOTaskPane extends EditTaskPane
{

    private BTextField sampleRadiusField = new BTextField("Float");
    private BTextField biasField = new BTextField("Float");
    private BTextField intensityField = new BTextField("Float");
    private BTextField scaleField = new BTextField("Float");

    /**
     *
     * @param ssaoFilter
     */
    public SSAOTaskPane(final SSAOFilter ssaoFilter)
    {
        biasField.setText("" + ssaoFilter.getBias());
        intensityField.setText("" + ssaoFilter.getIntensity());
        sampleRadiusField.setText("" + ssaoFilter.getSampleRadius());
        scaleField.setText("" + ssaoFilter.getScale());
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("SSAO Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ssaoFilter.setBias(Float.parseFloat(biasField.getText()));
                ssaoFilter.setIntensity(Float.parseFloat(intensityField.getText()));
                ssaoFilter.setSampleRadius(Float.parseFloat(sampleRadiusField.getText()));
                ssaoFilter.setScale(Float.parseFloat(scaleField.getText()));
                UserActionManager.addState(ssaoFilter, "Edit " + ssaoFilter.getName());
            }
        });
        taskPane.add("br left", new JLabel("Bias:"));
        taskPane.add("tab hfill", biasField);
        taskPane.add("br left", new JLabel("Intensity:"));
        taskPane.add("tab hfill", intensityField);
        taskPane.add("br left", new JLabel("Sample Radius:"));
        taskPane.add("tab hfill", sampleRadiusField);
        taskPane.add("br left", new JLabel("Scale:"));
        taskPane.add("tab hfill", scaleField);
        taskPane.add("br right", applyButton);
    }
}
