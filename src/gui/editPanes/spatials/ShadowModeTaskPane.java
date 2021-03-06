package gui.editPanes.spatials;

import b3dElements.spatials.B3D_Node;
import general.CurrentData;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import components.EditTaskPane;
import general.UAManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import java.util.concurrent.Callable;
import javax.swing.JComboBox;
import other.Wizard;

public class ShadowModeTaskPane extends EditTaskPane
{

    private JComboBox modeComboBox = new JComboBox(new String[]
    {
        "Cast", "Recieve", "Cast & Recieve", "Inherit", "Off"
    });
    private Spatial spatial;

    public ShadowModeTaskPane()
    {
        spatial = (Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
        if (spatial == null)
            return;
        if (spatial.getShadowMode().equals(RenderQueue.ShadowMode.Cast))
        {
            modeComboBox.setSelectedIndex(0);
        } else if (spatial.getShadowMode().equals(RenderQueue.ShadowMode.Receive))
        {
            modeComboBox.setSelectedIndex(1);
        } else if (spatial.getShadowMode().equals(RenderQueue.ShadowMode.CastAndReceive))
        {
            modeComboBox.setSelectedIndex(2);
        } else if (spatial.getShadowMode().equals(RenderQueue.ShadowMode.Inherit))
        {
            modeComboBox.setSelectedIndex(3);
        } else
        {
            modeComboBox.setSelectedIndex(4);
        }
        modeComboBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(final ItemEvent e)
            {
                switch (modeComboBox.getSelectedIndex())
                {
                    case 0:
                        spatial.setShadowMode(RenderQueue.ShadowMode.Cast);
                        break;
                    case 1:
                        spatial.setShadowMode(RenderQueue.ShadowMode.Receive);
                        break;
                    case 2:
                        spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                        break;
                    case 3:
                        spatial.setShadowMode(RenderQueue.ShadowMode.Inherit);
                        break;
                    case 4:
                        spatial.setShadowMode(RenderQueue.ShadowMode.Off);
                        break;
                }
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        if (e.getStateChange() == ItemEvent.SELECTED)
                            UAManager.add(spatial, "Set Shadow Mode of " + spatial.getName() + " to " + modeComboBox.getModel().getSelectedItem());
                        return null;
                    }
                });
            }
        });
        applyButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Vector<Spatial> spatials = new Vector<Spatial>();
                Wizard.insertAllSpatials((Node) spatial, spatials);
                for (Spatial s : spatials)
                    switch (modeComboBox.getSelectedIndex())
                    {
                        case 0:
                            s.setShadowMode(RenderQueue.ShadowMode.Cast);
                            break;
                        case 1:
                            s.setShadowMode(RenderQueue.ShadowMode.Receive);
                            break;
                        case 2:
                            s.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                            break;
                        case 3:
                            s.setShadowMode(RenderQueue.ShadowMode.Inherit);
                            break;
                        case 4:
                            s.setShadowMode(RenderQueue.ShadowMode.Off);
                            break;
                    }
            }
        });
        applyButton.setText("Also apply to children");
        taskPane.setTitle("Shadow Mode");
        taskPane.add(modeComboBox, BorderLayout.CENTER);
        if (Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(spatial.hashCode())) instanceof B3D_Node)
            taskPane.add(applyButton, BorderLayout.SOUTH);
        add(taskPane, BorderLayout.CENTER);
    }
}
