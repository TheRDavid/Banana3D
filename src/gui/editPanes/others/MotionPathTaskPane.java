package gui.editPanes.others;

import b3dElements.other.B3D_MotionEvent;
import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import monkeyStuff.MotionPathModel;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.PlayState;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import components.BButton;
import components.BTextField;
import components.Checker;
import components.Float3Panel;
import components.Float4Panel;
import dialogs.ObserverDialog;
import dialogs.SelectDialog;
import java.awt.BasicStroke;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.VerticalLayout;
import other.ElementToObjectConverter;
import other.ObjectToElementConverter;
import se.datadosen.component.RiverLayout;

public class MotionPathTaskPane extends EditTaskPane
{

    private B3D_MotionEvent b3D_MotionEvent;
    private WayPointTaskPane wayPointTaskPane;
    private PlayTaskPane playTaskPane;
    private JComboBox objectComboBox = new JComboBox();
    private BTextField speedField = new BTextField("Float");
    private BTextField curveTensionField = new BTextField("Float");
    private Checker cycleChecker = new Checker();
    private JComboBox directionTypeComboBox = new JComboBox(new String[]
    {
        "Path", "Rotation", "Path And Rotation", "Look At", "None"
    });
    private JComboBox loopModeComboBox = new JComboBox(new String[]
    {
        "Don't Loop", "Cycle", "Lopp"
    });
    private BColorButton colorButton;
    private MotionEvent motionEvent;
    private Float4Panel rotationPanel = new Float4Panel(Vector4f.ZERO);
    private JPanel setRotationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private Vector<Spatial> allSpatials = new Vector<Spatial>();
    private MotionPathModel motionPathModel;
    private Float3Panel positionPanel;

    /**
     *
     * @param mEvent
     * @param b3d_MotionEvent
     */
    public MotionPathTaskPane(MotionPathModel mpm, B3D_MotionEvent b3d_MotionEvent)
    {
        motionPathModel = mpm;
        motionEvent = mpm.getMotionEvent();
        this.b3D_MotionEvent = b3d_MotionEvent;
        if (motionEvent.getRotation() != null)
        {
            rotationPanel.setFloats(motionEvent.getRotation());
        }
        wayPointTaskPane = new WayPointTaskPane(motionEvent.getPath());
        setRotationPanel.add(new JLabel("Rotation:"));
        setRotationPanel.add(rotationPanel);
        playTaskPane = new PlayTaskPane();
        colorButton = new BColorButton(Wizard.makeColor(b3D_MotionEvent.getMotionPath().getColor()),
                b3D_MotionEvent);
        updateObjectComboBox();
        switch (motionEvent.getLoopMode())
        {
            case Cycle:
                loopModeComboBox.setSelectedIndex(1);
                break;
            case Loop:
                loopModeComboBox.setSelectedIndex(2);
        }
        positionPanel = new Float3Panel(motionPathModel.getSymbol().getLocalTranslation(), Wizard.getCamera())
        {
            @Override
            public void setVector(final Vector3f vec)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        float xDiff = vec.x - motionPathModel.getSymbol().getLocalTranslation().x;
                        float yDiff = vec.y - motionPathModel.getSymbol().getLocalTranslation().y;
                        float zDiff = vec.z - motionPathModel.getSymbol().getLocalTranslation().z;
                        motionPathModel.getSymbol().setLocalTranslation(vec);
                        updateLocations();
                        for (int i = 0; i < motionPathModel.getMotionEvent().getPath().getNbWayPoints(); i++)
                        {
                            if (!motionPathModel.getMotionEvent().getPath().isCycle() || i != 0)
                            {
                                motionPathModel.getMotionEvent().getPath().getWayPoint(i).set(
                                        motionPathModel.getMotionEvent().getPath().getWayPoint(i).addLocal(xDiff, yDiff, zDiff));
                            }
                        }
                        return null;
                    }
                });
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
                if (e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    positionPanel.setVector(new Vector3f(
                            Float.parseFloat(positionPanel.getxField().getText()),
                            Float.parseFloat(positionPanel.getyField().getText()),
                            Float.parseFloat(positionPanel.getzField().getText())));
                }
            }
        });
        positionPanel.addFieldFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                positionPanel.setVector(new Vector3f(
                        Float.parseFloat(positionPanel.getxField().getText()),
                        Float.parseFloat(positionPanel.getyField().getText()),
                        Float.parseFloat(positionPanel.getzField().getText())));
            }
        });
        speedField.addKeyListener(new KeyListener()
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
                try
                {
                    motionEvent.setSpeed(Float.parseFloat(speedField.getText()));
                    CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
                } catch (NumberFormatException nfe)
                {
                    ObserverDialog.getObserverDialog().printMessage("Fail: NumberFormatException -> Speed value in MotionTaskPane invalid");
                }
            }
        });
        curveTensionField.addKeyListener(new KeyListener()
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
                try
                {
                    motionEvent.getPath().setCurveTension(Float.parseFloat(curveTensionField.getText()));
                    b3D_MotionEvent.getMotionPath().setCurveTension(Float.parseFloat(curveTensionField.getText()));
                    CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
                } catch (NumberFormatException nfe)
                {
                    ObserverDialog.getObserverDialog().printMessage("Fail: NumberFormatException -> CurveTension value in MotionTaskPane invalid");
                }
            }
        });
        cycleChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                motionEvent.getPath().setCycle(cycleChecker.isChecked());
                b3D_MotionEvent.getMotionPath().setCycle(cycleChecker.isChecked());
                wayPointTaskPane.updatePanels(!cycleChecker.isChecked());
                CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
            }
        });
        rotationPanel.addFieldKeyListener(new KeyListener()
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
                motionEvent.setRotation(new Quaternion(
                        rotationPanel.getVector().getX(),
                        rotationPanel.getVector().getY(),
                        rotationPanel.getVector().getZ(),
                        rotationPanel.getVector().getW()));
                b3D_MotionEvent.getMotionPath().setRotation(new Quaternion(
                        rotationPanel.getVector().getX(),
                        rotationPanel.getVector().getY(),
                        rotationPanel.getVector().getZ(),
                        rotationPanel.getVector().getW()));
                CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
            }
        });
        switch (motionEvent.getDirectionType())
        {
            case Rotation:
                directionTypeComboBox.setSelectedIndex(1);
                break;
            case PathAndRotation:
                directionTypeComboBox.setSelectedIndex(2);
                break;
            case LookAt:
                directionTypeComboBox.setSelectedIndex(3);
                break;
            case None:
                directionTypeComboBox.setSelectedIndex(4);
        }
        directionTypeComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (directionTypeComboBox.getSelectedIndex() == 3)
                {
                    Vector<String> objects = new Vector<String>();
                    objects.add("Camera");
                    updateSpatialList();
                    for (Spatial s : allSpatials)
                    {
                        objects.add(s.getName());
                    }
                    SelectDialog selectDialog = new SelectDialog("Look at what?", objects.toArray());
                    if (selectDialog.isOk())
                    {
                        UUID elementUUID = Wizard.getObjectReferences().getUUID(motionEvent.hashCode());
                        B3D_MotionEvent b3D_MotionEvent = (B3D_MotionEvent) Wizard.getObjects().getB3D_Element(elementUUID);
                        if (selectDialog.getSelectedValue().equals("Camera"))
                        {
                            b3D_MotionEvent.getMotionPath().setLookAtObject("Camera");
                            motionEvent.setLookAt(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation(), Vector3f.UNIT_Y);
                            directionTypeComboBox.removeAllItems();
                            directionTypeComboBox.addItem("Path");
                            directionTypeComboBox.addItem("Rotation");
                            directionTypeComboBox.addItem("Path And Rotation");
                            directionTypeComboBox.addItem("Look At (Camera)");
                            directionTypeComboBox.addItem("None");
                            directionTypeComboBox.setSelectedIndex(3);
                        } else
                        {
                            //Just use hashcode b3D_MotionEvent.getMotionPath().
                            //        setLookAtObject((String) allGeometrys.get(selectDialog.getSelectedIndex() - 1).getUserData("ID"));
                            b3D_MotionEvent.getMotionPath().
                                    setLookAtObject(Wizard.getObjectReferences().getUUID(allSpatials.get(selectDialog.getSelectedIndex() - 1).hashCode()));
                            motionEvent.setLookAt(allSpatials.get(selectDialog.getSelectedIndex() - 1).getLocalTranslation(), Vector3f.UNIT_Y);
                            directionTypeComboBox.removeAllItems();
                            directionTypeComboBox.addItem("Path");
                            directionTypeComboBox.addItem("Rotation");
                            directionTypeComboBox.addItem("Path And Rotation");
                            directionTypeComboBox.addItem("Look At (" + allSpatials.get(selectDialog.getSelectedIndex() - 1).getName() + ")");
                            directionTypeComboBox.addItem("None");
                            directionTypeComboBox.setSelectedIndex(3);
                        }
                    }
                }
                arrangeGUI();
                switch (directionTypeComboBox.getSelectedIndex())
                {
                    case 0:
                        motionEvent.setDirectionType(MotionEvent.Direction.Path);
                        motionEvent.setRotation(null);
                        break;
                    case 1:
                        motionEvent.setDirectionType(MotionEvent.Direction.Rotation);
                        motionEvent.setRotation(new Quaternion(
                                rotationPanel.getVector().getX(),
                                rotationPanel.getVector().getY(),
                                rotationPanel.getVector().getZ(),
                                rotationPanel.getVector().getW()));
                        break;
                    case 2:
                        motionEvent.setDirectionType(MotionEvent.Direction.PathAndRotation);
                        motionEvent.setRotation(new Quaternion(
                                rotationPanel.getVector().getX(),
                                rotationPanel.getVector().getY(),
                                rotationPanel.getVector().getZ(),
                                rotationPanel.getVector().getW()));
                        break;
                    case 3:
                        motionEvent.setDirectionType(MotionEvent.Direction.LookAt);
                        motionEvent.setRotation(null);
                        break;
                    case 4:
                        motionEvent.setDirectionType(MotionEvent.Direction.None);
                        motionEvent.setRotation(null);
                }
            }
        });
        cycleChecker.setChecked(motionEvent.getPath().isCycle());
        curveTensionField.setText("" + motionEvent.getPath().getCurveTension());
        speedField.setText("" + motionEvent.getSpeed());
        loopModeComboBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (loopModeComboBox.getSelectedIndex() == 0)
                {
                    motionEvent.setLoopMode(LoopMode.DontLoop);
                } else if (loopModeComboBox.getSelectedIndex() == 1)
                {
                    motionEvent.setLoopMode(LoopMode.Cycle);
                } else
                {
                    motionEvent.setLoopMode(LoopMode.Loop);
                }
            }
        });
        wayPointTaskPane.updatePanels(!cycleChecker.isChecked());
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Motion Path Attributes");
        add(taskPane, BorderLayout.NORTH);
        add(wayPointTaskPane, BorderLayout.CENTER);
        add(playTaskPane, BorderLayout.SOUTH);
        arrangeGUI();
    }

    private void updateSpatialList()
    {
        allSpatials.clear();
        Wizard.insertAllSpatials(CurrentData.getEditorWindow().getB3DApp().getSceneNode(), allSpatials);
        Collections.sort(allSpatials, new Comparator<Spatial>()
        {
            @Override
            public int compare(Spatial o1, Spatial o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private void updateObjectComboBox()
    {
        if (objectComboBox.getActionListeners().length > 0)
        {
            objectComboBox.removeActionListener(objectComboBox.getActionListeners()[0]);
        }
        objectComboBox.removeAllItems();
        updateSpatialList();
        objectComboBox.addItem("Camera");
        for (Spatial s : allSpatials)
        {
            objectComboBox.addItem(s.getName());
        }
        for (int i = 0; i < allSpatials.size(); i++)
        {
            if (allSpatials.get(i).getName().equals(motionEvent.getSpatial().getName()))
            {
                objectComboBox.setSelectedIndex(i + 1);
            }
        }
        objectComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                motionEvent.getSpatial().setUserData("motionEventName", null);
                if (objectComboBox.getSelectedIndex() == 0)
                {
                    motionEvent.setSpatial(CurrentData.getEditorWindow().getB3DApp().getCamNode());
                    b3D_MotionEvent.setObjectProbablyUUID(B3D_MotionEvent.Cam.CAM_ID);
                } else
                {
                    UUID oldUUID = b3D_MotionEvent.getUUID();
                    b3D_MotionEvent = (B3D_MotionEvent) ObjectToElementConverter.convertMotionEvent(motionEvent);
                    b3D_MotionEvent.setUuid(oldUUID);
                    int spatialID = allSpatials.elementAt(objectComboBox.getSelectedIndex() - 1).hashCode();
                    b3D_MotionEvent.setObjectProbablyUUID(Wizard.getObjectReferences().getUUID(spatialID));
                    Wizard.getObjects().remove(motionEvent.hashCode(), b3D_MotionEvent.getUUID());
                    motionEvent = ElementToObjectConverter.convertMotionEvent(b3D_MotionEvent);
                    Wizard.getObjects().add(motionEvent, b3D_MotionEvent);
                }
            }
        });
    }

    private void arrangeGUI()
    {
        taskPane.removeAll();
        taskPane.add("br left", new JLabel("Object:"));
        taskPane.add("tab hfill", objectComboBox);
        taskPane.add("br left", new JLabel("Position:"));
        taskPane.add("tab hfill", positionPanel);
        taskPane.add("br left", new JLabel("Speed:"));
        taskPane.add("tab hfill", speedField);
        taskPane.add("br left", new JLabel("Curve Tension:"));
        taskPane.add("tab hfill", curveTensionField);
        taskPane.add("br left", new JLabel("Cycle:"));
        taskPane.add("tab", cycleChecker);
        taskPane.add("br left", new JLabel("Loop mode:"));
        taskPane.add("tab hfill", loopModeComboBox);
        taskPane.add("br left", new JLabel("Direction Type:"));
        taskPane.add("tab hfill", directionTypeComboBox);
        if (directionTypeComboBox.getSelectedIndex() == 1 || directionTypeComboBox.getSelectedIndex() == 2)
        {
            taskPane.add("br hfill", setRotationPanel);
        }
        taskPane.add("br left", new JLabel("Symbol Color:"));
        taskPane.add("tab hfill", colorButton);
    }

    @Override
    public void updateData(boolean urgent)
    {
        if (!positionPanel.hasFocus())
            positionPanel.setVector(motionPathModel.getSymbol().getLocalTranslation());
        if (!motionEvent.getPlayState().equals(PlayState.Playing))
        {
            stop();
            cycleChecker.setEnabled(true);
        } else
        {
            wayPointTaskPane.repaint();
            wayPointTaskPane.updateRunningMark();
        }
        if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Spatial)
        {
            wayPointTaskPane.updateSelectionMark();
        }
        playTaskPane.getPlayButton().setEnabled(!motionEvent.getPlayState().equals(PlayState.Playing));
        playTaskPane.getPauseButton().setEnabled(!motionEvent.getPlayState().equals(PlayState.Paused)
                && !motionEvent.getPlayState().equals(PlayState.Stopped));
        playTaskPane.getStopButton().setEnabled(!motionEvent.getPlayState().equals(PlayState.Stopped));
        CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
    }

    private class PlayTaskPane extends JXTaskPane
    {

        private BButton playButton = new BButton(new ImageIcon("dat//img//menu//c_play.png"));
        private BButton pauseButton = new BButton(new ImageIcon("dat//img//menu//c_pause.png"));
        private BButton stopButton = new BButton(new ImageIcon("dat//img//menu//c_stop.png"));
        private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        public PlayTaskPane()
        {
            playButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    play();
                }
            });
            pauseButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    pause();
                }
            });
            stopButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    stop();
                }
            });
            buttonPanel.add(playButton);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            buttonPanel.add(pauseButton);
            buttonPanel.add(stopButton);
            setLayout(new VerticalLayout(10));
            setTitle("Play Event");
            add(buttonPanel);
        }

        public BButton getPlayButton()
        {
            return playButton;
        }

        public BButton getPauseButton()
        {
            return pauseButton;
        }

        public BButton getStopButton()
        {
            return stopButton;
        }
    }

    private class WayPointTaskPane extends JXTaskPane
    {

        private MotionPath motionPath;
        Vector<WayPointPanel> wayPointPanels = new Vector<WayPointPanel>();

        public WayPointTaskPane(MotionPath mp)
        {
            motionPath = mp;
            setLayout(new VerticalLayout(0));
            setTitle("Waypoints");
            updatePanels(true);
        }

        private void updatePanels(boolean controlsEnabled)
        {
            removeAll();
            wayPointPanels.clear();
            for (int i = 0; i < motionPath.getNbWayPoints(); i++)
            {
                wayPointPanels.add(new WayPointPanel(i, motionPath.getWayPoint(i), controlsEnabled));
                add(wayPointPanels.get(wayPointPanels.size() - 1));
            }
        }

        private void updateRunningMark()
        {
            for (WayPointPanel wpp : wayPointPanels)
            {
                wpp.setRunningMark(false);
            }
            wayPointPanels.get(motionEvent.getCurrentWayPoint()).setRunningMark(true);
        }

        private void updateSelectionMark()
        {
            for (WayPointPanel wpp : wayPointPanels)
            {
                wpp.setSelectionMark(false);
            }
            try
            {
                wayPointPanels.get((Integer) ((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getUserData("waypointNumber")).setSelectionMark(true);
            } catch (ArrayIndexOutOfBoundsException aioobe)
            {
                ObserverDialog.getObserverDialog().printMessage("Error thrown when updating SelectionMark of MotionTaskPane (IndexOutOfBounds)");
            }
        }

        private class WayPointPanel extends JPanel
        {

            private Float3Panel locationPanel;
            private BButton plusButton = new BButton("+"), deleteButton = new BButton("-"), selectButton = new BButton("Select");
            private int index;
            private boolean runningMark = false, selectionMark = false;
            private JLabel numberLabel = new JLabel();

            public WayPointPanel(int indx, Vector3f vec3f, boolean controlsEnabled)
            {
                numberLabel.setPreferredSize(new Dimension(45, 10));
                setLayout(new FlowLayout(FlowLayout.CENTER));
                index = indx;
                locationPanel = new Float3Panel(vec3f, Wizard.getCamera());
                locationPanel.addFieldKeyListener(new KeyListener()
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
                        motionPath.getWayPoint(index).set(locationPanel.getVector());
                        CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
                    }
                });
                plusButton.setEnabled(controlsEnabled);
                deleteButton.setEnabled(controlsEnabled);
                plusButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        motionPath.addWayPoint(locationPanel.getVector());
                        updatePanels(true);
                    }
                });
                deleteButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        if (motionPath.getNbWayPoints() > 2)
                        {
                            motionPath.removeWayPoint(index);
                            CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
                            updatePanels(true);
                        } else
                        {
                            JOptionPane.showMessageDialog(MotionPathTaskPane.this, "Can't have less than 2 Waypoints!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                selectButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        Vector<Geometry> allEditNodeGeometrys = new Vector<Geometry>();
                        Wizard.insertAllGeometrys(CurrentData.getEditorWindow().getB3DApp().getEditorNode(), allEditNodeGeometrys);
                        for (Geometry geometry : allEditNodeGeometrys)
                        {
                            if (geometry.getName().equals("Waypoint"))
                            {
                                if (((Integer) geometry.getUserData("B3D_ID")).equals(Wizard.getObjectReferences().getID(b3D_MotionEvent.getUUID())))
                                {
                                    if (geometry.getUserData("waypointNumber").equals(new Integer(index)))
                                    {
                                        CurrentData.getEditorWindow().getB3DApp().setSelectedObject(motionEvent, geometry);
                                        CurrentData.getEditorWindow().getB3DApp().getCamera().lookAt(geometry.getLocalTranslation(), Vector3f.UNIT_Y);
                                    }
                                }
                            }
                        }
                    }
                });
                numberLabel.setText(index + ":");
                add(numberLabel);
                add(locationPanel);
                add(selectButton);
                add(plusButton);
                add(deleteButton);
            }

            private void updateLocation()
            {
                try
                {
                    if (!locationPanel.getVector().equals(motionPath.getWayPoint(index)) && !locationPanel.hasFocus())
                    {
                        locationPanel.setVector(motionPath.getWayPoint(index));
                    }
                } catch (java.lang.IndexOutOfBoundsException ioobe)
                {
                    ObserverDialog.getObserverDialog().printMessage("Error thrown when updating Location in MotionPathTaskPane -> IndexOutOfBounds");
                }
            }

            private void setSelectionMark(boolean b)
            {
                selectionMark = b;
                repaint();
            }

            private void setRunningMark(boolean b)
            {
                runningMark = b;
                repaint();
            }

            @Override
            public void paint(Graphics g)
            {
                super.paint(g);
                if (runningMark || selectionMark)
                {
                    Graphics2D g2d = (Graphics2D) g;
                    if (runningMark)
                    {
                        g2d.setColor(Color.cyan);
                        g2d.setStroke(new BasicStroke(1.0f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND,
                                6, new float[]
                        {
                            20
                        }, 0.0f));
                        g2d.draw(new RoundRectangle2D.Double(30, 10,
                                getWidth() - 60,
                                getHeight() - 20,
                                10, 10));
                    }
                    if (selectionMark)
                    {
                        g2d.setColor(Color.green);
                        g2d.draw(new RoundRectangle2D.Double(20, 5,
                                getWidth() - 40,
                                getHeight() - 10,
                                10, 10));
                    }
                }
            }
        }

        public void updateLocations()
        {
            synchronized (wayPointPanels)
            {
                for (WayPointPanel wpp : wayPointPanels)
                {
                    wpp.updateLocation();
                }
            }
        }
    }

    public void updateLocations()
    {
        wayPointTaskPane.updateLocations();
    }

    public void play()
    {
        camNodeDetached = false;
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                if (motionEvent.getSpatial() instanceof CameraNode)
                {
                    CurrentData.getEditorWindow().getB3DApp().attachCam();
                }
                return null;
            }
        });
        cycleChecker.setEnabled(false);
        motionEvent.play();
        playTaskPane.getPlayButton().setEnabled(false);
        playTaskPane.getPauseButton().setEnabled(true);
        playTaskPane.getStopButton().setEnabled(true);
    }

    public void pause()
    {
        if (motionEvent.getSpatial() instanceof CameraNode)
        {
            CurrentData.getEditorWindow().getB3DApp().detachCam();
        }
        motionEvent.pause();
        playTaskPane.getPlayButton().setEnabled(true);
        playTaskPane.getPauseButton().setEnabled(false);
    }
    private boolean camNodeDetached = true;

    public void stop()
    {
        if (!camNodeDetached)
        {
            System.out.println("Detaching cam?");
            if (motionEvent.getSpatial() instanceof CameraNode)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        camNodeDetached = true;
                        System.out.println("Detaching cam");
                        CurrentData.getEditorWindow().getB3DApp().detachCam();
                        return null;
                    }
                });
            }
        }
        cycleChecker.setEnabled(true);
        motionEvent.stop();
        playTaskPane.getPlayButton().setEnabled(true);
        playTaskPane.getPauseButton().setEnabled(false);
        playTaskPane.getStopButton().setEnabled(false);
    }

    public MotionEvent getMotionEvent()
    {
        return motionEvent;
    }
}