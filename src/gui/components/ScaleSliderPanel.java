package gui.components;

import general.CurrentData;
import com.jme3.math.*;
import com.jme3.scene.*;
import components.BSlider;
import components.Float3Panel;
import general.UAManager;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A JPanel, used to edit the rotation of objects in the Scene
 *
 * @author David
 */
public class ScaleSliderPanel extends JPanel
{

    private Spatial spatial;
    private JPanel tPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private JPanel xPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private JPanel yPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private JPanel zPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private BSlider tSlider = new BSlider(Float.class, 0, 2, 1);
    private BSlider xSlider = new BSlider(Float.class, 0, 1, 0);
    private BSlider ySlider = new BSlider(Float.class, 0, 1, 0);
    private BSlider zSlider = new BSlider(Float.class, 0, 1, 0);
    private Vector3f scale = new Vector3f();
    private Float3Panel float3Panel;

    /**
     * Creates a RotationPanel, used to change the scale of objects
     * with angles
     *
     * @param panel3f -> depend on each other
     * @param _spatial that is being scaled
     */
    public ScaleSliderPanel(Float3Panel panel3f, Spatial _spatial)
    {
        //Give the slider of the total scale a special appearance
        tSlider.setOpaque(false);
        float3Panel = panel3f;
        spatial = _spatial;
        scale = _spatial.getUserData("scale");
        tSlider.setPaintLabels(false);
        xSlider.setPaintLabels(true);
        ySlider.setPaintLabels(true);
        zSlider.setPaintLabels(true);
        tSlider._setMax(2);
        xSlider._setMax(scale.getX() * 2);
        ySlider._setMax(scale.getY() * 2);
        zSlider._setMax(scale.getZ() * 2);
        xSlider._setValue(scale.getX());
        ySlider._setValue(scale.getY());
        zSlider._setValue(scale.getZ());
        setLayout(new GridLayout(5, 1));
        tPanel.add(new JLabel("T:"));
        xPanel.add(new JLabel("X:"));
        yPanel.add(new JLabel("Y:"));
        zPanel.add(new JLabel("Z:"));
        tPanel.add(tSlider);
        tPanel.setBackground(tPanel.getBackground().darker());
        xPanel.add(xSlider);
        yPanel.add(ySlider);
        zPanel.add(zSlider);
        add(new JLabel("Scale"));
        add(tPanel);
        add(xPanel);
        add(yPanel);
        add(zPanel);
        initSliderListener();
    }

    /**
     * Adds focus- and change-listeners
     */
    private void initSliderListener()
    {
        tSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateScale();
            }
        });
        xSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateScale();
            }
        });
        ySlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateScale();
            }
        });
        zSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateScale();
            }
        });
        tSlider.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                tSlider._setMax(tSlider._getValue() * 2);
                if (tSlider._getMax() < 1)
                {
                    tSlider._setMax(1);
                }
            }
        });
        xSlider.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                xSlider._setMax(xSlider._getValue() * 2);
                if (xSlider._getMax() < 1)
                {
                    xSlider._setMax(1);
                }
            }
        });
        ySlider.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                ySlider._setMax(ySlider._getValue() * 2);
                if (ySlider._getMax() < 1)
                {
                    ySlider._setMax(1);
                }
            }
        });
        zSlider.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                zSlider._setMax(zSlider._getValue() * 2);
                if (zSlider._getMax() < 1)
                {
                    zSlider._setMax(1);
                }
            }
        });
        MouseAdapter rotationAdapter = new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(spatial, "Scale " + spatial.getName());
            }
        };
        xSlider.addMouseListener(rotationAdapter);
        ySlider.addMouseListener(rotationAdapter);
        zSlider.addMouseListener(rotationAdapter);
    }

    private void updateScale()
    {
        final Vector3f newScale = new Vector3f(xSlider._getValue() * tSlider._getValue(),
                ySlider._getValue() * tSlider._getValue(),
                zSlider._getValue() * tSlider._getValue());
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                spatial.setLocalScale(newScale);
                float3Panel.setVector(newScale);
                return null;
            }
        });
        scale = newScale;
        spatial.setUserData("scale", scale);
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
     * Sets the current Vector3f & updates the GUI
     *
     * @param newVector3f
     */
    public void setVector3f(Vector3f newVector3f)
    {
        tSlider._setMax(2);
        xSlider._setMax(newVector3f.getX() * 2);
        ySlider._setMax(newVector3f.getY() * 2);
        zSlider._setMax(newVector3f.getZ() * 2);
        tSlider._setValue(1);
        xSlider._setValue(newVector3f.getX());
        ySlider._setValue(newVector3f.getY());
        zSlider._setValue(newVector3f.getZ());
        if (tSlider._getMax() < 1)
        {
            tSlider._setMax(1);
        }
        if (xSlider._getMax() < 1)
        {
            xSlider._setMax(1);
        }
        if (ySlider._getMax() < 1)
        {
            ySlider._setMax(1);
        }
        if (zSlider._getMax() < 1)
        {
            zSlider._setMax(1);
        }
    }
}
