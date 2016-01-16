package gui.editPanes.others;

import gui.dialogs.AdditionalCameraDialog;
import general.CurrentData;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import components.BSlider;
import components.EditTaskPane;
import components.Float3Panel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class AdditionalCameraTaskPane extends EditTaskPane
{

    private Float3Panel positionPanel;
    private RotationPanel rotationPanel;
    private AdditionalCameraDialog additionalCameraDialog;

    public AdditionalCameraTaskPane(AdditionalCameraDialog acd)
    {
        additionalCameraDialog = acd;
        positionPanel = new Float3Panel(acd.getRepresentative().getLocalTranslation(), Wizard.getCamera(), Float3Panel.HORIZONTAL)
        {
            @Override
            public void setVector(Vector3f vec)
            {
                if (positionPanel.isManualUserChangeMade())
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            additionalCameraDialog.getRepresentative().setLocalTranslation(positionPanel.getVector());
                            return null;
                        }
                    });
                    positionPanel.setManualUserChangeMade(false);
                }
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
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        additionalCameraDialog.getRepresentative().setLocalTranslation(positionPanel.getVector());
                        return null;
                    }
                });
            }
        });
        rotationPanel = new RotationPanel(additionalCameraDialog.getRepresentative());
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Camera");
        add(taskPane, BorderLayout.CENTER);
        taskPane.add("br", new JLabel("Position:"));
        taskPane.add(positionPanel);
        taskPane.add("br hfill", new JSeparator(JSeparator.HORIZONTAL));
        taskPane.add("br hfill", rotationPanel);
    }

    @Override
    public void updateData(boolean urgent)
    {
        if (!positionPanel.hasFocus())
        {
            positionPanel.setVector(additionalCameraDialog.getCamera().getLocation());
        }
    }

    public class RotationPanel extends JPanel
    {

        private Float3Panel rotationFloatPanel;
        private RotationSliderPanel sliderPanel;
        private Spatial tempSpatial;

        /**
         *
         * @param tempSpatial
         */
        public RotationPanel(final Spatial spatial)
        {
            tempSpatial = spatial;
            rotationFloatPanel = new Float3Panel((Vector3f) tempSpatial.getUserData("angles"), Wizard.getCamera(), Float3Panel.HORIZONTAL);
            rotationFloatPanel.addFieldFocusListener(new FocusListener()
            {
                @Override
                public void focusGained(FocusEvent e)
                {
                }

                @Override
                public void focusLost(FocusEvent e)
                {
                    updateRotation();
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
                    updateRotation();
                }
            });
            sliderPanel = new RotationSliderPanel(rotationFloatPanel, tempSpatial);
            setLayout(new RiverLayout());
            add("br", sliderPanel);
            add("br tab left", new JLabel("X, Y, Z:"));
            add(rotationFloatPanel);
        }

        private void updateRotation()
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
                    tempSpatial.setLocalRotation(nextRotation);
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
                sliderPanel.setVector3f(vec);
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
}
