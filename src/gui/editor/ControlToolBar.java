package gui.editor;

import general.CurrentData;
import com.jme3.math.Vector3f;
import components.BButton;
import components.Checker;
import dialogs.ObserverDialog;
import general.Preference;
import other.Wizard;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import se.datadosen.component.RiverLayout;

public class ControlToolBar extends JToolBar
{

    private JPanel panelLeft = new JPanel(new RiverLayout());
    private JPanel panelMid = new JPanel(new RiverLayout());
    private JPanel panelRight = new JPanel(new GridLayout(4, 1));
    private Checker gridChecker = new Checker();
    private Checker filterChecker = new Checker();
    private JComboBox camSpeedComboBox = new JComboBox(new String[]
    {
        CurrentData.CAM_VERY_SLOW + "  (Very Slow)",
        CurrentData.CAM_SLOW + "  (Slow)",
        CurrentData.CAM_DEFAULT + " (Default)",
        CurrentData.CAM_FAST + " (Fast)",
        CurrentData.CAM_VERY_FAST + " (Very Fast)",
        "Custom"
    });
    private Checker wireChecker = new Checker();
    private Checker physicsStartStopChecker = new Checker();
    private BButton camToCenterButton = new BButton("Center Camera", new EmptyBorder(1, 10, 1, 10));
    private Checker sceneryViewChecker = new Checker();
    private Checker physicsDebugChecker = new Checker();
    /*Labels*/
    private JLabel camLocationLabel = new JLabel("Camera Location: ");
    private JLabel camDirectionLabel = new JLabel("Camera Direction: ");
    private JLabel selectedObjectLabel = new JLabel("Selected Object: ");
    private JLabel dimensionLabel = new JLabel("Resolution: ");

    public ControlToolBar()
    {
        physicsStartStopChecker.setChecked(false);
        camSpeedComboBox.setLightWeightPopupEnabled(false);
        initConfigValues();
        initConfigListener();
        setBorderPainted(false);
        setBorder(null);
        setLayout(new GridLayout(1, 3));
        setMargin(new Insets(0, 0, 0, 0));
        panelLeft.add(new JLabel("Show Grid"));
        panelLeft.add("tab", gridChecker);
        panelLeft.add("br", new JLabel("Show Filters:"));
        panelLeft.add("tab", filterChecker);
        panelLeft.add("br", new JLabel("Wireframe:"));
        panelLeft.add("tab", wireChecker);
        panelLeft.add("br", new JLabel("Scenery View:"));
        panelLeft.add("tab", sceneryViewChecker);
        panelMid.add(new JLabel("Physics:"));
        panelMid.add("tab", physicsStartStopChecker);
        panelMid.add("br", new JLabel("Physics Shapes"));
        panelMid.add("tab", physicsDebugChecker);
        panelMid.add("br", new JLabel("Cam Speed:"));
        panelMid.add("tab", camSpeedComboBox);
        panelMid.add("br tab", camToCenterButton);
        panelRight.add(selectedObjectLabel);
        panelRight.add(camLocationLabel);
        panelRight.add(camDirectionLabel);
        panelRight.add(dimensionLabel);
        add(panelLeft);
        add(panelMid);
        add(panelRight);
        setEnabled(false);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep(40);
                    } catch (InterruptedException ex)
                    {
                        ObserverDialog.getObserverDialog().printError("Thread.sleep in ControlToolBar interrupted", ex);
                    }
                    if (CurrentData.isAppRunning())
                        updateLabels();
                }
            }
        }).start();
    }

    private void updateLabels()
    {
        camDirectionLabel.setText("Camera Direction: " + CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection());
        camLocationLabel.setText("Camera Location: " + CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation());
        try
        {
            selectedObjectLabel.setText("ID: " + CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
        } catch (java.lang.NullPointerException npe)
        {
            selectedObjectLabel.setText("");
        }
        dimensionLabel.setText("Dimension:  " + CurrentData.getEditorWindow().getCanvasPanel().getSize().width + " x " + CurrentData.getEditorWindow().getCanvasPanel().getSize().height);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        for (Component component : getComponents())
        {
            component.setEnabled(enabled);
            if (component instanceof Container)
                setEnabled((Container) component, enabled);
        }
    }

    /**
     *
     * @param container
     * @param enabled
     */
    public void setEnabled(Container container, boolean enabled)
    {
        for (Component component : container.getComponents())
        {
            component.setEnabled(enabled);
            if (component instanceof Container)
                setEnabled((Container) component, enabled);
        }
    }

    private void initConfigValues()
    {
        gridChecker.setChecked((Boolean) CurrentData.getPrefs().get(Preference.SHOW_GRID));
        filterChecker.setChecked((Boolean) CurrentData.getPrefs().get(Preference.SHOW_FILTERS));
        float speed = (Float) CurrentData.getPrefs().get(Preference.CAM_SPEED);
        if (speed == 10)
            camSpeedComboBox.setSelectedIndex(0);
        else if (speed == 50)
            camSpeedComboBox.setSelectedIndex(1);
        else if (speed == 100)
            camSpeedComboBox.setSelectedIndex(2);
        else if (speed == 200)
            camSpeedComboBox.setSelectedIndex(3);
        else if (speed == 400)
            camSpeedComboBox.setSelectedIndex(4);
        else
        {
            camSpeedComboBox.removeItemAt(camSpeedComboBox.getItemCount() - 1);
            camSpeedComboBox.addItem("Custom (" + CurrentData.getPrefs().get(Preference.CAM_SPEED) + ")");
            camSpeedComboBox.setSelectedIndex(camSpeedComboBox.getItemCount() - 1);
        }
        wireChecker.setChecked((Boolean) CurrentData.getPrefs().get(Preference.SHOW_WIREFRAME));
        sceneryViewChecker.setChecked((Boolean) CurrentData.getPrefs().get(Preference.SHOW_SCENERY));
    }
    private JPopupMenu idPopup = new JPopupMenu();
    private JMenuItem copyItem = new JMenuItem("Copy ID", new ImageIcon("dat//img//menu//duplicate.png"));

    private void initConfigListener()
    {
        idPopup.add(copyItem);
        copyItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selectedObjectLabel.getText().substring(4)), null);
            }
        });
        selectedObjectLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON3)
                    idPopup.show(selectedObjectLabel, e.getX(), e.getY());
            }
        });
        physicsStartStopChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.setPhysicsRunning(physicsStartStopChecker.isChecked());
            }
        });
        camToCenterButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().getCamera().setLocation(Vector3f.ZERO);
            }
        });
        physicsDebugChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().getBulletAppState().setDebugEnabled(physicsDebugChecker.isChecked());
            }
        });
        gridChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getPrefs().set(Preference.SHOW_GRID, gridChecker.isChecked());
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        if (gridChecker.isChecked())
                        {
                            CurrentData.getEditorWindow().getB3DApp().getEditorNode().attachChild(
                                    CurrentData.getEditorWindow().getB3DApp().getGridGeometry());
                        } else
                        {
                            CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(
                                    CurrentData.getEditorWindow().getB3DApp().getGridGeometry());
                        }
                        return null;
                    }
                });
            }
        });
        filterChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getPrefs().set(Preference.SHOW_FILTERS, filterChecker.isChecked());
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        if (filterChecker.isChecked())
                        {
                            CurrentData.getEditorWindow().getB3DApp().getViewPort().addProcessor(
                                    CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor());
                        } else
                        {
                            CurrentData.getEditorWindow().getB3DApp().getViewPort().removeProcessor(
                                    CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor());
                        }
                        return null;
                    }
                });
            }
        });
        wireChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getPrefs().set(Preference.SHOW_WIREFRAME, wireChecker.isChecked());
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        Wizard.setWireframe(CurrentData.getEditorWindow().getB3DApp().getSceneNode(), wireChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        sceneryViewChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        if (sceneryViewChecker.isChecked())
                        {
                            CurrentData.getEditorWindow().getB3DApp().getRootNode().detachChild(
                                    CurrentData.getEditorWindow().getB3DApp().getEditorNode());
                        } else
                        {
                            CurrentData.getEditorWindow().getB3DApp().getRootNode().attachChild(
                                    CurrentData.getEditorWindow().getB3DApp().getEditorNode());
                        }
                        return null;
                    }
                });
                CurrentData.getPrefs().set(Preference.SHOW_SCENERY, sceneryViewChecker.isChecked());
            }
        });
        camSpeedComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (camSpeedComboBox.getSelectedIndex() == 0)
                {
                    CurrentData.getEditorWindow().getB3DApp().getFlyByCamera().setMoveSpeed(10);
                } else if (camSpeedComboBox.getSelectedIndex() == 1)
                {
                    CurrentData.getEditorWindow().getB3DApp().getFlyByCamera().setMoveSpeed(50);
                } else if (camSpeedComboBox.getSelectedIndex() == 2)
                {
                    CurrentData.getEditorWindow().getB3DApp().getFlyByCamera().setMoveSpeed(100);
                } else if (camSpeedComboBox.getSelectedIndex() == 3)
                {
                    CurrentData.getEditorWindow().getB3DApp().getFlyByCamera().setMoveSpeed(200);
                } else if (camSpeedComboBox.getSelectedIndex() == 4)
                {
                    CurrentData.getEditorWindow().getB3DApp().getFlyByCamera().setMoveSpeed(400);
                } else
                {
                    float newSpeed = Float.parseFloat(JOptionPane.showInputDialog("New Camera Speed:"));
                    CurrentData.getEditorWindow().getB3DApp().getFlyByCamera().setMoveSpeed(newSpeed);
                    camSpeedComboBox.removeItemAt(camSpeedComboBox.getItemCount() - 1);
                    camSpeedComboBox.addItem("Custom (" + newSpeed + ")");
                    camSpeedComboBox.setSelectedIndex(camSpeedComboBox.getItemCount() - 1);
                }
                CurrentData.getPrefs().set(Preference.CAM_SPEED, CurrentData.getEditorWindow().getB3DApp().getFlyByCamera().getMoveSpeed());
            }
        });
    }

    public Checker getPhysicsStartStopChecker()
    {
        return physicsStartStopChecker;
    }

    public Checker getGridChecker()
    {
        return gridChecker;
    }

    public Checker getFilterChecker()
    {
        return filterChecker;
    }

    public Checker getWireChecker()
    {
        return wireChecker;
    }

    public Checker getSceneryViewChecker()
    {
        return sceneryViewChecker;
    }
}
