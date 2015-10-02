package gui.editPanes.filters;

import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import components.BTextField;
import components.Checker;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import monkeyStuff.ColorScaleFilterWithGetters;
import other.Wizard;

public class ColorScaleTaskPane extends EditTaskPane
{

    private BTextField colorDensityField;
    private BColorButton colorButton;
    private Checker multiplyChecker, overlayChecker;

    public ColorScaleTaskPane(final ColorScaleFilterWithGetters filter)
    {
        colorDensityField = new BTextField("Float", Float.toString(filter.getColorDensity()));
        colorButton = new BColorButton(Wizard.makeColor(filter.getFilterColor()));
        multiplyChecker = new Checker();
        multiplyChecker.setChecked(filter.isMultiply());
        overlayChecker = new Checker();
        overlayChecker.setChecked(filter.isOverlay());
        multiplyChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        filter.setMultiply(multiplyChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        overlayChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        filter.setOverlay(overlayChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        taskPane.add("left", new JLabel("Color:"));
        taskPane.add("tab hfill", colorButton);
        taskPane.add("br left", new JLabel("Color Density:"));
        taskPane.add("tab hfill", colorDensityField);
        taskPane.add("br left", new JLabel("Multiply:"));
        taskPane.add("tab", multiplyChecker);
        taskPane.add("br left", new JLabel("Overlay:"));
        taskPane.add("tab", overlayChecker);
        taskPane.add("br left", applyButton);
        taskPane.setTitle("Color Scale Filter");
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        filter.setColorDensity(Float.parseFloat(colorDensityField.getText()));
                        filter.setFilterColor(Wizard.makeColorRGBA(colorButton.getColor()));
                        filter.setMultiply(multiplyChecker.isChecked());
                        filter.setOverlay(overlayChecker.isChecked());
                        /*because..................................................*/
                        filter.setColorDensity(Float.parseFloat(colorDensityField.getText()));
                        filter.setFilterColor(Wizard.makeColorRGBA(colorButton.getColor()));
                        filter.setMultiply(multiplyChecker.isChecked());
                        filter.setOverlay(overlayChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        add(taskPane, BorderLayout.CENTER);
    }
}
