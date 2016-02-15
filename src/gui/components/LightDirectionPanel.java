package gui.components;

import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.math.Vector3f;
import components.BSlider;
import components.Float3Panel;
import general.CurrentData;
import general.UAManager;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import monkeyStuff.LightModel;
import other.Wizard;
import se.datadosen.component.RiverLayout;

/**
 *
 * @author David
 */
public abstract class LightDirectionPanel extends JPanel
{

    private Float3Panel float3Panel;
    private DirectionSliderPanel sliderPanel;
    private LightModel lightModel;
    private boolean skip = false;

    public LightDirectionPanel(Vector3f vec)
    {
        init(vec);
    }

    public LightDirectionPanel(LightModel lm)
    {
        lightModel = lm;
        Vector3f vec = null;
        if (lm.getLight() instanceof DirectionalLight)
            vec = ((DirectionalLight) lightModel.getLight()).getDirection();
        else
            vec = ((SpotLight) lightModel.getLight()).getDirection();
        init(vec);
    }

    private void init(Vector3f vec)
    {
        float3Panel = new Float3Panel(vec, Wizard.getCamera(), Float3Panel.HORIZONTAL)
        {
            @Override
            public void setVector(Vector3f vec)
            {
                if (!skip)
                    updateDirection(vec);
                super.setVector(vec);
                skip = false;
            }
        };
        sliderPanel = new DirectionSliderPanel(vec);
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
                        updateDirection(newDirection);
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
                            updateDirection(newDirection);
                            return null;
                        }
                    });
                }
            }
        });
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

    public void setVector(Vector3f newVec)
    {
        System.out.println("Setting ui to "+newVec);
        float3Panel.setVector(newVec);
        sliderPanel.setVector3f(newVec);
        updateDirection(newVec);
    }

    public class DirectionSliderPanel extends JPanel
    {

        private JPanel xPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        private JPanel yPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        private JPanel zPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        private BSlider xSlider = new BSlider(Float.class, -1, 1, 0);
        private BSlider ySlider = new BSlider(Float.class, -1, 1, 0);
        private BSlider zSlider = new BSlider(Float.class, -1, 1, 0);

        public DirectionSliderPanel(Vector3f v)
        {
            xSlider.setPaintLabels(true);
            ySlider.setPaintLabels(true);
            zSlider.setPaintLabels(true);
            xSlider._setValue(v.getX());
            ySlider._setValue(v.getY());
            zSlider._setValue(v.getZ());
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
            MouseAdapter rotationAdapter = new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    if (lightModel != null)
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

        public BSlider getxSlider()
        {
            return xSlider;
        }

        public void setxSlider(BSlider xSlider)
        {
            this.xSlider = xSlider;
        }

        public BSlider getySlider()
        {
            return ySlider;
        }

        public void setySlider(BSlider ySlider)
        {
            this.ySlider = ySlider;
        }

        public BSlider getzSlider()
        {
            return zSlider;
        }

        public void setzSlider(BSlider zSlider)
        {
            this.zSlider = zSlider;
        }
    }

    public DirectionSliderPanel getSliderPanel()
    {
        return sliderPanel;
    }

    public void setSliderPanel(DirectionSliderPanel sliderPanel)
    {
        this.sliderPanel = sliderPanel;
    }

    public void skipUpdate()
    {
        skip = true;
    }

    public abstract void updateDirection(final Vector3f direction);
}
