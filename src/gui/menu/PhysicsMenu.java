package gui.menu;

import general.CurrentData;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Spatial;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import other.Wizard;

public class PhysicsMenu extends JMenu
{

    private JMenuItem startStopItem = new JMenuItem("Start",new ImageIcon("dat//img//menu//play.png"));
    private JMenuItem clearItem = new JMenuItem("Clear ALL Physics",new ImageIcon("dat//img//menu//clear.png"));
    private JMenuItem speedItem = new JMenuItem("Set Speed",new ImageIcon("dat//img//menu//speed.png"));

    public PhysicsMenu()
    {
        initShortcuts();
        setText("Physics");
        add(clearItem);
        add(speedItem);
        add(new JSeparator());
        add(startStopItem);
        initActions();
    }

    private void initActions()
    {
        startStopItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.setPhysicsRunning(!CurrentData.getEditorWindow().getB3DApp().isPhysicsPlaying());
            }
        });
        speedItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().getBulletAppState().setSpeed(
                        Float.parseFloat(JOptionPane.showInputDialog("Set Physics Speed (Default: 1)",
                        CurrentData.getEditorWindow().getB3DApp().getBulletAppState().getSpeed())));
            }
        });
        clearItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (JOptionPane.showInternalConfirmDialog(PhysicsMenu.this.getParent(), "Are you sure? This will delete all Physics Properties of every Object") == JOptionPane.YES_OPTION)
                {
                    for (Object o : Wizard.getObjects().getOriginalObjectsIterator())
                    {
                        if (o instanceof Spatial)
                        {
                            if (((Spatial) o).getControl(RigidBodyControl.class) != null)
                            {
                                CurrentData.getEditorWindow().getB3DApp().getBulletAppState().getPhysicsSpace().remove(o);
                                ((Spatial) o).removeControl(RigidBodyControl.class);
                            }
                        }
                    }
                }
                CurrentData.getEditorWindow().getEditPane().arrange(true);
            }
        });
    }

    private void initShortcuts()
    {
        startStopItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
    }

    public JMenuItem getStartStopItem()
    {
        return startStopItem;
    }
}
