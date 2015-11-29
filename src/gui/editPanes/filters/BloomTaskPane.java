package gui.editPanes.filters;

import gui.editPanes.EditTaskPane;
import com.jme3.post.filters.BloomFilter;
import components.BTextField;
import general.UserActionManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class BloomTaskPane extends EditTaskPane
{

    private BTextField intensityField = new BTextField("Float");
    private BTextField blurScaleField = new BTextField("Float");
    private BTextField exposureCuttOffField = new BTextField("Float");
    private BTextField exposurePowerField = new BTextField("Float");
    private BTextField downSamplingFactorField = new BTextField("Float");

    /**
     *
     * @param bloomFilter
     */
    public BloomTaskPane(final BloomFilter bloomFilter)
    {
        intensityField.setText("" + bloomFilter.getBloomIntensity());
        blurScaleField.setText("" + bloomFilter.getBlurScale());
        exposureCuttOffField.setText("" + bloomFilter.getExposureCutOff());
        exposurePowerField.setText("" + bloomFilter.getExposurePower());
        downSamplingFactorField.setText("" + bloomFilter.getDownSamplingFactor());
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Bloom Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                bloomFilter.setBloomIntensity(Float.parseFloat(intensityField.getText()));
                bloomFilter.setBlurScale(Float.parseFloat(blurScaleField.getText()));
                bloomFilter.setExposureCutOff(Float.parseFloat(exposureCuttOffField.getText()));
                bloomFilter.setExposurePower(Float.parseFloat(exposurePowerField.getText()));
                bloomFilter.setDownSamplingFactor(Float.parseFloat(downSamplingFactorField.getText()));
                UserActionManager.addState(bloomFilter, "Edit " + bloomFilter.getName());
            }
        });
        taskPane.add("left", new JLabel("Intensity:"));
        taskPane.add("tab hfill", intensityField);
        taskPane.add("br left", new JLabel("Blur Scale:"));
        taskPane.add("tab hfill", blurScaleField);
        taskPane.add("br left", new JLabel("Exposure CuttOff:"));
        taskPane.add("tab hfill", exposureCuttOffField);
        taskPane.add("br left", new JLabel("Exposure Power:"));
        taskPane.add("tab hfill", exposurePowerField);
        taskPane.add("br left", new JLabel("Downsampling Factor:"));
        taskPane.add("tab hfill", downSamplingFactorField);
        taskPane.add("br right", applyButton);
    }
}
