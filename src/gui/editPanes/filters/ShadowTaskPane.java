package gui.editPanes.filters;

import general.CurrentData;
import com.jme3.shadow.AbstractShadowFilter;
import static com.jme3.shadow.EdgeFilteringMode.Bilinear;
import static com.jme3.shadow.EdgeFilteringMode.Dither;
import static com.jme3.shadow.EdgeFilteringMode.Nearest;
import static com.jme3.shadow.EdgeFilteringMode.PCF4;
import static com.jme3.shadow.EdgeFilteringMode.PCF8;
import static com.jme3.shadow.EdgeFilteringMode.PCFPOISSON;
import components.BComboBox;
import components.BSlider;
import components.BTextField;
import components.Checker;
import components.EditTaskPane;
import general.UAManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import se.datadosen.component.RiverLayout;

public class ShadowTaskPane extends EditTaskPane
{

    private BSlider intensitySlider = new BSlider(Float.class, 0, 1, .7f);
    private BTextField edgeThicknessField = new BTextField("Integer", "1");
    private BComboBox filteringModesComboBox = new BComboBox(new String[]
    {
        "Bilinear", "Dither", "Nearest", "PCF4", "PCF8", "PCFPOISSON"
    });
    private Checker flushQueuesChecker = new Checker();
    private String typeName = "";

    public ShadowTaskPane(final AbstractShadowFilter shadowFilter)
    {
        flushQueuesChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        shadowFilter.setFlushQueues(flushQueuesChecker.isChecked());
                        UAManager.add(shadowFilter, "Set Flush Queues to " + flushQueuesChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        edgeThicknessField.setText("" + shadowFilter.getEdgesThickness());
        switch (shadowFilter.getEdgeFilteringMode())
        {
            case Bilinear:
                filteringModesComboBox.setSelectedIndex(0);
                break;
            case Dither:
                filteringModesComboBox.setSelectedIndex(1);
                break;
            case Nearest:
                filteringModesComboBox.setSelectedIndex(2);
                break;
            case PCF4:
                filteringModesComboBox.setSelectedIndex(3);
                break;
            case PCF8:
                filteringModesComboBox.setSelectedIndex(4);
                break;
            case PCFPOISSON:
                filteringModesComboBox.setSelectedIndex(5);
        }
        flushQueuesChecker.setChecked(shadowFilter.isFlushQueues());
        intensitySlider._setValue(shadowFilter.getShadowIntensity());
        intensitySlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                shadowFilter.setShadowIntensity(intensitySlider._getValue());
            }
        });
        intensitySlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(shadowFilter, "Set Intensity to " + intensitySlider._getValue());
            }
        });
        taskPane.setLayout(new RiverLayout());
        add(taskPane, BorderLayout.CENTER);
        taskPane.add("left", new JLabel("Intensity:"));
        taskPane.add("tab hfill", intensitySlider);
        taskPane.add("br left", new JLabel("Edges Thickness:"));
        taskPane.add("tab hfill", edgeThicknessField);
        taskPane.add("br left", new JLabel("Edge Filterin Mode:"));
        taskPane.add("tab hfill", filteringModesComboBox);
        taskPane.add("br left", new JLabel("Flush Queues:"));
        taskPane.add("tab", flushQueuesChecker);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ((filteringModesComboBox.getSelectedIndex() == 3 || filteringModesComboBox.getSelectedIndex() == 4)
                        && Integer.parseInt(edgeThicknessField.getText()) == 0)
                {
                    JOptionPane.showMessageDialog(applyButton, "Do you want this program to crash?", "Dude...", JOptionPane.WARNING_MESSAGE);
                    JOptionPane.showMessageDialog(applyButton, "Because this is how you make this program crash.", "Duuude...", JOptionPane.WARNING_MESSAGE);
                    JOptionPane.showMessageDialog(applyButton, "Please don't try to make this program crash...", "Duuuuuuuuuuuude...", JOptionPane.WARNING_MESSAGE);
                } else
                {
                    switch (filteringModesComboBox.getSelectedIndex())
                    {
                        case 0:
                            shadowFilter.setEdgeFilteringMode(Bilinear);
                            break;
                        case 1:
                            shadowFilter.setEdgeFilteringMode(Dither);
                            break;
                        case 2:
                            shadowFilter.setEdgeFilteringMode(Nearest);
                            break;
                        case 3:
                            shadowFilter.setEdgeFilteringMode(PCF4);
                            break;
                        case 4:
                            shadowFilter.setEdgeFilteringMode(PCF8);
                            break;
                        case 5:
                            shadowFilter.setEdgeFilteringMode(PCFPOISSON);
                    }
                    shadowFilter.setEdgesThickness(Integer.parseInt(edgeThicknessField.getText()));
                    shadowFilter.setFlushQueues(flushQueuesChecker.isChecked());
                    UAManager.add(shadowFilter, "Edit " + shadowFilter.getName());
                }
            }
        });
        taskPane.add("br left", applyButton);
        taskPane.setTitle(typeName + "Shadow");
    }
}
