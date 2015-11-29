package gui.editPanes.filters;

import b3dElements.filters.B3D_FrostedGlass;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import com.shaderblow.filter.frostedglass.FrostedGlassFilter;
import components.BTextField;
import general.UserActionManager;
import other.ObjectToElementConverter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class FrostedGlassTaskPane extends EditTaskPane
{

    private BTextField randomFactorField, randomScaleField;

    public FrostedGlassTaskPane(final FrostedGlassFilter filter)
    {
        randomFactorField = new BTextField("Float", Float.toString(filter.getRandomFactor()));
        randomScaleField = new BTextField("Float", Float.toString(filter.getRandomScale()));
        add(taskPane, BorderLayout.CENTER);
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
                        CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().removeFilter(
                                filter);
                        B3D_FrostedGlass oldB3D_FrostedGlass = (B3D_FrostedGlass) Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(filter.hashCode()));
                        Wizard.getObjects().remove(filter.hashCode(), Wizard.getObjectReferences().getUUID(filter.hashCode()));
                        filter.setRandomFactor(Float.parseFloat(randomFactorField.getText()));
                        filter.setRandomScale(Float.parseFloat(randomScaleField.getText()));
                        CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(
                                filter);
                        //ConvertMode does not matter here, the filter can not possibly have a LightControl
                        B3D_FrostedGlass b3D_FrostedGlass = ObjectToElementConverter.convertFrostedGlass(filter,
                                oldB3D_FrostedGlass.getFilterIndex());
                        Wizard.getObjects().add(filter, b3D_FrostedGlass);
                        CurrentData.getEditorWindow().getB3DApp().setSelectedUUID(b3D_FrostedGlass.getUUID());
                        CurrentData.getEditorWindow().getTree().sync();
                        CurrentData.getEditorWindow().getEditPane().arrange(false);
                        UserActionManager.addState(filter, "Edit " + filter.getName());
                        return null;
                    }
                });
            }
        });
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Frosted Glass Filter");
        taskPane.add("left", new JLabel("Random Factor:"));
        taskPane.add("tab hfill", randomFactorField);
        taskPane.add("br left", new JLabel("Random Scale:"));
        taskPane.add("tab hfill", randomScaleField);
        taskPane.add("br right", applyButton);
    }
}
