package gui.editPanes.lights;

import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import monkeyStuff.LightModel;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Line;
import components.BSlider;
import components.Float3Panel;
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
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import se.datadosen.component.RiverLayout;

public class DirectionalLightTaskPane extends EditTaskPane
{

    private BColorButton colorButton;
    private DirectionalLightDirectionPanel rotationPanel;
    private LightModel lightModel;

    /**
     *
     * @param model
     */
    public DirectionalLightTaskPane(LightModel model)
    {
        lightModel = model;
        colorButton = new BColorButton(Wizard.makeColor(lightModel.getLight().getColor()));
        rotationPanel = new DirectionalLightDirectionPanel();
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Directional Light");
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                lightModel.getLight().setColor(Wizard.makeColorRGBA(colorButton.getColor()));
                lightModel.getSymbol().getMaterial().setColor("Color", lightModel.getLight().getColor());
            }
        });
        taskPane.add("br", new JLabel("Color:"));
        taskPane.add("hfill", colorButton);
        taskPane.add("br", rotationPanel);
        taskPane.add("br", applyButton);
        add(taskPane, BorderLayout.CENTER);
    }

    public class DirectionalLightDirectionPanel extends JPanel
    {

        private Float3Panel float3Panel;
        private DirectionSliderPanel sliderPanel;

        public DirectionalLightDirectionPanel()
        {
            float3Panel = new Float3Panel(((DirectionalLight) lightModel.getLight()).getDirection(), Wizard.getCamera())
            {
                @Override
                public void setVector(Vector3f vec)
                {
                    ((DirectionalLight) lightModel.getLight()).setDirection(vec);
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
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        public Void call() throws Exception
                        {
                            float x = float3Panel.getVector().getX();
                            float y = float3Panel.getVector().getY();
                            float z = float3Panel.getVector().getZ();
                            Vector3f newDirection = new Vector3f(x, y, z);
                            sliderPanel.setVector3f(newDirection);
                            ((DirectionalLight) lightModel.getLight()).setDirection(newDirection);
                            return null;
                        }
                    });
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
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                        {
                            @Override
                            public Void call() throws Exception
                            {
                                float x = float3Panel.getVector().getX();
                                float y = float3Panel.getVector().getY();
                                float z = float3Panel.getVector().getZ();
                                Vector3f newDirection = new Vector3f(x, y, z);
                                sliderPanel.setVector3f(newDirection);
                                ((DirectionalLight) lightModel.getLight()).setDirection(newDirection);
                                return null;
                            }
                        });
                    }
                }
            });
            sliderPanel = new DirectionSliderPanel();
            setLayout(new RiverLayout());
            add("br", sliderPanel);
            add("br tab left", new JLabel("X, Y, Z:"));
            add(float3Panel);
        }

        public Float3Panel getFloat3Panel()
        {
            return float3Panel;
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
                xSlider._setValue(((DirectionalLight) lightModel.getLight()).getDirection().getX());
                ySlider._setValue(((DirectionalLight) lightModel.getLight()).getDirection().getY());
                zSlider._setValue(((DirectionalLight) lightModel.getLight()).getDirection().getZ());
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
                        updateDirection(sliderPanel.getVector());
                    }
                });
                ySlider.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        updateDirection(sliderPanel.getVector());
                    }
                });
                zSlider.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        updateDirection(sliderPanel.getVector());
                    }
                });
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

    private void updateDirection(final Vector3f direction)
    {
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                ((DirectionalLight) lightModel.getLight()).setDirection(direction);
                rotationPanel.float3Panel.setVector(direction);
                Vector3f end = lightModel.getRepresentative().getLocalTranslation().add(
                        ((DirectionalLight) lightModel.getLight()).getDirection().mult(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumFar() * 2));
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
        if (!rotationPanel.sliderPanel.xSlider.hasFocus() && !rotationPanel.sliderPanel.ySlider.hasFocus() && !rotationPanel.sliderPanel.zSlider.hasFocus()
                && !rotationPanel.getFloat3Panel().hasFocus())
            updateDirection(((DirectionalLight) lightModel.getLight()).getDirection());
    }
}
