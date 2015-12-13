package gui.editPanes.filters;

import com.jme3.post.filters.DepthOfFieldFilter;
import components.BTextField;
import components.EditTaskPane;
import general.UAManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class DepthOfFieldTaskPane extends EditTaskPane
{

    private BTextField blurScaleField = new BTextField("Float");
    private BTextField focusDistanceField = new BTextField("Float");
    private BTextField focusRangeField = new BTextField("Float");

    /**
     *
     * @param dofFilter
     */
    public DepthOfFieldTaskPane(final DepthOfFieldFilter dofFilter)
    {
        blurScaleField.setText("" + dofFilter.getBlurScale());
        focusDistanceField.setText("" + dofFilter.getFocusDistance());
        focusRangeField.setText("" + dofFilter.getFocusRange());
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Depth Of Field Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dofFilter.setBlurScale(Float.parseFloat(blurScaleField.getText()));
                dofFilter.setFocusDistance(Float.parseFloat(focusDistanceField.getText()));
                dofFilter.setFocusRange(Float.parseFloat(focusRangeField.getText()));
                UAManager.add(dofFilter, "Edit " + dofFilter.getName());
            }
        });
        taskPane.add("left", new JLabel("Blur Scale:"));
        taskPane.add("tab hfill", blurScaleField);
        taskPane.add("br left", new JLabel("Focus Distance:"));
        taskPane.add("tab hfill", focusDistanceField);
        taskPane.add("br left", new JLabel("Focus Range:"));
        taskPane.add("tab hfill", focusRangeField);
        taskPane.add("br right", applyButton);
    }
}
