package gui.editPanes.spatials;

import gui.editPanes.EditTaskPane;
import general.CurrentData;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.ConeCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import components.BTextField;
import components.Checker;
import components.Float3Panel;
import components.RoundBorder;
import general.UAManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class PhysicsTaskPane extends EditTaskPane
{

    private Checker enablePhysicsChecker = new Checker();
    private BTextField massField = new BTextField("Float");
    private BTextField restitutionField = new BTextField("Float");
    private BTextField motionThresholdField = new BTextField("Float");
    private BTextField frictionField = new BTextField("Float");
    private Checker kinematicChecker = new Checker();
    private JComboBox collisionShapeComboBox = new JComboBox(new String[]
    {
        "Box", "Capsule", "Cone", "Cylinder", "Dynamic Mesh", "Static Mesh (unstable)"
    });
    private Spatial spatial;
    private JPanel currentCShapePanel = new JPanel();
    private CShape_BoxPanel cShape_BoxPanel;
    private CShape_CapsulePanel cShape_CapsulePanel;
    private CShape_ConePanel cShape_ConePanel;
    private CShape_CylinderPanel cShape_CylinderPanel;
    private Float3Panel positionPanel = new Float3Panel(Vector3f.ZERO, Wizard.getCamera());

    public PhysicsTaskPane()
    {
        kinematicChecker.setChecked(false);
        spatial = (Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
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
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            spatial.getControl(RigidBodyControl.class).setPhysicsLocation(positionPanel.getVector());
                            return null;
                        }
                    });
                }
            }
        });
        positionPanel.addFieldFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        UAManager.add(spatial, "Change Physics Position of " + spatial.getName());
                        return null;
                    }
                });
            }
        });
        enablePhysicsChecker.setChecked(spatial.getControl(RigidBodyControl.class) != null);
        collisionShapeComboBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                switch (collisionShapeComboBox.getSelectedIndex())
                {
                    case 0:
                        massField.setEnabled(true);
                        spatial.getControl(RigidBodyControl.class).setCollisionShape(new BoxCollisionShape(new Vector3f(1, 1, 1)));
                        break;
                    case 1:
                        massField.setEnabled(true);
                        spatial.getControl(RigidBodyControl.class).setCollisionShape(new CapsuleCollisionShape(1, 1.5f));
                        break;
                    case 2:
                        massField.setEnabled(true);
                        spatial.getControl(RigidBodyControl.class).setCollisionShape(new ConeCollisionShape(1, 1.5f));
                        break;
                    case 3:
                        massField.setEnabled(true);
                        spatial.getControl(RigidBodyControl.class).setCollisionShape(new CylinderCollisionShape(new Vector3f(1, 1, 1)));
                        break;
                    case 4:
                        massField.setEnabled(true);
                        spatial.getControl(RigidBodyControl.class).setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(spatial));
                        spatial.setUserData("cShape", "dynamic");
                        break;
                    case 5:
                        massField.setText("0");
                        massField.setEnabled(false);
                        spatial.getControl(RigidBodyControl.class).setMass(0);
                        spatial.getControl(RigidBodyControl.class).setCollisionShape(CollisionShapeFactory.createMeshShape(spatial));
                        spatial.setUserData("cShape", "static");
                        break;
                }
                arrangeCShapePanel();
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        UAManager.add(spatial, "Change Collision Shape of " + spatial.getName());
                        return null;
                    }
                });
            }
        });
        enablePhysicsChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        if (enablePhysicsChecker.isChecked())
                        {
                            spatial.addControl(new RigidBodyControl(new BoxCollisionShape(new Vector3f(1, 1, 1)), 3));
                            spatial.getControl(RigidBodyControl.class).setRestitution(1);
                            spatial.setUserData("cShape", "");
                            CurrentData.getEditorWindow().getB3DApp().getBulletAppState().getPhysicsSpace().add(spatial.getControl(RigidBodyControl.class));
                        } else
                        {
                            CurrentData.getEditorWindow().getB3DApp().getBulletAppState().getPhysicsSpace().remove(spatial.getControl(RigidBodyControl.class));
                            spatial.removeControl(RigidBodyControl.class);
                        }
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                        {
                            @Override
                            public Void call() throws Exception
                            {
                                UAManager.add(spatial, (enablePhysicsChecker.isChecked() ? "Enable" : "Disable") + " Physics of " + spatial.getName());
                                return null;
                            }
                        });
                        arrange();
                        return null;
                    }
                });
            }
        });
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateRigidBodyControl();
            }
        });
        taskPane.setTitle("Physics Properties");
        taskPane.setLayout(new RiverLayout(10, 5));
        add(taskPane, BorderLayout.CENTER);
        taskPane.add("left", new JLabel("Enable"));
        taskPane.add("tab hfill", enablePhysicsChecker);
        arrange();
    }

    private void arrange()
    {
        taskPane.removeAll();
        taskPane.add("left", new JLabel("Enable"));
        taskPane.add("tab", enablePhysicsChecker);
        if (spatial.getControl(RigidBodyControl.class) != null)
        {
            if (spatial.getControl(RigidBodyControl.class).getCollisionShape() instanceof CapsuleCollisionShape)
            {
                collisionShapeComboBox.setSelectedIndex(1);
            } else if (spatial.getControl(RigidBodyControl.class).getCollisionShape() instanceof ConeCollisionShape)
            {
                collisionShapeComboBox.setSelectedIndex(2);
            } else if (spatial.getControl(RigidBodyControl.class).getCollisionShape() instanceof CylinderCollisionShape)
            {
                collisionShapeComboBox.setSelectedIndex(3);
            } else if (spatial.getControl(RigidBodyControl.class).getCollisionShape() instanceof BoxCollisionShape)
            {
                collisionShapeComboBox.setSelectedIndex(0);
            } else if (spatial.getUserData("cShape").equals("dynamic"))
            {
                collisionShapeComboBox.setSelectedIndex(4);
            } else if (spatial.getUserData("cShape").equals("static"))
            {
                collisionShapeComboBox.setSelectedIndex(5);
            }
            kinematicChecker.setChecked(spatial.getControl(RigidBodyControl.class).isKinematic());
            taskPane.add("br left", new JLabel("Kincematic:"));
            taskPane.add("tab", kinematicChecker);
            taskPane.add("br left", new JLabel("Mass:"));
            taskPane.add("tab hfill", massField);
            taskPane.add("br left", new JLabel("Restitution:"));
            taskPane.add("tab hfill", restitutionField);
            taskPane.add("br left", new JLabel("Friction:"));
            taskPane.add("tab hfill", frictionField);
            taskPane.add("br left", new JLabel("Motion Treshold:"));
            taskPane.add("tab hfill", motionThresholdField);
            taskPane.add("br left", new JLabel("Collision Shape:"));
            taskPane.add("tab hfill", collisionShapeComboBox);
            massField.setText("" + spatial.getControl(RigidBodyControl.class).getMass());
            motionThresholdField.setText("" + spatial.getControl(RigidBodyControl.class).getCcdMotionThreshold());
            frictionField.setText("" + spatial.getControl(RigidBodyControl.class).getFriction());
            restitutionField.setText("" + spatial.getControl(RigidBodyControl.class).getRestitution());
            arrangeCShapePanel();
            taskPane.add("br", new JLabel("Physics Location:"));
            taskPane.add("tab hfill", positionPanel);
            taskPane.add("br right", applyButton);
        }
    }

    private void updateRigidBodyControl()
    {
        CurrentData.getEditorWindow().getB3DApp().getBulletAppState().getPhysicsSpace().remove(spatial);
        CollisionShape collisionShape = null;
        if (collisionShapeComboBox.getSelectedIndex() == 4)
        {
            collisionShape = CollisionShapeFactory.createDynamicMeshShape(spatial);
        } else if (collisionShapeComboBox.getSelectedIndex() == 5)
        {
            collisionShape = CollisionShapeFactory.createMeshShape(spatial);
        } else if (currentCShapePanel instanceof CShape_BoxPanel)
        {
            CShape_BoxPanel cShape_BoxPanel = (CShape_BoxPanel) currentCShapePanel;
            collisionShape = new BoxCollisionShape(cShape_BoxPanel.getHalfExtents());
        } else if (currentCShapePanel instanceof CShape_CapsulePanel)
        {
            CShape_CapsulePanel cShape_CapsulePanel = (CShape_CapsulePanel) currentCShapePanel;
            collisionShape = new CapsuleCollisionShape(cShape_CapsulePanel.getRadius(), cShape_CapsulePanel.getShapeHeight());
        } else if (currentCShapePanel instanceof CShape_ConePanel)
        {
            CShape_ConePanel cShape_ConePanel = (CShape_ConePanel) currentCShapePanel;
            collisionShape = new ConeCollisionShape(cShape_ConePanel.getRadius(), cShape_ConePanel.getShapeHeight());
        } else if (currentCShapePanel instanceof CShape_CylinderPanel)
        {
            CShape_CylinderPanel cShape_CylinderPanel = (CShape_CylinderPanel) currentCShapePanel;
            collisionShape = new CylinderCollisionShape(cShape_CylinderPanel.getHalfExtents());
        }
        spatial.getControl(RigidBodyControl.class).setCcdMotionThreshold(Float.parseFloat(motionThresholdField.getText()));
        spatial.getControl(RigidBodyControl.class).setFriction(Float.parseFloat(frictionField.getText()));
        spatial.getControl(RigidBodyControl.class).setKinematic(kinematicChecker.isChecked());
        spatial.getControl(RigidBodyControl.class).setMass(Float.parseFloat(massField.getText()));
        spatial.getControl(RigidBodyControl.class).setRestitution(Float.parseFloat(restitutionField.getText()));
        spatial.getControl(RigidBodyControl.class).setCollisionShape(collisionShape);
        CurrentData.getEditorWindow().getB3DApp().getBulletAppState().getPhysicsSpace().add(spatial);
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                UAManager.add(spatial, "Edit Physics of " + spatial.getName());
                return null;
            }
        });
    }

    private void arrangeCShapePanel()
    {
        if (currentCShapePanel != null)
        {
            taskPane.remove(currentCShapePanel);
        }
        taskPane.remove(applyButton);
        if (spatial.getControl(RigidBodyControl.class).getCollisionShape() instanceof CapsuleCollisionShape)
        {
            cShape_CapsulePanel = new CShape_CapsulePanel((CapsuleCollisionShape) spatial.getControl(RigidBodyControl.class).getCollisionShape());
            currentCShapePanel = cShape_CapsulePanel;
            taskPane.add("br hfill", currentCShapePanel);
        } else if (spatial.getControl(RigidBodyControl.class).getCollisionShape() instanceof ConeCollisionShape)
        {
            cShape_ConePanel = new CShape_ConePanel((ConeCollisionShape) spatial.getControl(RigidBodyControl.class).getCollisionShape());
            currentCShapePanel = cShape_ConePanel;
            taskPane.add("br hfill", currentCShapePanel);
        } else if (spatial.getControl(RigidBodyControl.class).getCollisionShape() instanceof CylinderCollisionShape)
        {
            cShape_CylinderPanel = new CShape_CylinderPanel((CylinderCollisionShape) spatial.getControl(RigidBodyControl.class).getCollisionShape());
            currentCShapePanel = cShape_CylinderPanel;
            taskPane.add("br hfill", currentCShapePanel);
        } else if (spatial.getControl(RigidBodyControl.class).getCollisionShape() instanceof BoxCollisionShape)
        {
            cShape_BoxPanel = new CShape_BoxPanel((BoxCollisionShape) spatial.getControl(RigidBodyControl.class).getCollisionShape());
            currentCShapePanel = cShape_BoxPanel;
            taskPane.add("br hfill", currentCShapePanel);
        }
        taskPane.add("br right", applyButton);
    }

    class CShape_BoxPanel extends JPanel
    {

        private Float3Panel halfExtendsPanel;

        public CShape_BoxPanel(BoxCollisionShape bcs)
        {
            setBorder(new RoundBorder());
            halfExtendsPanel = new Float3Panel(bcs.getHalfExtents(), Wizard.getCamera());
            setLayout(new RiverLayout(10, 5));
            add("left", new JLabel("Half Extends:"));
            add("tab hfill", halfExtendsPanel);
        }

        Vector3f getHalfExtents()
        {
            return halfExtendsPanel.getVector();
        }
    }

    class CShape_CapsulePanel extends JPanel
    {

        private BTextField radiusField = new BTextField("float");
        private BTextField heightTextField = new BTextField("float");

        public CShape_CapsulePanel(CapsuleCollisionShape ccs)
        {
            setBorder(new RoundBorder());
            taskPane.remove(currentCShapePanel);
            radiusField.setText("" + ccs.getRadius());
            heightTextField.setText("" + ccs.getHeight());
            setLayout(new RiverLayout(10, 5));
            add("left", new JLabel("Height:"));
            add("tab hfill", heightTextField);
            add("br left", new JLabel("Radius:"));
            add("tab hfill", radiusField);
        }

        float getRadius()
        {
            return Float.parseFloat(radiusField.getText());
        }

        float getShapeHeight()
        {
            return Float.parseFloat(heightTextField.getText());
        }
    }

    class CShape_ConePanel extends JPanel
    {

        private BTextField radiusField = new BTextField("float");
        private BTextField heightTextField = new BTextField("float");

        public CShape_ConePanel(ConeCollisionShape ccs)
        {
            setBorder(new RoundBorder());
            radiusField.setText("" + ccs.getRadius());
            heightTextField.setText("" + ccs.getHeight());
            setLayout(new RiverLayout(10, 5));
            add("left", new JLabel("Height:"));
            add("tab hfill", heightTextField);
            add("br left", new JLabel("Radius:"));
            add("tab hfill", radiusField);
        }

        float getRadius()
        {
            return Float.parseFloat(radiusField.getText());
        }

        float getShapeHeight()
        {
            return Float.parseFloat(heightTextField.getText());
        }
    }

    class CShape_CylinderPanel extends JPanel
    {

        private Float3Panel halfExtendsPanel;

        public CShape_CylinderPanel(CylinderCollisionShape ccs)
        {
            setBorder(new RoundBorder());
            halfExtendsPanel = new Float3Panel(ccs.getHalfExtents(), Wizard.getCamera());
            halfExtendsPanel.getyField().setEnabled(false);
            setLayout(new RiverLayout(10, 5));
            add("left", new JLabel("Half Extends:"));
            add("tab hfill", halfExtendsPanel);
        }

        Vector3f getHalfExtents()
        {
            return halfExtendsPanel.getVector();
        }
    }

    @Override
    public void updateData(boolean urgent)
    {
        //Sync own physics
        if (spatial.getControl(RigidBodyControl.class) != null)
        {
            positionPanel.setVector(spatial.getControl(RigidBodyControl.class).getPhysicsLocation());
            if (!CurrentData.getEditorWindow().getB3DApp().isPhysicsPlaying())
            {
                spatial.getControl(RigidBodyControl.class).setPhysicsLocation(spatial.getWorldTranslation());
                spatial.getControl(RigidBodyControl.class).setPhysicsRotation(spatial.getWorldRotation());
            }
        }
    }
}