package gui.editPanes.spatials;

import gui.components.ScaleSliderPanel;
import general.CurrentData;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import components.Float3Panel;
import general.UAManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class ScalePanel extends JPanel
{

    private Float3Panel scalePanel;
    private ScaleSliderPanel sliderPanel;

    /**
     *
     * @param tempSpatial
     */
    public ScalePanel(final Spatial tempSpatial)
    {
        scalePanel = new Float3Panel((Vector3f) tempSpatial.getUserData("scale"), Wizard.getCamera());
        scalePanel.addFieldFocusListener(new FocusListener()
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
                        tempSpatial.setLocalScale(scalePanel.getVector());
                        sliderPanel.setVector3f(scalePanel.getVector());
                        return null;
                    }
                });
            }
        });
        scalePanel.addFieldKeyListener(new KeyListener()
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
                            tempSpatial.setLocalScale(scalePanel.getVector());
                            sliderPanel.setVector3f(scalePanel.getVector());
                            UAManager.add(tempSpatial, "Scale " + tempSpatial.getName());
                            return null;
                        }
                    });
                }
            }
        });
        sliderPanel = new ScaleSliderPanel(scalePanel, tempSpatial);
        setLayout(new RiverLayout());
        add("br", sliderPanel);
        add("br tab left", new JLabel("X, Y, Z:"));
        add(scalePanel);
    }

    public Vector3f getVector()
    {
        return scalePanel.getVector();
    }
}
