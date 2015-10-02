package general;

import b3dElements.animations.B3D_Animation;
import b3dElements.B3D_Element;
import b3dElements.filters.B3D_Filter;
import b3dElements.lights.B3D_Light;
import b3dElements.spatials.B3D_Spatial;
import b3dElements.other.B3D_MotionEvent;
import files.Configuration;
import files.Project;
import gui.components.B3DSFileView;
import gui.dialogs.AssetBrowserDialog;
import gui.dialogs.CreateProjectDialog;
import gui.dialogs.ExitDialog;
import gui.dialogs.RecorderDialog;
import gui.dialogs.ScreenshotDialog;
import gui.dialogs.OpenProjectDialog;
import gui.editPanes.others.MotionPathTaskPane;
import gui.editor.EditorWindow;
import monkeyStuff.LightModel;
import monkeyStuff.LightScatteringModel;
import monkeyStuff.MotionPathModel;
import monkeyStuff.NodeModel;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.cinematic.PlayState;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LightControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.thoughtworks.xstream.XStream;
import dialogs.ObserverDialog;
import dialogs.SplashDialog;
import gui.dialogs.AnimationScriptDialog;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import other.ElementToObjectConverter;
import other.ObjectToElementConverter;
import other.B3D_Scene;
import other.Wizard;

public class CurrentData
{

    private static Project project;
    private static Configuration configuration;
    private static EditorWindow editorWindow;
    private static AssetBrowserDialog assetBrowserDialog;
    private static File materialDirectory = new File("dat//mats");
    private static SplashDialog splashDialog;
    private static ArrayList<String> materialFiles = new ArrayList<String>();
    private static Float[] fieldOfView;
    public static float CAM_VERY_SLOW = 1,
            CAM_SLOW = 10,
            CAM_DEFAULT = 20,
            CAM_FAST = 40,
            CAM_VERY_FAST = 100;
    private static boolean appRunning = false;
    private static String[] forbiddenNames =
    {
        "LightModel", "LightScatteringSymbol", "motionPathSymbol", "nodeModel", "nonI_Grid", "SelectionBox", "modelChild"
    };
    public static boolean textfieldSelected = false;
    private static Thread autosaveThread;
    private static AutosaveRunnable autosaveRunnable = new AutosaveRunnable();
    private static AnimationScriptDialog asd = new AnimationScriptDialog();

    public static AnimationScriptDialog getAnimationScriptDialog()
    {
        return asd;
    }

    public static SplashDialog getSplashDialog()
    {
        return splashDialog;
    }

    /**
     *
     * @param splashDialog
     */
    public static void setSplashDialog(SplashDialog splashDialog)
    {
        CurrentData.splashDialog = splashDialog;
    }

    public static File getMaterialDirectory()
    {
        return materialDirectory;
    }

    /**
     *
     * @param materialDirectory
     */
    public static void setMaterialDirectory(File materialDirectory)
    {
        CurrentData.materialDirectory = materialDirectory;
    }

    public static EditorWindow getEditorWindow()
    {
        return editorWindow;
    }

    /**
     *
     * @param editorWindow
     */
    public static void setEditorWindow(EditorWindow editorWindow)
    {
        CurrentData.editorWindow = editorWindow;
    }

    public static Project getProject()
    {
        return project;
    }

    /**
     *
     * @param proj
     */
    public static void setProject(Project proj)
    {
        CurrentData.project = proj;
    }

    public static Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     *
     * @param configuration
     */
    public static void setConfiguration(Configuration configuration)
    {
        CurrentData.configuration = configuration;
    }

    public static AssetBrowserDialog getAssetBrowserDialog()
    {
        return assetBrowserDialog;
    }

    /**
     *
     * @param assetBrowserDialog
     */
    public static void setAssetBrowserDialog(AssetBrowserDialog assetBrowserDialog)
    {
        CurrentData.assetBrowserDialog = assetBrowserDialog;
    }

    public static List<String> getAllMaterialFiles()
    {
        return materialFiles;
    }

    public static Thread getAutosaveThread()
    {
        return autosaveThread;
    }

    /**
     * Starts the autosave-thread
     */
    public static void startAutosaveThread()
    {
        autosaveThread = new Thread(autosaveRunnable);
        autosaveThread.start();
    }

    /**
     * Finds all the material files within the jars inside the 'lib'-folder
     */
    static void findMaterials()
    {
        Vector<String> paths = new Vector<String>();
        File[] libFiles = new File("lib").listFiles();
        for (int i = 0; i < libFiles.length; i++)
            //All .jar-files
            if (libFiles[i].getName().endsWith(".jar"))
            {
                paths.add(libFiles[i].getAbsolutePath());
                ObserverDialog.getObserverDialog().printMessage("Hunting " + libFiles[i].getName());
            }
        for (String s : paths)
        {
            URL jar = null;
            try
            {
                jar = new File(s).toURI().toURL();
            } catch (MalformedURLException ex)
            {
                ObserverDialog.getObserverDialog().printError("Error while reading\n\tPath -> " + s, ex);
            }
            //Jars are basically zip-archives
            ZipInputStream zis = null;
            try
            {
                zis = new ZipInputStream(jar.openStream());
            } catch (IOException ex)
            {
                ObserverDialog.getObserverDialog().printError("Error creating ZipInputStream", ex);
            }
            while (true)
            {
                ZipEntry entry = null;
                try
                {
                    entry = zis.getNextEntry();
                    //Loop through all jars
                } catch (IOException ex)
                {
                    ObserverDialog.getObserverDialog().printError("Error getting next ZipEntry", ex);
                    break;
                }
                if (entry != null)
                {
                    //Get the name
                    String e = entry.toString();
                    //Sort out troublemakers and non-material-files
                    if (e.endsWith(".j3md")
                            && !e.contains("ShowNormals")
                            && !e.contains("NiftyQuadGrad")
                            && !e.contains("SimpleSpriteParticle")
                            && !e.contains("PreShadow")
                            && !e.contains("PostShadow")
                            && !e.contains("tonegod"))
                    {
                        ObserverDialog.getObserverDialog().printMessage("Adding MatFile: " + e);
                        materialFiles.add(e);
                    }
                } else
                    //if entry is null, we are out of jar-files. Quit the loop
                    break;
            }
        }
    }

    public static boolean isAppRunning()
    {
        return appRunning;
    }

    /**
     *
     * @param appRunning
     */
    public static void setAppRunning(boolean appRunning)
    {
        CurrentData.appRunning = appRunning;
    }

    public static AutosaveRunnable getAutosaveRunnable()
    {
        return autosaveRunnable;
    }

    public static Float[] getFieldOfView()
    {
        return fieldOfView;
    }

    public static void setDefaultFieldOfView(Float[] fieldOfView)
    {
        CurrentData.fieldOfView = fieldOfView;
    }

    public static String[] getForbiddenNames()
    {
        return forbiddenNames;
    }

    /**
     * Creates a new project.
     */
    public static void execNewProject()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                boolean proceed = true;
                if (editorWindow.getB3DApp() != null)
                {
                    proceed = JOptionPane.showConfirmDialog(
                            editorWindow,
                            "All unsaved data will be lost...",
                            "Just making sure...",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
                }
                if (proceed)
                    //If the user confirms, display the respective dialog
                    new CreateProjectDialog();
            }
        });
    }

    public static void execNewScene()
    {
        boolean proceed = editorWindow.getB3DApp() != null;
        if (proceed)
        {
            if (editorWindow.getB3DApp() != null)
                proceed = JOptionPane.showConfirmDialog(
                        editorWindow,
                        "All unsaved data will be lost...",
                        "Just making sure...",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
            if (proceed)
            {
                String sceneName = JOptionPane.showInputDialog("Name of new Scene:");
                if (new File(project.getMainFolder().getAbsolutePath() + "\\" + sceneName + ".b3ds").exists())
                    proceed = JOptionPane.showConfirmDialog(
                            editorWindow,
                            "A scene with that name already exists! Replace it?",
                            "Just making sure...",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
                if (proceed)
                {
                    editorWindow.setTitle(sceneName + " - Banana3D");
                    if (editorWindow.getB3DApp() != null)
                        execReset(false);
                    B3D_Scene scene = new B3D_Scene(sceneName);
                    project.setCurrentScene(scene);
                    Wizard.saveFile(project.getMainFolder().getAbsolutePath() + "\\" + sceneName + ".b3ds", scene);
                }
            }
        } else
            JOptionPane.showMessageDialog(editorWindow, "Open or create a new Project first!", "No Scene without a Project, damnit!", JOptionPane.ERROR_MESSAGE);
    }
    static B3D_Element selectedElement;

    public static void execRename()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                selectedElement = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
                String newName = JOptionPane.showInputDialog("New Name:", selectedElement.getName());
                if (newName != null)
                {
                    boolean nameForbidden = false;
                    for (String fName : CurrentData.getForbiddenNames())
                        if (fName.equals(newName))
                            nameForbidden = true;
                    if (nameForbidden)
                        JOptionPane.showMessageDialog(null, "Name already taken for In-Editor stuff!", "Nope", JOptionPane.ERROR_MESSAGE);
                    else
                    {
                        if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Spatial)
                            ((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).setName(newName);
                        else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Light)
                            ((Light) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).setName(newName);
                        else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Filter)
                            ((Filter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).setName(newName);
                        selectedElement.setName(newName);
                        editorWindow.getB3DApp().setTreeSyncNeeded(true);
                    }
                }
            }
        });
    }

    /**
     * Refreshes all B3D_Elements and saves them in the B3D_Scene-File
     *
     * @param scenePath
     */
    public static void execSaveScene(String scenePath)
    {
        ObjectToElementConverter.convertMode = ObjectToElementConverter.ConvertMode.SAVING;
        synchronized (Wizard.getObjects().getOriginalObjectsIterator())
        {
            B3D_Scene scene = new B3D_Scene(getProject().getScene().getName());
            scene.setPhysicsSpeed(getEditorWindow().getB3DApp().getBulletAppState().getSpeed());
            scene.setViewPortColor(getEditorWindow().getB3DApp().getViewPort().getBackgroundColor());
            /*
             * Ignoring the Filters, MotionEvents and LightControls here so their spatials and lights will be refreshed first
             */
            //Lights first
            for (Object o : Wizard.getObjects().getOriginalObjectsIterator().toArray())
            {
                if (o instanceof Light)
                {
                    //B3D_Light b3D_Light = (B3D_Light) Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(o.hashCode()));
                    B3D_Light oldB3D_Light = (B3D_Light) Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(o.hashCode()));
                    B3D_Light b3D_Light = ObjectToElementConverter.convertLight((Light) o);
                    b3D_Light.setUuid(oldB3D_Light.getUUID());
                    System.out.println("Saving b3dLight with UUID: " + b3D_Light.getUUID());
                    scene.getElements().add(b3D_Light);
                    b3D_Light.setAnimations((ArrayList<B3D_Animation>) oldB3D_Light.getAnimations().clone());
                }
            }
            //Now spatials, but only those without a parent, they will also load their children
            for (Object o : Wizard.getObjects().getOriginalObjectsIterator().toArray())
            {
                if (o instanceof Spatial)
                {
                    //Can not call convertSpatial, because the Lights of the LightControls it may have would be duplicated
                    //B3D_Spatial b3D_Spatial = ObjectToElementConverter.convertSpatial((Spatial) o);
                    if (((Spatial) o).getParent() == getEditorWindow().getB3DApp().getSceneNode())
                    {
                        B3D_Spatial b3D_Spatial = (B3D_Spatial) ObjectToElementConverter.convertToElement(o);
                        scene.getElements().add(b3D_Spatial);
                    }
                }
            }
            // Now the Filters, MotionEvents
            for (Object o : Wizard.getObjects().getOriginalObjectsIterator().toArray())
            {
                if (o instanceof Filter || o instanceof MotionEvent)
                {
                    B3D_Element oldB3D_Element = (B3D_Element) Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(o.hashCode()));
                    B3D_Element newB3D_Element = ObjectToElementConverter.convertToElement(o);
                    newB3D_Element.setUuid(oldB3D_Element.getUUID());
                    if (oldB3D_Element instanceof B3D_Filter)
                        ((B3D_Filter) newB3D_Element).setFilterIndex(((B3D_Filter) oldB3D_Element).getFilterIndex());
                    newB3D_Element.setAnimations((ArrayList<B3D_Animation>) oldB3D_Element.getAnimations().clone());
                    scene.getElements().add(newB3D_Element);
                }
            }
            System.out.println("Saving scene with " + scene.getElements().size() + " Elements at " + scenePath);
            Wizard.saveFile(scenePath, scene);
            ObserverDialog.getObserverDialog().printMessage("Saved to " + scenePath);
            try
            {
                XStream xStream = new XStream();
                xStream.setMode(XStream.ID_REFERENCES);
                Wizard.saveFile(scenePath + ".xml", xStream.toXML(scene));
            } catch (Exception e)
            {
                e.printStackTrace();
                ObserverDialog.getObserverDialog().printError("Saving XML failed", e);
            }
            Wizard.saveFile(project.getMainFolder().getAbsolutePath() + "//" + project.getMainFolder().getName() + ".b3dp", project);
        }
    }

    /**
     * Resets the 3D-B3D_Scene
     *
     * @param ask if the user is sure. If not, DON'T DO IT
     */
    public static void execReset(boolean ask)
    {
        boolean reset = true;
        if (ask)
            reset = JOptionPane.showConfirmDialog(
                    editorWindow,
                    "Are you sure that you want to delete like everything in this scene?",
                    "Please confirm",
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
        if (reset)
            //Remove everything
            //MotionPaths
            editorWindow.getB3DApp().enqueue(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    CurrentData.getEditorWindow().getB3DApp().getViewPort().setBackgroundColor(Wizard.defaultViewportBackground);
                    for (MotionPathModel mpm : CurrentData.getEditorWindow().getB3DApp().getMotionPathModels())
                    {
                        CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(mpm.getSymbol());
                    }
                    CurrentData.getEditorWindow().getB3DApp().getMotionPathModels().removeAllElements();
                    //Lights
                    for (LightModel lm : CurrentData.getEditorWindow().getB3DApp().getLightModels())
                    {
                        CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(lm.getNode());
                    }
                    CurrentData.getEditorWindow().getB3DApp().getLightModels().removeAllElements();
                    //Nodes
                    for (NodeModel nm : CurrentData.getEditorWindow().getB3DApp().getNodeModels())
                    {
                        CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(nm.getNode());
                        CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(nm.getLineNode());
                        CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(nm.getModel());
                    }
                    CurrentData.getEditorWindow().getB3DApp().getNodeModels().removeAllElements();
                    //Filters
                    CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().removeAllFilters();
                    for (LightScatteringModel lsm : CurrentData.getEditorWindow().getB3DApp().getLightScatteringModels())
                    {
                        CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(lsm.getSymbol());
                    }
                    CurrentData.getEditorWindow().getB3DApp().getLightScatteringModels().removeAllElements();
                    //Physics
                    CurrentData.getEditorWindow().getB3DApp().getBulletAppState().getPhysicsSpace().removeAll(CurrentData.getEditorWindow().getB3DApp().getSceneNode());
                    //Spatials
                    CurrentData.getEditorWindow().getB3DApp().getSceneNode().detachAllChildren();
                    //Lights (again! wheee!)
                    for (Light light : CurrentData.getEditorWindow().getB3DApp().getSceneNode().getWorldLightList())
                    {
                        CurrentData.getEditorWindow().getB3DApp().getSceneNode().removeLight(light);
                    }
                    //Clear the ElementList
                    Wizard.getObjects().clear();
                    //Nothing is really selected
                    CurrentData.getEditorWindow().getB3DApp().setSelectedElement(Wizard.NULL_SELECTION, null);
                    //Sync Tree
                    CurrentData.getEditorWindow().getB3DApp().setTreeSyncNeeded(true);
                    ObserverDialog.getObserverDialog().printMessage("Reset");
                    return null;
                }
            });
    }

    /**
     * Opens a new Project and a scene if there is one. If not, a new one will
     * be created
     *
     * @param projectPath
     */
    public static void execOpenProject(final String projectPath)
    {
        /* //Could be called from the 3D-Context and since we don't want the App to
         //explode, we'll let Swing handle it a little later.
         SwingUtilities.invokeLater(new Runnable()
         {
         @Override
         public void run()
         {*/
        boolean proceed = true;
        if (editorWindow.getB3DApp() != null)
            //In case a 3D-B3D_Scene is opened (since it will be closed...)
            proceed = JOptionPane.showConfirmDialog(
                    editorWindow,
                    "All unsaved data will be lost...",
                    "Just making sure...",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
        if (proceed)
        {
            if (projectPath == null)
            {
                //Ask for the new path
                JFileChooser jfc = new JFileChooser();
                jfc.setFileView(new B3DSFileView());
                jfc.setFileFilter(new FileFilter()
                {
                    @Override
                    public boolean accept(File f)
                    {
                        return (f.isDirectory() || f.getAbsolutePath().endsWith(".b3dp"));
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Banana3D Project (.b3dp)";
                    }
                });
                jfc.showOpenDialog(editorWindow);
                if (jfc.getSelectedFile() != null)
                {
                    //Create the Project
                    OpenProjectDialog ssd = new OpenProjectDialog(jfc.getSelectedFile());
                    new Project(ssd.getProjectFile(), ssd.getSceneFile());
                }
            } else
            {
                OpenProjectDialog ssd = new OpenProjectDialog(new File(projectPath));
                new Project(ssd.getProjectFile(), ssd.getSceneFile());
            }
            if (project != null)
            {
                if (editorWindow.getB3DApp() != null)
                {
                    //Reset B3D_Scene if there is one and load the content of the new (or loaded) one
                    execReset(false);
                    CurrentData.buildSceneIntoEditor(project.getScene());
                } else
                    //Of no B3D_Scene was opened before, initialize the 3D-Context
                    CurrentData.getEditorWindow().initNewScene(project.getScene());
            }
        }
        //}
        // });
    }

    public static void execQuit()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                configuration.editorWidth = editorWindow.getWidth();
                configuration.editorHeight = editorWindow.getHeight();
                configuration.save();
                if (CurrentData.getConfiguration().exitwithoutprompt)
                    Runtime.getRuntime().exit(0);
                 else
                    new ExitDialog();
            }
        });
    }

    public static void execCreateTwin()
    {
        System.out.println("Creating Twin!");
        editorWindow.getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                ObjectToElementConverter.convertMode = ObjectToElementConverter.ConvertMode.CREATING_TWIN;
                if (!(CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Filter) && CurrentData.getEditorWindow().getB3DApp().getSelectedObject() != null)
                {
                    B3D_Element oldElement = Wizard.getObjects().getB3D_Element(
                            Wizard.getObjectReferences().getUUID(CurrentData.getEditorWindow().getB3DApp().getSelectedObject().hashCode()));
                    final B3D_Element newElement = ObjectToElementConverter.convertToElement(CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                    for (B3D_Animation animation : oldElement.getAnimations())
                    {
                        B3D_Animation copy = animation.copy();
                        copy.setObject(newElement.getUUID());
                        newElement.getAnimations().add((B3D_Animation) copy);
                    }
                    final Object newObject = ElementToObjectConverter.convertToObject(newElement);
                    System.out.println("Element: " + newElement + " -> " + newObject);
                    System.out.println("called it");
                    /* if (newObject instanceof Node)
                     {
                     CurrentData.getEditorWindow().getB3DApp().setSelectedNode(((Node) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getParent());
                     }*/
                    if (newObject instanceof Spatial)
                    {
                        CurrentData.getEditorWindow().getB3DApp().setSelectedNode(((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getParent());
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                        {
                            @Override
                            public Void call() throws Exception
                            {
                                Wizard.getObjects().add(newObject, newElement);
                                //Just use hashcode ((Spatial) newObject).setUserData("index", newElement.getUUID());
                                CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild((Spatial) newObject);
                                if (((Spatial) newObject).getControl(RigidBodyControl.class) != null)
                                {
                                    System.out.println("Adding to Bullet");
                                    CurrentData.getEditorWindow().getB3DApp().getBulletAppState().getPhysicsSpace().add(newObject);
                                }
                                return null;
                            }
                        });
                    } else if (newObject instanceof Light)
                    {
                        System.out.println("add light");
                        Wizard.getObjects().add(newObject, newElement);
                        CurrentData.getEditorWindow().getB3DApp().getSceneNode().addLight((Light) newObject);
                    } else if (newObject instanceof MotionEvent)
                    {
                        CurrentData.getEditorWindow().getB3DApp().getMotionPathModels().add(new MotionPathModel((MotionEvent) newObject));
                        Wizard.getObjects().add(newObject, newElement);
                    }
                    editorWindow.getB3DApp().setSyncTree(true);
                } /*  if (Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID()) instanceof B3D_Spatial
                 && ((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getControl(LightControl.class) != null)
                 {
                 new Thread(new Runnable()
                 {
                 @Override
                 public void run()
                 {
                 //Yeah, it's bad... but it friggin works (prevents crash that I juST DON'T UNDERSTAND)
                 try
                 {
                 Thread.sleep(1000);
                 } catch (InterruptedException ex)
                 {
                 Logger.getLogger(CurrentData.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 //If there is a LightControl, ask the user how to deal with it
                 LightControlDuplicateDialog lcdd = new LightControlDuplicateDialog();
                 String action = lcdd.getAction();
                 if (action.equals("Transfer"))
                 {
                 ((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).removeControl(LightControl.class);
                 } else if (action.equals("Remain"))
                 {
                 ((Spatial) newObject).removeControl(LightControl.class);
                 } else
                 {
                 ((Spatial) newObject).removeControl(LightControl.class);
                 //duplicate Light
                 Light l = ElementToObjectConverter.convertLight(
                 ObjectToElementConverter.convertLight(
                 ((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getControl(LightControl.class).getLight()));
                 CurrentData.getEditorWindow().getB3DApp().getSceneNode().addLight(l);
                 //create new LightControl (with new Light)
                 LightControl newLightControl = new LightControl(l);
                 ((Spatial) newObject).addControl(newLightControl);
                 editorWindow.getB3DApp().getControls().add(newLightControl);
                 }
                 editorWindow.getB3DApp().setSyncTree(true);
                 }
                 }).start();
                 }*/ else
                    ObserverDialog.getObserverDialog().printMessage("Filters can not be duplicated (because of reasons)");
                return null;
            }
        });
    }

    public static void execFind()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                CurrentData.getEditorWindow().getTree().getSearchable().search(JOptionPane.showInputDialog("Search"));
            }
        });
    }

    public static void execLookAt()
    {
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Spatial)
                {
                    CurrentData.getEditorWindow().getB3DApp().getCamera().lookAt(((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getWorldTranslation(), Vector3f.UNIT_Y);
                } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Light)
                {
                    /*Only Spot-And PointLight have that Item*/
                    if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof PointLight)
                    {
                        CurrentData.getEditorWindow().getB3DApp().getCamera().lookAt(((PointLight) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getPosition(), Vector3f.UNIT_Y);
                    } else
                    {
                        CurrentData.getEditorWindow().getB3DApp().getCamera().lookAt(((SpotLight) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getPosition(), Vector3f.UNIT_Y);
                    }
                } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof MotionEvent)
                {
                    CurrentData.getEditorWindow().getB3DApp().getCamera().lookAt(((MotionEvent) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getPath().getWayPoint(0), Vector3f.UNIT_Y);
                }
                return null;
            }
        });
    }

    public static void execPlayPauseMotionPath()
    {
        if (CurrentData.getEditorWindow().getEditPane().getCurrentEditPane() instanceof MotionPathTaskPane)
        {
            if (((MotionPathTaskPane) CurrentData.getEditorWindow().getEditPane().getCurrentEditPane()).getMotionEvent().getPlayState().equals(PlayState.Playing))
            {
                ((MotionPathTaskPane) CurrentData.getEditorWindow().getEditPane().getCurrentEditPane()).pause();
            } else
            {
                ((MotionPathTaskPane) CurrentData.getEditorWindow().getEditPane().getCurrentEditPane()).play();
            }
        }
    }

    public static void execPlayPauseMotionPath(JMenuItem popupItem)
    {
        if (((MotionPathTaskPane) CurrentData.getEditorWindow().getEditPane().getCurrentEditPane()).getMotionEvent().getPlayState().equals(PlayState.Playing))
        {
            ((MotionPathTaskPane) CurrentData.getEditorWindow().getEditPane().getCurrentEditPane()).pause();
            popupItem.setIcon(new ImageIcon("dat//img//menu//play.png"));
            popupItem.setText("Play");
        } else
        {
            ((MotionPathTaskPane) CurrentData.getEditorWindow().getEditPane().getCurrentEditPane()).play();
            popupItem.setIcon(new ImageIcon("dat//img//menu//pause.png"));
            popupItem.setText("Pause");
        }
    }

    public static void execDelete()
    {
        if (CurrentData.getEditorWindow().getB3DApp().getSelectedUUID() != null && !(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID().equals(Wizard.NULL_SELECTION)))
        {
            //Class-name of element
            final String className = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID()).getClass().getName();
            if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Spatial)
            {
                if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Node && !(CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof TerrainQuad)
                        && editorWindow.getB3DApp().getSelectedNode() == CurrentData.getEditorWindow().getB3DApp().getSelectedObject())
                {
                    editorWindow.getB3DApp().setSelectedNode(editorWindow.getB3DApp().getSceneNode());
                }
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        Spatial spatial = ((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        //Is there a LightControl depending on this Spatial? If so, delete it.
                        for (LightControl lc : editorWindow.getB3DApp().getLightControls())
                        {
                            if (lc.getSpatial() == spatial)
                            {
                                editorWindow.getB3DApp().getLightControls().remove(lc);
                                lc.getSpatial().removeControl(lc);
                            }
                        }
                        spatial.getParent().detachChild(spatial);
                        if (spatial.getControl(RigidBodyControl.class) != null)
                        {
                            CurrentData.getEditorWindow()
                                    .getB3DApp().getBulletAppState().getPhysicsSpace().remove(spatial);
                        }
                        Wizard.getObjects().remove(spatial.hashCode());
                        CurrentData.getEditorWindow().getB3DApp().setSelectedElement(Wizard.NULL_SELECTION, null);
                        CurrentData.getEditorWindow().getEditPane().arrange(false);
                        editorWindow.getTree().sync();
                        return null;
                    }
                });
                if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Node && !(CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof TerrainQuad))
                {
                    for (final NodeModel nodeModel : CurrentData.getEditorWindow().getB3DApp().getNodeModels())
                    {
                        if (nodeModel.getNode().equals(CurrentData.getEditorWindow().getB3DApp().getSelectedObject()))
                        {
                            final Vector<NodeModel> toRemove = new Vector<NodeModel>();
                            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                            {
                                @Override
                                public Void call() throws Exception
                                {
                                    synchronized (CurrentData.getEditorWindow().getB3DApp().getNodeModels())
                                    {
                                        for (NodeModel nm : CurrentData.getEditorWindow().getB3DApp().getNodeModels())
                                        {
                                            if (nm.getNode().hasAncestor(nodeModel.getNode()))
                                            {
                                                toRemove.add(nm);
                                            }
                                        }
                                        toRemove.add(nodeModel);
                                    }
                                    for (NodeModel n : toRemove)
                                    {
                                        n.getNode().removeFromParent();
                                        n.getModel().removeFromParent();
                                        n.getLineNode().removeFromParent();
                                        CurrentData.getEditorWindow().getB3DApp().getNodeModels().remove(n);
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                }
            } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Light)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        //Is there a LightControl depending on this Light? If so, delete it.
                        for (Object lc : editorWindow.getB3DApp().getLightControls().toArray())
                        {
                            if (((LightControl) lc).getLight() == CurrentData.getEditorWindow().getB3DApp().getSelectedObject()
                                    && Wizard.getObjectReferences().getUUID(((LightControl) lc).getSpatial().hashCode()) != null)
                            {
                                editorWindow.getB3DApp().getLightControls().remove(((LightControl) lc));
                                ((LightControl) lc).getSpatial().removeControl(((LightControl) lc));
                            }
                        }
                        Wizard.getObjects().remove(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
                        CurrentData.getEditorWindow().getB3DApp().getSceneNode().removeLight(
                                (Light) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        CurrentData.getEditorWindow().getEditPane().arrange(false);
                        CurrentData.getEditorWindow().getB3DApp().setSelectedElement(Wizard.NULL_SELECTION, null);
                        editorWindow.getTree().sync();
                        return null;
                    }
                });
            } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Filter)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        Filter filter = (Filter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                        //Adjust index...
                        for (Filter f : CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList())
                            if (f != filter)
                            {
                                UUID tempFilter1UUID = Wizard.getObjectReferences().getUUID(f.hashCode());
                                B3D_Filter tempFilter1 = (B3D_Filter) Wizard.getObjects().getB3D_Element(tempFilter1UUID);
                                UUID tempFilter2UUID = Wizard.getObjectReferences().getUUID(filter.hashCode());
                                B3D_Filter tempFilter2 = (B3D_Filter) Wizard.getObjects().getB3D_Element(tempFilter2UUID);
                                if (tempFilter1.getFilterIndex() > tempFilter2.getFilterIndex())
                                    tempFilter1.indexUp();
                            }
                        if (filter instanceof LightScatteringFilter)
                        {
                            LightScatteringModel l = null;
                            for (LightScatteringModel lsm : CurrentData.getEditorWindow().getB3DApp().getLightScatteringModels())
                                if (lsm.getScatteringFilter().equals(filter))
                                    l = lsm;
                            CurrentData.getEditorWindow().getB3DApp().getLightScatteringModels().remove(l);
                            CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(l.getSymbol());
                        }
                        CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().removeFilter(filter);
                        Wizard.getObjects().remove(filter.hashCode());
                        CurrentData.getEditorWindow().getB3DApp().setSelectedElement(Wizard.NULL_SELECTION, null);
                        CurrentData.getEditorWindow().getEditPane().arrange(false);
                        editorWindow.getTree().sync();
                        return null;
                    }
                });
            } else if (className.equals(B3D_MotionEvent.class.getName()))
            {
                CurrentData.getEditorWindow()
                        .getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        if (((B3D_MotionEvent) Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID())).getObjectProbablyUUID().equals(B3D_MotionEvent.Cam.CAM_ID))
                            CurrentData.getEditorWindow().getB3DApp().detachCam();
                        ((MotionEvent) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).stop();
                        synchronized (CurrentData.getEditorWindow().getB3DApp().getMotionPathModels())
                        {
                            try
                            {
                                for (MotionPathModel mpm : CurrentData.getEditorWindow().getB3DApp().getMotionPathModels())
                                    CurrentData.getEditorWindow().getB3DApp().getMotionPathModels().remove(mpm);
                                Wizard.getObjects().remove(CurrentData.getEditorWindow().getB3DApp().getSelectedObject().hashCode());
                                CurrentData.getEditorWindow().getB3DApp().setSelectedElement(Wizard.NULL_SELECTION, null);
                                CurrentData.getEditorWindow().getEditPane().arrange(false);
                                editorWindow.getTree().sync();
                            } catch (java.util.ConcurrentModificationException cme)
                            {
                                ObserverDialog.getObserverDialog().printError("Deleting MotionPath failed", cme);
                            }
                            return null;
                        }
                    }
                });
            }
        }
    }

    public static void setPhysicsRunning(final boolean running)
    {
        System.out.println("running: " + running);
        if (!running)
            CurrentData.getEditorWindow().getB3DApp().pausePhysics();
        else
            CurrentData.getEditorWindow().getB3DApp().playPhysics();
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (editorWindow.getToolbar().getPhysicsStartStopChecker().isChecked() != running)
                    editorWindow.getToolbar().getPhysicsStartStopChecker().setChecked(!running);
                if (running)
                    editorWindow.getMainMenu().getPhysicsMenu().getStartStopItem().setText("Start");
                else
                    editorWindow.getMainMenu().getPhysicsMenu().getStartStopItem().setText("Stop");
            }
        });
    }

    public static void execScreenshot()
    {
        new ScreenshotDialog();
    }

    public static void execRecord()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new RecorderDialog();
            }
        });
    }

    public static void execFullscreen()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (!CurrentData.getConfiguration().fullscreen)
                {
                    CurrentData.getEditorWindow().dispose();
                    CurrentData.getEditorWindow().setUndecorated(true);
                    CurrentData.getEditorWindow().setSize(Toolkit.getDefaultToolkit().getScreenSize());
                    CurrentData.getEditorWindow().setLocation(0, 0);
                    CurrentData.getEditorWindow().setVisible(true);
                    ObserverDialog.getObserverDialog().printMessage("Fullscreen: On");
                } else
                {
                    CurrentData.getEditorWindow().dispose();
                    CurrentData.getEditorWindow().setUndecorated(false);
                    CurrentData.getEditorWindow().setSize(1360, 768);
                    CurrentData.getEditorWindow().setLocationRelativeTo(null);
                    CurrentData.getEditorWindow().setVisible(true);
                    ObserverDialog.getObserverDialog().printMessage("Fullscreen: Off");
                }
                CurrentData.getConfiguration().setFullscreen(!CurrentData.getConfiguration().fullscreen);
                editorWindow.getMainMenu().getViewMenu().getFullscreenItem().setSelected(CurrentData.getConfiguration().fullscreen);
                CurrentData.getEditorWindow().arrangeComponentSizes();
            }
        });
    }

    public static void exec9gag()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    java.awt.Desktop.getDesktop().browse(new URI("http://www.9gag.com/random"));
                } catch (IOException ex)
                {
                    ObserverDialog.getObserverDialog().printError("IOException when doing random things", ex);
                } catch (URISyntaxException ex)
                {
                    ObserverDialog.getObserverDialog().printError("URI Syntax-Error calling 9gag", ex);
                }
            }
        });
    }

    /**
     * Loads the content of a given B3D_Scene
     *
     * @param app
     * @param scene
     */
    public static void buildSceneIntoEditor(final B3D_Scene scene)
    {
        getEditorWindow().getB3DApp().getBulletAppState().setSpeed(scene.getPhysicsSpeed());
        getEditorWindow().getB3DApp().getViewPort().setBackgroundColor(scene.getViewPortColor());
        getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                int elementsAdded = 0;
                //Lights first
                for (B3D_Element e : scene.getElements())
                {
                    if (e instanceof B3D_Light)
                    {
                        Light l = ElementToObjectConverter.convertLight((B3D_Light) e);
                        Wizard.getObjects().add(l, e);
                        getEditorWindow().getB3DApp().getSceneNode().addLight(l);
                        elementsAdded++;
                    }
                }
                //Filters
                ArrayList<Filter> filters = new ArrayList<Filter>();
                for (B3D_Element e : scene.getElements())
                {
                    if (e instanceof B3D_Filter)
                    {
                        Filter f = ElementToObjectConverter.convertFilter((B3D_Filter) e);
                        Wizard.getObjects().add(f, e);
                        //Right order
                        filters.add(((B3D_Filter) e).getFilterIndex(), f);
                        elementsAdded++;
                    }
                }
                for (Filter f : filters)
                {
                    if (f instanceof LightScatteringFilter)
                        CurrentData.getEditorWindow().getB3DApp().getLightScatteringModels().add(new LightScatteringModel((LightScatteringFilter) f));
                    getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(f);
                }
                //Spatials
                for (B3D_Element e : scene.getElements())
                {
                    if (e instanceof B3D_Spatial && ((B3D_Spatial) e).getParentUUID().equals(Wizard.NULL_SELECTION))
                    {
                        Spatial s = ElementToObjectConverter.convertSpatial((B3D_Spatial) e);
                        Wizard.getObjects().add(s, e);
                        getEditorWindow().getB3DApp().getSceneNode().attachChild(s);
                        elementsAdded++;
                    }
                }
                //MotionPaths
                for (B3D_Element e : scene.getElements())
                {
                    if (e instanceof B3D_MotionEvent)
                    {
                        MotionEvent m = ElementToObjectConverter.convertMotionEvent((B3D_MotionEvent) e);
                        Wizard.getObjects().add(m, e);
                        final MotionPathModel mpm = new MotionPathModel(m);
                        getEditorWindow().getB3DApp().getMotionPathModels().add(mpm);
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                        {
                            @Override
                            public Void call() throws Exception
                            {
                                CurrentData.getEditorWindow().getB3DApp().getEditorNode().attachChild(mpm.getSymbol());
                                return null;
                            }
                        });
                        elementsAdded++;
                    }
                }
                System.out.println(elementsAdded + " Elemente registriert von " + scene.getElements().size());
                editorWindow.getTree().sync();
                return null;
            }
        });
    }

    public static void updateAssetRegister()
    {
        updateAssetRegister(CurrentData.getProject().getAssetsFolder());
    }

    private static void updateAssetRegister(File assetsFolder)
    {
        getEditorWindow().getB3DApp().getAssetManager().registerLocator(assetsFolder.getAbsolutePath(), FileLocator.class);
        for (File f : assetsFolder.listFiles())
            if (f.isDirectory())
                updateAssetRegister(f);
    }
}