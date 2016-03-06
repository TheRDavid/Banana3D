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
import components.BToggleButton;
import components.StatusBar;
import dialogs.ObserverDialog;
import general.Preference;
import general.UAManager;
import gui.dialogs.keyframeAnimationEditor.KeyframeAnimationFrame;
import other.B3D_Scene;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Callable;
import javax.swing.*;
import monkeyStuff.NodeModel;
import org.jdesktop.swingx.VerticalLayout;

public class EditorWindow extends JFrame
{

    private MainMenu editorMenu = new MainMenu();
    private EditPane editPane = new EditPane();
    private JPanel treePanel = new JPanel(new VerticalLayout(0));
    private JScrollPane treeScrollPane = new JScrollPane();
    private ElementTree tree;
    private JPanel canvasPanel = new JPanel();
    private JPanel middlePanel = new JPanel(new VerticalLayout());
    private ControlToolBar toolbar = new ControlToolBar();
    private B3DApp b3DSimpleApplication;
    private JmeCanvasContext canvasContext;
    private JPanel upperPanel = new JPanel();
    private KeyframeAnimationFrame kfad;
    private BButton fieldOfViewButton = new BButton("FOV", new ImageIcon("dat//img//menu//fieldOfView.png")), addCamButton = new BButton("Add Camera", new ImageIcon("dat//img//menu//camera.png"));
    private BToggleButton showNodeHierarchyButton = new BToggleButton(new ImageIcon("dat//img//menu//c_node.png"), true);
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
        kfad = new KeyframeAnimationFrame();
        initShortcuts();
        sortModeComboBox.setFont(new Font("Tahoma", Font.PLAIN, 13));
        CurrentData.setEditorWindow(this);
        tree = new ElementTree();
        tree.init();
        fieldOfViewButton.setEnabled(false);
        showNodeHierarchyButton.setSelected((Boolean) CurrentData.getPrefs().get(Preference.SHOW_NODE_HIERARCHY));
        addCamButton.setEnabled(false);
        if (CurrentData.getPrefs().get(Preference.TREESORT).equals("z-a(no_cs)"))
            sortModeComboBox.setSelectedIndex(1);
        else if (CurrentData.getPrefs().get(Preference.TREESORT).equals("a-z(cs)"))
            sortModeComboBox.setSelectedIndex(2);
        else if (CurrentData.getPrefs().get(Preference.TREESORT).equals("z-a(cs)"))
            sortModeComboBox.setSelectedIndex(2);
        sortModeComboBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                switch (sortModeComboBox.getSelectedIndex())
                {
                    case 0:
                        CurrentData.getPrefs().set(Preference.TREESORT, "a-z(no_cs)");
                        break;
                    case 1:
                        CurrentData.getPrefs().set(Preference.TREESORT, "z-a(no_cs)");
                        break;
                    case 2:
                        CurrentData.getPrefs().set(Preference.TREESORT, "a-z(cs)");
                        break;
                    case 3:
                        CurrentData.getPrefs().set(Preference.TREESORT, "z-a(cs)");
                        break;
                }
                tree.sync();
            }
        });
        showNodeHierarchyButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getPrefs().set(Preference.SHOW_NODE_HIERARCHY, showNodeHierarchyButton.isSelected());
                CurrentData.getPrefs().save();
                b3DSimpleApplication.enqueue(new Callable<Void>()
                {
                    public Void call() throws Exception
                    {
                        for (NodeModel nm : b3DSimpleApplication.getNodeModels())
                            if (showNodeHierarchyButton.isSelected())
                                b3DSimpleApplication.getEditorNode().attachChild(nm.getModel());
                            else
                                b3DSimpleApplication.getEditorNode().detachChild(nm.getModel());
                        return null;
                    }
                });
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
        if ((Boolean) CurrentData.getPrefs().get(Preference.FULLSCREEN))
        {
            setUndecorated(true);
            setSize(Toolkit.getDefaultToolkit().getScreenSize());
            setLocation(0, 0);
        } else
        {
            setSize((Dimension) CurrentData.getPrefs().get(Preference.EDITOR_WINDOW_SIZE));
            if (CurrentData.getPrefs().get(Preference.EDITOR_WINDOW_LOCATION) == null)
                setLocationRelativeTo(null);
            else
                setLocation((Point) CurrentData.getPrefs().get(Preference.EDITOR_WINDOW_LOCATION));
        }
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                arrangeComponentSizes();
                CurrentData.getPrefs().set(Preference.EDITOR_WINDOW_SIZE, getSize());
                CurrentData.getPrefs().save();
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                arrangeComponentSizes();
                CurrentData.getPrefs().set(Preference.EDITOR_WINDOW_LOCATION, getLocation());
                CurrentData.getPrefs().save();
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
        addWindowListener(new WindowAdapter()
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
        });
        setIconImage(new ImageIcon("dat//img//other//logo.png").getImage());
        setTitle("Banana3D");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        add(editPane, BorderLayout.WEST);
        middlePanel.add(upperPanel);
        upperPanel.setLayout(new BorderLayout());
        upperPanel.add(addCamButton, BorderLayout.WEST);
        upperPanel.add(showNodeHierarchyButton, BorderLayout.EAST);
        upperPanel.add(fieldOfViewButton, BorderLayout.CENTER);
        middlePanel.add(canvasPanel);
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
        setVisible(true);
    }

    /**
     * Should be called after every resize. Arranges the components of the
     * Editor Window so the Tree and EditPanel have their minimum size
     * guaranteed.
     */
    public void arrangeComponentSizes()
    {
        editPane.setPreferredSize(new Dimension(530, getHeight() - 20));
        upperPanel.setPreferredSize(new Dimension(180, 30));
        treeScrollPane.setPreferredSize(new Dimension(320, treeScrollPane.getParent().getHeight() - sortModeComboBox.getHeight() - treePanel.getComponent(1).getHeight()));
        if (isUndecorated())
            middlePanel.setPreferredSize(new Dimension(getContentPane().getWidth() - tree.getWidth() - editPane.getWidth(), getHeight() - 60));
        else
            middlePanel.setPreferredSize(new Dimension(getContentPane().getWidth() - tree.getWidth() - editPane.getWidth(), getHeight() - 110));
        toolbar.setPreferredSize(new Dimension(middlePanel.getWidth(), 110));
        canvasPanel.setPreferredSize(new Dimension(
                middlePanel.getPreferredSize().width,
                middlePanel.getPreferredSize().height - upperPanel.getPreferredSize().height - toolbar.getPreferredSize().height));
        if (canvasContext != null)
        {
            b3DSimpleApplication.restart();
            canvasContext.getCanvas().setPreferredSize(canvasPanel.getPreferredSize());
        }
        repaint();
        revalidate();
        validate();
    }

    /**
     * Initializes the 3D-Context
     *
     * @param world
     */
    public void initNewScene(B3D_Scene world)
    {
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate((Integer) CurrentData.getPrefs().get(Preference.FRAMERATE));
        settings.setRenderer(AppSettings.LWJGL_OPENGL3);
        settings.setVSync((Boolean) CurrentData.getPrefs().get(Preference.VSYNC));
        settings.setBitsPerPixel((Integer) CurrentData.getPrefs().get(Preference.COLOR_DEPTH));
        settings.setResolution(canvasPanel.getWidth(), canvasPanel.getHeight());
        settings.setSamples((Integer) CurrentData.getPrefs().get(Preference.MULTISAMPLING));
        settings.setDepthBits((Integer) CurrentData.getPrefs().get(Preference.DEPTH_BITS));
        b3DSimpleApplication = new B3DApp(1000, world);
        b3DSimpleApplication.setSettings(settings);
        b3DSimpleApplication.createCanvas();
        b3DSimpleApplication.startCanvas();
        canvasContext = (JmeCanvasContext) b3DSimpleApplication.getContext();
        canvasContext.getCanvas().setPreferredSize(canvasPanel.getSize());
        canvasContext.setSystemListener(b3DSimpleApplication);
        canvasPanel.add(canvasContext.getCanvas());
        editorMenu.setDisabled(false);
        tree.setEnabled(true);
        toolbar.setEnabled(true);
        repaint();
        validate();
        kfad.setVisible((Boolean) CurrentData.getPrefs().get(Preference.KEY_ANIMATION_EDITOR_SHOWN));
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
                    } else if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        UAManager.undo();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_Y) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        UAManager.redo();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_R) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
                    {
                        CurrentData.execRename();
                        ret = true;
                    } else if ((e.getKeyCode() == KeyEvent.VK_F7))
                    {
                        if (b3DSimpleApplication != null)
                            CurrentData.execFullscreen();
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
                        CurrentData.execDuplicate();
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
                        System.out.println("EXEC DELETE");
                        CurrentData.execDelete(true);
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

    public JPanel getCanvasPanel()
    {
        return canvasPanel;
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

    public KeyframeAnimationFrame getKeyframeAnimationEditor()
    {
        return kfad;
    }
}
