package gui.editPanes.spatials;

import b3dElements.spatials.B3D_Spatial;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import components.BSlider;
import components.Float3Panel;
import dialogs.ObserverDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class Basic3DTaskPane extends EditTaskPane
{

    private RotationPanel rotationPanel;
    private Float3Panel positionPanel;
    private ScalePanel scalePanel;
    private Spatial basicSpatial;
    private WorldTransformPane worldTransformPane;

    public Basic3DTaskPane()
    {
        basicSpatial = (Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
        worldTransformPane = new WorldTransformPane();
        rotationPanel = new RotationPanel(basicSpatial);
        scalePanel = new ScalePanel(basicSpatial);
        positionPanel = new Float3Panel(basicSpatial.getLocalTranslation(), Wizard.getCamera())
        {
            @Override
            public void setVector(final Vector3f vec)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        basicSpatial.setLocalTranslation(vec);
                        return null;
                    }
                });
                super.setVector(vec);
            }
        };
        positionPanel.addFieldFocusListener(new FocusListener()
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
                    @Override
                    public Void call() throws Exception
                    {
                        basicSpatial.setLocalTranslation(positionPanel.getVector());
                        return null;
                    }
                });
            }
        });
        positionPanel.addFieldKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    public Void call() throws Exception
                    {
                        basicSpatial.setLocalTranslation(positionPanel.getVector());
                        return null;
                    }
                });
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
            }
        });
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Transformation");
        add(taskPane, BorderLayout.CENTER);
        taskPane.add("br", new JLabel("Position:"));
        taskPane.add(positionPanel);
        taskPane.add("br hfill", new JSeparator(JSeparator.HORIZONTAL));
        taskPane.add("br hfill", rotationPanel);
        taskPane.add("br hfill", new JSeparator(JSeparator.HORIZONTAL));
        taskPane.add("br hfill", scalePanel);
        taskPane.add("br hfill", worldTransformPane);
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
                        float[] angles = new float[3];
                        angles[0] = FastMath.PI * rotationPanel.getVector().getX() / 180;
                        angles[1] = FastMath.PI * rotationPanel.getVector().getY() / 180;
                        angles[2] = FastMath.PI * rotationPanel.getVector().getZ() / 180;
                        basicSpatial.setLocalRotation(new Quaternion(angles));
                        basicSpatial.setUserData("angles", rotationPanel.getVector());
                        basicSpatial.setLocalTranslation(positionPanel.getVector().getX(), positionPanel.getVector().getY(), positionPanel.getVector().getZ());
                        basicSpatial.setLocalScale(scalePanel.getVector());
                        return null;
                    }
                });
            }
        });
    }

    public class RotationPanel extends JPanel
    {

        private Float3Panel rotationFloatPanel;
        private RotationSliderPanel sliderPanel;
        private Spatial spatial;

        /**
         *
         * @param tempSpatial
         */
        public RotationPanel(Spatial tempSpatial)
        {
            spatial = tempSpatial;
            rotationFloatPanel = new Float3Panel((Vector3f) spatial.getUserData("angles"), Wizard.getCamera());
            rotationFloatPanel.addFieldFocusListener(new FocusListener()
            {
                @Override
                public void focusGained(FocusEvent e)
                {
                }

                @Override
                public void focusLost(FocusEvent e)
                {
                    final Quaternion nextRotation = new Quaternion(new float[]
                    {
                        FastMath.DEG_TO_RAD * rotationFloatPanel.getVector().getX(),
                        FastMath.DEG_TO_RAD * rotationFloatPanel.getVector().getY(),
                        FastMath.DEG_TO_RAD * rotationFloatPanel.getVector().getZ()
                    });
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            spatial.setLocalRotation(nextRotation);
                            return null;
                        }
                    });
                    sliderPanel.setVector3f(new Vector3f(rotationFloatPanel.getVector().getX(), rotationFloatPanel.getVector().getY(), rotationFloatPanel.getVector().getZ()));
                }
            });
            rotationFloatPanel.addFieldKeyListener(new KeyListener()
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
                        updateSliderAndSpatial();
                    }
                }
            });
            sliderPanel = new RotationSliderPanel(rotationFloatPanel, spatial);
            setLayout(new RiverLayout());
            add("br", sliderPanel);
            add("br tab left", new JLabel("X, Y, Z:"));
            add(rotationFloatPanel);
        }

        private void updateSliderAndSpatial()
        {
            final Quaternion nextRotation = new Quaternion(new float[]
            {
                FastMath.DEG_TO_RAD * rotationFloatPanel.getVector().getX(),
                FastMath.DEG_TO_RAD * rotationFloatPanel.getVector().getY(),
                FastMath.DEG_TO_RAD * rotationFloatPanel.getVector().getZ()
            });
            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    spatial.setLocalRotation(nextRotation);
                    return null;
                }
            });
            sliderPanel.setVector3f(new Vector3f(rotationFloatPanel.getVector().getX(), rotationFloatPanel.getVector().getY(), rotationFloatPanel.getVector().getZ()));
        }

        public Float3Panel getRotationFloatPanel()
        {
            return rotationFloatPanel;
        }

        public RotationSliderPanel getSliderPanel()
        {
            return sliderPanel;
        }

        public Vector3f getVector()
        {
            return rotationFloatPanel.getVector();
        }

        public class RotationSliderPanel extends JPanel
        {

            private Spatial spatial;
            private JPanel xPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            private JPanel yPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            private JPanel zPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            private BSlider xSlider = new BSlider(Float.class, 0, 360, 0);
            private BSlider ySlider = new BSlider(Float.class, 0, 360, 0);
            private BSlider zSlider = new BSlider(Float.class, 0, 360, 0);
            private Vector3f rotation = new Vector3f();
            private Float3Panel float3Panel;

            /**
             *
             * @param panel3f
             * @param s
             */
            public RotationSliderPanel(Float3Panel panel3f, Spatial s)
            {
                float3Panel = panel3f;
                spatial = s;
                rotation = s.getUserData("angles");
                xSlider.setPaintLabels(true);
                ySlider.setPaintLabels(true);
                zSlider.setPaintLabels(true);
                xSlider._setValue(rotation.getX());
                ySlider._setValue(rotation.getY());
                zSlider._setValue(rotation.getZ());
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

            private void initSliderListener()
            {
                xSlider.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        updateRotation(getVector());
                    }
                });
                ySlider.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        updateRotation(getVector());
                    }
                });
                zSlider.addChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        updateRotation(getVector());
                    }
                });
            }

            private void updateRotation(Vector3f vec)
            {
                final Quaternion nextRotation = new Quaternion(new float[]
                {
                    FastMath.DEG_TO_RAD * vec.getX(),
                    FastMath.DEG_TO_RAD * vec.getY(),
                    FastMath.DEG_TO_RAD * vec.getZ()
                });
                float3Panel.setVector(vec);
                setVector3f(vec);
                spatial.setUserData("angles", vec);
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        spatial.setLocalRotation(nextRotation);
                        return null;
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

            public BSlider getxSlider()
            {
                return xSlider;
            }

            public BSlider getySlider()
            {
                return ySlider;
            }

            public BSlider getzSlider()
            {
                return zSlider;
            }
        }
    }

    @Override
    public void updateData(boolean urgent)
    {
        worldTransformPane.update();
        if (!rotationPanel.getVector().equals(basicSpatial.getUserData("angles"))
                && !rotationPanel.getRotationFloatPanel().getxField().hasFocus()
                && !rotationPanel.getRotationFloatPanel().getyField().hasFocus()
                && !rotationPanel.getRotationFloatPanel().getzField().hasFocus())
        {
            rotationPanel.getSliderPanel().updateRotation((Vector3f) basicSpatial.getUserData("angles"));
        }
        if (!basicSpatial.getLocalTranslation().equals(positionPanel.getVector())
                && !positionPanel.getyField().hasFocus()
                && !positionPanel.getzField().hasFocus()
                && !positionPanel.getxField().hasFocus())
        {
            if (!basicSpatial.getLocalTranslation().equals(positionPanel.getVector()))
            {
                positionPanel.setVector(basicSpatial.getLocalTranslation());
            }
        }
        /*DAFAQ?*/ try
        {
            UUID elementUUID = CurrentData.getEditorWindow().getB3DApp().getSelectedUUID();
            B3D_Spatial element = (B3D_Spatial) Wizard.getObjects().getB3D_Element(elementUUID);
            /*angles*/
            float[] angles = new float[3];
            angles[0] = FastMath.PI * rotationPanel.getVector().getX() / 180;
            angles[1] = FastMath.PI * rotationPanel.getVector().getY() / 180;
            angles[2] = FastMath.PI * rotationPanel.getVector().getZ() / 180;
            element.setRotation(new Quaternion(angles));
            /*location*/
            element.setTranslation(positionPanel.getVector());
        } catch (Exception cce)
        {
            ObserverDialog.getObserverDialog().printError("Error in Basic3DTaskPane while writing new Transform into B3D_Spatial", cce);
        }
    }

    private class WorldTransformPane extends EditTaskPane
    {

        private JLabel locationLabel = new JLabel();
        private JLabel rotationLabel = new JLabel();
        private JLabel scaleLabel = new JLabel();

        public WorldTransformPane()
        {
            taskPane.setCollapsed(true);
            taskPane.setLayout(new RiverLayout());
            taskPane.setTitle("World Transformation");
            add(taskPane, BorderLayout.CENTER);
            taskPane.add("br", new JLabel("Translation:"));
            taskPane.add("tab hfill", locationLabel);
            taskPane.add("br", new JLabel("Rotation:"));
            taskPane.add("tab hfill", rotationLabel);
            taskPane.add("br", new JLabel("Scale:"));
            taskPane.add("tab hfill", scaleLabel);
        }

        public void update()
        {
            DecimalFormat rounder = new DecimalFormat("0.0000");
            locationLabel.setText("X: "
                    + rounder.format(basicSpatial.getWorldTranslation().getX()) + "; Y: "
                    + rounder.format(basicSpatial.getWorldTranslation().getY()) + "; Z: "
                    + rounder.format(basicSpatial.getWorldTranslation().getZ()));
            rotationLabel.setText("X: "
                    + rounder.format(basicSpatial.getWorldRotation().getX()) + "; Y: "
                    + rounder.format(basicSpatial.getWorldRotation().getY()) + "; Z: "
                    + rounder.format(basicSpatial.getWorldRotation().getZ()) + "; W: "
                    + rounder.format(basicSpatial.getWorldRotation().getW()));
            scaleLabel.setText("X: "
                    + rounder.format(basicSpatial.getWorldScale().getX()) + "; Y: "
                    + rounder.format(basicSpatial.getWorldScale().getY()) + "; Z: "
                    + rounder.format(basicSpatial.getWorldScale().getZ()));
        }
    }
    
}
