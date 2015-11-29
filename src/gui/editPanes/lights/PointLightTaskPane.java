package gui.editPanes.lights;

import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import monkeyStuff.LightModel;
import com.jme3.light.PointLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.shape.Sphere;
import components.BTextField;
import components.Float3Panel;
import general.UserActionManager;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class PointLightTaskPane extends EditTaskPane
{

    private BColorButton colorButton;
    private Float3Panel positionPanel;
    private BTextField radiusTextField;
    private LightModel lightModel;

    /**
     *
     * @param model
     */
    public PointLightTaskPane(LightModel model)
    {
        lightModel = model;
        colorButton = new BColorButton(Wizard.makeColor(lightModel.getLight().getColor()));
        positionPanel = new Float3Panel(((PointLight) lightModel.getLight()).getPosition(), Wizard.getCamera())
        {
            @Override
            public void setVector(final Vector3f vec)
            {
                ((PointLight) lightModel.getLight()).setPosition(vec);
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        lightModel.getRepresentative().setLocalTranslation(vec);
                        lightModel.getSymbol().setLocalTranslation(vec);
                        lightModel.getSymbol().getMaterial().setColor("Color", lightModel.getLight().getColor());
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
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    updateLight();
            }
        });
        positionPanel.addFieldFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                updateLight();
            }
        });
        radiusTextField = new BTextField("Float", Float.toString(((PointLight) lightModel.getLight()).getRadius()));
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Point Light");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateLight();
            }
        });
        taskPane.add("br left", new JLabel("Color:"));
        taskPane.add("tab hfill", colorButton);
        taskPane.add("br left", new JLabel("Position:"));
        taskPane.add("tab hfill", positionPanel);
        taskPane.add("br left", new JLabel("Radius:"));
        taskPane.add("tab hfill", radiusTextField);
        taskPane.add("br right", applyButton);
        updateLight();
    }

    @Override
    public void updateData(boolean urgent)
    {
        if (!positionPanel.hasFocus())
        {
            positionPanel.setVector(((PointLight) lightModel.getLight()).getPosition());
            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    lightModel.getSymbol().setLocalTranslation(positionPanel.getVector());
                    return null;
                }
            });
        }
    }

    private void updateLight()
    {
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PointLight pLight = ((PointLight) lightModel.getLight());
                lightModel.getLight().setColor(Wizard.makeColorRGBA(colorButton.getColor()));
                pLight.setPosition(positionPanel.getVector());
                pLight.setRadius(Float.parseFloat(radiusTextField.getText()));
                lightModel.getRepresentative().setLocalTranslation(positionPanel.getVector());
                lightModel.getSymbol().setMesh(new Sphere(15, 15, pLight.getRadius()));
                lightModel.getSymbol().setLocalTranslation(positionPanel.getVector());
                UserActionManager.addState(lightModel.getLight(), "Edit " + lightModel.getLight().getName());
                return null;
            }
        });
    }
}
