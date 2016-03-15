package gui.dialogs.keyframeAnimationEditor;

import Other.GUI_Tools;
import b3dElements.B3D_Element;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import components.BButton;
import components.BComboBox;
import components.BToggleButton;
import components.Checker;
import components.Float3Panel;
import components.Float4Panel;
import general.CurrentData;
import general.Preference;
import gui.dialogs.SelectElementDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import monkeyStuff.keyframeAnimation.LiveKeyframeAnimation;
import monkeyStuff.keyframeAnimation.LiveKeyframeProperty;
import monkeyStuff.keyframeAnimation.LiveKeyframeUpdater;
import b3dElements.animations.keyframeAnimations.AnimationType;
import b3dElements.spatials.B3D_Spatial;
import com.jme3.effect.ParticleEmitter;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import components.BSpinnerNumberModel;
import gui.components.BColorButton;
import gui.components.ElementSelectionPanel;
import gui.components.LightDirectionPanel;
import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.UUID;
import javax.swing.JSpinner;
import monkeyStuff.CustomParticleEmitter;
import org.jdesktop.swingx.VerticalLayout;
import other.Wizard;
import se.datadosen.component.RiverLayout;

/**
 *
 * @author David
 */
public class KeyframeAnimationFrame extends JFrame
{

    private ToolsPanel toolsPanel = new ToolsPanel();
    private ValuePanel valuePanel = new ValuePanel();
    private AttributesPanel attributesPanel = new AttributesPanel();
    private EditPanel editPanel = new EditPanel();
    private KeyframePanel keyframePanel = new KeyframePanel();
    private TimelinePanel timelinePanel = new TimelinePanel();
    private EditorPanel editorPanel = new EditorPanel();
    private static final int TIMELINE_HEIGHT = 40;
    private int zoom = 20, maxFrame = 60;
    private boolean firstPaint = true;
    private LiveKeyframeAnimation currentAnimation = null;
    private boolean editingEnabled = true;

    public ValuePanel getValuePanel()
    {
        return valuePanel;
    }

    private void editingEnabled(boolean b)
    {
        editingEnabled = b;
        editPanel.keyframeEditor.repaint();
        for (AnimationElementTree aet : keyframePanel.animationElementTrees)
            aet.setEnabled(b);
    }

    public KeyframeAnimationFrame()
    {
        setIconImage(new ImageIcon("dat//img//other//logo.png").getImage());
        if (CurrentData.getPrefs().get(Preference.KEY_ANIMATION_DIALOG_SIZE) == null)
            setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 3 * 2, 499);
        else
            setSize((Dimension) CurrentData.getPrefs().get(Preference.KEY_ANIMATION_DIALOG_SIZE));
        if (CurrentData.getPrefs().get(Preference.KEY_ANIMATION_DIALOG_LOCATION) == null)
            setLocationRelativeTo(null);
        else
            setLocation((Point) CurrentData.getPrefs().get(Preference.KEY_ANIMATION_DIALOG_LOCATION));
        setTitle("Keyframe Animations");
        setAlwaysOnTop((Boolean) CurrentData.getPrefs().get(Preference.KEY_ANIMATION_EDITOR_ON_TOP));
        setLayout(new BorderLayout(0, 0));
        add(toolsPanel, BorderLayout.NORTH);
        add(editorPanel, BorderLayout.CENTER);
        add(valuePanel, BorderLayout.EAST);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {
                super.windowOpened(e);
                CurrentData.getPrefs().set(Preference.KEY_ANIMATION_EDITOR_SHOWN, true);
                CurrentData.getPrefs().save();
            }

            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                CurrentData.getPrefs().set(Preference.KEY_ANIMATION_EDITOR_SHOWN, false);
                CurrentData.getPrefs().save();
            }
        });
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentMoved(ComponentEvent e)
            {
                super.componentMoved(e);
                CurrentData.getPrefs().set(Preference.KEY_ANIMATION_DIALOG_LOCATION, getLocation());
                CurrentData.getPrefs().save();
            }

            @Override
            public void componentResized(ComponentEvent e)
            {
                super.componentResized(e); //To change body of generated methods, choose GUI_Tools | Templates.
                arrangeSizes();
                arrangeScrollbars();
                CurrentData.getPrefs().set(Preference.KEY_ANIMATION_DIALOG_SIZE, getSize());
                CurrentData.getPrefs().save();
            }
        });
        updateAnimationCollection();
        arrangeSizes();
    }

    public void arrangeScrollbars()
    {
        if (currentAnimation != null)
        {
            currentAnimation.calcMaxFrames();
            maxFrame = (currentAnimation.getMaxFrames() > maxFrame ? currentAnimation.getMaxFrames() : maxFrame);
        }
        int h = attributesPanel.getRequieredHeight() + 1;
        keyframePanel.vscrollbar.setValues(editPanel.keyframeEditor.yOffset, editPanel.keyframeEditor.getHeight(), 0, (int) (h * 1.25f));
        keyframePanel.vscrollbar.repaint();
        /* System.out.println("Value: " + editPanel.keyframeEditor.yOffset);
         System.out.println("Req: " + h);
         System.out.println("Ext: " + editPanel.keyframeEditor.getHeight() * 100 / h);
         System.out.println("Max: " + h);*/
        editPanel.hscrollbar.setValues(
                editPanel.keyframeEditor.xOffset,
                editPanel.keyframeEditor.getWidth(),
                0,
                (int) (maxFrame * timelinePanel.gapSize * 1.2));
        editPanel.hscrollbar.repaint();
        attributesPanel.repaint();
    }

    public final void updateAnimationCollection()
    {
        Object selection = null;
        currentAnimation = null;
        toolsPanel.animationSelector.removeAllItems();
        for (LiveKeyframeAnimation lka : Wizard.getKeyframeAnimations())
            toolsPanel.animationSelector.addItem(lka.getName());
        if (toolsPanel.animationSelector.getItemCount() > 0)
            toolsPanel.animationSelector.setSelectedIndex(0);
        //System.out.println("SELECTEC: " + toolsPanel.animationSelector.getSelectedItem());
        if (toolsPanel.animationSelector.getSelectedItem() != null)
            selection = toolsPanel.animationSelector.getSelectedItem().toString();
        if (selection != null)
        {
            for (LiveKeyframeAnimation lka : Wizard.getKeyframeAnimations())
                if (lka.getName().equals(selection))
                    currentAnimation = lka;
            for (int i = 0; i < toolsPanel.animationSelector.getItemCount(); i++)
                if (toolsPanel.animationSelector.getItemAt(i).equals(selection))
                    toolsPanel.animationSelector.setSelectedIndex(i);
            currentAnimation.uncalcValues();
        }
        attributesPanel.newUpdaters();
        GUI_Tools.repaintAll(this);
    }

    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        repaint();
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                arrangeSizes();
            }
        });
        setSize(getWidth() + 1, getHeight() + 1);
        arrangeSizes();
        GUI_Tools.revalidateAll(attributesPanel);
        GUI_Tools.repaintAll(attributesPanel);
    }

    private void arrangeSizes()
    {
        valuePanel.setPreferredSize(new Dimension(320, editPanel.getHeight()));
        toolsPanel.setPreferredSize(new Dimension(KeyframeAnimationFrame.this.getWidth(), 26));
        editorPanel.setPreferredSize(new Dimension(KeyframeAnimationFrame.this.getWidth(), getHeight() - toolsPanel.getHeight()));
        keyframePanel.setPreferredSize(new Dimension(editorPanel.getWidth(), editorPanel.getHeight() - TIMELINE_HEIGHT));
        timelinePanel.setPreferredSize(new Dimension(editorPanel.getWidth() - 200, TIMELINE_HEIGHT));
        attributesPanel.setPreferredSize(new Dimension(200, 200));
        editPanel.setPreferredSize(new Dimension(keyframePanel.getWidth() - 500, keyframePanel.getHeight()));
        //why? toolsPanel.updateSlider();
        toolsPanel.repaint();
        editorPanel.repaint();
        keyframePanel.repaint();
        timelinePanel.repaint();
        attributesPanel.repaint();
        editPanel.repaint();
        arrangeScrollbars();
        repaint();
        GUI_Tools.repaintAll(attributesPanel);
    }

    private void updateFrames()
    {
        timelinePanel.repaint();
        editPanel.keyframeEditor.repaint();
    }

    public void updateOnPlay()
    {
        if (currentAnimation.getCurrentFrame() < currentAnimation.getMaxFrames())
        {
            timelinePanel.currentFrame = currentAnimation.getCurrentFrame();
            timelinePanel.repaint();
            editPanel.keyframeEditor.repaint();
        } else
        {
            toolsPanel.playButton.setIcon(new ImageIcon("dat//img//menu//keyframe//play.png"));
            editingEnabled(true);
        }
    }

    void removeElement(AnimationElementTree aThis)
    {
        keyframePanel.animationElementTrees.remove(aThis);
        attributesPanel.treePanel.remove(aThis);
        attributesPanel.treePanel.repaint();
        attributesPanel.treePanel.revalidate();
        attributesPanel.revalidate();
        editPanel.keyframeEditor.repaint();
    }

    boolean isEditable()
    {
        return editingEnabled;
    }

    public void compileCurrent()
    {
        currentAnimation.removeAllUpdaters();
        for (AnimationElementTree aet : keyframePanel.animationElementTrees)
            currentAnimation.addUpdater(aet.getKeyframeUpdater().createNew());
    }

    void drag(AnimationElementTree updater, int y)
    {
        Component c = attributesPanel.getComponent(0).getComponentAt(66, y + updater.getY());
        if (c instanceof AnimationElementTree)
        {
            AnimationElementTree other = (AnimationElementTree) c;
            int oldIndex = keyframePanel.animationElementTrees.indexOf(updater);
            int newIndex = keyframePanel.animationElementTrees.indexOf(other);
            Collections.swap(keyframePanel.animationElementTrees, oldIndex, newIndex);

            // System.out.println("Switching " + updater.getRootNode().getUserObject() + " with " + other.getRootNode().getUserObject());

            currentAnimation.removeAllUpdaters();
            for (AnimationElementTree aet : keyframePanel.animationElementTrees)
                currentAnimation.addUpdater(aet.getKeyframeUpdater().createNew());
            currentAnimation.calcValues();
            attributesPanel.newUpdaters();
        }
    }

    void updateTreeDragging(AnimationElementTree aet, int y)
    {
        attributesPanel.drag = aet.getY() + y;
        attributesPanel.treePanel.repaint();
    }

    class ToolsPanel extends JPanel implements ActionListener
    {

        private BButton previousFrameButton = new BButton(new ImageIcon("dat//img//menu//keyframe//backward.png"), false);
        private BButton playButton = new BButton(new ImageIcon("dat//img//menu//keyframe//play.png"), false);
        private BButton nextFrameButton = new BButton(new ImageIcon("dat//img//menu//keyframe//forward.png"), false);
        private BButton stopButton = new BButton(new ImageIcon("dat//img//menu//keyframe//stop.png"), false);
        private BButton zoomInButton = new BButton(new ImageIcon("dat//img//menu//keyframe//plus.png"), false);
        private JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 10, 200, 30);
        private BButton zoomOutButton = new BButton(new ImageIcon("dat//img//menu//keyframe//minus.png"), false);
        private JLabel currentFrameLabel = new JLabel("Frame: ");
        private BComboBox<String> animationSelector = new BComboBox<String>();
        private BButton newAnimationButton = new BButton(new ImageIcon("dat//img//menu//keyframe//plus.png"), false);
        private BButton deleteAnimationButton = new BButton(new ImageIcon("dat//img//menu//keyframe//delete.png"), false);
        private BToggleButton onTopButton = new BToggleButton(new ImageIcon("dat//img//menu//keyframe//pin.png"), (Boolean) CurrentData.getPrefs().get(Preference.KEY_ANIMATION_EDITOR_ON_TOP));
        private boolean dont = false;

        public ToolsPanel()
        {
            previousFrameButton.setActionCommand("prev");
            nextFrameButton.setActionCommand("next");
            playButton.setActionCommand("play");
            stopButton.setActionCommand("stop");
            deleteAnimationButton.setActionCommand("delete");
            onTopButton.setActionCommand("aot");
            previousFrameButton.addActionListener(this);
            nextFrameButton.addActionListener(this);
            playButton.addActionListener(this);
            stopButton.addActionListener(this);
            deleteAnimationButton.addActionListener(this);
            onTopButton.addActionListener(this);

            previousFrameButton.setToolTipText("Previous Frame");
            nextFrameButton.setToolTipText("Nex Frame");
            playButton.setToolTipText("Play / Pause");
            stopButton.setToolTipText("Stop");
            zoomInButton.setToolTipText("Zoom in");
            zoomOutButton.setToolTipText("Zoom out");
            newAnimationButton.setToolTipText("Create New Animation");
            deleteAnimationButton.setToolTipText("Delete");
            animationSelector.setPreferredSize(new Dimension(150, 25));
            animationSelector.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        if (currentAnimation != null)
                        {
                            currentAnimation.removeAllUpdaters();
                            //System.out.println("Trees: " + keyframePanel.animationElementTrees.size());
                            for (AnimationElementTree aet : keyframePanel.animationElementTrees)
                                currentAnimation.addUpdater(aet.getKeyframeUpdater().createNew());
                            currentAnimation.calcValues();
                            currentAnimation.uncalcValues(); //kek
                        }
                        for (LiveKeyframeAnimation lka : Wizard.getKeyframeAnimations())
                            if (lka.getName().equals(e.getItem().toString()))
                            {
                                currentAnimation = lka;
                                currentAnimation.uncalcValues();
                                editPanel.keyframeEditor.repaint();
                                attributesPanel.newUpdaters();
                            }
                    }
                }
            });
            zoomSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    if (firstPaint) // elegant af
                    {
                        firstPaint = false;
                        KeyframeAnimationFrame.this.setSize(
                                KeyframeAnimationFrame.this.getWidth() + 1,
                                KeyframeAnimationFrame.this.getHeight() + 1);
                    }
                    if (dont) // elegant af
                    {
                        dont = false;
                        return;
                    }
                    zoom = zoomSlider.getValue() * timelinePanel.getWidth() / 500;
                    updateFrames();
                    arrangeScrollbars();
                }
            });
            zoomOutButton.addActionListener(this);
            zoomOutButton.setActionCommand("+");
            zoomInButton.addActionListener(this);
            zoomInButton.setActionCommand("-");
            newAnimationButton.addActionListener(this);
            newAnimationButton.setActionCommand("new");
            setLayout(new RiverLayout(0, 0));
            add("left", onTopButton);
            add(new JLabel("    ")); //elegant af
            add("tab", new JLabel("Select:   "));
            add(animationSelector);
            add(newAnimationButton);
            add(deleteAnimationButton);
            add(new JLabel("                ")); //elegant af
            add(previousFrameButton);
            add(playButton);
            add(nextFrameButton);
            add(stopButton);
            add(new JLabel("           ")); //elegant af
            add(zoomInButton);
            add(zoomSlider);
            add(zoomOutButton);
            add(new JLabel("              ")); //elegant af
            add(currentFrameLabel);
        }

        public JLabel getCurrentFrameLabel()
        {
            return currentFrameLabel;
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("+"))
                zoomSlider.setValue(zoomSlider.getValue() + 1);
            else if (e.getActionCommand().equals("-"))
                zoomSlider.setValue(zoomSlider.getValue() - 1);
            else if (e.getActionCommand().equals("delete"))
            {
                if (currentAnimation != null && JOptionPane.showConfirmDialog(deleteAnimationButton, "Are you sure that you want to delete this animation?") == JOptionPane.YES_OPTION)
                {
                    Wizard.getKeyframeAnimations().remove(currentAnimation);
                    currentAnimation = null;
                    animationSelector.removeItemAt(animationSelector.getSelectedIndex());
                    attributesPanel.newUpdaters();
                    GUI_Tools.repaintAll(KeyframeAnimationFrame.this);
                }
            } else if (e.getActionCommand().equals("new"))
            {
                String name = JOptionPane.showInputDialog(this, "Animation Name", "New Animation", JOptionPane.INFORMATION_MESSAGE);
                if (name != null && !"".equals(name))
                {
                    for (LiveKeyframeAnimation k : Wizard.getKeyframeAnimations())
                        if (k.getName().equals(name))
                        {
                            JOptionPane.showMessageDialog(this, "Name already taken!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    LiveKeyframeAnimation kfa = new LiveKeyframeAnimation(name);
                    Wizard.getKeyframeAnimations().add(kfa);
                    //currentAnimation = kfa;
                    //updateAnimationCollection();
                    animationSelector.addItem(kfa.getName());
                    //animationSelector.setSelectedIndex(animationSelector.getItemCount() - 1);
                    //attributesPanel.newUpdaters();
                    GUI_Tools.repaintAll(KeyframeAnimationFrame.this);
                }
            } else if (e.getActionCommand().equals("prev"))
            {
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
                else
                {
                    if (timelinePanel.currentFrame > 0)
                    {
                        if (currentAnimation.isPlaying()) // Pause
                        {
                            playButton.setIcon(new ImageIcon("dat//img//menu//keyframe//play.png"));
                            currentAnimation.pause();
                        }
                        timelinePanel.currentFrame--;
                        timelinePanel.repaint();
                        if (currentAnimation.getCurrentFrame() != 0) // Pause
                            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                            {
                                public Void call() throws Exception
                                {
                                    currentAnimation.goTo(timelinePanel.currentFrame);
                                    return null;
                                }
                            });
                        editPanel.keyframeEditor.select(timelinePanel.currentFrame);
                        editPanel.keyframeEditor.repaint();
                    }
                }
            } else if (e.getActionCommand().equals("next"))
            {
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
                else
                {
                    if (currentAnimation.isPlaying()) // Pause
                    {
                        playButton.setIcon(new ImageIcon("dat//img//menu//keyframe//play.png"));
                        currentAnimation.pause();
                    }
                    timelinePanel.currentFrame++;
                    editPanel.keyframeEditor.select(timelinePanel.currentFrame);
                    timelinePanel.repaint();
                    editPanel.keyframeEditor.repaint();
                    if (currentAnimation.getCurrentFrame() != 0) // Pause
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                        {
                            public Void call() throws Exception
                            {
                                currentAnimation.goTo(timelinePanel.currentFrame);
                                return null;
                            }
                        });
                }
            } else if (e.getActionCommand().equals("play"))
            {
                valuePanel.deselect();
                editingEnabled(false);
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
                else
                {
                    valuePanel.setLive(false);
                    if (currentAnimation.isPlaying()) // Pause
                    {
                        playButton.setIcon(new ImageIcon("dat//img//menu//keyframe//play.png"));
                        currentAnimation.pause();
                    } else if (currentAnimation.getCurrentFrame() == 0) // Play from beginning
                    {
                        currentAnimation.removeAllUpdaters();
                        for (AnimationElementTree aet : keyframePanel.animationElementTrees)
                            currentAnimation.addUpdater(aet.getKeyframeUpdater().createNew());
                        currentAnimation.play(true);
                        playButton.setIcon(new ImageIcon("dat//img//menu//keyframe//pause.png"));
                    } else // Unpause
                    {
                        currentAnimation.play(false);
                        playButton.setIcon(new ImageIcon("dat//img//menu//keyframe//pause.png"));
                    }
                }
            } else if (e.getActionCommand().equals("stop"))
            {
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
                else
                {
                    currentAnimation.stop();
                    editingEnabled(true);
                    playButton.setIcon(new ImageIcon("dat//img//menu//keyframe//play.png"));
                }
            } else if (e.getActionCommand().equals("delete"))
            {
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (e.getActionCommand().equals("aot"))
            {
                setAlwaysOnTop(onTopButton.isSelected());
                CurrentData.getPrefs().set(Preference.KEY_ANIMATION_EDITOR_ON_TOP, onTopButton.isSelected());
            }
        }

        private void updateSlider()
        {
            if (timelinePanel.getWidth() != 0)
            {
                dont = true;
                int newValue = zoom * 1000 / timelinePanel.getWidth();
                if (newValue > 100)
                    zoom = timelinePanel.getWidth() / 10;
                zoomSlider.setValue(zoom * 1000 / timelinePanel.getWidth());
                repaint();
            }
        }
    }

    public class ValuePanel extends JPanel
    {

        private LiveKeyframeProperty property;
        private int frame = -1;
        private Checker liveValuesChecker = new Checker();
        private JComponent valueComponent;

        public ValuePanel()
        {
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setLayout(new RiverLayout(10, 10));
            liveValuesChecker.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        public Void call() throws Exception
                        {
                            if (liveValuesChecker.isChecked())
                                CurrentData.getEditorWindow().getB3DApp().frameSelected(frame);
                            else
                                CurrentData.getEditorWindow().getB3DApp().frameUnselected();
                            return null;
                        }
                    });
                }
            });
        }

        public void updateValues(LiveKeyframeProperty currentProperty, int currentFrame)
        {
            boolean liveEnabled = true;
            liveValuesChecker.setChecked(false);
            removeAll();
            property = currentProperty;
            frame = currentFrame;
            if (property.getValues().length > frame && property.getValues()[frame] != null)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    public Void call() throws Exception
                    {
                        CurrentData.getEditorWindow().getB3DApp().frameSelected(frame);
                        return null;
                    }
                });
                add("hfill", new JLabel(property.type.toString() + " [" + frame + "]", SwingConstants.CENTER));
                add("br hfill", new JSeparator(JSeparator.HORIZONTAL));
                if (property.type == AnimationType.Translation
                        || property.type == AnimationType.Scale
                        || property.type == AnimationType.Position)
                {
                    //Index 2
                    valueComponent = new Float3Panel((Vector3f) property.getValues()[frame],
                            CurrentData.getEditorWindow().getB3DApp().getCamera(),
                            Float3Panel.VERTICAL);
                    ((Float3Panel) valueComponent).addFieldKeyListener(new KeyAdapter()
                    {
                        @Override
                        public void keyReleased(KeyEvent e)
                        {
                            property.setValue(frame, ((Float3Panel) valueComponent).getVector());
                        }
                    });
                    add("br hfill", valueComponent);
                } else if (property.type == AnimationType.Direction)
                {
                    //Index 2
                    valueComponent = new LightDirectionPanel((Vector3f) property.getValues()[frame])
                    {
                        @Override
                        public void updateDirection(Vector3f direction)
                        {
                            property.setValue(frame, ((LightDirectionPanel) valueComponent).getVector());
                        }
                    };
                    add("br", new JLabel("Rotation: "));
                    add("br hfill", valueComponent);
                } else if (property.type.toString().contains("Constraint"))
                {
                    liveEnabled = false;
                    Class[] allowedClasses = null;
                    if (property.type == AnimationType.Translation_Constraint)
                        allowedClasses = new Class[]
                        {
                            B3D_Spatial.class
                        };
                    //Index 2
                    valueComponent = new ElementSelectionPanel((UUID) property.getValues()[frame], allowedClasses)
                    {
                        @Override
                        public void onSelectionChange()
                        {
                            System.out.println("ELEMENT: " + element);
                            //why nullpointer? System.out.println("Set value at " + frame + " to " + element == null ? null : element.getUUID());
                            if (element == null)
                                property.setValue(frame, null);
                            else
                                property.setValue(frame, element.getUUID());
                        }
                    };
                    add("br", new JLabel("Reference: "));
                    add("br hfill", valueComponent);
                } else if (property.type == AnimationType.Rotation)
                {
                    //Index 2
                    valueComponent = new Float4Panel(
                            property.getValues()[frame],
                            Float4Panel.VERTICAL);
                    ((Float4Panel) valueComponent).addFieldKeyListener(new KeyAdapter()
                    {
                        @Override
                        public void keyReleased(KeyEvent e)
                        {
                            property.setValue(frame, new Quaternion(((Float4Panel) valueComponent).getQuaternion()));
                        }
                    });
                    add("br hfill", valueComponent);
                } else if (property.type == AnimationType.Frozen)
                {
                    //Index 2
                    valueComponent = new Checker();
                    ((Checker) valueComponent).setChecked((Boolean) property.getValues()[frame]);
                    ((Checker) valueComponent).addMouseListener(new MouseAdapter()
                    {
                        @Override
                        public void mouseReleased(MouseEvent e)
                        {
                            property.setValue(frame, ((Checker) valueComponent).isChecked());
                        }
                    });
                    add("br", new JLabel("Freeze: "));
                    add("tab", valueComponent);
                } else if (property.type == AnimationType.Emit_All)
                {
                    //Index 2
                    valueComponent = new Checker();
                    ((Checker) valueComponent).setChecked((Boolean) property.getValues()[frame]);
                    ((Checker) valueComponent).addMouseListener(new MouseAdapter()
                    {
                        @Override
                        public void mouseReleased(MouseEvent e)
                        {
                            property.setValue(frame, ((Checker) valueComponent).isChecked());
                        }
                    });
                    add("br", new JLabel("Emit 'em ALL!: "));
                    add("tab", valueComponent);
                } else if (property.type == AnimationType.Particles_Per_Second)
                {
                    valueComponent = new JSpinner(new BSpinnerNumberModel(0, (Integer) property.getValues()[frame], 0, Integer.MAX_VALUE, 1));
                    ((JSpinner) valueComponent).addChangeListener(new ChangeListener()
                    {
                        public void stateChanged(ChangeEvent e)
                        {
                            property.setValue(frame, (Serializable) ((JSpinner) valueComponent).getValue());
                        }
                    });
                    add("br", new JLabel("Particles Per Second: "));
                    add("tab", valueComponent);
                } else if (property.type == AnimationType.End_Color_Blend || property.type == AnimationType.Start_Color_Blend || property.type == AnimationType.Light_Color_Blend)
                {
                    System.out.println("At frame " + frame + ": " + property.getValues()[frame]);
                    Color newColor = Wizard.makeColor((ColorRGBA) property.getValues()[frame]);
                    valueComponent = new BColorButton(new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue()))
                    {
                        @Override
                        public void andThenDoThis()
                        {
                            property.setValue(frame, (Serializable) Wizard.makeColorRGBA(((BColorButton) valueComponent).getColor()));
                        }
                    };
                    add("br", new JLabel("Color: "));
                    add("tab", valueComponent);
                }
                if (liveEnabled)
                {
                    add("br", new JLabel("Use Live-Value: "));
                    add("tab", liveValuesChecker);
                }
            }
            repaint();
            revalidate();
            validate();
            repaint();
        }

        public void deselect()
        {
            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
            {
                public Void call() throws Exception
                {
                    CurrentData.getEditorWindow().getB3DApp().frameUnselected();
                    return null;
                }
            });
            property = null;
            frame = -1;
            removeAll();
            repaint();
        }

        public void refresh()
        {
            if (frame != -1 && property != null && valueComponent != null && liveValuesChecker.isChecked())
            {
                System.out.println("Refresh");
                if (property.type == AnimationType.Translation)
                {
                    if (!((Float3Panel) valueComponent).hasFocus())
                    {
                        Vector3f newVec = new Vector3f(((Spatial) property.getUpdater().getObject()).getLocalTranslation());
                        ((Float3Panel) valueComponent).setVector(newVec);
                        property.setValue(frame, newVec);
                    }
                } else if (property.type == AnimationType.Position)
                {
                    if (!((Float3Panel) valueComponent).hasFocus())
                    {
                        Vector3f newVec;
                        if (property.getUpdater().getObject() instanceof PointLight)
                            newVec = new Vector3f(((PointLight) property.getUpdater().getObject()).getPosition());
                        else
                            newVec = new Vector3f(((SpotLight) property.getUpdater().getObject()).getPosition());
                        ((Float3Panel) valueComponent).setVector(newVec);
                        property.setValue(frame, newVec);
                    }
                } else if (property.type == AnimationType.Scale)
                {
                    if (!((Float3Panel) valueComponent).hasFocus())
                    {
                        Vector3f newVec = new Vector3f(((Spatial) property.getUpdater().getObject()).getLocalScale());
                        ((Float3Panel) valueComponent).setVector(newVec);
                        property.setValue(frame, newVec);
                    }
                } else if (property.type == AnimationType.Direction)
                {
                    if (!((Float3Panel) valueComponent).hasFocus())
                    {
                        Vector3f newVec;
                        if (property.getUpdater().getObject() instanceof DirectionalLight)
                            newVec = new Vector3f(((DirectionalLight) property.getUpdater().getObject()).getDirection());
                        else
                            newVec = new Vector3f(((SpotLight) property.getUpdater().getObject()).getDirection());
                        ((LightDirectionPanel) valueComponent).setVector(newVec);
                        property.setValue(frame, newVec);
                    }
                } else if (property.type == AnimationType.Rotation)
                {
                    if (!((Float4Panel) valueComponent).hasFocus())
                    {
                        Quaternion newQuat = new Quaternion(((Spatial) property.getUpdater().getObject()).getLocalRotation());
                        ((Float4Panel) valueComponent).setFloats(newQuat);
                        property.setValue(frame, newQuat);
                    }
                } else if (property.type == AnimationType.Frozen)
                {
                    boolean enabled = ((ParticleEmitter) property.getUpdater().getObject()).isEnabled();
                    ((Checker) valueComponent).setChecked(enabled);
                    property.setValue(frame, enabled);
                } else if (property.type == AnimationType.Emit_All)
                {
                    boolean enabled = false;
                    ((Checker) valueComponent).setChecked(enabled);
                    property.setValue(frame, enabled);
                } else if (property.type == AnimationType.Particles_Per_Second)
                {
                    int pps = (int) ((CustomParticleEmitter) property.getUpdater().getObject()).getParticlesPerSec();
                    ((JSpinner) valueComponent).setValue(pps);
                    property.setValue(frame, pps);
                } else if (property.type == AnimationType.Start_Color_Blend)
                {
                    ColorRGBA color = (ColorRGBA) ((CustomParticleEmitter) property.getUpdater().getObject()).getStartColor();
                    ((BColorButton) valueComponent).setColor(Wizard.makeColor(color));
                    property.setValue(frame, color);
                } else if (property.type == AnimationType.End_Color_Blend)
                {
                    ColorRGBA color = (ColorRGBA) ((CustomParticleEmitter) property.getUpdater().getObject()).getEndColor();
                    ((BColorButton) valueComponent).setColor(Wizard.makeColor(color));
                    property.setValue(frame, color);
                } else if (property.type == AnimationType.Light_Color_Blend)
                {
                    ColorRGBA color = (ColorRGBA) ((Light) property.getUpdater().getObject()).getColor();
                    ((BColorButton) valueComponent).setColor(Wizard.makeColor(color));
                    property.setValue(frame, color);
                }
                valueComponent.repaint();
            }
        }

        private void setLive(boolean b)
        {
            liveValuesChecker.setChecked(b);
        }

        public void setFrame(int frame)
        {
            this.frame = frame;
        }
    }

    class EditorPanel extends JPanel
    {

        private AttributesControlsPanel acp = new AttributesControlsPanel();

        public EditorPanel()
        {
            setLayout(new BorderLayout(0, 0));
            add(keyframePanel, BorderLayout.SOUTH);
            add(timelinePanel, BorderLayout.EAST);
            add(acp, BorderLayout.WEST);
        }

        class AttributesControlsPanel extends JPanel implements ActionListener
        {

            private BButton addElementButton = new BButton(new ImageIcon("dat//img//menu//keyframe//plus.png"), false);
            private BComboBox<String> sortTypes = new BComboBox<String>(new String[]
            {
                "Chronological", "Start time", "Alphabetical", "Length"
            });

            public AttributesControlsPanel()
            {
                setLayout(new RiverLayout(0, 0));
                setBorder(new EmptyBorder(5, 0, 0, 0));
                //  add(sortTypes);
                add(addElementButton);
                addElementButton.setActionCommand("add");
                addElementButton.addActionListener(this);
            }

            public void actionPerformed(ActionEvent e)
            {
                if (e.getActionCommand().equals("add"))
                {
                    if (currentAnimation == null)
                    {
                        JOptionPane.showMessageDialog(this, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    setAlwaysOnTop(false);
                    ArrayList<B3D_Element> exclude = new ArrayList<B3D_Element>();
                    for (AnimationElementTree aet : keyframePanel.animationElementTrees)
                        exclude.add(aet.getElement());
                    SelectElementDialog sed = new SelectElementDialog(KeyframeAnimationFrame.this.getLocation(), exclude);
                    if (sed.getSelectedElement() != null)
                        keyframePanel.addElement(sed.getSelectedElement());
                    setAlwaysOnTop((Boolean) CurrentData.getPrefs().get(Preference.KEY_ANIMATION_EDITOR_ON_TOP));
                    attributesPanel.treePanel.setSize(attributesPanel.treePanel.getWidth(), attributesPanel.treePanel.getHeight() + 300);
                    arrangeScrollbars();
                }
            }
        }
    }

    public void setTreePanelCursor(Cursor c)
    {
        attributesPanel.treePanel.setCursor(c);
    }

    class TimelinePanel extends JPanel
    {

        private double gapSize;
        private int currentFrame = 0;
        private int[] yVals = new int[]
        {
            19, 19, 30, 36, 30
        };

        public TimelinePanel()
        {
            addMouseMotionListener(new MouseMotionAdapter()
            {
                @Override
                public void mouseDragged(MouseEvent e)
                {
                    int x = e.getX() + editPanel.keyframeEditor.xOffset;
                    if (x < 0)
                        x = 0;
                    currentFrame = (int) (x / timelinePanel.gapSize);
                    editPanel.keyframeEditor.select(currentFrame);
                    if (currentAnimation.getCurrentFrame() != 0)
                    {
                        toolsPanel.playButton.setIcon(new ImageIcon("dat//img//menu//keyframe//play.png"));
                        currentAnimation.pause();
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                        {
                            public Void call() throws Exception
                            {
                                currentAnimation.goTo(currentFrame);
                                return null;
                            }
                        });
                    }
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0)
        {
            super.paintComponent(g0);
            //Antialiasing ON
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (zoom < 3)
                zoom = 3;
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.LIGHT_GRAY);
            double x;
            gapSize = (double) getWidth() / zoom;
            //System.out.println("Width: " + getWidth());
            //System.out.println("Zoom: " + zoom);
            //   System.out.println("Gap Size: " + gapSize);
            for (int i = 0; i < zoom + 1 + editPanel.keyframeEditor.xOffset; i++)
            {
                x = i * gapSize - editPanel.keyframeEditor.xOffset;
                //System.out.println("New x: " + x);
                g.drawLine((int) x, 0, (int) x, getHeight());
                String frameNumber = "" + (i);
                if (gapSize > 20)
                    g.drawString(frameNumber, (int) x + (int) gapSize / 2 - g.getFontMetrics().stringWidth(frameNumber) / 2, TIMELINE_HEIGHT / 2 - 7);
            }
            g.setColor(Color.orange);
            //  g.fillRect((int) (currentFrame * gapSize + gapSize / 5 * 2), TIMELINE_HEIGHT - 15, (int) gapSize / 5, 15);
            double xBase = -editPanel.keyframeEditor.xOffset + gapSize * currentFrame + gapSize / 2;
            int[] xVals = new int[]
            {
                (int) (xBase - 6),
                (int) (xBase + 6),
                (int) (xBase + 6),
                (int) (xBase),
                (int) (xBase - 6)
            };
            g.setColor(Color.orange);
            g.fillPolygon(xVals, yVals, 5);
            g.setColor(Color.black); // :)
            g.drawPolygon(xVals, yVals, 5);
        }

        public int getCurrentFrame()
        {
            return currentFrame;
        }
    }

    class KeyframePanel extends JPanel implements AdjustmentListener
    {

        private JScrollBar vscrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 50, 0, 100);
        private ArrayList<B3D_Element> elements = new ArrayList<B3D_Element>();
        protected ArrayList<AnimationElementTree> animationElementTrees = new ArrayList<AnimationElementTree>();

        public KeyframePanel()
        {
            setBackground(Color.GRAY);
            setLayout(new BorderLayout(0, 0));
            vscrollbar.addAdjustmentListener(this);
            add(attributesPanel, BorderLayout.WEST);
            add(editPanel, BorderLayout.CENTER);
            add(vscrollbar, BorderLayout.EAST);
        }

        public ArrayList<B3D_Element> getElements()
        {
            return elements;
        }

        public void setElements(ArrayList<B3D_Element> elements)
        {
            this.elements = elements;
        }

        public void addElement(B3D_Element e)
        {
            elements.add(e);
            AnimationElementTree aet = new AnimationElementTree(e, null);
            animationElementTrees.add(aet);
            attributesPanel.treePanel.add(aet);
            aet.updateElements();
            repaint();
            //attributesPanel.treePanel.setBounds(0, attributesPanel.treePanel.getBounds().y, 200, 1000 * keyframePanel.animationElementTrees.size());
            attributesPanel.treePanel.repaint();
            attributesPanel.treePanel.revalidate();
            attributesPanel.revalidate();
            editPanel.keyframeEditor.repaint();
            revalidate();
        }

        public void adjustmentValueChanged(AdjustmentEvent e)
        {
            editPanel.keyframeEditor.yOffset = e.getValue();
            attributesPanel.treePanel.setBounds(0, -e.getValue(), 200, 1000 * keyframePanel.animationElementTrees.size());
            editPanel.keyframeEditor.repaint();
            attributesPanel.repaint();
        }
    }

    public void updateNames()
    {
        attributesPanel.newUpdaters();
    }

    class AttributesPanel extends JPanel
    {

        private JPanel treePanel = new JPanel(new VerticalLayout(0))
        {
            @Override
            public void paintChildren(Graphics g)
            {
                super.paintChildren(g);
                if (drag != -1)
                {
                    g.setColor(Color.orange);
                    g.drawLine(0, drag, getWidth(), drag);
                    attributesPanel.drag = -1;
                }
            }
        };
        private int drag = -1;

        public AttributesPanel()
        {
            treePanel.setBounds(0, 0, 200, 10000);
            setLayout(null);
            setBackground(Color.GRAY);
            add(treePanel);
            treePanel.repaint();
        }

        public void updateAttributes()
        {
            editPanel.getKeyframeEditor().repaint();
        }

        private void newUpdaters()
        {
            keyframePanel.animationElementTrees.clear();
            treePanel.removeAll();
            if (currentAnimation != null)
            {
                currentAnimation.uncalcValues();
                for (LiveKeyframeUpdater lku : currentAnimation.getUpdaters())
                {
                    B3D_Element element = Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(lku.getObject().hashCode()));
                    AnimationElementTree aet = new AnimationElementTree(element, lku);
                    keyframePanel.animationElementTrees.add(aet);
                    treePanel.add(aet);
                    aet.updateElements();
                    int aetMax = aet.getKeyframeUpdater().calcMaxFrames();
                    maxFrame = maxFrame > aetMax ? maxFrame : aetMax;
                }
            }
            //attributesPanel.treePanel.setBounds(0, attributesPanel.treePanel.getBounds().y, 200, 1000 * keyframePanel.animationElementTrees.size());
            treePanel.repaint();
            treePanel.revalidate();
            attributesPanel.revalidate();
            editPanel.keyframeEditor.repaint();
            arrangeScrollbars();
        }

        private int getRequieredHeight()
        {
            int height = 25;
            for (AnimationElementTree aet : keyframePanel.animationElementTrees)
                height += aet.getHeight();
            return height;
        }
    }

    class EditPanel extends JPanel implements ActionListener
    {

        private JScrollBar hscrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 30, 0, 40);
        private KeyframeEditor keyframeEditor = new KeyframeEditor();
        private int currentFrame = -1;
        private LiveKeyframeProperty currentProperty;
        private JPopupMenu keyPopup = new JPopupMenu();
        private JMenuItem copyKeyItem = new JMenuItem("Copy", new ImageIcon("dat//img//menu//duplicate.png"));
        private JMenuItem deleteKeyItem = new JMenuItem("Delete", new ImageIcon("dat//img//menu//delete.png"));

        public EditPanel()
        {
            keyPopup.add(copyKeyItem);
            keyPopup.add(deleteKeyItem);
            deleteKeyItem.addActionListener(this);
            copyKeyItem.addActionListener(this);
            hscrollbar.addAdjustmentListener(new AdjustmentListener()
            {
                public void adjustmentValueChanged(AdjustmentEvent e)
                {
                    editPanel.keyframeEditor.xOffset = hscrollbar.getValue();
                    editPanel.keyframeEditor.repaint();
                    timelinePanel.repaint();
                }
            });
            setLayout(new BorderLayout(0, 0));
            add(keyframeEditor, BorderLayout.CENTER);
            add(hscrollbar, BorderLayout.SOUTH);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == deleteKeyItem)
            {
                if (currentFrame == 0)
                    JOptionPane.showMessageDialog(this, "There has to be a Startvalue!", "Error", JOptionPane.ERROR_MESSAGE);
                else if (currentProperty == null && keyframeEditor.mksd != null)
                    for (LiveKeyframeProperty lkp : keyframeEditor.mksd.properties)
                        for (int i = keyframeEditor.mksd.firstFrame; i <= keyframeEditor.mksd.lastFrame; i++)
                        {
                            System.out.println("frames between " + keyframeEditor.mksd.firstFrame + " and " + keyframeEditor.mksd.lastFrame);
                            if (lkp.getValues().length > i)
                                lkp.setValue(i, null);
                        }
                else if (currentProperty.numKeyframes() > 2)
                {
                    if (editPanel.keyframeEditor.mksd == null)
                        currentProperty.setValue(currentFrame, null);
                    else
                        for (LiveKeyframeProperty lkp : keyframeEditor.mksd.properties)
                            for (int i = keyframeEditor.mksd.firstFrame; i <= keyframeEditor.mksd.lastFrame; i++)
                                if (lkp.getValues().length > i)
                                    lkp.setValue(i, null);
                    keyframeEditor.repaint();
                } else
                    JOptionPane.showMessageDialog(this, "At least 2 Keyframes requiered!", "Error", JOptionPane.ERROR_MESSAGE);
            } else
            {
                int destination = -1;
                try
                {
                    destination = Integer.parseInt(JOptionPane.showInputDialog(this, "Copy to Frame:"));
                } catch (NumberFormatException nfe)
                {
                    JOptionPane.showMessageDialog(this, "Invalid Frame!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (destination < 0)
                {
                    JOptionPane.showMessageDialog(this, "Invalid Frame!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean valid = currentProperty.getValues().length <= destination;
                if (!valid)
                    valid = currentProperty.getValues()[destination] == null;
                if (!valid)
                    valid = JOptionPane.showConfirmDialog(this, "Overwrite Keyframe at " + destination + "?", "Overwrite Keyframe?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                if (valid)
                {
                    currentProperty.setValue(destination, currentProperty.getValues()[currentFrame]);
                    keyframeEditor.repaint();
                }
            }
            arrangeScrollbars();
        }

        class KeyframeEditor extends JPanel
        {

            private Color selectionColor = new Color(180, 220, 180, 80);
            private int dragX = 0;

            private class MultipleKeyframeSelectionData
            {

                private int xStart, xEnd = Integer.MIN_VALUE, yStart, yEnd, firstFrame, lastFrame, dragStart, dragEnd;
                private ArrayList<LiveKeyframeProperty> properties = new ArrayList<LiveKeyframeProperty>();
                private boolean active = false, ready = false, dragging = false;

                public MultipleKeyframeSelectionData(int xStart, int yStart)
                {
                    active = true;
                    this.xStart = xStart;
                    this.xEnd = xStart;
                    this.yStart = yStart;
                    this.yEnd = xStart;
                }

                public void update(int xE, int yE)
                {
                    xEnd = xE;
                    yEnd = yE;
                }

                public void done()
                {
                    ready = true;
                    active = false;

                    // Correct values

                    if (xStart > xEnd)
                    {
                        int temp = xStart;
                        xStart = xEnd;
                        xEnd = temp;
                    }

                    if (yStart > yEnd)
                    {
                        int temp = yStart;
                        yStart = yEnd;
                        yEnd = temp;
                    }

                    // Get all involved Properties
                    int frameCount = 0;
                    for (int i = yStart; i <= yEnd; i += 10)
                    {

                        Component comp = attributesPanel.getComponent(0).getComponentAt(66, i + keyframeEditor.yOffset);
                        AnimationElementTree.AttributeNode aNode = null;
                        AnimationElementTree aet = null;
                        if (comp instanceof AnimationElementTree)
                        {
                            aet = (AnimationElementTree) comp;
                            if (aet.getClosestPathForLocation(10, -aet.getLocation().y + i + keyframeEditor.yOffset).
                                    getLastPathComponent() instanceof AnimationElementTree.AttributeNode)
                                aNode = (AnimationElementTree.AttributeNode) aet.getClosestPathForLocation(
                                        10, -aet.getLocation().y + i + keyframeEditor.yOffset).getLastPathComponent();
                        }
                        if (aNode != null && aet != null)
                        {
                            LiveKeyframeProperty property = aNode.getProperty();
                            if (!properties.contains(property))
                                properties.add(property);
                        }
                    }

                    if (properties.size() > 0)
                    {
                        firstFrame = (int) ((xStart + keyframeEditor.xOffset) / timelinePanel.gapSize);
                        if (firstFrame < 1)
                            firstFrame = 1;
                        lastFrame = (int) ((xEnd + keyframeEditor.xOffset) / timelinePanel.gapSize);

                        // System.out.println("MULTISELECT: ");
                        // System.out.println("Keyframes from " + firstFrame + " to " + lastFrame);
                        // System.out.println("Involved Properties:");
                        for (LiveKeyframeProperty lkp : properties)
                            for (int i = firstFrame; i <= lastFrame; i++)
                                if (lkp.getValues().length > i && lkp.getValues()[i] != null)
                                    frameCount++;
                        if (frameCount < 2)
                        {
                            ready = false;
                            System.out.println("No multiple selection!");
                        }
                    } else
                        ready = false;

                }

                private boolean contains(LiveKeyframeProperty property, int frame)
                {
                    return frame >= firstFrame && frame <= lastFrame && properties.contains(property);
                }

                private void drag()
                {
                    int dragDifference = dragEnd - dragStart;
                    for (LiveKeyframeProperty property : properties)
                    {
                        Serializable[] copy = new Serializable[property.getValues().length + Math.abs(dragDifference)];
                        for (int i = 0; i < property.getValues().length; i++)
                            copy[i] = property.getValues()[i];
                        for (int i = (dragDifference > 0 ? (lastFrame >= property.getValues().length ? property.getValues().length - 1 : lastFrame) : firstFrame); (dragDifference > 0 ? i >= firstFrame : i <= (lastFrame > property.getValues().length ? property.getValues().length - 1 : lastFrame)); i += (dragDifference > 0 ? -1 : 1))
                            if (property.getValues()[i] != null)
                            {
                                copy[i + dragDifference] = property.getValues()[i];
                                copy[i] = null;
                            }
                        for (int i = 0; i < copy.length; i++)
                            property.setValue(i, copy[i]);
                    }
                    xStart += dragDifference;
                    xEnd += dragDifference;
                    firstFrame += dragDifference;
                    if (firstFrame < 1)
                        firstFrame = 1;
                    lastFrame += dragDifference;
                    if (lastFrame < 2)
                        lastFrame = 2;
                    dragging = false;
                    repaint();
                }
            }
            private boolean draggingSingle = false;
            private MultipleKeyframeSelectionData mksd = null;
            protected int dragStart = -1, xOffset = 0, yOffset = 0;
            private Serializable dragData = null;

            void select(int frame)
            {
                currentFrame = frame;
                timelinePanel.currentFrame = currentFrame;
                timelinePanel.repaint();
                if (currentProperty != null)
                    if (frame == -1)
                        valuePanel.deselect();
                    else
                    {
                        toolsPanel.currentFrameLabel.setText("Frame " + frame + " / " + (currentProperty.getValues().length - 1));
                        valuePanel.updateValues(currentProperty, frame);
                    }
                repaint();
            }

            public KeyframeEditor()
            {
                setDoubleBuffered(true);
                addMouseMotionListener(new MouseMotionAdapter()
                {
                    @Override
                    public void mouseDragged(MouseEvent e)
                    {
                        int selectedX = e.getX() + keyframeEditor.xOffset;

                        if (mksd != null && mksd.dragging)
                            repaint();
                        else if (editingEnabled && draggingSingle)
                        {
                            int cFrame = (int) (selectedX / timelinePanel.gapSize);
                            if (cFrame != dragStart)
                            {
                                if (cFrame < 0)
                                    cFrame = 0;
                                if (cFrame >= currentProperty.getValues().length || currentProperty.getValues()[cFrame] == null)
                                {
                                    currentProperty.setValue(dragStart, null);
                                    dragStart = cFrame;
                                    currentFrame = dragStart;
                                    valuePanel.setFrame(currentFrame);
                                    timelinePanel.currentFrame = currentFrame;
                                    currentProperty.setValue(dragStart, dragData);
                                    currentProperty.cutValues();
                                    toolsPanel.currentFrameLabel.setText("Frame " + currentFrame + " / " + (currentProperty.getValues().length - 1));
                                    timelinePanel.repaint();
                                    currentProperty.storeIndexes();
                                    repaint();
                                }
                            }
                        }
                        if (mksd != null && mksd.active)
                        {
                            mksd.update(e.getX(), e.getY());
                            repaint();
                        }
                    }
                });
                addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        arrangeScrollbars();
                        draggingSingle = false;
                        if (mksd != null)
                            if (mksd.dragging)
                            {
                                mksd.dragEnd = (int) ((e.getX() + keyframeEditor.xOffset) / timelinePanel.gapSize);
                                if (mksd.dragStart != mksd.dragEnd)
                                    mksd.drag();
                                else if (e.getButton() == MouseEvent.BUTTON1)
                                    mksd = null;
                            } else
                                mksd.done();
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        if (mksd != null)
                            mksd.dragging = false;
                        if (editingEnabled)
                        {
                            int selectedX = e.getX() + keyframeEditor.xOffset;
                            valuePanel.frame = -1;
                            int frame = (int) (selectedX / timelinePanel.gapSize);
                            Component comp = attributesPanel.getComponent(0).getComponentAt(66, e.getY() + keyframeEditor.yOffset);
                            AnimationElementTree.AttributeNode aNode = null;
                            AnimationElementTree aet = null;
                            if (comp instanceof AnimationElementTree)
                            {
                                aet = (AnimationElementTree) comp;
                                if (aet.getClosestPathForLocation(10, -aet.getLocation().y + e.getY() + keyframeEditor.yOffset).getLastPathComponent() instanceof AnimationElementTree.AttributeNode)
                                    aNode = (AnimationElementTree.AttributeNode) aet.getClosestPathForLocation(10, -aet.getLocation().y + e.getY() + keyframeEditor.yOffset).getLastPathComponent();
                            }
                            if (aNode != null && aet != null)
                            {
                                LiveKeyframeProperty property = aNode.getProperty();
                                LiveKeyframeUpdater updater = aet.getKeyframeUpdater();
                                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
                                    try
                                    {
                                        property.setValue(frame, updater.getLiveValue(property.type));
                                        arrangeScrollbars();
                                    } catch (Exception ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                else if (frame < aNode.getProperty().getValues().length && property.getValues()[frame] != null)
                                {
                                    if (e.getButton() == MouseEvent.BUTTON1)
                                    {
                                        if (draggingSingle || currentFrame != frame || currentProperty != property)
                                        {
                                            currentFrame = frame;
                                            currentProperty = property;
                                            select(currentFrame);
                                        }
                                    } /*else
                                     {
                                     selected = true;
                                     currentProperty = null;
                                     currentFrame = -1;
                                     select(currentFrame);
                                     }*/ else if (e.getButton() == MouseEvent.BUTTON3)
                                        keyPopup.show(KeyframeEditor.this, e.getX(), e.getY());
                                }
                                draggingSingle = false;
                                dragStart = -1;
                            }
                            if (currentProperty != null)
                                currentProperty.storeIndexes();
                            if (currentProperty != null // potential drag-start?
                                    //   && !selected
                                    && editingEnabled
                                    && currentFrame > 0
                                    && currentFrame < currentProperty.getValues().length
                                    && currentProperty.getIndices().contains(frame)
                                    && e.getButton() != MouseEvent.BUTTON3)
                            {
                                //  System.out.println("MKSD: " + mksd);
                                //  if (mksd != null)
                                //      System.out.println("MKSD READY: " + mksd.ready);
                                if (mksd == null || !mksd.ready || !mksd.contains(currentProperty, frame))
                                {
                                    draggingSingle = true;
                                    dragStart = currentFrame;
                                    dragData = currentProperty.getValues()[frame];
                                    mksd = null;
                                } else
                                {
                                    mksd.dragging = true;
                                    mksd.dragStart = frame;
                                    dragX = e.getX();
                                }
                            } else if (e.getButton() != MouseEvent.BUTTON3)// potential multi-select
                                mksd = new MultipleKeyframeSelectionData(e.getX(), e.getY());
                        }
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g0)
            {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                //Antialiasing ON
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int y = 25 - yOffset;
                int num = 1;
                AnimationElementTree aet;
                for (int i = 0; i < keyframePanel.animationElementTrees.size(); i++)
                {
                    aet = keyframePanel.animationElementTrees.get(i);
                    y += 25;
                    if (aet.isExpanded())
                        for (AnimationElementTree.AttributeNode an : aet.getAttributeNodes())
                        {
                            if (num++ % 2 > 0)
                                g.setColor(Color.darkGray);
                            else
                                g.setColor(Color.darkGray.darker());
                            g.fillRect(0, y - 25, getWidth(), 25);
                            for (int k = 0; k < an.getProperty().getValues().length; k++)
                                if (an.getProperty().getValues()[k] != null)
                                {
                                    if (k == 0)
                                        g.setColor(Color.red);
                                    else if ((mksd != null && mksd.ready && mksd.contains(an.getProperty(), k) // is it one of the selected?
                                            || (an.getProperty() == currentProperty && k == currentFrame))) // is it the selected?
                                        g.setColor(Color.cyan);
                                    else
                                        g.setColor(Color.orange);
                                    int radius = (int) (timelinePanel.gapSize > 25 ? 25 : timelinePanel.gapSize) / 4 * 3;
                                    if (radius < 7)
                                        radius = 7;
                                    g.fillOval(-xOffset + (int) (k * timelinePanel.gapSize + timelinePanel.gapSize / 2 - radius / 2),
                                            y - 12 - radius / 2,
                                            radius, radius);
                                }
                            y += 25;
                        }
                }
                g.setColor(Color.orange);
                int lineX = -xOffset + (int) (timelinePanel.currentFrame * timelinePanel.gapSize + timelinePanel.gapSize / 2) + 1;
                g.drawLine(lineX, 0, lineX, getHeight());
                if (!editingEnabled)
                {
                    g.setColor(new Color(200, 200, 200, 30));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                if (mksd != null)
                {
                    if (mksd.xEnd != Integer.MIN_VALUE && mksd.active)
                    {
                        int xStart = mksd.xStart, xEnd = mksd.xEnd, yStart = mksd.yStart, yEnd = mksd.yEnd;

                        if (xStart > xEnd)
                        {
                            int temp = xStart;
                            xStart = xEnd;
                            xEnd = temp;
                        }

                        if (yStart > yEnd)
                        {
                            int temp = yStart;
                            yStart = yEnd;
                            yEnd = temp;
                        }
                        g.setColor(selectionColor);
                        g.fillRect(xStart, yStart, xEnd - xStart, yEnd - yStart);
                        g.setColor(Color.ORANGE);
                        g.drawRect(xStart, yStart, xEnd - xStart, yEnd - yStart);
                    }
                    if (mksd.dragging && getMousePosition() != null)
                    {
                        g.setColor(Color.cyan);
                        g.drawLine(dragX, getMousePosition().y, getMousePosition().x, getMousePosition().y);
                    }
                }
            }
        }

        public KeyframeEditor getKeyframeEditor()
        {
            return keyframeEditor;
        }
    }

    public LiveKeyframeAnimation getCurrentAnimation()
    {
        return currentAnimation;
    }
}
