package gui.editPanes.spatials;

import b3dElements.controls.B3D_LightControl;
import b3dElements.controls.B3D_LightScatteringMotionControl;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.post.Filter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LightControl;
import components.BButton;
import components.BComboBox;
import components.Checker;
import dialogs.SelectDialog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import monkeyStuff.LightScatteringMotionControl;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class OtherTaskPane extends EditTaskPane
{

    private Spatial spatial;
    private BComboBox othersComboBox = new BComboBox<String>(new String[]
    {
        "Add User Data", "Add Light Control", "Add Light Scattering Control"
    });
    private JPanel dataPanel = new JPanel(new RiverLayout(0, 0));
    private BButton addButton = new BButton("+");

    public OtherTaskPane(Spatial _spatial)
    {
        spatial = _spatial;
        addButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                switch (othersComboBox.getSelectedIndex())
                {
                    case 0:
                        addUserData();
                        break;
                    case 1:
                        addLightControl();
                        break;
                    case 2:
                        addLightScatteringControl();
                        break;
                }
            }
        });
        othersComboBox.setPreferredSize(new Dimension(220, 30));
        addButton.setPreferredSize(new Dimension(30, 30));
        add(taskPane, BorderLayout.CENTER);
        taskPane.setTitle("Other");
        taskPane.setLayout(new RiverLayout());
        taskPane.add("br left", othersComboBox);
        taskPane.add("", addButton);
        taskPane.add("br left", dataPanel);
        arrangeDataPanel();
    }

    private void addUserData()
    {
        String key = JOptionPane.showInputDialog("User Data Name: ");
        spatial.setUserData(key, "");
        arrangeDataPanel();
    }

    private void addLightControl()
    {
        Vector<String> names = new Vector<String>();
        Vector<Light> lights = new Vector<Light>();
        for (Light l : CurrentData.getEditorWindow().getB3DApp().getSceneNode().getWorldLightList())
        {
            if (l instanceof PointLight || l instanceof SpotLight)
            {
                names.add(l.getName());
                try
                {
                    lights.add((PointLight) l);
                } catch (ClassCastException cce)
                {
                    lights.add((SpotLight) l);
                }
            }
        }
        if (lights.size() == 0)
        {
            JOptionPane.showMessageDialog(othersComboBox, "There are no position-dependent Lights in this scene!", "...", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Collections.sort(names, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });
        Collections.sort(lights, new Comparator<Light>()
        {
            @Override
            public int compare(Light o1, Light o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        SelectDialog selectDialog = new SelectDialog("Select Light", names.toArray());
        Light selectedLight = lights.get(selectDialog.getSelectedIndex());
        LightControl lightControl = new LightControl(selectedLight, LightControl.ControlDirection.SpatialToLight);
        UUID lightUUID = Wizard.getObjectReferences().getUUID(selectedLight.hashCode());
        UUID spatialUUID = Wizard.getObjectReferences().getUUID(spatial.hashCode());
        B3D_LightControl b3D_LightControl = new B3D_LightControl(lightUUID, spatialUUID, true);
        Wizard.getObjects().add(lightControl, b3D_LightControl);
        spatial.addControl(lightControl);
        CurrentData.getEditorWindow().getB3DApp().getLightControls().add(lightControl);
        arrangeDataPanel();
    }

    private void addLightScatteringControl()
    {
        Vector<String> names = new Vector<String>();
        Vector<LightScatteringFilter> lightScatteringFilters = new Vector<LightScatteringFilter>();
        for (Filter l : CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList())
        {
            if (l instanceof LightScatteringFilter)
            {
                names.add(l.getName());
                lightScatteringFilters.add((LightScatteringFilter) l);
            }
        }
        if (lightScatteringFilters.size() == 0)
        {
            JOptionPane.showMessageDialog(othersComboBox, "There are no Light Scattering Filters in this scene!", "...", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Collections.sort(names, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });
        Collections.sort(lightScatteringFilters, new Comparator<LightScatteringFilter>()
        {
            @Override
            public int compare(LightScatteringFilter o1, LightScatteringFilter o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        SelectDialog selectDialog = new SelectDialog("Select Filter", names.toArray());
        LightScatteringFilter selectedFilter = lightScatteringFilters.get(selectDialog.getSelectedIndex());
        LightScatteringMotionControl lightScatteringMotionControl = new LightScatteringMotionControl(Wizard.getObjectReferences().getUUID(selectedFilter.hashCode()), true);
        UUID spatialUUID = Wizard.getObjectReferences().getUUID(spatial.hashCode());
        B3D_LightScatteringMotionControl b3D_LightScatteringMotionControl = new B3D_LightScatteringMotionControl(lightScatteringMotionControl.getFilterUUID(), spatialUUID, true);
        Wizard.getObjects().add(lightScatteringMotionControl, b3D_LightScatteringMotionControl);
        spatial.addControl(lightScatteringMotionControl);
        arrangeDataPanel();
    }

    private void arrangeDataPanel()
    {
        dataPanel.removeAll();
        for (int i = 0; i < spatial.getNumControls(); i++)
        {
            if (spatial.getControl(i) instanceof LightControl)
            {
                dataPanel.add("br", new LightControlPanel((LightControl) spatial.getControl(i)));
            } else if (spatial.getControl(i) instanceof LightScatteringMotionControl)
            {
                dataPanel.add("br", new LightScatteringMotionControlPanel((LightScatteringMotionControl) spatial.getControl(i)));
            }
        }
        for (String key : spatial.getUserDataKeys())
        {
            if (!Wizard.getReservedUserData().contains(key))
            {
                dataPanel.add("br", new UserDataPanel(key));
            }
        }
        repaint();
        validate();
        revalidate();
        repaint();
        dataPanel.repaint();
        dataPanel.validate();
        dataPanel.revalidate();
        dataPanel.repaint();
    }

    private class LightScatteringMotionControlPanel extends JPanel
    {

        private Checker enabledChecker;
        private BButton removeButton = new BButton("Delete");

        public LightScatteringMotionControlPanel(final LightScatteringMotionControl lightScatteringMotionControl)
        {
            setPreferredSize(new Dimension(440, 40));
            setBorder(new BevelBorder(BevelBorder.LOWERED));
            enabledChecker = new Checker();
            enabledChecker.setChecked(lightScatteringMotionControl.isEnabled());
            removeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    spatial.removeControl(lightScatteringMotionControl);
                    arrangeDataPanel();
                }
            });
            enabledChecker.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    lightScatteringMotionControl.setEnabled(enabledChecker.isChecked());
                }
            });
            setLayout(new RiverLayout(10, 0));
            add("tab", new JLabel("Enabled:"));
            add(enabledChecker);
            add("tab", removeButton);
        }
    }

    class LightControlPanel extends JPanel
    {

        private JComboBox lightChoiceComboBox;
        private Checker enabledChecker;
        private BButton removeButton = new BButton("Delete");

        public LightControlPanel(final LightControl lControl)
        {
            setPreferredSize(new Dimension(440, 40));
            setBorder(new BevelBorder(BevelBorder.LOWERED));
            Vector<String> lightNames = new Vector<String>();
            Vector<Light> lights = new Vector<Light>();
            for (Light l : CurrentData.getEditorWindow().getB3DApp().getSceneNode().getWorldLightList())
            {
                if (l instanceof PointLight || l instanceof SpotLight)
                {
                    lightNames.add(l.getName());
                    try
                    {
                        lights.add((PointLight) l);
                    } catch (ClassCastException cce)
                    {
                        lights.add((SpotLight) l);
                    }
                }
            }
            Collections.sort(lightNames, new Comparator<String>()
            {
                @Override
                public int compare(String o1, String o2)
                {
                    return o1.compareTo(o2);
                }
            });
            Collections.sort(lights, new Comparator<Light>()
            {
                @Override
                public int compare(Light o1, Light o2)
                {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            lightChoiceComboBox = new JComboBox(lightNames);
            for (int i = 0; i < lights.size(); i++)
            {
                if (lights.get(i) == lControl.getLight())
                {
                    lightChoiceComboBox.setSelectedIndex(i);
                    break;
                }
            }
            enabledChecker = new Checker();
            enabledChecker.setChecked(lControl.isEnabled());
            removeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    spatial.removeControl(lControl);
                    arrangeDataPanel();
                }
            });
            enabledChecker.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    lControl.setEnabled(enabledChecker.isChecked());
                }
            });
            setLayout(new RiverLayout(10, 0));
            lightChoiceComboBox.setPreferredSize(new Dimension(110, 30));
            add("left", new JLabel("Light Control of"));
            add("tab", lightChoiceComboBox);
            add("tab", new JLabel("Enabled:"));
            add(enabledChecker);
            add("tab", removeButton);
        }
    }

    class UserDataPanel extends JPanel
    {

        private BButton removeButton = new BButton("-");

        public UserDataPanel(final String key)
        {
            setPreferredSize(new Dimension(440, 40));
            setBorder(new BevelBorder(BevelBorder.LOWERED));
            setLayout(new RiverLayout());
            add("left", new JLabel("Data:"));
            JLabel nameLabel = new JLabel(key);
            nameLabel.setPreferredSize(new Dimension(120, 30));
            add("tab", nameLabel);
            add(new UserDataTextField(key, spatial.getUserData(key).toString()));
            removeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    spatial.setUserData(key, null);
                    arrangeDataPanel();
                }
            });
            add(removeButton);
        }

        class UserDataTextField extends JTextField
        {

            public UserDataTextField(final String key, String value)
            {
                setText(value);
                setPreferredSize(new Dimension(200, 30));
                getDocument().addDocumentListener(new DocumentListener()
                {
                    @Override
                    public void insertUpdate(DocumentEvent e)
                    {
                        spatial.setUserData(key, getText());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e)
                    {
                        spatial.setUserData(key, getText());
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e)
                    {
                        spatial.setUserData(key, getText());
                    }
                });
            }
        }
    }
}
