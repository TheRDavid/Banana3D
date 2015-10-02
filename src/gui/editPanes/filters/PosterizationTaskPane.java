package gui.editPanes.filters;

import gui.editPanes.EditTaskPane;
import com.jme3.post.filters.PosterizationFilter;
import components.BTextField;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class PosterizationTaskPane extends EditTaskPane
{

    private BTextField numColorsField = new BTextField("Integer");
    private BTextField gammaField = new BTextField("Float");
    private BTextField strengthField = new BTextField("Float");

    /**
     *
     * @param posterizationFilter
     */
    public PosterizationTaskPane(final PosterizationFilter posterizationFilter)
    {
        gammaField.setText("" + posterizationFilter.getGamma());
        strengthField.setText("" + posterizationFilter.getStrength());
        numColorsField.setText("" + posterizationFilter.getNumColors());
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Posterization Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                posterizationFilter.setNumColors(Integer.parseInt(numColorsField.getText()));
                posterizationFilter.setGamma(Float.parseFloat(gammaField.getText()));
                posterizationFilter.setStrength(Float.parseFloat(strengthField.getText()));
            }
        });
        taskPane.add("br left", new JLabel("Number Of Colors:"));
        taskPane.add("tab hfill", numColorsField);
        taskPane.add("br left", new JLabel("Gamma:"));
        taskPane.add("tab hfill", gammaField);
        taskPane.add("br left", new JLabel("Strength:"));
        taskPane.add("tab hfill", strengthField);
        taskPane.add("br right", applyButton);
    }
}
