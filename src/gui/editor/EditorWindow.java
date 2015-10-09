package gui.editor;

import gui.elementTree.ElementTree;
import gui.components.SearchTextField;
import gui.dialogs.AdditionalCameraDialog;
import gui.dialogs.FieldOfViewDialog;
import gui.menu.MainMenu;
import general.CurrentData;
import monkeyStuff.B3DApp;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import components.BButton;
import components.StatusBar;
import dialogs.ObserverDialog;
import other.B3D_Scene;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.Vector;
import javax.swing.*;
import org.jdesktop.swingx.VerticalLayout;
import other.Wizard;

public class EditorWindow extends JFrame
{

    private MainMenu editorMenu = new MainMenu();
    private EditPane editPane = new EditPane();
    private JPanel treePanel = new JPanel(new VerticalLayout(0));
    private JScrollPane treeScrollPane = new JScrollPane();
    private ElementTree tree;
    private PlayPanel playPanel = new PlayPanel();
    private JPanel middlePanel = new JPanel(new VerticalLayout());
    private ControlToolBar toolbar = new ControlToolBar();
    private B3DApp b3DSimpleApplication;
    private JmeCanvasContext canvasContext;
    private JPanel upperPanel = new JPanel();
    private BButton fieldOfViewButton = new BButton("FOV", new ImageIcon("dat//img//menu//fieldOfView.png")), addCamButton = new BButton("Add Camera", new ImageIcon("dat//img//menu//camera.png"));
    private JComboBox sortModeComboBox = new JComboBox(new String[]
    {
        "A - Z", "Z - A", "A - Z (case sensitive)", "Z - A (case sensitive)"
    });

    /**
     * Initializing of components, setting this as EditorWindow in CurrentData,
     * creating listeners and setting up the Window.
     */
    public EditorWindow()
    {
        initShortcuts();
        sortModeComboBox.setFont(new Font("Tahoma", Font.PLAIN, 13));
        CurrentData.setEditorWindow(this);
        tree = new ElementTree();
        tree.init();
        fieldOfViewButton.setEnabled(false);
        addCamButton.setEnabled(false);
        if (CurrentData.getConfiguration().treesort.equals("z-a(no_cs)"))
        {
            sortModeComboBox.setSelectedIndex(1);
        } else if (CurrentData.getConfiguration().treesort.equals("a-z(cs)"))
        {
            sortModeComboBox.setSelectedIndex(2);
        } else if (CurrentData.getConfiguration().treesort.equals("z-a(cs)"))
        {
            sortModeComboBox.setSelectedIndex(2);
        }
        sortModeComboBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                switch (sortModeComboBox.getSelectedIndex())
                {
                    case 0:
                        CurrentData.getConfiguration().setTreesort("a-z(no_cs)");
                        break;
                    case 1:
                        CurrentData.getConfiguration().setTreesort("z-a(no_cs)");
                        break;
                    case 2:
                        CurrentData.getConfiguration().setTreesort("a-z(cs)");
                        break;
                    case 3:
                        CurrentData.getConfiguration().setTreesort("z-a(cs)");
                        break;
                }
                tree.sync();
            }
        });
        addCamButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new AdditionalCameraDialog(
                        b3DSimpleApplication.getCamera().getLocation(),
                        b3DSimpleApplication.getCamera().getRotation(),
                        640,
                        480,
                        b3DSimpleApplication.getRenderer(),
                        b3DSimpleApplication.getRenderManager(),
                        b3DSimpleApplication.getRootNode(),
                        b3DSimpleApplication.getAssetManager(),
                        true);
            }
        });
        fieldOfViewButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new FieldOfViewDialog();
            }
        });
        setJMenuBar(editorMenu);
        if (CurrentData.getConfiguration().fullscreen)
        {
            setUndecorated(true);
            setSize(Toolkit.getDefaultToolkit().getScreenSize());
            setLocation(0, 0);
        } else
        {
            setSize(CurrentData.getConfiguration().editorWidth, CurrentData.getConfiguration().editorHeight);
            setLocationRelativeTo(null);
        }
        addComponentListener(new ComponentListener()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                arrangeComponentSizes();
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                arrangeComponentSizes();
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
            }

            @Override
            public void componentHidden(ComponentEvent e)
            {
            }
        });
        addWindowStateListener(new WindowStateListener()
        {
            @Override
            public void windowStateChanged(WindowEvent e)
            {
                arrangeComponentSizes();
            }
        });
        addWindowListener(new WindowListener()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {
                arrangeComponentSizes();
            }

            @Override
            public void windowClosing(WindowEvent e)
            {
                CurrentData.execQuit();
            }

            @Override
            public void windowClosed(WindowEvent e)
            {
            }

            @Override
            public void windowIconified(WindowEvent e)
            {
            }

            @Override
            public void windowDeiconified(WindowEvent e)
            {
            }

            @Override
            public void windowActivated(WindowEvent e)
            {
            }

            @Override
            public void windowDeactivated(WindowEvent e)
            {
            }
        });
        setIconImage(new ImageIcon("dat//img//other//logo.png").getImage());
        setTitle("Banana3D");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        add(editPane, BorderLayout.WEST);
        middlePanel.add(upperPanel);
        upperPanel.setLayout(new BorderLayout());
        upperPanel.add(addCamButton, BorderLayout.WEST);
        upperPanel.add(fieldOfViewButton, BorderLayout.CENTER);
        middlePanel.add(playPanel);
        middlePanel.add(toolbar);
        add(middlePanel, BorderLayout.CENTER);
        treePanel.add(sortModeComboBox);
        treePanel.add(new SearchTextField(tree));
        treePanel.add(treeScrollPane);
        treeScrollPane.setViewportView(tree);
        treeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        treeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        treeScrollPane.setPreferredSize(new Dimension(320, getHeight() - 80));
        add(treePanel, BorderLayout.EAST);
        add(StatusBar.getStatusBar(), BorderLayout.SOUTH);
        //Time to let the user admire the fabulous logo
        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException ex)
        {
            ObserverDialog.getObserverDialog().printError("Thread.sleep in SplashDialog (CurrentData) interrupted", ex);
        }
        CurrentData.getSplashDialog().dispose();
        //Needs to be called after every resize
        arrangeComponentSizes();
        playPanel.setPlayIcon(Wizard.resizeImage(new ImageIcon("dat//img//other//playPic2.png").getImage(), getWidth(), getHeight(), true));
        playPanel.setPlayIcon2(Wizard.resizeImage(new ImageIcon("dat//img//other//playPic1.png").getImage(), getWidth(), getHeight(), true));
        setVisible(true);
    }

    /**
     * Should be called after every resize. Arranges the components of the
     * Editor Window so the Tree and EditPanel have their minimum size
     * necessary.
     */
    public void arrangeComponentSizes()
    {
        editPane.setPreferredSize(new Dimension(530, getHeight() - 20));
        upperPanel.setPreferredSize(new Dimension(180, 30));
        treeScrollPane.setPreferredSize(new Dimension(320, getHeight() - 100));
        if (isUndecorated())
            middlePanel.setPreferredSize(new Dimension(getContentPane().getWidth() - tree.getPreferredSize().width - editPane.getPreferredSize().width, getHeight() - 60));
        else
            middlePanel.setPreferredSize(new Dimension(getWidth() - tree.getPreferredSize().width - editPane.getPreferredSize().width, getHeight() - 110));
        toolbar.setPreferredSize(new Dimension(middlePanel.getWidth(), 110));
        playPanel.setPreferredSize(new Dimension(
                middlePanel.getPreferredSize().width,
                middlePanel.getPreferredSize().height - upperPanel.getPreferredSize().height - toolbar.getPreferredSize().height));
        if (canvasContext != null)
        {
            b3DSimpleApplication.restart();
            canvasContext.getCanvas().setPreferredSize(playPanel.getPreferredSize());
        }
    }

    /**
     * This Panel either contains the 3D-Canvas or the welcome image /
     * animation.
     */
    public class PlayPanel extends JPanel
    {

        private ImageIcon playIcon;
        private ImageIcon playIcon2;
        private Point mousePosition = new Point();
        private double pSize = 60;
        private boolean seriousNow = false;
        private Vector<TranspEllipse> ellipses = new Vector<TranspEllipse>();
        private boolean inc = false;
        private float weakning = .005f;
        private boolean strong = false;

        public PlayPanel()
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (!seriousNow)
                    {
                        try
                        {
                            Thread.sleep(10);
                        } catch (InterruptedException ex)
                        {
                            ObserverDialog.getObserverDialog().printError("Thread.sleep in EditorWindow interrupted", ex);
                        }
                        PlayPanel.this.repaint();
                    }
                    playIcon = null;
                    playIcon2 = null;
                    mousePosition = null;
                    ellipses = null;
                }
            }).start();
            addMouseMotionListener(new MouseMotionListener()
            {
                @Override
                public void mouseDragged(MouseEvent e)
                {
                    if (weakning > .0008f)
                    {
                        weakning -= .00002f;
                        strong = true;
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e)
                {
                    mousePosition = e.getPoint();
                    repaint();
                }
            });
        }
        private float duration = 50;
        private float transition = 3000;
        private boolean transitionDone = false;
        private Graphics2D g2d;
        private AlphaComposite ac;

        @Override
        public void paint(Graphics g)
        {
            if (duration < 0)
            {
                transitionDone = false;
                duration = Math.abs(transition) / 6;
            } else
            {
                duration--;
            }
            strong = false;
            super.paint(g);
            g2d = (Graphics2D) g;
            if (!seriousNow)
            {
                g.drawImage(playIcon.getImage(), getWidth() / 2 - playIcon.getIconWidth() / 2, getHeight() / 2 - playIcon.getIconHeight() / 2, playIcon.getIconWidth(), playIcon.getIconHeight(), null);
                for (TranspEllipse transpEllipse : ellipses)
                {
                    if (transpEllipse.alpha > 0)
                    {
                        ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, transpEllipse.alpha);
                        g2d.setComposite(ac);
                        g.setClip(transpEllipse.ellipse2D);
                        g.drawImage(playIcon2.getImage(), getWidth() / 2 - playIcon.getIconWidth() / 2, getHeight() / 2 - playIcon.getIconHeight() / 2, playIcon.getIconWidth(), playIcon.getIconHeight(), null);
                        transpEllipse.reduce();
                    }
                }
                ellipses.add(new TranspEllipse(new Ellipse2D.Double(mousePosition.x - pSize, mousePosition.y - pSize, pSize * 2, pSize * 2)));
            }
        }

        private class TranspEllipse
        {

            private float alpha = .1f;
            private Ellipse2D.Double ellipse2D;
            private double diff;

            public TranspEllipse(Ellipse2D.Double e2dd)
            {
                ellipse2D = e2dd;
            }

            private void reduce()
            {
                if (strong && !(alpha + weakning * 3 > 1))
                {
                    alpha += weakning * 3;
                } else
                {
                    alpha -= weakning / 2;
                }
                diff = Math.random();
                diff -= (diff - .8f) * 3;
                if (transition < 0)
                {
                    transitionDone = true;
                    transition = duration * 6;
                }
                if (transitionDone)
                {
                    ellipse2D.setFrame(ellipse2D.x + diff, ellipse2D.y - 3, ellipse2D.width, ellipse2D.height);
                } else
                {
                    ellipse2D.setFrame(ellipse2D.x + diff / transition, ellipse2D.y - 3, ellipse2D.width, ellipse2D.height);
                    transition--;
                }
            }
        }

        public ImageIcon getPlayIcon()
        {
            return playIcon;
        }

        public void setPlayIcon(ImageIcon playIcon)
        {
            this.playIcon = playIcon;
        }

        public ImageIcon getPlayIcon2()
        {
            return playIcon2;
        }

        public void setPlayIcon2(ImageIcon playIcon2)
        {
            this.playIcon2 = playIcon2;
        }
    }

    /**
     * Initializes the 3D-Context
     *
     * @param world
     */
    public void initNewScene(B3D_Scene world)
    {
        playPanel.seriousNow = true;
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(CurrentData.getConfiguration().framerate);
        settings.setRenderer(AppSettings.LWJGL_OPENGL3);
        settings.setVSync(CurrentData.getConfiguration().vSync);
        settings.setBitsPerPixel(CurrentData.getConfiguration().colorDepth);
        settings.setResolution(playPanel.getWidth(), playPanel.getHeight());
        settings.setSamples(CurrentData.getConfiguration().mutlisampling);
        settings.setDepthBits(CurrentData.getConfiguration().depthBits);
        b3DSimpleApplication = new B3DApp(1000, world);
        b3DSimpleApplication.setSettings(settings);
        b3DSimpleApplication.createCanvas();
        b3DSimpleApplication.startCanvas();
        canvasContext = (JmeCanvasContext) b3DSimpleApplication.getContext();
        canvasContext.getCanvas().setPreferredSize(playPanel.getSize());
        canvasContext.setSystemListener(b3DSimpleApplication);
        playPanel.add(canvasContext.getCanvas());
        editorMenu.setDisabled(false);
        tree.setEnabled(true);
        toolbar.setEnabled(true);
        repaint();
        validate();
    }

    /**
     * Since all shortcuts should work, whether Swing catches them or the
     * B3DApp, the global KeyboardFocusManager will listen to the commands and
     * call their respective methods.
     */
    private void initShortcuts()
    {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher()
        {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e)
            {
                boolean ret = false;
                if (e.getID() == KeyEvent.KEY_PRESSED)
                {
                    if ((e.getKeyCode() == KeyEvent.VK_Q) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execQuit();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_N) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execNewScene();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_R) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execRename();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_F7))
                    {
                        if (b3DSimpleApplication != null)
                        {
                            CurrentData.execFullscreen();
                        }
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_O) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execOpenProject(null);
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execSaveScene(CurrentData.getProject().getMainFolder().getAbsolutePath() + "/" + CurrentData.getProject().getScene().getName() + ".b3ds");
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_T) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execCreateTwin();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_F) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execFind();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_L) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execLookAt();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_M) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execPlayPauseMotionPath();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_DELETE) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execDelete();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_F6))
                    {
                        CurrentData.setPhysicsRunning(!b3DSimpleApplication.isPhysicsPlaying());
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_F1))
                    {
                        CurrentData.execScreenshot();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_F2))
                    {
                        CurrentData.execRecord();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_F3))
                    {
                        CurrentData.exec9gag();
                        ret = true;
                    }
                }
                return ret;
            }
        });
    }

    public MainMenu getMainMenu()
    {
        return editorMenu;
    }

    public B3DApp getB3DApp()
    {
        return b3DSimpleApplication;
    }

    /**
     *
     * @param b3DSimpleApplication
     */
    public void setB3DSimpleApplication(B3DApp b3DSimpleApplication)
    {
        this.b3DSimpleApplication = b3DSimpleApplication;
    }

    public ElementTree getTree()
    {
        return tree;
    }

    public EditPane getEditPane()
    {
        return editPane;
    }

    public PlayPanel getPlayPanel()
    {
        return playPanel;
    }

    public BButton getFieldOfViewButton()
    {
        return fieldOfViewButton;
    }

    public ControlToolBar getToolbar()
    {
        return toolbar;
    }

    public BButton getAddCamButton()
    {
        return addCamButton;
    }
}
