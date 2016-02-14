package gui.dialogs;

import general.CurrentData;
import com.jme3.asset.AssetManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import com.sun.awt.AWTUtilities;
import components.BButton;
import components.BTextField;
import components.BToggleButton;
import components.Checker;
import components.OKButton;
import dialogs.BasicDialog;
import other.ElementToObjectConverter;
import other.ObjectToElementConverter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import se.datadosen.component.RiverLayout;

public class AdditionalCameraDialog extends JFrame implements SceneProcessor
{

    private CameraPanel camPanel = new CameraPanel();
    private ControlPanel controlPanel;
    private Camera camera;
    private ByteBuffer byteBuffer;
    private BufferedImage image;
    private FrameBuffer frameBuffer;
    private int width, height;
    private Renderer renderer;
    private ViewPort viewPort;
    private FilterPostProcessor filterPostProcessor;
    private Node node;
    private boolean pause = false, loadFilters = false;
    private AssetManager assetManager;
    private RenderManager renderManager;
    private Point windowLocation;
    private int number = 0, reloadTime = 400;
    private Node representative;
    private boolean initialized = false, running = true;

    public AdditionalCameraDialog(Point loc,
            Vector3f location,
            Quaternion rot,
            int w,
            int h,
            Renderer render,
            RenderManager rManager,
            Node n,
            AssetManager aManager,
            List<Filter> filters,
            boolean onTop,
            int nmbr,
            boolean showFilters)
    {
        number = nmbr;
        loadFilters = showFilters;
        windowLocation = loc;
        filterPostProcessor = new FilterPostProcessor(aManager);
        for (Filter filter : filters)
        {
            filterPostProcessor.addFilter(filter);
        }
        init(location, rot, w, h, render, rManager, n, aManager, onTop);
    }

    public AdditionalCameraDialog(
            Vector3f location,
            Quaternion rot,
            int w,
            int h,
            Renderer render,
            RenderManager rManager,
            Node n,
            AssetManager aManager,
            boolean onTop)
    {
        filterPostProcessor = new FilterPostProcessor(aManager);
        init(location, rot, w, h, render, rManager, n, aManager, onTop);
    }

    public void init(
            Vector3f location,
            Quaternion rot,
            int w,
            int h,
            Renderer render,
            RenderManager rManager,
            Node n,
            AssetManager aManager,
            final boolean onTop)
    {
        setIconImage(new ImageIcon("dat//img//other//logo.png").getImage());
        renderManager = rManager;
        assetManager = aManager;
        node = n;
        width = w;
        height = h;
        representative = (Node) assetManager.loadModel("Models/cam.j3o");
        representative.setName("cam");
        representative.setLocalTranslation(location);
        representative.setLocalRotation(rot);
        representative.setUserData("angles", new Vector3f());
        camera = new Camera(width, height);
        camera.setRotation(representative.getLocalRotation());
        camera.setFrustumPerspective(45f, 1f, 1f, 1000f);
        camera.setViewPort(0, 1, 0, 1);
        AWTUtilities.setWindowOpacity(this, 1f);
        if (number == 0)
        {
            number = CurrentData.getEditorWindow().getB3DApp().getAdditionalCameraDialogs().size() + 1;
        }
        setTitle("Cam " + number);
        frameBuffer = new FrameBuffer(width, height, 1);
        frameBuffer.setDepthBuffer(Image.Format.Depth);
        frameBuffer.setColorBuffer(Image.Format.RGB8);
        renderer = render;
        image = new BufferedImage(width, height,
                BufferedImage.TYPE_4BYTE_ABGR);
        byteBuffer = BufferUtils.createByteBuffer(width * height * 4);
        viewPort = renderManager.createPostView("Offscreen View", camera);
        viewPort.setClearFlags(true, true, true);
        viewPort.setOutputFrameBuffer(frameBuffer);
        viewPort.addProcessor(this);
        viewPort.addProcessor(filterPostProcessor);
        CurrentData.getEditorWindow().getB3DApp().addAdditionalCamera(this);
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                viewPort.attachScene(node);
                return null;
            }
        });
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                AdditionalCameraDialog.this.setAlwaysOnTop(onTop);
                camPanel.setPreferredSize(new Dimension(width, height));
                getContentPane().add(camPanel, BorderLayout.CENTER);
                controlPanel = new ControlPanel();
                getContentPane().add(controlPanel, BorderLayout.EAST);
                addWindowListener(new WindowListener()
                {
                    @Override
                    public void windowOpened(WindowEvent e)
                    {
                    }

                    @Override
                    public void windowClosing(WindowEvent e)
                    {
                        System.out.println("Window closing");
                        CurrentData.getEditorWindow().getB3DApp().removeAdditionalCamera(AdditionalCameraDialog.this);
                        running = false;
                        dispose();
                        System.out.println("Window closed");
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
                pack();
                if (windowLocation == null)
                {
                    setLocationRelativeTo(null);
                } else
                {
                    setLocation(windowLocation);
                }
                setResizable(false);
                setVisible(true);
                toFront();
                initialized = true;
            }
        });
    }

    private class CameraPanel extends JPanel
    {

        @Override
        public void paintComponent(Graphics gfx)
        {
            super.paintComponent(gfx);
            synchronized (image)
            {
                gfx.drawImage(image, 0, 0, null);
            }
        }
    }

    public void updateImg()
    {
        byteBuffer.clear();
        renderer.readFrameBuffer(frameBuffer, byteBuffer);
        synchronized (image)
        {
            Screenshots.convertScreenShot(byteBuffer, image);
        }
        if (camPanel != null)
        {
            camPanel.repaint();
        }
        if (filterPostProcessor.getFilterList().size()
                != CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size()
                || reloadTime == 0)
        {
            reloadTime = 500;
            viewPort.setBackgroundColor(CurrentData.getEditorWindow().getB3DApp().getViewPort().getBackgroundColor());
            filterPostProcessor.removeAllFilters();
            for (int i = 0; i < CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size(); i++)
            {
                filterPostProcessor.addFilter(
                        ElementToObjectConverter.convertFilter(
                        ObjectToElementConverter.convertFilter(
                        CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().get(i), i)));
            }
        } else
        {
            reloadTime--;
        }
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp)
    {
    }

    @Override
    public void reshape(ViewPort vp, int w, int h)
    {
    }

    @Override
    public boolean isInitialized()
    {
        return true;
    }

    @Override
    public void preFrame(float tpf)
    {
    }

    @Override
    public void postQueue(RenderQueue rq)
    {
    }

    @Override
    public void postFrame(FrameBuffer out)
    {
        if (isVisible() && !pause && initialized && running)
        {
            updateImg();
            camera.setLocation(representative.getLocalTranslation());
            camera.setRotation(representative.getLocalRotation());
        }
    }

    @Override
    public void cleanup()
    {
    }

    public class ControlPanel extends JPanel
    {

        private Checker alwaysOnTopChecker = new Checker();
        private BToggleButton continuePauseButton = new BToggleButton("Pause", true);
        private BButton sizeButton = new BButton("Resize");
        private Checker sceneryViewChecker = new Checker();
        private Checker filterChecker = new Checker();

        public ControlPanel()
        {
            alwaysOnTopChecker.setChecked(AdditionalCameraDialog.this.isAlwaysOnTop());
            filterChecker.setChecked(loadFilters);
            sceneryViewChecker.setChecked(node.equals(CurrentData.getEditorWindow().getB3DApp().getSceneNode()));
            setPreferredSize(new Dimension(180, 0));
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
                            viewPort.detachScene(node);
                            if (sceneryViewChecker.isChecked())
                            {
                                node = CurrentData.getEditorWindow().getB3DApp().getSceneNode();
                            } else
                            {
                                node = CurrentData.getEditorWindow().getB3DApp().getRootNode();
                            }
                            viewPort.attachScene(node);
                            return null;
                        }
                    });
                }
            });
            alwaysOnTopChecker.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    AdditionalCameraDialog.this.setAlwaysOnTop(alwaysOnTopChecker.isChecked());
                }
            });
            continuePauseButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (continuePauseButton.isSelected())
                    {
                        continuePauseButton.setText("Pause");
                        pause = false;
                    } else
                    {
                        continuePauseButton.setText("Continue");
                        pause = true;
                    }
                }
            });
            sizeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new SizeDialog();
                }
            });
            filterChecker.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    loadFilters = filterChecker.isChecked();
                }
            });
            continuePauseButton.setPreferredSize(new Dimension(80,continuePauseButton.getPreferredSize().height));
            setLayout(new RiverLayout());
            add(new JLabel("On Top:"));
            add("tab", alwaysOnTopChecker);
            add("br", new JLabel("Scenery View:"));
            add("tab", sceneryViewChecker);
            add("br", new JLabel("Show Filters:"));
            add("tab", filterChecker);
            add("br",continuePauseButton);
            add(sizeButton);
        }

        public Checker getFilterChecker()
        {
            return filterChecker;
        }

        private class SizeDialog extends BasicDialog
        {

            private BTextField widthField = new BTextField("Integer");
            private BTextField heightField = new BTextField("Integer");
            private OKButton okButton = new OKButton("Ok");

            public SizeDialog()
            {
                setAlwaysOnTop(true);
                widthField.setText("" + width);
                heightField.setText("" + height);
                okButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        dispose();
                        viewPort.setEnabled(false);
                        renderManager.removePostView(viewPort);
                        running = false;
                        AdditionalCameraDialog.this.dispose();
                        CurrentData.getEditorWindow().getB3DApp().removeAdditionalCamera(AdditionalCameraDialog.this);
                        respawn(Integer.parseInt(widthField.getText()), Integer.parseInt(heightField.getText()));
                    }
                });
                setTitle("Set Size");
                setLayout(new RiverLayout());
                add(new JLabel("Width: "));
                add("tab hfill", widthField);
                add("br", new JLabel("Height: "));
                add("tab hfill", heightField);
                add("br right", okButton);
                pack();
                setLocation(AdditionalCameraDialog.this.getLocation());
                setVisible(true);
            }
        }
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    public Camera getCamera()
    {
        return camera;
    }

    public Node getRepresentative()
    {
        return representative;
    }

    public ControlPanel getControlPanel()
    {
        return controlPanel;
    }

    public ViewPort getViewPort()
    {
        return viewPort;
    }

    public FilterPostProcessor getFilterPostProcessor()
    {
        return filterPostProcessor;
    }

    private void respawn(int w, int h)
    {
        pause = true;
        new AdditionalCameraDialog(
                getLocation(),
                camera.getLocation(),
                camera.getRotation(),
                w,
                h,
                renderer,
                renderManager,
                node,
                assetManager,
                filterPostProcessor.getFilterList(),
                isAlwaysOnTop(),
                getNumber(),
                loadFilters);
    }

    public boolean initialized()
    {
        return initialized;
    }
}
