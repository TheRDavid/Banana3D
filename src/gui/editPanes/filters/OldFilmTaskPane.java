package gui.editPanes.filters;

import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import com.shaderblow.filter.oldfilm.OldFilmFilter;
import components.BTextField;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class OldFilmTaskPane extends EditTaskPane
{

    private BColorButton colorButton;
    private BTextField colorDensityField, noiseDensityField, scratchDensityField, vignetteValueField;

    public OldFilmTaskPane(final OldFilmFilter oldFilmFilter)
    {
        taskPane.setTitle("Old Film Filter");
        colorButton = new BColorButton(Wizard.makeColor(oldFilmFilter.getFilterColor()));
        colorDensityField = new BTextField("Float", Float.toString(oldFilmFilter.getColorDensity()));
        noiseDensityField = new BTextField("Float", Float.toString(oldFilmFilter.getNoiseDensity()));
        scratchDensityField = new BTextField("Float", Float.toString(oldFilmFilter.getScratchDensity()));
        vignetteValueField = new BTextField("Float", Float.toString(oldFilmFilter.getVignettingValue()));
        taskPane.setLayout(new RiverLayout());
        taskPane.add("left", new JLabel("Color:"));
        taskPane.add("tab hfill", colorButton);
        taskPane.add("br left", new JLabel("Color Density:"));
        taskPane.add("tab hfill", colorDensityField);
        taskPane.add("br left", new JLabel("Noise Density:"));
        taskPane.add("tab hfill", noiseDensityField);
        taskPane.add("br left", new JLabel("Scratch Density:"));
        taskPane.add("tab hfill", scratchDensityField);
        taskPane.add("br left", new JLabel("Vignette Value:"));
        taskPane.add("tab hfill", vignetteValueField);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                oldFilmFilter.setColorDensity(Float.parseFloat(colorDensityField.getText()));
                oldFilmFilter.setFilterColor(Wizard.makeColorRGBA(colorButton.getColor()));
                oldFilmFilter.setNoiseDensity(Float.parseFloat(noiseDensityField.getText()));
                oldFilmFilter.setScratchDensity(Float.parseFloat(scratchDensityField.getText()));
                oldFilmFilter.setVignettingValue(Float.parseFloat(vignetteValueField.getText()));
            }
        });
        taskPane.add("br right", applyButton);
        add(taskPane, BorderLayout.CENTER);
    }
}
