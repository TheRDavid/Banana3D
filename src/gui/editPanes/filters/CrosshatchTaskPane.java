package gui.editPanes.filters;

import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import com.jme3.post.filters.CrossHatchFilter;
import components.BTextField;
import general.UserActionManager;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import se.datadosen.component.RiverLayout;

public class CrosshatchTaskPane extends EditTaskPane
{

    private BColorButton lineColorButton;
    private BColorButton paperColorButton;
    private BTextField colorInfluenceLineField = new BTextField("float");
    private BTextField colorInfluencePaperField = new BTextField("float");
    private BTextField fillValueField = new BTextField("float");
    private BTextField lineThicknessField = new BTextField("float");
    private BTextField lineDistanceField = new BTextField("float");
    private BTextField luminance1Field = new BTextField("float");
    private BTextField luminance2Field = new BTextField("float");
    private BTextField luminance3Field = new BTextField("float");
    private BTextField luminance4Field = new BTextField("float");
    private BTextField luminance5Field = new BTextField("float");

    /**
     *
     * @param crossHatchFilter
     */
    public CrosshatchTaskPane(final CrossHatchFilter crossHatchFilter)
    {
        lineColorButton = new BColorButton(Wizard.makeColor(crossHatchFilter.getLineColor()));
        paperColorButton = new BColorButton(Wizard.makeColor(crossHatchFilter.getPaperColor()));
        colorInfluenceLineField.setText(Float.toString(crossHatchFilter.getColorInfluenceLine()));
        colorInfluencePaperField.setText(Float.toString(crossHatchFilter.getColorInfluencePaper()));
        fillValueField.setText(Float.toString(crossHatchFilter.getFillValue()));
        lineThicknessField.setText(Float.toString(crossHatchFilter.getLineThickness()));
        lineDistanceField.setText(Float.toString(crossHatchFilter.getLineDistance()));
        luminance1Field.setText(Float.toString(crossHatchFilter.getLuminance1()));
        luminance2Field.setText(Float.toString(crossHatchFilter.getLuminance2()));
        luminance3Field.setText(Float.toString(crossHatchFilter.getLuminance3()));
        luminance4Field.setText(Float.toString(crossHatchFilter.getLuminance4()));
        luminance5Field.setText(Float.toString(crossHatchFilter.getLuminance5()));
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Crosshatch Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                crossHatchFilter.setLineColor(Wizard.makeColorRGBA(lineColorButton.getColor()));
                crossHatchFilter.setPaperColor(Wizard.makeColorRGBA(paperColorButton.getColor()));
                crossHatchFilter.setColorInfluenceLine(Float.parseFloat(colorInfluenceLineField.getText()));
                crossHatchFilter.setColorInfluencePaper(Float.parseFloat(colorInfluencePaperField.getText()));
                crossHatchFilter.setFillValue(Float.parseFloat(fillValueField.getText()));
                crossHatchFilter.setLineThickness(Float.parseFloat(lineThicknessField.getText()));
                crossHatchFilter.setLineDistance(Float.parseFloat(lineDistanceField.getText()));
                crossHatchFilter.setLuminanceLevels(
                        Float.parseFloat(luminance1Field.getText()),
                        Float.parseFloat(luminance2Field.getText()),
                        Float.parseFloat(luminance3Field.getText()),
                        Float.parseFloat(luminance4Field.getText()),
                        Float.parseFloat(luminance5Field.getText()));
                UserActionManager.addState(crossHatchFilter, "Edit " + crossHatchFilter.getName());
            }
        });
        taskPane.add("left", new JLabel("Line Color:"));
        taskPane.add("tab hfill", lineColorButton);
        taskPane.add("left", new JLabel("Paper Color:"));
        taskPane.add("tab hfill", paperColorButton);
        taskPane.add("br left", new JLabel("Color Influence Line:"));
        taskPane.add("tab hfill", colorInfluenceLineField);
        taskPane.add("br left", new JLabel("Color Influence Paper:"));
        taskPane.add("tab hfill", colorInfluencePaperField);
        taskPane.add("br left", new JLabel("Fill Value:"));
        taskPane.add("tab hfill", fillValueField);
        taskPane.add("br left", new JLabel("Line Distance:"));
        taskPane.add("tab hfill", lineDistanceField);
        taskPane.add("br left", new JLabel("Line Thickness:"));
        taskPane.add("tab hfill", lineThicknessField);
        taskPane.add("br hfill", new JSeparator());
        taskPane.add("br left", new JLabel("Luminance 1:"));
        taskPane.add("tab hfill", luminance1Field);
        taskPane.add("br left", new JLabel("Luminance 2:"));
        taskPane.add("tab hfill", luminance2Field);
        taskPane.add("br left", new JLabel("Luminance 3:"));
        taskPane.add("tab hfill", luminance3Field);
        taskPane.add("br left", new JLabel("Luminance 4:"));
        taskPane.add("tab hfill", luminance4Field);
        taskPane.add("br left", new JLabel("Luminance 5:"));
        taskPane.add("tab hfill", luminance5Field);
        taskPane.add("br right", applyButton);
    }
}
