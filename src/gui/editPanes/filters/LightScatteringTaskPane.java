package gui.editPanes.filters;

import general.CurrentData;
import monkeyStuff.LightScatteringModel;
import com.jme3.post.filters.LightScatteringFilter;
import components.BSlider;
import components.BTextField;
import components.EditTaskPane;
import components.Float3Panel;
import general.UAManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class LightScatteringTaskPane extends EditTaskPane
{

    private LightScatteringFilter lightScatteringFilter;
    private Float3Panel positionPanel;
    private BTextField blurStartField = new BTextField("Float");
    private BTextField blurWidthField = new BTextField("Float");
    private BTextField densityField = new BTextField("Float");
    private BSlider samplesSlider;

    /**
     *
     * @param scatteringFilter
     */
    public LightScatteringTaskPane(LightScatteringFilter scatteringFilter)
    {
        lightScatteringFilter = scatteringFilter;
        positionPanel = new Float3Panel(scatteringFilter.getLightPosition(), Wizard.getCamera(), Float3Panel.HORIZONTAL);
        positionPanel.addFieldKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                lightScatteringFilter.setLightPosition(positionPanel.getVector());
                for (LightScatteringModel lsm : CurrentData.getEditorWindow().getB3DApp().getLightScatteringModels())
                    if (lsm.getScatteringFilter().equals(lightScatteringFilter))
                        lsm.getSymbol().setLocalTranslation(lightScatteringFilter.getLightPosition());
                UAManager.add(lightScatteringFilter, "Move " + lightScatteringFilter.getName());
            }
        });
        samplesSlider = new BSlider(Integer.class, 10, 150, lightScatteringFilter.getNbSamples());
        samplesSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                lightScatteringFilter.setNbSamples(samplesSlider.getValue());
            }
        });
        samplesSlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(lightScatteringFilter, "Set Samples to " + samplesSlider.getValue());
            }
        });
        blurStartField.setText("" + scatteringFilter.getBlurStart());
        blurWidthField.setText("" + scatteringFilter.getBlurWidth());
        densityField.setText("" + scatteringFilter.getLightDensity());
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Light Scattering Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                lightScatteringFilter.setBlurStart(Float.parseFloat(blurStartField.getText()));
                lightScatteringFilter.setBlurWidth(Float.parseFloat(blurWidthField.getText()));
                lightScatteringFilter.setLightDensity(Float.parseFloat(densityField.getText()));
                lightScatteringFilter.setLightPosition(positionPanel.getVector());
                lightScatteringFilter.setNbSamples(samplesSlider.getValue());
                UAManager.add(lightScatteringFilter, "Edit " + lightScatteringFilter.getName());
            }
        });
        taskPane.add("br left", new JLabel("Position:"));
        taskPane.add("tab hfill", positionPanel);
        taskPane.add("br left", new JLabel("Quality:"));
        taskPane.add("tab hfill", samplesSlider);
        taskPane.add("br left", new JLabel("Blur Start:"));
        taskPane.add("tab hfill", blurStartField);
        taskPane.add("br left", new JLabel("Blur Width:"));
        taskPane.add("tab hfill", blurWidthField);
        taskPane.add("br left", new JLabel("Density:"));
        taskPane.add("tab hfill", densityField);
        taskPane.add("br right", applyButton);
    }

    @Override
    public void updateData(boolean urgent)
    {
        if (!positionPanel.hasFocus())
            positionPanel.setVector(lightScatteringFilter.getLightPosition());
    }
}
