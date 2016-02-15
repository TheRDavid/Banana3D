package gui.editPanes.lights;

import gui.components.BColorButton;
import general.CurrentData;
import monkeyStuff.LightModel;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Line;
import components.EditTaskPane;
import general.UAManager;
import gui.components.LightDirectionPanel;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class DirectionalLightTaskPane extends EditTaskPane
{

    private BColorButton colorButton;
    private LightDirectionPanel rotationPanel;
    private LightModel lightModel;

    /**
     *
     * @param model
     */
    public DirectionalLightTaskPane(LightModel model)
    {
        lightModel = model;
        colorButton = new BColorButton(Wizard.makeColor(lightModel.getLight().getColor()));
        rotationPanel = new LightDirectionPanel(lightModel)
        {
            @Override
            public void updateDirection(final Vector3f direction)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        ((DirectionalLight) lightModel.getLight()).setDirection(direction);
                        rotationPanel.skipUpdate();
                        rotationPanel.getFloat3Panel().setVector(direction);
                        Vector3f end = lightModel.getRepresentative().getLocalTranslation().add(
                                ((DirectionalLight) lightModel.getLight()).getDirection().mult(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumFar() * 2));
                        Mesh mesh = new Line(lightModel.getRepresentative().getLocalTranslation(), end);
                        mesh.setPointSize(5);
                        lightModel.getSymbol().setMesh(mesh);
                        return null;
                    }
                });
            }
        };
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Directional Light");
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                lightModel.getLight().setColor(Wizard.makeColorRGBA(colorButton.getColor()));
                lightModel.getSymbol().getMaterial().setColor("Color", lightModel.getLight().getColor());
                UAManager.add(lightModel.getLight(), "Edit " + lightModel.getLight().getName());
            }
        });
        taskPane.add("br", new JLabel("Color:"));
        taskPane.add("hfill", colorButton);
        taskPane.add("br", rotationPanel);
        taskPane.add("br", applyButton);
        add(taskPane, BorderLayout.CENTER);
    }

    @Override
    public void updateData(boolean urgent)
    {
        if (!rotationPanel.getSliderPanel().getxSlider().hasFocus()
                && !rotationPanel.getSliderPanel().getySlider().hasFocus()
                && !rotationPanel.getSliderPanel().getzSlider().hasFocus()
                && !rotationPanel.getFloat3Panel().hasFocus())
            rotationPanel.updateDirection(((DirectionalLight) lightModel.getLight()).getDirection());
    }
}
