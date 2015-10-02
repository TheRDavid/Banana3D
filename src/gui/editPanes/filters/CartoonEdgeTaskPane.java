package gui.editPanes.filters;

import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import com.jme3.post.filters.CartoonEdgeFilter;
import components.BTextField;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class CartoonEdgeTaskPane extends EditTaskPane
{

    private BColorButton colorButton;
    private BTextField depthSensivityField = new BTextField("float");
    private BTextField depthTresholdField = new BTextField("float");
    private BTextField edgeIntensityField = new BTextField("float");
    private BTextField edgeWidthField = new BTextField("float");
    private BTextField normalSensivityField = new BTextField("float");
    private BTextField normalTresholdField = new BTextField("float");

    /**
     *
     * @param cartoonEdgeFilter
     */
    public CartoonEdgeTaskPane(final CartoonEdgeFilter cartoonEdgeFilter)
    {
        colorButton = new BColorButton(Wizard.makeColor(cartoonEdgeFilter.getEdgeColor()));
        depthSensivityField.setText(Float.toString(cartoonEdgeFilter.getDepthSensitivity()));
        depthTresholdField.setText(Float.toString(cartoonEdgeFilter.getDepthThreshold()));
        edgeIntensityField.setText(Float.toString(cartoonEdgeFilter.getEdgeIntensity()));
        edgeWidthField.setText(Float.toString(cartoonEdgeFilter.getEdgeWidth()));
        normalSensivityField.setText(Float.toString(cartoonEdgeFilter.getNormalSensitivity()));
        normalTresholdField.setText(Float.toString(cartoonEdgeFilter.getNormalThreshold()));
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Cartoon Edge Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                cartoonEdgeFilter.setDepthSensitivity(Float.parseFloat(depthSensivityField.getText()));
                cartoonEdgeFilter.setDepthThreshold(Float.parseFloat(depthTresholdField.getText()));
                cartoonEdgeFilter.setEdgeColor(Wizard.makeColorRGBA(colorButton.getColor()));
                cartoonEdgeFilter.setEdgeIntensity(Float.parseFloat(edgeIntensityField.getText()));
                cartoonEdgeFilter.setEdgeWidth(Float.parseFloat(edgeWidthField.getText()));
                cartoonEdgeFilter.setNormalSensitivity(Float.parseFloat(normalSensivityField.getText()));
                cartoonEdgeFilter.setNormalThreshold(Float.parseFloat(normalTresholdField.getText()));
            }
        });
        taskPane.add("left", new JLabel("Edge Color:"));
        taskPane.add("tab hfill", colorButton);
        taskPane.add("br left", new JLabel("Edge Intensity:"));
        taskPane.add("tab hfill", edgeIntensityField);
        taskPane.add("br left", new JLabel("Edge Width:"));
        taskPane.add("tab hfill", edgeWidthField);
        taskPane.add("br left", new JLabel("Depth Sensivity:"));
        taskPane.add("tab hfill", depthSensivityField);
        taskPane.add("br left", new JLabel("Depth Treshold:"));
        taskPane.add("tab hfill", depthTresholdField);
        taskPane.add("br left", new JLabel("Normal Sensivity:"));
        taskPane.add("tab hfill", normalSensivityField);
        taskPane.add("br left", new JLabel("Normal Treshold:"));
        taskPane.add("tab hfill", normalTresholdField);
        taskPane.add("br right", applyButton);
    }
}
