package gui.dialogs.keyframeAnimationEditor;

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
import java.awt.BasicStroke;
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
import monkeyStuff.keyframeAnimation.KeyframeAnimation;
import monkeyStuff.keyframeAnimation.KeyframeProperty;
import monkeyStuff.keyframeAnimation.KeyframeUpdater;
import monkeyStuff.keyframeAnimation.Updaters.AnimationType;
import org.jdesktop.swingx.VerticalLayout;
import other.Wizard;
import se.datadosen.component.RiverLayout;

/**
 *
 * @author David
 */
public class KeyframeAnimationFrame extends JFrame
{

    private Selector selector = new Selector();
    private ToolsPanel toolsPanel = new ToolsPanel();
    private ValuePanel valuePanel = new ValuePanel();
    private AttributesPanel attributesPanel = new AttributesPanel();
    private EditPanel editPanel = new EditPanel();
    private KeyframePanel keyframePanel = new KeyframePanel();
    private TimelinePanel timelinePanel = new TimelinePanel();
    private EditorPanel editorPanel = new EditorPanel();
    private static final int TIMELINE_HEIGHT = 40;
    private int minFrame = 0, zoom = 20;
    private boolean firstPaint = true;
    private KeyframeAnimation currentAnimation = null;

    public ValuePanel getValuePanel()
    {
        return valuePanel;
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
                super.componentResized(e); //To change body of generated methods, choose Tools | Templates.
                arrangeSizes();
                CurrentData.getPrefs().set(Preference.KEY_ANIMATION_DIALOG_SIZE, getSize());
                CurrentData.getPrefs().save();
            }
        });
        arrangeSizes();
    }

    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b); //To change body of generated methods, choose Tools | Templates.
        repaint();
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                arrangeSizes();
            }
        });
        setSize(getWidth() + 1, getHeight() + 1);
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
        toolsPanel.updateSlider();
        toolsPanel.repaint();
        editorPanel.repaint();
        keyframePanel.repaint();
        timelinePanel.repaint();
        attributesPanel.repaint();
        editPanel.repaint();
    }

    private void updateFrames()
    {
        timelinePanel.repaint();
        editPanel.keyframeEditor.repaint();
    }

    class ToolsPanel extends JPanel implements ActionListener
    {

        private BButton previousFrameButton = new BButton(new ImageIcon("dat//img//menu//keyframe//backward.png"), false);
        private BButton playButton = new BButton(new ImageIcon("dat//img//menu//keyframe//play.png"), false);
        private BButton nextFrameButton = new BButton(new ImageIcon("dat//img//menu//keyframe//forward.png"), false);
        private BButton stopButton = new BButton(new ImageIcon("dat//img//menu//keyframe//stop.png"), false);
        private BButton zoomInButton = new BButton(new ImageIcon("dat//img//menu//keyframe//plus.png"), false);
        private JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 3, 100, 30);
        private BButton zoomOutButton = new BButton(new ImageIcon("dat//img//menu//keyframe//minus.png"), false);
        private JLabel currentFrameLabel = new JLabel("Frame: " + selector.currentFrame);
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
            else if (e.getActionCommand().equals("new"))
            {
                String name = JOptionPane.showInputDialog(this, "Animation Name", "New Animation", JOptionPane.INFORMATION_MESSAGE);
                if (name != null && !"".equals(name))
                {
                    for (KeyframeAnimation k : Wizard.getKeyframeAnimations())
                        if (k.getName().equals(name))
                        {
                            JOptionPane.showMessageDialog(this, "Name already taken!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    KeyframeAnimation kfa = new KeyframeAnimation(name);
                    Wizard.getKeyframeAnimations().add(kfa);
                    animationSelector.addItem(kfa.getName());
                    currentAnimation = kfa;
                }
            } else if (e.getActionCommand().equals("prev"))
            {
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (e.getActionCommand().equals("next"))
            {
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (e.getActionCommand().equals("play"))
            {
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
                else
                {
                    currentAnimation.removeAllUpdaters();
                    valuePanel.setLive(false);
                    for (AnimationElementTree aet : keyframePanel.animationElementTrees)
                        currentAnimation.addUpdater(aet.getKeyframeUpdater().createNew());
                    currentAnimation.play();
                }
            } else if (e.getActionCommand().equals("stop"))
            {
                if (currentAnimation == null)
                    JOptionPane.showMessageDialog(playButton, "Select an Animation first!", "Error", JOptionPane.ERROR_MESSAGE);
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

        private KeyframeProperty property;
        private int frame = -1;
        private Checker liveValuesChecker = new Checker();
        private JComponent valueComponent;

        public ValuePanel()
        {
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setLayout(new RiverLayout(10, 10));
        }

        public void updateValues(KeyframeProperty currentProperty, int currentFrame)
        {
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
                add("hfill", new JLabel(property.type.toString(), SwingConstants.CENTER));
                add("br hfill", new JSeparator(JSeparator.HORIZONTAL));
                if (property.type == AnimationType.Translation || property.type == AnimationType.Scale)
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
                }
                add("br", new JLabel("Use Live-Value: "));
                add("tab", liveValuesChecker);
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
            if (property != null && valueComponent != null && liveValuesChecker.isChecked())
                if (property.getUpdater().getObject() instanceof Spatial)
                    if (property.type == AnimationType.Translation)
                    {
                        Vector3f newVec = new Vector3f(((Spatial) property.getUpdater().getObject()).getLocalTranslation());
                        ((Float3Panel) valueComponent).setVector(newVec);
                        property.setValue(frame, newVec);
                    } else if (property.type == AnimationType.Scale)
                    {
                        Vector3f newVec = new Vector3f(((Spatial) property.getUpdater().getObject()).getLocalScale());
                        ((Float3Panel) valueComponent).setVector(newVec);
                        property.setValue(frame, newVec);
                    } else if (property.type == AnimationType.Rotation)
                    {
                        Quaternion newQuat = new Quaternion(((Spatial) property.getUpdater().getObject()).getLocalRotation());
                        ((Float4Panel) valueComponent).setFloats(newQuat);
                        property.setValue(frame, newQuat);
                    }
        }

        private void setLive(boolean b)
        {
            liveValuesChecker.setChecked(b);
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
            private BButton deleteElementButton = new BButton(new ImageIcon("dat//img//menu//keyframe//delete.png"), false);
            private BComboBox<String> sortTypes = new BComboBox<String>(new String[]
            {
                "Chronological", "Start time", "Alphabetical", "Length"
            });

            public AttributesControlsPanel()
            {
                setLayout(new RiverLayout(0, 0));
                setBorder(new EmptyBorder(5, 0, 0, 0));
                add(sortTypes);
                add(addElementButton);
                add(deleteElementButton);
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
                }
            }
        }
    }

    class TimelinePanel extends JPanel
    {

        private double gapSize;
        int currentFrame = 0;

        public TimelinePanel()
        {
            addMouseMotionListener(new MouseMotionAdapter()
            {
                @Override
                public void mouseDragged(MouseEvent e)
                {
                    if (e.getX() >= 0)
                    {
                        currentFrame = (int) (e.getX() / timelinePanel.gapSize);
                        editPanel.keyframeEditor.select(currentFrame);
                        repaint();
                    }
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
            for (int i = minFrame; i < zoom + 1; i++)
            {
                x = i * gapSize;
                //System.out.println("New x: " + x);
                g.drawLine((int) x, 0, (int) x, getHeight());
                String frameNumber = "" + i;
                if (gapSize > 20)
                    g.drawString(frameNumber, (int) x + (int) gapSize / 2 - g.getFontMetrics().stringWidth(frameNumber) / 2, TIMELINE_HEIGHT / 2 - 7);
            }
            g.setColor(Color.orange);
            //  g.fillRect((int) (currentFrame * gapSize + gapSize / 5 * 2), TIMELINE_HEIGHT - 15, (int) gapSize / 5, 15);
            int[] xVals;

            if (gapSize <= 10)
                xVals = new int[]
                {
                    (int) (gapSize * currentFrame),
                    (int) (gapSize + gapSize * currentFrame),
                    (int) (gapSize + gapSize * currentFrame),
                    (int) (gapSize / 2 + gapSize * currentFrame),
                    (int) (gapSize * currentFrame)
                };
            else if (gapSize <= 20)
                xVals = new int[]
                {
                    (int) ((gapSize / 9 * 2) + (gapSize * currentFrame)),
                    (int) ((gapSize / 9 * 7) + (gapSize * currentFrame)),
                    (int) ((gapSize / 9 * 7) + (gapSize * currentFrame)),
                    (int) ((gapSize / 9 * 5) + (gapSize * currentFrame)),
                    (int) ((gapSize / 9 * 2) + (gapSize * currentFrame))
                };
            else if (gapSize <= 50)
                xVals = new int[]
                {
                    (int) ((gapSize / 8 * 3 + (gapSize * currentFrame))),
                    (int) ((gapSize / 8 * 5 + (gapSize * currentFrame))),
                    (int) ((gapSize / 8 * 5 + (gapSize * currentFrame))),
                    (int) ((gapSize / 8 * 4 + (gapSize * currentFrame))),
                    (int) ((gapSize / 8 * 3 + (gapSize * currentFrame)))
                };
            else
                xVals = new int[]
                {
                    (int) (gapSize / 2 - 8 + (gapSize * currentFrame)),
                    (int) (gapSize / 2 + 8 + (gapSize * currentFrame)),
                    (int) (gapSize / 2 + 8 + (gapSize * currentFrame)),
                    (int) (gapSize / 2 + (gapSize * currentFrame)),
                    (int) (gapSize / 2 - 8 + (gapSize * currentFrame))
                };

            g.setColor(Color.orange);
            g.fillPolygon(xVals,
                    new int[]
            {
                19, 19, 30, 36, 30
            }, 5);
            g.setColor(Color.black); // :)
            g.drawPolygon(xVals,
                    new int[]
            {
                19, 19, 30, 36, 30
            }, 5);
        }

        public int getCurrentFrame()
        {
            return currentFrame;
        }
    }

    class KeyframePanel extends JPanel
    {

        private JScrollBar vscrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 30, 0, 40);
        private ArrayList<B3D_Element> elements = new ArrayList<B3D_Element>();
        private ArrayList<AnimationElementTree> animationElementTrees = new ArrayList<AnimationElementTree>();

        public KeyframePanel()
        {
            setBackground(Color.GRAY);
            setLayout(new BorderLayout(0, 0));
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
            AnimationElementTree aet = new AnimationElementTree(e);
            animationElementTrees.add(aet);
            attributesPanel.add(aet);
            repaint();
            attributesPanel.repaint();
            revalidate();
        }
    }

    class AttributesPanel extends JPanel
    {

        public AttributesPanel()
        {
            setLayout(new VerticalLayout(0));
            setBackground(Color.GRAY);
        }

        public void updateAttributes()
        {
            editPanel.getKeyframeEditor().repaint();
        }
    }

    class EditPanel extends JPanel implements ActionListener
    {

        private JScrollBar hscrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 30, 0, 40);
        private KeyframeEditor keyframeEditor = new KeyframeEditor();
        private int currentFrame = -1;
        private KeyframeProperty currentProperty;
        private JPopupMenu keyPopup = new JPopupMenu();
        private JMenuItem duplicateItem = new JMenuItem("Copy Keyframe", new ImageIcon("dat//img//menu//duplicate.png"));
        private JMenuItem deleteKeyItem = new JMenuItem("Delete Keyframe", new ImageIcon("dat//img//menu//delete.png"));

        public EditPanel()
        {
            keyPopup.add(duplicateItem);
            keyPopup.add(deleteKeyItem);
            deleteKeyItem.addActionListener(this);
            setLayout(new BorderLayout(0, 0));
            add(keyframeEditor, BorderLayout.CENTER);
            add(hscrollbar, BorderLayout.SOUTH);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == deleteKeyItem)
            {
                JOptionPane.showMessageDialog(rootPane, "Deleted " + currentFrame);
                currentProperty.setValue(currentFrame, null);
                keyframeEditor.repaint();
            }
        }

        class KeyframeEditor extends JPanel
        {

            private boolean dragging = false;
            private int dragStart = -1;
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
                        if (dragging)
                        {
                            System.out.println("Dragging!");
                            int cFrame = (int) (e.getX() / timelinePanel.gapSize);
                            if (cFrame != dragStart
                                    && (cFrame >= currentProperty.getValues().length || currentProperty.getValues()[cFrame] == null))
                            {
                                currentProperty.setValue(dragStart, null);
                                dragStart = cFrame;
                                currentFrame = dragStart;
                                currentProperty.setValue(dragStart, dragData);
                                toolsPanel.currentFrameLabel.setText("Frame " + currentFrame + " / " + (currentProperty.getValues().length - 1));
                                repaint();
                            }
                        }
                    }
                });
                addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        if (currentFrame != -1 && currentProperty != null)
                        {
                            dragging = true;
                            dragStart = currentFrame;
                            dragData = currentProperty.getValues()[currentFrame];
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        dragging = false;
                        dragStart = -1;
                        int frame = (int) (e.getX() / timelinePanel.gapSize);
                        Component comp = attributesPanel.getComponentAt(66, e.getY());
                        AnimationElementTree.AttributeNode aNode = null;
                        AnimationElementTree aet = null;
                        if (comp instanceof AnimationElementTree)
                        {
                            aet = (AnimationElementTree) comp;
                            if (aet.getClosestPathForLocation(10, e.getY()).getLastPathComponent() instanceof AnimationElementTree.AttributeNode)
                                aNode = (AnimationElementTree.AttributeNode) aet.getClosestPathForLocation(10, e.getY()).getLastPathComponent();
                        }
                        if (aNode != null && aet != null)
                        {
                            KeyframeProperty property = aNode.getProperty();
                            System.out.println("Property: " + property + " - CurrentProperty: " + currentProperty);
                            KeyframeUpdater updater = aet.getKeyframeUpdater();
                            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
                                try
                                {
                                    property.setValue(frame, updater.getLiveValue(property.type));
                                } catch (Exception ex)
                                {
                                    JOptionPane.showMessageDialog(KeyframeEditor.this, "Out of Bounds? " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            else if (frame < aNode.getProperty().getValues().length && property.getValues()[frame] != null)
                            {
                                if (e.getButton() == MouseEvent.BUTTON1)
                                    if (currentFrame != frame || currentProperty != property)
                                    {
                                        currentFrame = frame;
                                        currentProperty = property;
                                        select(currentFrame);
                                    } else
                                    {
                                        currentProperty = null;
                                        currentFrame = -1;
                                        select(currentFrame);
                                    }
                                else if (e.getButton() == MouseEvent.BUTTON3)
                                    keyPopup.show(KeyframeEditor.this, e.getX(), e.getY());
                            }
                            repaint();
                        }
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
                int y = 25;
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
                            {
                                if (an.getProperty().getValues()[k] != null)
                                {
                                    if (an.getProperty() == currentProperty && k == currentFrame)
                                        g.setColor(Color.cyan);
                                    else
                                        g.setColor(Color.orange);
                                    int radius = (int) (timelinePanel.gapSize > 25 ? 25 : timelinePanel.gapSize) / 4 * 3;
                                    g.fillOval((int) (k * timelinePanel.gapSize + timelinePanel.gapSize / 2 - radius / 2),
                                            y - 12 - radius / 2,
                                            radius, radius);
                                }
                            }
                            y += 25;
                        }
                }
                g.setColor(Color.orange);
                g.drawLine((int) (timelinePanel.currentFrame * timelinePanel.gapSize + timelinePanel.gapSize / 2) + 1, 0, (int) (timelinePanel.currentFrame * timelinePanel.gapSize + timelinePanel.gapSize / 2 + 1), getHeight());

            }
        }

        public KeyframeEditor getKeyframeEditor()
        {
            return keyframeEditor;
        }
    }

    class Selector
    {

        int currentFrame = 0;
    }
}
