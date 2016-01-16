package gui.editPanes.lights;

import gui.components.BColorButton;
import general.CurrentData;
import monkeyStuff.LightModel;
import com.jme3.light.SpotLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Line;
import components.BSlider;
import components.BTextField;
import components.EditTaskPane;
import components.Float3Panel;
import general.UAManager;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import se.datadosen.component.RiverLayout;

public class SpotLightTaskPane extends EditTaskPane
{

    private BColorButton colorButton;
    private Float3Panel positionPanel;
    private SpotLightDirectionPanel rotationPanel;
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
        rotationPanel = new SpotLightDirectionPanel();
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

    public class SpotLightDirectionPanel extends JPanel
    {

        private Float3Panel float3Panel;
        private DirectionSliderPanel sliderPanel;

        public SpotLightDirectionPanel()
        {
            float3Panel = new Float3Panel(((SpotLight) lightModel.getLight()).getDirection(), Wizard.getCamera(), Float3Panel.HORIZONTAL)
            {
                @Override
                public void setVector(Vector3f vec)
                {
                    ((SpotLight) lightModel.getLight()).setDirection(vec);
                    super.setVector(vec);
                }
            };
            float3Panel.addFieldFocusListener(new FocusListener()
            {
                @Override
                public void focusGained(FocusEvent e)
                {
                }

                @Override
                public void focusLost(FocusEvent e)
                {
                    float x = float3Panel.getVector().getX();
                    float y = float3Panel.getVector().getY();
                    float z = float3Panel.getVector().getZ();
                    Vector3f newDirection = new Vector3f(x, y, z);
                    sliderPanel.setVector3f(newDirection);
                    updateLightDirection(newDirection);
                }
            });
            float3Panel.addFieldKeyListener(new KeyListener()
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
                    {
                        float x = float3Panel.getVector().getX();
                        float y = float3Panel.getVector().getY();
                        float z = float3Panel.getVector().getZ();
                        Vector3f newDirection = new Vector3f(x, y, z);
                        sliderPanel.setVector3f(newDirection);
                        UAManager.add(lightModel.getLight(), "Rotate " + lightModel.getLight().getName());
                        updateLightDirection(newDirection);
                    }
                }
            });
            sliderPanel = new DirectionSliderPanel();
            setLayout(new RiverLayout());
            add("br", sliderPanel);
            add("br tab left", new JLabel("X, Y, Z:"));
            add(float3Panel);
        }

        public Vector3f getVector()
        {
            return float3Panel.getVector();
        }

        public class DirectionSliderPanel extends JPanel
        {

            private JPanel xPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            private JPanel yPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            private JPanel zPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            private BSlider xSlider = new BSlider(Float.class, -1, 1, 0);
            private BSlider ySlider = new BSlider(Float.class, -1, 1, 0);
            private BSlider zSlider = new BSlider(Float.class, -1, 1, 0);

            public DirectionSliderPanel()
            {
                xSlider.setPaintLabels(true);
                ySlider.setPaintLabels(true);
                zSlider.setPaintLabels(true);
                xSlider._setValue(((SpotLight) lightModel.getLight()).getDirection().getX());
                ySlider._setValue(((SpotLight) lightModel.getLight()).getDirection().getY());
                zSlider._setValue(((SpotLight) lightModel.getLight()).getDirection().getZ());
                setLayout(new GridLayout(4, 1));
                xPanel.add(new JLabel(" X:"));
                yPanel.add(new JLabel(" Y:"));
                zPanel.add(new JLabel(" Z:"));
                xPanel.add(xSlider);
                yPanel.add(ySlider);
                zPanel.add(zSlider);
                add(new JLabel("Rotate"));
                add(xPanel);
                add(yPanel);
                add(zPanel);
                initSliderListener();
            }
            int number = 180;

            private void initSliderListener()
            {
                xSlider.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        updateLightDirection(sliderPanel.getVector());
                    }
                });
                ySlider.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        updateLightDirection(sliderPanel.getVector());
                    }
                });
                zSlider.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        updateLightDirection(sliderPanel.getVector());
                    }
                });
                MouseAdapter rotationAdapter = new MouseAdapter()
                {
                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        UAManager.add(lightModel.getLight(), "Rotate " + lightModel.getLight().getName());
                    }
                };
                xSlider.addMouseListener(rotationAdapter);
                ySlider.addMouseListener(rotationAdapter);
                zSlider.addMouseListener(rotationAdapter);
            }

            /**
             *
             * @return the current Vector3f
             */
            public Vector3f getVector()
            {
                return new Vector3f(xSlider._getValue(), ySlider._getValue(), zSlider._getValue());
            }

            /**
             * Sets the current Vector3f
             *
             * @param newVector3f
             */
            public void setVector3f(Vector3f newVector3f)
            {
                xSlider._setValue(newVector3f.getX());
                ySlider._setValue(newVector3f.getY());
                zSlider._setValue(newVector3f.getZ());
            }
        }
    }

    private void updateLightDirection(final Vector3f direction)
    {
        rotationPanel.float3Panel.setVector(direction);
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
        if (!positionPanel.hasFocus())
        {
            positionPanel.setVector(((SpotLight) lightModel.getLight()).getPosition());
            updateLightDirection(((SpotLight) lightModel.getLight()).getDirection());
        }
    }
}
