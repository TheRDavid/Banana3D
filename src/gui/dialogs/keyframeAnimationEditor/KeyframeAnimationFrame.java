package gui.dialogs.keyframeAnimationEditor;

import b3dElements.B3D_Element;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import components.BButton;
import components.BComboBox;
import components.BToggleButton;
import components.Float3Panel;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private static final int TIMELINE_HEIGHT = 45;
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

    class ValuePanel extends JPanel implements ActionListener
    {

        private KeyframeProperty property;
        private int frame;
        private BButton refreshButton = new BButton("Refresh", new ImageIcon("dat//img//menu//keyframe//refresh.png"));
        private JComponent valueComponent;

        public ValuePanel()
        {
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setLayout(new RiverLayout(10, 10));
        }

        public void updateValues(KeyframeProperty currentProperty, int currentFrame)
        {
            property = currentProperty;
            frame = currentFrame;
            removeAll();
            add("hfill", new JLabel(property.type.toString(), SwingConstants.CENTER));
            add("br hfill", new JSeparator(JSeparator.HORIZONTAL));
            if (property.type == AnimationType.Translation || property.type == AnimationType.Scale)
            {
                //Index 2
                valueComponent = new Float3Panel((Vector3f) property.getValues()[frame],
                        CurrentData.getEditorWindow().getB3DApp().getCamera(),
                        Float3Panel.VERTICAL);
                add("br hfill", valueComponent);
            }
            add("br", refreshButton);
            refreshButton.addActionListener(this);
            repaint();
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == refreshButton)
                refreshValues();
        }

        private void refreshValues()
        {
            if (property.getUpdater().getObject() instanceof Spatial && property.type == AnimationType.Translation)
            {
                Vector3f newVec = ((Spatial) property.getUpdater().getObject()).getLocalTranslation();
                ((Float3Panel) valueComponent).setVector(newVec);
                property.setValue(frame, newVec);
            }
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
                add(new JLabel("            ")); //elegant af
                add("br", sortTypes);
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

        private double gapSize, currentStart = 0;

        public TimelinePanel()
        {
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
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
                if (gapSize > 20)
                    g.drawString("" + i, (int) x + (int) gapSize / 2 - 5, TIMELINE_HEIGHT / 2 - 5);
            }
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

    class EditPanel extends JPanel
    {

        private JScrollBar hscrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 30, 0, 40);
        private KeyframeEditor keyframeEditor = new KeyframeEditor();
        private int currentFrame;
        private KeyframeProperty currentProperty;

        public EditPanel()
        {
            setLayout(new BorderLayout(0, 0));
            add(keyframeEditor, BorderLayout.CENTER);
            add(hscrollbar, BorderLayout.SOUTH);
        }

        class KeyframeEditor extends JPanel
        {

            public KeyframeEditor()
            {
                setDoubleBuffered(true);
                addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        int frame = (int) (e.getX() / timelinePanel.gapSize);
                        Component comp = attributesPanel.getComponentAt(66, e.getY());
                        AnimationElementTree.AttributeNode aNode = null;
                        AnimationElementTree aet = null;
                        if (comp instanceof AnimationElementTree)
                        {
                            // toolsPanel.currentFrameLabel.setText("Tree");
                            aet = (AnimationElementTree) comp;
                            if (aet.getClosestPathForLocation(10, e.getY()).getLastPathComponent() instanceof AnimationElementTree.AttributeNode)
                            {
                                aNode = (AnimationElementTree.AttributeNode) aet.getClosestPathForLocation(10, e.getY()).getLastPathComponent();
                                /*    toolsPanel.currentFrameLabel.setText("Frame: "
                                 + frame
                                 + " "
                                 + aet.getElement().getName()
                                 + ":"
                                 + aNode.getUserObject().toString() + " (" + aNode.getProperty().type + ")");*/
                            }
                        }
                        if (aNode != null && aet != null)
                        {
                            KeyframeProperty property = aNode.getProperty();
                            KeyframeUpdater updater = aet.getKeyframeUpdater();
                            if (e.getClickCount() == 2)
                            {
                                try
                                {
                                    property.setValue(frame, updater.getLiveValue(property.type));
                                } catch (Exception ex)
                                {
                                    JOptionPane.showMessageDialog(KeyframeEditor.this, "Out of Bounds? " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } else if (e.getButton() == MouseEvent.BUTTON1 && frame < aNode.getProperty().getValues().length && property.getValues()[frame] != null)
                            {
                                currentFrame = frame;
                                currentProperty = property;
                                toolsPanel.currentFrameLabel.setText("Frame " + frame + " / " + (currentProperty.getValues().length - 1));
                                valuePanel.updateValues(currentProperty, currentFrame);
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
