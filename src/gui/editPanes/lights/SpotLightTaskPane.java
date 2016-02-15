package gui.editPanes.lights;

import gui.components.BColorButton;
import general.CurrentData;
import monkeyStuff.LightModel;
import com.jme3.light.SpotLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Line;
import components.BTextField;
import components.EditTaskPane;
import components.Float3Panel;
import general.UAManager;
import gui.components.LightDirectionPanel;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class SpotLightTaskPane extends EditTaskPane
{

    private BColorButton colorButton;
    private Float3Panel positionPanel;
    private LightDirectionPanel rotationPanel;
    private BTextField spotRangeField;
    private BTextField innerAngleField;
    private BTextField outerAngleField;
    private LightModel lightModel;

    /**
     *
     * @param model
     */
    public SpotLightTaskPane(LightModel model)
    {
        lightModel = model;
        positionPanel = new Float3Panel(((SpotLight) model.getLight()).getPosition(), Wizard.getCamera(), Float3Panel.HORIZONTAL)
        {
            @Override
            public void setVector(final Vector3f vec)
            {
                getxField().setText("" + getRounder().format(vec.getX()).replace(',', '.'));
                getyField().setText("" + getRounder().format(vec.getY()).replace(',', '.'));
                getzField().setText("" + getRounder().format(vec.getZ()).replace(',', '.'));
                ((SpotLight) lightModel.getLight()).setPosition(vec);
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        lightModel.getRepresentative().setLocalTranslation(vec);
                        return null;
                    }
                });
                super.setVector(vec);
            }
        };
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
                updateLightDirection(((SpotLight) lightModel.getLight()).getDirection());
                ((SpotLight) lightModel.getLight()).setPosition(positionPanel.getVector());
                lightModel.getRepresentative().setLocalTranslation(((SpotLight) lightModel.getLight()).getPosition());
                Vector3f end = lightModel.getRepresentative().getLocalTranslation().add(
                        ((SpotLight) lightModel.getLight()).getDirection().mult(((SpotLight) lightModel.getLight()).getSpotRange()));
                Mesh mesh = new Line(lightModel.getRepresentative().getLocalTranslation(), lightModel.getRepresentative().getLocalTranslation().add(((SpotLight) lightModel.getLight()).getDirection().mult(((SpotLight) lightModel.getLight()).getSpotRange())));
                mesh.setPointSize(5);
                lightModel.getSymbol().setMesh(mesh);
                UAManager.add(lightModel.getLight(), "Move " + lightModel.getLight().getName());
            }
        });
        colorButton = new BColorButton(Wizard.makeColor(lightModel.getLight().getColor()));
        rotationPanel = new LightDirectionPanel(model)
        {
            @Override
            public void updateDirection(final Vector3f direction)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        ((SpotLight) lightModel.getLight()).setDirection(direction);
                        rotationPanel.skipUpdate();
                        rotationPanel.getFloat3Panel().setVector(direction);
                        Vector3f end = lightModel.getRepresentative().getLocalTranslation().add(
                                ((SpotLight) lightModel.getLight()).getDirection().mult(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumFar() * 2));
                        Mesh mesh = new Line(lightModel.getRepresentative().getLocalTranslation(), end);
                        mesh.setPointSize(5);
                        lightModel.getSymbol().setMesh(mesh);
                        return null;
                    }
                });
            }
        };
        spotRangeField = new BTextField("Float", Float.toString(((SpotLight) model.getLight()).getSpotRange()));
        innerAngleField = new BTextField("Float", Float.toString(((SpotLight) model.getLight()).getSpotInnerAngle()));
        outerAngleField = new BTextField("Float", Float.toString(((SpotLight) model.getLight()).getSpotOuterAngle()));
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Spot Light");
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                lightModel.getLight().setColor(Wizard.makeColorRGBA(colorButton.getColor()));
                lightModel.getSymbol().getMaterial().setColor("Color", lightModel.getLight().getColor());
                ((SpotLight) lightModel.getLight()).setSpotRange(Float.parseFloat(spotRangeField.getText()));
                ((SpotLight) lightModel.getLight()).setSpotInnerAngle(Float.parseFloat(innerAngleField.getText()));
                ((SpotLight) lightModel.getLight()).setSpotOuterAngle(Float.parseFloat(outerAngleField.getText()));
                UAManager.add(lightModel.getLight(), "Edit " + lightModel.getLight().getName());
            }
        });
        taskPane.add("br left", new JLabel("Color:"));
        taskPane.add("tab hfill", colorButton);
        taskPane.add("br left", new JLabel("Position:"));
        taskPane.add("tab hfill", positionPanel);
        taskPane.add("br left", new JLabel("Range:"));
        taskPane.add("tab hfill", spotRangeField);
        taskPane.add("br left", new JLabel("Inner Angle:"));
        taskPane.add("tab hfill", innerAngleField);
        taskPane.add("br left", new JLabel("Outer Angle:"));
        taskPane.add("tab hfill", outerAngleField);
        taskPane.add("br left", rotationPanel);
        taskPane.add("br right", applyButton);
        add(taskPane, BorderLayout.CENTER);
    }

    private void updateLightDirection(final Vector3f direction)
    {
        rotationPanel.getFloat3Panel().setVector(direction);
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                ((SpotLight) lightModel.getLight()).setDirection(direction);
                Vector3f end = lightModel.getRepresentative().getLocalTranslation().add(
                        ((SpotLight) lightModel.getLight()).getDirection().mult(((SpotLight) lightModel.getLight()).getSpotRange()));
                Mesh mesh = new Line(lightModel.getRepresentative().getLocalTranslation(), end);
                mesh.setPointSize(5);
                lightModel.getSymbol().setMesh(mesh);
                return null;
            }
        });
    }

    @Override
    public void updateData(boolean urgent)
    {
        if (!positionPanel.hasFocus() && !rotationPanel.getSliderPanel().getxSlider().hasFocus()
                && !rotationPanel.getSliderPanel().getySlider().hasFocus()
                && !rotationPanel.getSliderPanel().getzSlider().hasFocus()
                && !rotationPanel.getFloat3Panel().hasFocus())
        {
            positionPanel.setVector(((SpotLight) lightModel.getLight()).getPosition());
            updateLightDirection(((SpotLight) lightModel.getLight()).getDirection());
        }
    }
}
