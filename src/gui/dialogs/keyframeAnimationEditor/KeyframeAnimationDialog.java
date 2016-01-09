package gui.dialogs.keyframeAnimationEditor;

import b3dElements.B3D_Element;
import components.BButton;
import components.BComboBox;
import components.BToggleButton;
import general.CurrentData;
import general.Preference;
import gui.dialogs.SelectElementDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import monkeyStuff.keyframeAnimation.KeyframeAnimation;
import other.Wizard;
import se.datadosen.component.RiverLayout;

/**
 *
 * @author David
 */
public class KeyframeAnimationDialog extends JFrame
{

    private Selector selector = new Selector();
    private ToolsPanel toolsPanel = new ToolsPanel();
    private AttributesPanel attributesPanel = new AttributesPanel();
    private EditPanel editPanel = new EditPanel();
    private KeyframePanel keyframePanel = new KeyframePanel();
    private TimelinePanel timelinePanel = new TimelinePanel();
    private EditorPanel editorPanel = new EditorPanel();
    private static final int TIMELINE_HEIGHT = 45;
    private int minFrame = 0, zoom = 20;
    private boolean firstPaint = true;
    private KeyframeAnimation currentAnimation = null;

    public KeyframeAnimationDialog()
    {
        setType(Type.UTILITY);
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
        setAlwaysOnTop((Boolean) CurrentData.getPrefs().get(Preference.KEY_ANIMATION_EDITOR_SHOWN));
        setLayout(new BorderLayout(0, 0));
        add(toolsPanel, BorderLayout.NORTH);
        add(editorPanel, BorderLayout.CENTER);
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
        toolsPanel.setPreferredSize(new Dimension(KeyframeAnimationDialog.this.getWidth(), 26));
        editorPanel.setPreferredSize(new Dimension(KeyframeAnimationDialog.this.getWidth(), getHeight() - toolsPanel.getHeight()));
        keyframePanel.setPreferredSize(new Dimension(editorPanel.getWidth(), editorPanel.getHeight() - TIMELINE_HEIGHT));
        timelinePanel.setPreferredSize(new Dimension(editorPanel.getWidth() - 500, TIMELINE_HEIGHT));
        attributesPanel.setPreferredSize(new Dimension(500, 200));
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
        private JLabel maxFramesLabel = new JLabel("Number of Frames: " + 200);
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
                        KeyframeAnimationDialog.this.setSize(KeyframeAnimationDialog.this.getWidth() + 1, KeyframeAnimationDialog.this.getHeight() + 1);
                    }
                    if (dont) // elegant af
                    {
                        dont = false;
                        return;
                    }
                    zoom = zoomSlider.getValue() * timelinePanel.getWidth() / 1000;
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
            add(new JLabel("                  ")); //elegant af
            add("tab", new JLabel("Select:   "));
            add(animationSelector);
            add(newAnimationButton);
            add(deleteAnimationButton);
            add(new JLabel("                  ")); //elegant af
            add(previousFrameButton);
            add(playButton);
            add(nextFrameButton);
            add(stopButton);
            add(new JLabel("             ")); //elegant af
            add(zoomInButton);
            add(zoomSlider);
            add(zoomOutButton);
            add(new JLabel("                ")); //elegant af
            add(currentFrameLabel);
            add(new JLabel("          ")); //elegant af
            add(maxFramesLabel);
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

    class ValuePanel extends JPanel
    {

        public ValuePanel()
        {
            setBackground(Color.RED);
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
                add(new JLabel("            ")); //elegant af
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
                    SelectElementDialog sed = new SelectElementDialog(KeyframeAnimationDialog.this.getLocation());
                    if (sed.getSelectedElement() != null)
                        keyframePanel.addElement(sed.getSelectedElement());
                    setAlwaysOnTop((Boolean) CurrentData.getPrefs().get(Preference.KEY_ANIMATION_EDITOR_ON_TOP));
                }
            }
        }
    }

    class TimelinePanel extends JPanel
    {

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
            double gapSize = (double) getWidth() / zoom;
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
            KeyframePanel.this.arrange();
        }

        private void arrange()
        {
            for (B3D_Element e : elements)
            {
                AnimationElementTree aet = new AnimationElementTree(e);
                animationElementTrees.add(aet);
                attributesPanel.add(aet);
                currentAnimation.addUpdater(aet.getKeyframeUpdater());
            }
            repaint();
            attributesPanel.repaint();
            revalidate();
        }
    }

    class AttributesPanel extends JPanel
    {

        public AttributesPanel()
        {
            setLayout(new BorderLayout(0, 0));
            setBackground(Color.GRAY);
        }
    }

    class EditPanel extends JPanel
    {

        private JScrollBar hscrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 30, 0, 40);

        public EditPanel()
        {
            setLayout(new BorderLayout(0, 0));
            add(hscrollbar, BorderLayout.SOUTH);
        }
    }

    class Selector
    {

        int currentFrame = 0;
    }
}
