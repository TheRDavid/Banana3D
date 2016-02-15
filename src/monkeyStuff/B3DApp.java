package monkeyStuff;

import b3dElements.B3D_Element;
import b3dElements.filters.B3D_Filter;
import b3dElements.filters.B3D_Water;
import b3dElements.lights.B3D_PointLight;
import b3dElements.spatials.geometries.B3D_Geometry;
import b3dElements.spatials.B3D_Model;
import b3dElements.spatials.B3D_Node;
import b3dElements.spatials.B3D_Spatial;
import b3dElements.other.B3D_MotionEvent;
import gui.dialogs.AdditionalCameraDialog;
import gui.dialogs.AssetBrowserDialog;
import gui.editPanes.others.MotionPathTaskPane;
import general.CurrentData;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Spline;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Curve;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import dialogs.ObserverDialog;
import general.Preference;
import general.UAManager;
import other.Wizard;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.lwjgl.input.Mouse;
import other.B3D_Scene;
import other.ObjectToElementConverter;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.menuing.Menu;
import tonegod.gui.core.Screen;

public class B3DApp extends SimpleApplication implements ActionListener, AnalogListener
{

    //Nodes, bullet, cam-stuff, object-lists, predefined spatials & their mats
    private B3D_Scene initialScene;
    private SceneNode sceneNode = new SceneNode();
    private Node editorNode = new Node("EditorNode");
    private EditorCamera flyingEditor;
    private DirectionalLight editorNodeCamLight = new DirectionalLight();
    private Node motionEventNode = new Node("Motion Events");
    private Node selectedNode = sceneNode;
    private BulletAppState bulletAppState;
    private CameraNode camNode;
    private Vector<NodeModel> nodeModels = new Vector<NodeModel>();
    private Vector<LightModel> lightModels = new Vector<LightModel>();
    private Vector<LightControl> lightControls = new Vector<LightControl>();
    private Vector<LightScatteringModel> lightScatteringModels = new Vector<LightScatteringModel>();
    private Vector<AdditionalCameraDialog> additionalCameraDialogs = new Vector<AdditionalCameraDialog>();
    private Vector<MotionPathModel> motionPathModels = new Vector<MotionPathModel>();
    private Gizmo gizmo;
    private Geometry gridGeometry = new Geometry("nonI_Grid", new Grid(
            (Integer) CurrentData.getPrefs().get(Preference.GRID_X),
            (Integer) CurrentData.getPrefs().get(Preference.GRID_Y),
            (Integer) CurrentData.getPrefs().get(Preference.GRID_GAP)));
    //Element & Object that are focused
    private UUID selectedUUID = Wizard.NULL_SELECTION;
    private Object selectedObject;
    //Tree needs an update?
    private boolean treeSyncNeeded = false;
    private short treeWait = 10;
    private FilterPostProcessor filterPostProcessor;
    //What on earth is the user up to?
    private InteractionType interactionType = InteractionType.Default;
    //Can be a texture that is being set on an object
    private Picture mousePicture = new Picture("MouseImage");
    //Name of the asset that is being set
    private String insertAssetName = null;
    //Make the last selected LightModel invisible again (might need rework)
    private LightModel lastSelectedLightModel = null;
    /*ui*/
    private Screen screen;
    private Menu texturesMenu;
    private ButtonAdapter selectButton;
    private ButtonAdapter cancelButton;
    private Geometry putTextureOn = null;
    private boolean updateMotionPath, allPathsShown = false;
    private boolean physicsPlaying = false;
    /*drag 'n drop*/
    private Node newParent;
    private Spatial newChild;
    private B3D_Node newB3DParent;
    private B3D_Spatial newB3DChild;
    /*shortcuts*/
    private ComboMove newProjectCombo = new ComboMove("New Project");
    private ComboMoveExecution newComboExec = new ComboMoveExecution(newProjectCombo);
    private ComboMove undoCombo = new ComboMove("Undo");
    private ComboMoveExecution undoComboExec = new ComboMoveExecution(undoCombo);
    private ComboMove redoCombo = new ComboMove("Redo");
    private ComboMoveExecution redoComboExec = new ComboMoveExecution(redoCombo);
    private ComboMove openProjectCombo = new ComboMove("Open Project");
    private ComboMoveExecution openComboExec = new ComboMoveExecution(openProjectCombo);
    private ComboMove saveSceneCombo = new ComboMove("Save Scene");
    private ComboMoveExecution saveComboExec = new ComboMoveExecution(saveSceneCombo);
    private ComboMove quitCombo = new ComboMove("Quit");
    private ComboMoveExecution quitComboExec = new ComboMoveExecution(quitCombo);
    private ComboMove renameCombo = new ComboMove("Rename");
    private ComboMoveExecution renameComboExec = new ComboMoveExecution(renameCombo);
    private ComboMove twinCombo = new ComboMove("Create Twin");
    private ComboMoveExecution twinComboExec = new ComboMoveExecution(twinCombo);
    private ComboMove findCombo = new ComboMove("Find");
    private ComboMoveExecution findComboExec = new ComboMoveExecution(findCombo);
    private ComboMove lookAtCombo = new ComboMove("LookAt");
    private ComboMoveExecution lookAtComboExec = new ComboMoveExecution(lookAtCombo);
    private ComboMove playMotionCombo = new ComboMove("Play / Pause MotionPath");
    private ComboMoveExecution playMotionComboExec = new ComboMoveExecution(playMotionCombo);
    private ComboMove deleteMove = new ComboMove("Delete");
    private ComboMoveExecution deleteExec = new ComboMoveExecution(deleteMove);
    private ComboMove startStopPhysicsMove = new ComboMove("Start / Stop Physics");
    private ComboMoveExecution startStopPhysicsExec = new ComboMoveExecution(startStopPhysicsMove);
    private ComboMove screenshotMove = new ComboMove("Screenshot");
    private ComboMoveExecution screenshotExec = new ComboMoveExecution(screenshotMove);
    private ComboMove recordMove = new ComboMove("Record");
    private ComboMoveExecution recordExec = new ComboMoveExecution(recordMove);
    private ComboMove neingagMove = new ComboMove("9gag");
    private ComboMoveExecution neingagMoveExec = new ComboMoveExecution(neingagMove);
    private ComboMove fullscreenMove = new ComboMove("bool_fullscreen");
    private ComboMoveExecution fullscreenMoveExec = new ComboMoveExecution(fullscreenMove);
    private HashSet<String> pressedMappings = new HashSet<String>();
    private ComboMove currentMove = null;
    private float currentMoveCastTime = 0;
    private float time = 0;
    private boolean waterTexturesSynced = false, returnToNormalSpeed = false;
    private Vector<Spatial> spatials = new Vector<Spatial>();
    private NodeModel currentNodeModel;
    //AnimationFrame selected
    private BitmapText frameText;
    private boolean keyframePlaying = false;

    /**
     * Just settin variables
     *
     * @param camFrustumFar
     */
    public B3DApp(float camFrustumFar, B3D_Scene initScene)
    {
        Wizard.setApp(this);
        initialScene = initScene;
    }

    /**
     * Sets the gridLocation regarding the gap-sizes and number of lines
     */
    public void correctGridLocation()
    {
        int gap = (Integer) CurrentData.getPrefs().get(Preference.GRID_GAP);
        int gx = (Integer) CurrentData.getPrefs().get(Preference.GRID_X);
        int gy = (Integer) CurrentData.getPrefs().get(Preference.GRID_Y);
        gridGeometry.setLocalTranslation(gx * gap / -2, 0, gy * gap / -2);
    }

    public void playPhysics()
    {
        bulletAppState.setEnabled(true);
        physicsPlaying = true;
        Vector<Spatial> allObjects = new Vector<Spatial>();
        Wizard.insertAllSpatials(sceneNode, allObjects);
    }

    public void pausePhysics()
    {
        physicsPlaying = false;
        bulletAppState.setEnabled(false);
    }

    private void checkPicking(Spatial pickedGeometry)
    {
        if (Wizard.getObjectReferences().getUUID(pickedGeometry.hashCode()) == null)
        {
            if (pickedGeometry.getParent() != null)
                checkPicking(pickedGeometry.getParent());
        } else
        {
            gizmo.select(null);
            setSelectedUUID(Wizard.getObjectReferences().getUUID(pickedGeometry.hashCode()));
            setSelectedObject(selectedObject);
        }
    }

    private void syncWaterTextures()
    {
        for (Filter f : filterPostProcessor.getFilterList())
        {
            if (f instanceof WaterFilterWithGetters)
            {
                UUID elementUUID = Wizard.getObjectReferences().getUUID(f.hashCode());
                B3D_Element water = Wizard.getObjects().getB3D_Element(elementUUID);
                ((WaterFilterWithGetters) f).setHeightTexture((Texture2D) assetManager.loadTexture(((B3D_Water) water).getHeightTexture()));
                ((WaterFilterWithGetters) f).setCausticsTexture((Texture2D) assetManager.loadTexture(((B3D_Water) water).getCausistsTexture()));
                ((WaterFilterWithGetters) f).setFoamTexture((Texture2D) assetManager.loadTexture(((B3D_Water) water).getFoamTexture()));
                ((WaterFilterWithGetters) f).setNormalTexture((Texture2D) assetManager.loadTexture(((B3D_Water) water).getNormalTexture()));
            }
        }
    }

    public void sortFilters()
    {
        viewPort.removeProcessor(filterPostProcessor);
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.addAll(filterPostProcessor.getFilterList());
        filterPostProcessor.removeAllFilters();
        Collections.sort(filters, new Comparator<Filter>()
        {
            @Override
            public int compare(Filter o1, Filter o2)
            {
                UUID filter1UUID = Wizard.getObjectReferences().getUUID(o1.hashCode());
                UUID filter2UUID = Wizard.getObjectReferences().getUUID(o2.hashCode());
                B3D_Filter b3D_Filter1 = (B3D_Filter) Wizard.getObjects().getB3D_Element(filter1UUID);
                B3D_Filter b3D_Filter2 = (B3D_Filter) Wizard.getObjects().getB3D_Element(filter2UUID);
                return b3D_Filter1.getFilterIndex() - b3D_Filter2.getFilterIndex();
            }
        });
        //System.out.println("Sorting Filters:");
        int count = 0;
        for (Filter f : filters)
        {
            //System.out.println("Next: " + f.getName() + " at " + count++);
            filterPostProcessor.addFilter(f);
        }
        viewPort.addProcessor(filterPostProcessor);
    }

    public void frameSelected(int frame)
    {
        frameText.setText("Animation Frame: " + frame);
        guiNode.attachChild(frameText);
    }

    public void frameUnselected()
    {
        guiNode.detachChild(frameText);
    }

    public enum InteractionType
    {

        Default,
        InsertTexture,
        InsertModel,
        DontDoANYTHING
    };

    @Override
    public void simpleInitApp()
    {
        frameText = new BitmapText(guiFont);
        frameText.setLocalTranslation(300, 30, 0);
        gizmo = new Gizmo(assetManager);
        flyingEditor = new EditorCamera(cam);
        flyingEditor.registerWithInput(inputManager);
        CurrentData.getEditorWindow().getMainMenu().getExtrasMenu().getAutosaveEnabledItem().setSelected(
                CurrentData.getProject().getAutosaveOptions().isEnabled());
        CurrentData.getEditorWindow().getFieldOfViewButton().setEnabled(true);
        CurrentData.getEditorWindow().getAddCamButton().setEnabled(true);
        CurrentData.getAnimationScriptDialog().setVisible((Boolean) CurrentData.getPrefs().get(Preference.ANIMATIONSCRIPT_DIALOG_VISIBLE));
        float fov[] = (float[]) CurrentData.getPrefs().get(Preference.FIELD_OF_VIEW);
        cam.setFrustum(fov[0], fov[1], fov[2], fov[3], fov[4], fov[5]);
        CurrentData.setDefaultFieldOfView(new Float[]
        {
            cam.getFrustumBottom(),
            cam.getFrustumFar(),
            cam.getFrustumLeft(),
            cam.getFrustumNear(),
            cam.getFrustumRight(),
            cam.getFrustumTop()
        });
        initComponents();
        initBasicSettings();
        filterPostProcessor = new FilterPostProcessor(assetManager);
        if ((Boolean) CurrentData.getPrefs().get(Preference.SHOW_FILTERS))
            viewPort.addProcessor(filterPostProcessor);
        initInput();
        initMonkeyGUI();
        Wizard.setAssetManager(assetManager);
        Wizard.setCamera(cam);
        Wizard.setCameraNode(camNode);
        Wizard.setSceneNode(sceneNode);
        CurrentData.buildSceneIntoEditor(initialScene);
        CurrentData.setAssetBrowserDialog(new AssetBrowserDialog());
        flyCam.unregisterInput();
        flyCam.setEnabled(false);
    }

    /**
     *
     * @param tpf
     */
    @Override
    public void simpleUpdate(float tpf)
    {
        if (returnToNormalSpeed)
            flyingEditor.setMoveSpeed((Float) CurrentData.getPrefs().get(Preference.CAM_SPEED));
        if (!waterTexturesSynced)
        {
            syncWaterTextures();
            waterTexturesSynced = true;
        }
        if (CurrentData.getEditorWindow().getKeyframeAnimationEditor().getCurrentAnimation() != null)
        {
            keyframePlaying = CurrentData.getEditorWindow().getKeyframeAnimationEditor().getCurrentAnimation().isPlaying();
            Wizard.updateCustomAnimations(tpf);
            if (keyframePlaying)
                CurrentData.getEditorWindow().getKeyframeAnimationEditor().updateOnPlay();
            keyframePlaying = CurrentData.getEditorWindow().getKeyframeAnimationEditor().getCurrentAnimation().isPlaying();
        }
        Wizard.updateKeyframeAnimations();
        spatials.clear();
        Wizard.insertAllSpatials(sceneNode, spatials);
        for (Spatial s : spatials)
        {
            if (s.getUserData("correctedTranslation") != null)
            {
                if (!((Vector3f) s.getUserData("correctedTranslation")).equals(s.getLocalTranslation()))
                {
                    s.setLocalTranslation((Vector3f) s.getUserData("correctedTranslation"));
                    if (s.getControl(RigidBodyControl.class) != null)
                        s.getControl(RigidBodyControl.class).setPhysicsLocation((Vector3f) s.getWorldTranslation());
                    s.setUserData("correctedTranslation", null);
                }
            }
            if (!bulletAppState.isEnabled() && s.getControl(RigidBodyControl.class) != null)
                bulletAppState.getPhysicsSpace().add(s);
            if (s.getUserData("adjust") != null)
            {
                B3D_Spatial b3D_Spatial = (B3D_Spatial) Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(s.hashCode()));
                s.setLocalTranslation(b3D_Spatial.getTranslation());
                s.getControl(RigidBodyControl.class).setPhysicsLocation(b3D_Spatial.getPhysics().getPhysicsLocation());
                s.getUserDataKeys().remove("adjust");
            }
        }
        checkForShortcut(tpf);
        if (!CurrentData.isAppRunning())
            CurrentData.setAppRunning(true);
        if (newParent != null)
            updatePairing();
        updateEditNode();
        updateDragging();
        if (treeSyncNeeded)
            if (--treeWait == 0)
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        CurrentData.getEditorWindow().getTree().sync();
                        treeSyncNeeded = false;
                        treeWait = 10;
                    }
                });
        mousePicture.setLocalTranslation(inputManager.getCursorPosition().x - 20, inputManager.getCursorPosition().y - 20, 10);
        updateAdditionalCameras();
        returnToNormalSpeed = true;
    }

    private void checkForShortcut(float tpf)
    {
        time += tpf;
        newComboExec.updateExpiration(time);
        if (currentMove != null)
        {
            currentMoveCastTime -= tpf;
            if (currentMoveCastTime <= 0)
            {
                if (currentMove.equals(newProjectCombo))
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            CurrentData.execNewScene();
                        }
                    });
                } else if (currentMove.equals(openProjectCombo))
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            CurrentData.execOpenProject(null);
                        }
                    });
                } else if (currentMove.equals(undoCombo))
                {
                    UAManager.undo();
                } else if (currentMove.equals(redoCombo))
                {
                    UAManager.redo();
                } else if (currentMove.equals(saveSceneCombo))
                {
                    CurrentData.execSaveScene(CurrentData.getProject().getMainFolder().getAbsolutePath() + "/" + CurrentData.getProject().getScene().getName() + ".b3ds");
                } else if (currentMove.equals(quitCombo))
                {
                    CurrentData.execQuit();
                } else if (currentMove.equals(renameCombo))
                {
                    CurrentData.execRename();
                } else if (currentMove.equals(twinCombo))
                {
                    CurrentData.execDuplicate();
                } else if (currentMove.equals(findCombo))
                {
                    CurrentData.execFind();
                } else if (currentMove.equals(lookAtCombo))
                {
                    CurrentData.execLookAt();
                } else if (currentMove.equals(playMotionCombo))
                {
                    CurrentData.execPlayPauseMotionPath();
                } else if (currentMove.equals(deleteMove))
                {
                    CurrentData.execDelete(true);
                } else if (currentMove.equals(startStopPhysicsMove))
                {
                    CurrentData.setPhysicsRunning(!isPhysicsPlaying());
                } else if (currentMove.equals(screenshotMove))
                {
                    CurrentData.execScreenshot();
                } else if (currentMove.equals(recordMove))
                {
                    CurrentData.execRecord();
                } else if (currentMove.equals(neingagMove))
                {
                    CurrentData.exec9gag();
                } else if (currentMove.equals(fullscreenMove))
                {
                    CurrentData.execFullscreen();
                }
                currentMoveCastTime = 0;
                currentMove = null;
            }
        }
    }

    private void initMonkeyGUI()
    {
        screen = new Screen(this);
        guiNode.addControl(screen);
    }

    private void initBasicSettings()
    {
        initView();
        initPhysics();
        CurrentData.updateAssetRegister();
    }

    private void initPhysics()
    {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setEnabled(false);
    }

    private void initView()
    {
        if (CurrentData.getProject().getScene().getViewPortColor() == null)
        {
            viewPort.setBackgroundColor(ColorRGBA.Black);
            CurrentData.getProject().getScene().setViewPortColor(ColorRGBA.Black);
        } else
            viewPort.setBackgroundColor(CurrentData.getProject().getScene().getViewPortColor());
        setPauseOnLostFocus(false);
        cam.setLocation(new Vector3f(0, 0, 0));
        flyingEditor.setDragToRotate(true);
        flyingEditor.setMoveSpeed((Float) CurrentData.getPrefs().get(Preference.CAM_SPEED));
    }

    private void initComponents()
    {
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        rootNode.attachChild(sceneNode);
        if (!(Boolean) CurrentData.getPrefs().get(Preference.SHOW_SCENERY))
            rootNode.attachChild(editorNode);
        editorNode.attachChild(motionEventNode);
        editorNode.addLight(new AmbientLight());
        editorNode.addLight(editorNodeCamLight);
        gridGeometry.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        gridGeometry.getMaterial().setColor("Color", new ColorRGBA(0.22f, 0.24f, 0.22f, 1.0f));
        gridGeometry.setShadowMode(RenderQueue.ShadowMode.Off);
        gridGeometry.setLocalTranslation(-10000, 0, -10000);
        mousePicture.setLocalScale(40, 40, 10);
        editorNode.attachChild(gizmo);
        if ((Boolean) CurrentData.getPrefs().get(Preference.SHOW_GRID))
            editorNode.attachChild(gridGeometry);
        correctGridLocation();
    }

    public void setSelectedUUID(UUID uuid)
    {
        selectedUUID = uuid;
        //System.out.println("Selected UUID: " + uuid);
        if (selectedUUID != null && !selectedUUID.equals(Wizard.NULL_SELECTION))
        {
            UAManager.curr(Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(uuid)), uuid);
            setSelectedObject(Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(uuid)));
        }
    }

    private void setSelectedObject(Object object)
    {
        CurrentData.getEditorWindow().getTree().setCodeSelect(false);
        if (lastSelectedLightModel != null)
        {
            lastSelectedLightModel.setSymbolVisible(false);
        }
        if (object instanceof Spatial)
        {
            Spatial tempSpatial = (Spatial) object;
            if (tempSpatial.getName().equals("LightModel"))
            {
                for (int i = 0; i < lightModels.size(); i++)
                {
                    if (lightModels.get(i).getNode().equals(tempSpatial))
                    {
                        setSelectedObject(lightModels.get(i).getLight());
                    }
                }
            } else
            {
                selectedObject = object;
                if (!(object instanceof TerrainQuad) && object instanceof Node && !(Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(object.hashCode())) instanceof B3D_Model))
                {
                    setSelectedNode((Node) object);
                } else
                {
                    setSelectedNode(sceneNode);
                }
            }
        } else
        {
            selectedObject = object;
            if (selectedObject instanceof Light)
            {
                for (int i = 0; i < lightModels.size(); i++)
                {
                    if (lightModels.get(i).getLight().equals(selectedObject))
                    {
                        lightModels.get(i).setSymbolVisible(true);
                        lastSelectedLightModel = lightModels.get(i);
                    }
                }
            }
        }
        if (Wizard.getObjects().getB3D_Element(selectedUUID) instanceof B3D_MotionEvent)
        {
            updateSelectedMotionPath();
        }
        updateSelection();
        CurrentData.getEditorWindow().getEditPane().arrange(false);
    }

    /**
     *
     * @param id
     * @param spatial
     */
    public void setSelectedElement(UUID uuid, Spatial spatial)
    {
        selectedUUID = uuid;
        //System.out.println("Selected UUID: " + uuid);
        if (selectedUUID != null && !selectedUUID.equals(Wizard.NULL_SELECTION))
            UAManager.curr(Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(uuid)), uuid);
        if (spatial == null)
        {
            setSelectedObject(null);
        } else
        {
            int objectID = Wizard.getObjectReferences().getID(uuid);
            setSelectedObject(Wizard.getObjects().getOriginalObject(objectID), spatial);
        }
    }

    /**
     *
     * @param object
     * @param spatial
     */
    public void setSelectedObject(Object object, Spatial spatial)
    {
        UAManager.curr(object, Wizard.getObjectReferences().getUUID(object.hashCode()));
        CurrentData.getEditorWindow().getTree().setCodeSelect(true);
        if (lastSelectedLightModel != null)
        {
            lastSelectedLightModel.setSymbolVisible(false);
        }
        if (object instanceof Spatial)
        {
            Spatial tempSpatial = (Spatial) object;
            if (tempSpatial.getName().equals("LightModel"))
            {
                for (int i = 0; i < lightModels.size(); i++)
                {
                    if (lightModels.get(i).getNode().equals(tempSpatial))
                    {
                        setSelectedObject(lightModels.get(i).getLight());
                        break;
                    }
                }
            } else
            {
                selectedObject = object;
                if (!(object instanceof TerrainQuad) && object instanceof Node && !(Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(object.hashCode())) instanceof B3D_Model))
                {
                    setSelectedNode((Node) object);
                } else
                {
                    setSelectedNode(sceneNode);
                }
            }
            CurrentData.getEditorWindow().getEditPane().arrange(false);
        } else if (object instanceof MotionEvent)
        {
            if (spatial.getName().equals("motionPathSymbol"))
            {
                for (MotionPathModel mpm : motionPathModels)
                {
                    if (mpm.getSymbol().equals(spatial))
                    {
                        selectedObject = mpm.getMotionEvent();
                    }
                }
            } else
            {
                selectedObject = spatial;
            }
        } else
        {
            selectedObject = object;
            if (selectedObject instanceof Light)
            {
                for (int i = 0; i < lightModels.size(); i++)
                {
                    if (lightModels.get(i).getLight().equals(selectedObject))
                    {
                        lightModels.get(i).setSymbolVisible(true);
                        lastSelectedLightModel = lightModels.get(i);
                        break;
                    }
                }
            }
        }
        CurrentData.getEditorWindow().getEditPane().arrange(false);
        updateSelection();
    }

    /**
     *
     * @param spatial
     */
    public void setSelectedElement(Spatial spatial)
    {
        if (spatial == null)
        {
            selectedUUID = Wizard.NULL_SELECTION;
            setSelectedObject(null);
        } else
        {
            setSelectedObject(spatial);
            selectedUUID = Wizard.getObjectReferences().getUUID(spatial.hashCode());
        }
    }

    public void updateSelection()
    {
        enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                if (selectedObject instanceof Spatial)
                {
                    Spatial tempSpatial = (Spatial) selectedObject;
                    if (!tempSpatial.getName().equals("SkyBox") && tempSpatial != null)
                        gizmo.setLocalTranslation(tempSpatial.getWorldTranslation());
                } else if (selectedObject instanceof MotionEvent)
                {
                    for (MotionPathModel mpm : motionPathModels)
                        if (mpm.getMotionEvent().equals(selectedObject))
                            gizmo.setLocalTranslation(mpm.getSymbol().getLocalTranslation());
                } else if (selectedObject instanceof Light)
                {
                    for (int i = 0; i < lightModels.size(); i++)
                        if (lightModels.get(i).getLight().equals(selectedObject))
                            if (!(lightModels.get(i).getLight() instanceof AmbientLight))
                                /*Highlight LightModelRep.*/
                                gizmo.setLocalTranslation(lightModels.get(i).getRepresentative().getWorldTranslation());
                } else if (selectedObject instanceof LightScatteringFilter)
                {
                    for (LightScatteringModel lsm : lightScatteringModels)
                        if (lsm.getScatteringFilter().equals(selectedObject))
                            gizmo.setLocalTranslation(lsm.getSymbol().getWorldTranslation());
                } else if (selectedObject instanceof AdditionalCameraDialog)
                {
                    Spatial tempSpatial = ((AdditionalCameraDialog) selectedObject).getRepresentative();
                    gizmo.setLocalTranslation(tempSpatial.getWorldTranslation());
                }
                return null;
            }
        });
    }

    private void initInput()
    {
        inputManager.addMapping("mouseLeft", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("mouseRight", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("mouseMiddle", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addMapping("keyX", new KeyTrigger(KeyInput.KEY_X));
        inputManager.addMapping("keyY", new KeyTrigger(KeyInput.KEY_Y));
        inputManager.addMapping("keyZ", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping("doubleSpeed", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addListener(this, new String[]
        {
            "mouseLeft", "mouseRight", "mouseMiddle", "keyX", "keyY", "keyZ", "doubleSpeed"
        });
        initShortcuts();
    }

    private void initShortcuts()
    {
        inputManager.addMapping("strg", new KeyTrigger(KeyInput.KEY_LCONTROL));
        inputManager.addMapping("n", new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("t", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("z", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("y", new KeyTrigger(KeyInput.KEY_Y));
        inputManager.addMapping("s", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("o", new KeyTrigger(KeyInput.KEY_O));
        inputManager.addMapping("q", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("r", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("l", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("f", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("m", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("delete", new KeyTrigger(KeyInput.KEY_DELETE));
        inputManager.addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("startStopPhysics", new KeyTrigger(KeyInput.KEY_F6));
        inputManager.addMapping("screenshot", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("record", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addMapping("9gag", new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addMapping("bool_fullscreen", new KeyTrigger(KeyInput.KEY_F7));
        inputManager.addListener(this, new String[]
        {
            "strg", "n", "t", "z", "y", "s", "r", "o", "q", "l", "f", "m", "delete", "space",
            "startStopPhysics", "screenshot", "record", "9gag", "bool_fullscreen"
        });
        /*strg + n*/
        newProjectCombo.setCastTime(0);
        newProjectCombo.press("strg").notPress("n").done();
        newProjectCombo.press("strg", "n").done();
        newProjectCombo.notPress("strg", "n").done();
        /*strg + z*/
        undoCombo.setCastTime(0);
        undoCombo.press("strg").notPress("z").done();
        undoCombo.press("strg", "z").done();
        undoCombo.notPress("z").done();
        /*strg + y*/
        redoCombo.setCastTime(0);
        redoCombo.press("strg").notPress("y").done();
        redoCombo.press("strg", "y").done();
        redoCombo.notPress("y").done();
        /*strg + 0*/
        openProjectCombo.setCastTime(0);
        openProjectCombo.press("strg").notPress("o").done();
        openProjectCombo.press("strg", "o").done();
        openProjectCombo.notPress("strg", "o").done();
        /*strg + s*/
        saveSceneCombo.setCastTime(0);
        saveSceneCombo.press("strg").notPress("s").done();
        saveSceneCombo.press("strg", "s").done();
        saveSceneCombo.notPress("strg", "s").done();
        /*strg + q*/
        quitCombo.setCastTime(0);
        quitCombo.press("strg").notPress("q").done();
        quitCombo.press("strg", "q").done();
        quitCombo.notPress("strg", "q").done();
        /*strg + r*/
        renameCombo.setCastTime(0);
        renameCombo.press("strg").notPress("r").done();
        renameCombo.press("strg", "r").done();
        renameCombo.notPress("strg", "r").done();
        /*strg + t*/
        twinCombo.setCastTime(0);
        twinCombo.press("strg").notPress("t").done();
        twinCombo.press("strg", "t").done();
        twinCombo.notPress("strg", "t").done();
        /*strg + f*/
        findCombo.setCastTime(0);
        findCombo.press("strg").notPress("f").done();
        findCombo.press("strg", "f").done();
        findCombo.notPress("strg", "f").done();
        /*strg + l*/
        lookAtCombo.setCastTime(0);
        lookAtCombo.press("strg").notPress("l").done();
        lookAtCombo.press("strg", "l").done();
        lookAtCombo.notPress("strg", "l").done();
        /*strg + m*/
        playMotionCombo.setCastTime(0);
        playMotionCombo.press("strg").notPress("m").done();
        playMotionCombo.press("strg", "m").done();
        playMotionCombo.notPress("strg", "m").done();
        /*entf*/
        deleteMove.setCastTime(0);
        deleteMove.press("delete").done();
        deleteMove.notPress("delete").done();
        /*f6*/
        startStopPhysicsMove.setCastTime(0);
        startStopPhysicsMove.press("startStopPhysics").done();
        startStopPhysicsMove.notPress("startStopPhysics").done();
        /*f1*/
        screenshotMove.setCastTime(0);
        screenshotMove.press("screenshot").done();
        screenshotMove.notPress("screenshot").done();
        /*f2*/
        recordMove.setCastTime(0);
        recordMove.press("record").done();
        recordMove.notPress("record").done();
        /*f3*/
        neingagMove.setCastTime(0);
        neingagMove.press("9gag").done();
        neingagMove.notPress("9gag").done();
        /*f3*/
        fullscreenMove.setCastTime(0);
        fullscreenMove.press("bool_fullscreen").done();
        fullscreenMove.notPress("bool_fullscreen").done();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        // Record pressed mappings
        if (isPressed)
            pressedMappings.add(name);
        else
            pressedMappings.remove(name);
        List<ComboMove> invokedMoves = new ArrayList<ComboMove>();
        if (newComboExec.updateState(pressedMappings, time))
            invokedMoves.add(newProjectCombo);
        else if (undoComboExec.updateState(pressedMappings, time))
            invokedMoves.add(undoCombo);
        else if (redoComboExec.updateState(pressedMappings, time))
            invokedMoves.add(redoCombo);
        else if (openComboExec.updateState(pressedMappings, time))
            invokedMoves.add(openProjectCombo);
        else if (saveComboExec.updateState(pressedMappings, time))
            invokedMoves.add(saveSceneCombo);
        else if (quitComboExec.updateState(pressedMappings, time))
            invokedMoves.add(quitCombo);
        else if (renameComboExec.updateState(pressedMappings, time))
            invokedMoves.add(renameCombo);
        else if (twinComboExec.updateState(pressedMappings, time))
            invokedMoves.add(twinCombo);
        else if (findComboExec.updateState(pressedMappings, time))
            invokedMoves.add(findCombo);
        else if (lookAtComboExec.updateState(pressedMappings, time))
            invokedMoves.add(lookAtCombo);
        else if (playMotionComboExec.updateState(pressedMappings, time))
            invokedMoves.add(playMotionCombo);
        else if (deleteExec.updateState(pressedMappings, time))
            invokedMoves.add(deleteMove);
        else if (startStopPhysicsExec.updateState(pressedMappings, time))
            invokedMoves.add(startStopPhysicsMove);
        else if (screenshotExec.updateState(pressedMappings, time))
            invokedMoves.add(screenshotMove);
        else if (recordExec.updateState(pressedMappings, time))
            invokedMoves.add(recordMove);
        else if (neingagMoveExec.updateState(pressedMappings, time))
            invokedMoves.add(neingagMove);
        else if (fullscreenMoveExec.updateState(pressedMappings, time))
            invokedMoves.add(fullscreenMove);
        if (invokedMoves.size() > 0)
        {
            // identify the move with highest priority
            float priority = 0;
            ComboMove toExec = null;
            for (ComboMove move : invokedMoves)
            {
                if (move.getPriority() > priority)
                {
                    priority = move.getPriority();
                    toExec = move;
                }
            }
            currentMove = toExec;
            currentMoveCastTime = currentMove.getCastTime();
        }
        if (name.equals("space"))
        {
            if (isPressed)
                interactionType = InteractionType.DontDoANYTHING;
            else
                interactionType = InteractionType.Default;
        }
        if (name.equals("mouseLeft"))
        {
            CollisionResults results = new CollisionResults();
            Vector2f click2d = inputManager.getCursorPosition();
            Vector3f click3d = cam.getWorldCoordinates(
                    new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(
                    new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
            Ray ray = new Ray(click3d, dir);
            rootNode.collideWith(ray, results);
            if (results.getClosestCollision() != null)
            {
                Geometry pickedGeometry = results.getClosestCollision().getGeometry();
                boolean checking = true;
                int cNumber = 0;
                while (checking)
                {
                    if (pickedGeometry.getName() == null || pickedGeometry.getName().equals("LightSymbol") || pickedGeometry.getName().contains("nonI_")
                            || pickedGeometry.getUserData("north") != null)
                        try
                        {
                            if (results.getCollision(cNumber++).getGeometry() != null)
                                pickedGeometry = results.getCollision(cNumber).getGeometry();
                        } catch (java.lang.IndexOutOfBoundsException ioobe)
                        {
                            checking = false;
                        }
                    else
                        checking = false;
                }
                System.out.println("Angeklickt: " + pickedGeometry.getName());
                if (interactionType.equals(InteractionType.Default) && isPressed)
                {
                    if (pickedGeometry.hasAncestor(gizmo))
                        if (pickedGeometry.hasAncestor(gizmo.getxArrow()))
                            gizmo.select(Gizmo.Arrow.X);
                        else if (pickedGeometry.hasAncestor(gizmo.getyArrow()))
                            gizmo.select(Gizmo.Arrow.Y);
                        else
                            gizmo.select(Gizmo.Arrow.Z);
                    else if (pickedGeometry.getName().equals("cam") && editorNode.hasChild(pickedGeometry))
                    {
                        selectedUUID = Wizard.NULL_SELECTION;
                        for (AdditionalCameraDialog acd : additionalCameraDialogs)
                            if (acd.getRepresentative().hasChild(pickedGeometry))
                            {
                                selectedObject = acd;
                                CurrentData.getEditorWindow().getEditPane().arrange(true);
                                break;
                            }
                    } else if (pickedGeometry.getName().equals("motionPathSymbol"))
                    {
                        for (MotionPathModel mpm : motionPathModels)
                            if (pickedGeometry.equals(mpm.getSymbol()))
                            {
                                setSelectedElement(Wizard.getObjectReferences().getUUID(mpm.getMotionEvent().hashCode()), mpm.getSymbol());
                                CurrentData.getEditorWindow().getTree().updateSelection();
                            }
                    } else if (pickedGeometry.getName().equals("LightModel"))
                    {
                        for (int i = 0; i < lightModels.size(); i++)
                        {
                            try
                            {
                                if (lightModels.get(i).getNode().equals(pickedGeometry)
                                        || lightModels.get(i).getRepresentative().equals(pickedGeometry))
                                {
                                    setSelectedObject(lightModels.get(i).getLight());
                                    for (B3D_Element element : Wizard.getObjects().getB3D_ElementsIterator())
                                    {
                                        UUID elementUUID = Wizard.getObjectReferences().getUUID(lightModels.get(i).getLight().hashCode());
                                        if (Wizard.getObjects().getB3D_Element(elementUUID).equals(element))
                                        {
                                            setSelectedUUID(elementUUID);
                                            CurrentData.getEditorWindow().getTree().updateSelection();
                                        }
                                    }
                                }
                            } catch (NullPointerException npe)
                            {
                                ObserverDialog.getObserverDialog().printError("Error picking LightModel", npe);
                            }
                        }
                    } else if (pickedGeometry.getName().equals("LightScatteringSymbol"))
                    {
                        for (LightScatteringModel lsm : lightScatteringModels)
                        {
                            if (lsm.getSymbol().equals(pickedGeometry))
                            {
                                UUID elementUUID = Wizard.getObjectReferences().getUUID(lsm.getScatteringFilter().hashCode());
                                setSelectedElement(elementUUID, lsm.getSymbol());
                                CurrentData.getEditorWindow().getTree().updateSelection();
                            }
                        }
                    } else if (pickedGeometry.getName().equals("nodeModel"))
                    {
                        for (NodeModel nodeModel : nodeModels)
                        {
                            if (nodeModel.getModel().equals(pickedGeometry)
                                    || nodeModel.getModel().equals(pickedGeometry.getParent())
                                    || nodeModel.getModel().equals(pickedGeometry.getParent().getParent()))
                            {
                                UUID elementUUID = Wizard.getObjectReferences().getUUID(nodeModel.getNode().hashCode());
                                setSelectedUUID(elementUUID);
                                CurrentData.getEditorWindow().getTree().updateSelection();
                            }
                        }
                    } else if (pickedGeometry.getName().equals("Waypoint"))
                    {
                        setSelectedElement((Wizard.getObjectReferences().getUUID((Integer) pickedGeometry.getUserData("motionPathID"))), pickedGeometry);
                        CurrentData.getEditorWindow().getTree().updateSelection();
                    } else if (pickedGeometry.getUserData("north") == null)
                    {
                        checkPicking(pickedGeometry);
                        CurrentData.getEditorWindow().getTree().updateSelection();
                    }
                } else if (interactionType.equals(InteractionType.InsertTexture)
                        && !pickedGeometry.getName().equals("LightSymbol")
                        && !pickedGeometry.getName().contains("nonI_"))
                {
                    UUID elementUUID = Wizard.getObjectReferences().getUUID(pickedGeometry.hashCode());
                    if (!(Wizard.getObjects().getB3D_Element(elementUUID) instanceof B3D_Model))
                    {
                        putTextureOn = pickedGeometry;
                        UUID putTextureOnUUID = Wizard.getObjectReferences().getUUID(putTextureOn.hashCode());
                        selectedUUID = putTextureOnUUID;
                        selectedObject = putTextureOn;
                        HashMap<String, String> tempHashMap = Wizard.readMat(new File("matD//" + pickedGeometry.getMaterial().getMaterialDef().getAssetName()));
                        ArrayList<String> textures = new ArrayList<String>();
                        for (Map.Entry<String, String> e : tempHashMap.entrySet())
                        {
                            if (e.getValue().equals("Texture") || e.getValue().equals("Texture2D"))
                            {
                                textures.add(e.getKey());
                            }
                        }
                        createTexturePopup(textures);
                        setInteractionType(InteractionType.Default, null);
                    } else
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                JOptionPane.showMessageDialog(null, "Can not be placed on a predefined model", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                } else if (interactionType.equals(InteractionType.InsertModel))
                {
                    UAManager.curr(null, Wizard.NULL_SELECTION);
                    Spatial s = CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadModel(insertAssetName);
                    s.setUserData("angles", new Vector3f());
                    s.setUserData("scale", new Vector3f(1, 1, 1));
                    s.setUserData("modelName", insertAssetName);
                    s.setName("Model");
                    s.setLocalTranslation(cam.getLocation().add(cam.getDirection().mult(10)));
                    /*Set UserData to all children*/
                    if (s instanceof Node)
                    {
                        Vector<Spatial> geoms = new Vector<Spatial>();
                        Wizard.insertAllSpatials((Node) s, geoms);
                        for (Spatial g : geoms)
                        {
                            g.setUserData("angles", new Vector3f());
                            g.setUserData("scale", new Vector3f(1, 1, 1));
                            g.setUserData("modelChild", "yup");
                            g.setUserData("modelName", "modelChild");
                            g.setShadowMode(RenderQueue.ShadowMode.Inherit);
                            g.setName("modelChild");
                        }
                    }
                    Wizard.getObjects().add(s, ObjectToElementConverter.convertToElement(s));
                    sceneNode.attachChild(s);
                    setInteractionType(InteractionType.Default, null);
                    UAManager.add(s, "Add " + s.getName());
                }
            }
        } else if (name.equals("mouseMiddle") && isPressed)
        {
            //??
        } else if (name.equals("mouseRight") && isPressed)
            setInteractionType(InteractionType.Default, null);
        else if (name.equals("keyX"))
            gizmo.select(Gizmo.Arrow.X);
        else if (name.equals("keyY"))
            gizmo.select(Gizmo.Arrow.Y);
        else if (name.equals("keyZ"))
            gizmo.select(Gizmo.Arrow.Z);

    }

    @Override
    public void onAnalog(String name, float value, float tpf)
    {
        if (name.equals("doubleSpeed"))
        {
            flyingEditor.setMoveSpeed((Float) CurrentData.getPrefs().get(Preference.CAM_SPEED) * 2);
            returnToNormalSpeed = false;
        }
    }

    private void updateAdditionalCameras()
    {
        for (AdditionalCameraDialog acd : additionalCameraDialogs)
            if (acd.initialized())
            {
                if (acd.getControlPanel().getFilterChecker().isChecked())
                {
                    if (!acd.getViewPort().getProcessors().contains(acd.getFilterPostProcessor()))
                        acd.getViewPort().addProcessor(acd.getFilterPostProcessor());
                } else if (acd.getViewPort().getProcessors().contains(acd.getFilterPostProcessor()))
                    acd.getViewPort().removeProcessor(acd.getFilterPostProcessor());
            }
    }
    private boolean wasDragging = false;

    private void updateDragging()
    {
        if (Mouse.isCreated())
        {
            if (Mouse.isButtonDown(1))
            {
                wasDragging = true;
                CollisionResults results = new CollisionResults();
                Vector2f click2d = inputManager.getCursorPosition();
                Vector3f click3d = cam.getWorldCoordinates(
                        new Vector2f(click2d.x, click2d.y), 0f).clone();
                Vector3f dir = cam.getWorldCoordinates(
                        new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
                Ray ray = new Ray(click3d, dir);
                if (selectedObject instanceof WaterFilterWithGetters)
                {
                    gizmo.getyArrow().collideWith(ray, results);
                    CollisionResult closest = results.getClosestCollision();
                    if (closest != null)
                    {
                        WaterFilterWithGetters selectedWaterFilter = (WaterFilterWithGetters) selectedObject;
                        selectedWaterFilter.setWaterHeight(closest.getContactPoint().getY());
                    }
                } else
                {
                    rootNode.collideWith(ray, results);
                    boolean selectionLegit = false;
                    int resultNr = 0;
                    while (!selectionLegit)
                    {
                        if (results.size() > resultNr)
                        {
                            if (!results.getCollision(resultNr).getGeometry().getName().equals("LightSymbol")
                                    && !results.getCollision(resultNr).getGeometry().getName().contains("nonI_"))
                            {
                                selectionLegit = true;
                            } else
                            {
                                resultNr++;
                            }
                        } else
                        {
                            return;
                        }
                    }
                    if (results.getCollision(resultNr) != null && selectedObject != null)
                    {
                        float xDiff = 0, yDiff = 0, zDiff = 0;
                        if (gizmo.getSelectedArrow() != null)
                        {
                            if (gizmo.getSelectedArrow().equals(gizmo.getxArrow()))
                            {
                                xDiff = results.getCollision(resultNr).getContactPoint().getX() - gizmo.getLocalTranslation().getX();
                                gizmo.setLocalTranslation(new Vector3f(
                                        results.getCollision(resultNr).getContactPoint().getX(),
                                        gizmo.getLocalTranslation().getY(),
                                        gizmo.getLocalTranslation().getZ()));
                            } else if (gizmo.getSelectedArrow().equals(gizmo.getyArrow()))
                            {
                                yDiff = results.getCollision(resultNr).getContactPoint().getY() - gizmo.getLocalTranslation().getY();
                                gizmo.setLocalTranslation(new Vector3f(
                                        gizmo.getLocalTranslation().getX(),
                                        results.getCollision(resultNr).getContactPoint().getY(),
                                        gizmo.getLocalTranslation().getZ()));
                            } else
                            {
                                zDiff = results.getCollision(resultNr).getContactPoint().getZ() - gizmo.getLocalTranslation().getZ();
                                gizmo.setLocalTranslation(new Vector3f(
                                        gizmo.getLocalTranslation().getX(),
                                        gizmo.getLocalTranslation().getY(),
                                        results.getCollision(resultNr).getContactPoint().getZ()));
                            }
                            if (selectedObject instanceof Spatial)
                            {
                                Spatial tempSpatial = (Spatial) selectedObject;
                                if (tempSpatial.getName().equals("Waypoint"))
                                {
                                    int motionEventID = Wizard.getObjectReferences().getID(selectedUUID);
                                    MotionEvent motionEvent = (MotionEvent) Wizard.getObjects().getOriginalObject(motionEventID);
                                    motionEvent.getPath().getWayPoint((Integer) tempSpatial.getUserData("waypointNumber")).set(gizmo.getLocalTranslation());
                                    updateSelectedMotionPath();
                                }
                                Vector3f move = new Vector3f(xDiff, yDiff, zDiff);
                                //System.out.println("Diff " + move);
                                Quaternion rot = tempSpatial.getParent().getWorldRotation().inverse();
                                //System.out.println("Rotate by " + rot);
                                Vector3f m = rot.mult(move);
                                //System.out.println("Move by " + m);
                                tempSpatial.move(m.divide(tempSpatial.getParent().getWorldScale())); // much genius
                                //sync childrens' physics if there are any
                                if (tempSpatial instanceof Node)
                                {
                                    Vector<Spatial> children = new Vector<Spatial>();
                                    children.clear();
                                    Wizard.insertAllSpatials((Node) tempSpatial, children);
                                    for (Spatial s : children)
                                    {
                                        if (s.getControl(RigidBodyControl.class) != null && !CurrentData.getEditorWindow().getB3DApp().isPhysicsPlaying())
                                        {
                                            s.getControl(RigidBodyControl.class).setPhysicsLocation(s.getWorldTranslation());
                                            s.getControl(RigidBodyControl.class).setPhysicsRotation(s.getWorldRotation());
                                        }
                                    }
                                }
                            } else if (selectedObject instanceof MotionEvent)
                            {
                                for (MotionPathModel mpm : motionPathModels)
                                {
                                    if (mpm.getMotionEvent().equals(selectedObject))
                                    {
                                        mpm.getSymbol().setLocalTranslation(gizmo.getLocalTranslation());
                                        ((MotionPathTaskPane) CurrentData.getEditorWindow().getEditPane().getCurrentEditPane()).updateLocations();
                                        for (int i = 0; i < ((MotionEvent) selectedObject).getPath().getNbWayPoints(); i++)
                                        {
                                            if (!((MotionEvent) selectedObject).getPath().isCycle() || i != 0)
                                            {
                                                ((MotionEvent) selectedObject).getPath().getWayPoint(i).set(((MotionEvent) selectedObject).getPath().getWayPoint(i).addLocal(xDiff, yDiff, zDiff));
                                            }
                                        }
                                    }
                                }
                            }/*Auch LightModels verschieben!*/ else if (selectedObject instanceof Light)
                            {
                                for (int i = 0; i < lightModels.size(); i++)
                                {
                                    if (lightModels.get(i).getLight().equals(selectedObject))
                                    {
                                        if (selectedObject instanceof SpotLight)
                                        {
                                            lightModels.get(i).getRepresentative().setLocalTranslation(gizmo.getLocalTranslation());
                                            Vector3f end = lightModels.get(i).getRepresentative().getWorldTranslation().add(
                                                    ((SpotLight) lightModels.get(i).getLight()).getDirection().mult(((SpotLight) lightModels.get(i).getLight()).getSpotRange()));
                                            Mesh mesh = new Line(gizmo.getLocalTranslation(), gizmo.getLocalTranslation().add(((SpotLight) lightModels.get(i).getLight()).getDirection().mult(((SpotLight) lightModels.get(i).getLight()).getSpotRange())));
                                            mesh.setPointSize(5);
                                            lightModels.get(i).getSymbol().setMesh(mesh);
                                            ((SpotLight) selectedObject).setPosition(lightModels.get(i).getRepresentative().getLocalTranslation());
                                        } else if (selectedObject instanceof PointLight)
                                        {
                                            lightModels.get(i).getRepresentative().setLocalTranslation(gizmo.getLocalTranslation());
                                            ((PointLight) selectedObject).setPosition(lightModels.get(i).getRepresentative().getLocalTranslation());
                                            //Update Element
                                            ((B3D_PointLight) Wizard.getObjects().getB3D_Element(selectedUUID)).setPosition(lightModels.get(i).getRepresentative().getLocalTranslation());
                                        }
                                    }
                                }
                            } else if (selectedObject instanceof LightScatteringFilter)
                            {
                                ((LightScatteringFilter) selectedObject).setLightPosition(gizmo.getLocalTranslation().clone());
                            } else if (selectedObject instanceof AdditionalCameraDialog)
                            {
                                ((AdditionalCameraDialog) selectedObject).getRepresentative().setLocalTranslation(gizmo.getLocalTranslation());
                            }
                        }
                    }
                }
            } else if (!Mouse.isButtonDown(1) && wasDragging && !Wizard.NULL_SELECTION.equals(selectedUUID) && selectedUUID != null)
            {
                //System.out.println("Add State for: " + selectedUUID);
                // selectedObject might be a waypoint or some shit
                UAManager.add(Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(selectedUUID)), "Move " + Wizard.getObjects().getB3D_Element(selectedUUID).getName());
                wasDragging = false;
            }
        }
    }

    /**
     *
     * @param interactionType
     * @param objectName
     */
    public void setInteractionType(InteractionType interactionType, String objectName)
    {
        if (interactionType.equals(InteractionType.Default))
        {
            guiNode.detachChild(mousePicture);
        } else if (interactionType.equals(InteractionType.InsertTexture))
        {
            mousePicture.setLocalScale(50, 50, 10);
            mousePicture.setTexture(assetManager, (Texture2D) assetManager.loadTexture(objectName), true);
            insertAssetName = objectName;
            guiNode.attachChild(mousePicture);
        } else if (interactionType.equals(InteractionType.InsertModel))
        {
            mousePicture.setLocalScale(180, 40, 10);
            mousePicture.setTexture(assetManager, (Texture2D) assetManager.loadTexture("Textures/insertModel.png"), true);
            insertAssetName = objectName;
            guiNode.attachChild(mousePicture);
        }
        this.interactionType = interactionType;
    }

    private void createTexturePopup(ArrayList<String> textures)
    {
        texturesMenu = new Menu(screen, inputManager.getCursorPosition(), false)
        {
            @Override
            public void onMenuItemClicked(int index, Object value, boolean isToggled)
            {
                UUID elementUUID = Wizard.getObjectReferences().getUUID(putTextureOn.hashCode());
                B3D_Geometry putOnTextureElement = (B3D_Geometry) Wizard.getObjects().getB3D_Element(elementUUID);
                if (putOnTextureElement != null)
                {
                    putTextureOn.getMaterial().setTexture(value.toString(), assetManager.loadTexture(insertAssetName));
                    try
                    {
                        if (putOnTextureElement.getMaterial().getPropertyList().has(value.toString()))
                            putOnTextureElement.getMaterial().getPropertyList().change(value.toString(), insertAssetName);
                        else
                            putOnTextureElement.getMaterial().getPropertyList().add(value.toString(), "Texture", insertAssetName);
                        UAManager.add(selectedObject, "Set " + value.toString() + " of " + putOnTextureElement.getName() + " to " + insertAssetName);
                        screen.removeElement(cancelButton);
                        screen.removeElement(selectButton);
                        screen.removeElement(texturesMenu);
                        screen.removeElement(this);
                        CurrentData.getEditorWindow().getEditPane().arrange(true);
                    } catch (NullPointerException npe)
                    {
                        ObserverDialog.getObserverDialog().printError("Error putting texture / removing In-Editor-GUI", npe);
                    }
                }
            }
        };
        for (String texture : textures)
            texturesMenu.addMenuItem(texture, texture, null);
        selectButton = new ButtonAdapter(screen, inputManager.getCursorPosition())
        {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean isToggled)
            {
                texturesMenu.showMenu(null, getAbsoluteX(), getAbsoluteY() - texturesMenu.getHeight());
            }
        };
        selectButton.setText(" Select Type: ");
        cancelButton = new ButtonAdapter(screen, new Vector2f(inputManager.getCursorPosition().getX(), inputManager.getCursorPosition().getY() + selectButton.getHeight()))
        {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean isToggled)
            {
                screen.removeElement(texturesMenu);
                screen.removeElement(selectButton);
                screen.removeElement(this);
            }
        };
        cancelButton.setText(" Cancel ");
        screen.addElement(cancelButton);
        screen.addElement(selectButton);
        screen.addElement(texturesMenu);
    }

    private void updateEditNode()
    {
        editorNodeCamLight.setColor(ColorRGBA.LightGray);
        editorNodeCamLight.setDirection(cam.getDirection());
        updateNodes();
        updateLightScatteringModels();
        updateSelection();
        updateObjectScales();
        if (selectedObject instanceof WaterFilterWithGetters)
            updateYArrow();
        else
        {
            gizmo.attachChild(gizmo.getxArrow());
            gizmo.attachChild(gizmo.getzArrow());
        }
        if ((Boolean) CurrentData.getPrefs().get(Preference.SHOW_ALL_MOTIONPATHS) && !allPathsShown)
            updateAllMotionPathGeometrys();
        if (updateMotionPath && Wizard.getObjects().getB3D_Element(selectedUUID) instanceof B3D_MotionEvent)
            updateMotionPathGeometrys();
        else if (!(Wizard.getObjects().getB3D_Element(selectedUUID) instanceof B3D_MotionEvent))
            motionEventNode.detachAllChildren();
    }

    private void updateLightScatteringModels()
    {
        for (LightScatteringModel lsm : lightScatteringModels)
        {
            lsm.getSymbol().setLocalTranslation(lsm.getScatteringFilter().getLightPosition());
        }
    }

    private void updateNodes()
    {
        currentNodeModel = null;
        synchronized (nodeModels)
        {
            for (NodeModel nodeModel : nodeModels)
            {
                if (nodeModel.getNode().equals(selectedObject))
                    currentNodeModel = nodeModel;
                editorNode.detachChild(nodeModel.getModel());
                editorNode.detachChild(nodeModel.getLineNode());
            }
            if (currentNodeModel != null)
                currentNodeModel.update(true);
        }
    }

    private void updateYArrow()
    {
        gizmo.detachChild(gizmo.getxArrow());
        gizmo.detachChild(gizmo.getzArrow());
        gizmo.getyArrow().setMaterial(gizmo.getyArrowMaterial());
        WaterFilterWithGetters tempWaterFilter = (WaterFilterWithGetters) selectedObject;
        gizmo.setLocalTranslation(cam.getLocation().add(cam.getDirection().multLocal(cam.getLocation().getY() * 2.5f - tempWaterFilter.getWaterHeight())));
        gizmo.setLocalTranslation(gizmo.getLocalTranslation().getX(),
                tempWaterFilter.getWaterHeight(),
                gizmo.getLocalTranslation().getZ());
    }

    private void updateMotionPathGeometrys()
    {
        if ((Boolean) CurrentData.getPrefs().get(Preference.SHOW_ALL_MOTIONPATHS))
            updateAllMotionPathGeometrys();
        else
        {
            if (Wizard.getObjects().getB3D_Element(selectedUUID) instanceof B3D_MotionEvent)
                for (MotionPathModel mpm : motionPathModels)
                {
                    int motionEventID = Wizard.getObjectReferences().getID(selectedUUID);
                    if (mpm.getMotionEvent().equals(Wizard.getObjects().getOriginalObject(motionEventID)))
                    {
                        motionEventNode.detachAllChildren();
                        updateMotionPath((B3D_MotionEvent) Wizard.getObjects().getB3D_Element(selectedUUID), mpm);
                    }
                }
            else
                motionEventNode.detachAllChildren();
            updateMotionPath = false;
            allPathsShown = false;
        }

    }

    private void updateAllMotionPathGeometrys()
    {
        motionEventNode.detachAllChildren();
        synchronized (motionPathModels)
        {
            for (MotionPathModel mpm : motionPathModels)
            {
                UUID elementUUID = Wizard.getObjectReferences().getUUID(mpm.getMotionEvent().hashCode());
                updateMotionPath((B3D_MotionEvent) Wizard.getObjects().getB3D_Element(elementUUID), mpm);
            }
            allPathsShown = true;
        }
    }

    private void updateMotionPath(B3D_MotionEvent b3D_Spatial_MotionPath, MotionPathModel mpm)
    {
        //Name of a Node: b3D_Spatial_MotionPath.getUUID()+"-MP"
        if (motionEventNode.getChild(b3D_Spatial_MotionPath.getUUID() + "-MP") != null)
            motionEventNode.detachChild(motionEventNode.getChild(b3D_Spatial_MotionPath.getUUID() + "-MP"));
        Node pathNode = new Node(b3D_Spatial_MotionPath.getUUID() + "-MP");
        pathNode.attachChild(mpm.getSymbol());
        Vector3f[] waypoints = new Vector3f[mpm.getMotionEvent().getPath().getNbWayPoints()];
        for (int i = 0; i < mpm.getMotionEvent().getPath().getNbWayPoints(); i++)
            waypoints[i] = mpm.getMotionEvent().getPath().getWayPoint(i);
        Spline spline = new Spline(Spline.SplineType.CatmullRom, waypoints, b3D_Spatial_MotionPath.getMotionPath().getCurveTension(), b3D_Spatial_MotionPath.getMotionPath().isCycled());
        Curve curve = new Curve(spline, 100);
        Geometry curveGeometry = new Geometry("Curve", curve);
        curveGeometry.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        curveGeometry.getMaterial().setColor("Color", b3D_Spatial_MotionPath.getMotionPath().getColor());
        pathNode.attachChild(curveGeometry);
        for (int i = 0; i < waypoints.length; i++)
        {
            Geometry pointGeometry = new Geometry("Waypoint", new Sphere(20, 20, 1));
            //Just use hashcode pointGeometry.setUserData("index", b3D_Spatial_MotionPath.getUUID());
            pointGeometry.setUserData("waypointNumber", new Integer(i));
            pointGeometry.setUserData("B3D_ID", mpm.getMotionEvent().hashCode());
            pointGeometry.setUserData("motionPathID", mpm.getMotionEvent().hashCode());
            pointGeometry.setLocalTranslation(waypoints[i]);
            pointGeometry.setMaterial(new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"));
            pointGeometry.getMaterial().setColor("Diffuse", b3D_Spatial_MotionPath.getMotionPath().getColor());
            pointGeometry.setLocalScale(cam.getLocation().distance(pointGeometry.getWorldTranslation()) / 85);
            pathNode.attachChild(pointGeometry);
        }
        motionEventNode.attachChild(pathNode);
    }

    private void updateObjectScales()
    {
        gizmo.setLocalScale(cam.getLocation().distance(gizmo.getLocalTranslation()) / 55);
        for (MotionPathModel mpm : motionPathModels)
            mpm.getSymbol().setLocalScale(cam.getLocation().distance(mpm.getSymbol().getWorldTranslation()) / 55);
        for (LightModel lm : lightModels)
            if (lm.getRepresentative() != null)
                lm.getRepresentative().setLocalScale(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().distance(lm.getRepresentative().getWorldTranslation()) / 60);
    }

    private void updatePairing()
    {
        newChild.getParent().detachChild(newChild);
        UUID newChildUUID = Wizard.getObjectReferences().getUUID(newChild.hashCode());
        if (Wizard.getObjectReferences().getUUID(selectedNode.hashCode()) == null || selectedNode.equals(newChild))
        {
            ((B3D_Spatial) Wizard.getObjects().getB3D_Element(newChildUUID)).setParentUUID(Wizard.NULL_SELECTION);
            sceneNode.attachChild(newChild);
        } else
        {
            ((B3D_Spatial) Wizard.getObjects().getB3D_Element(newChildUUID)).setParentUUID((UUID) Wizard.getObjectReferences().getUUID(selectedNode.hashCode()));
            selectedNode.attachChild(newChild);
        }
        newChild = null;
        newParent = null;
    }

    public void prepairPairing(B3D_Node parent, B3D_Spatial child)
    {
        newB3DChild = child;
        newB3DParent = parent;
    }

    public void prepairSceneNodePairing(B3D_Spatial child)
    {
        newB3DChild = child;
        newB3DParent = null;
    }

    public void startPairing()
    {
        if (newB3DChild != null)
            if (newB3DParent == null)
                pairWithSceneNode();
            else
                pair();
    }

    private void pair()
    {
        if (newB3DChild.getParentUUID() != Wizard.NULL_SELECTION)
        {
            B3D_Node oldParent = (B3D_Node) Wizard.getObjects().getB3D_Element(newB3DChild.getParentUUID());
            oldParent.getChildren().remove(newB3DChild);
        }
        newB3DParent.getChildren().add(newB3DChild);
        newB3DChild.setParentUUID(newB3DParent.getUUID());
        int parentID = Wizard.getObjectReferences().getID(newB3DParent.getUUID());
        newParent = (Node) Wizard.getObjects().getOriginalObject(parentID);
        setSelectedNode(newParent);
        selectedUUID = newB3DParent.getUUID();
        int childID = Wizard.getObjectReferences().getID(newB3DChild.getUUID());
        newChild = (Spatial) Wizard.getObjects().getOriginalObject(childID);
    }

    private void pairWithSceneNode()
    {
        if (newB3DChild.getParentUUID() != Wizard.NULL_SELECTION)
        {
            B3D_Node oldParent = (B3D_Node) Wizard.getObjects().getB3D_Element(newB3DChild.getParentUUID());
            oldParent.getChildren().remove(newB3DChild);
        }
        newB3DChild.setParentUUID(Wizard.NULL_SELECTION);
        newParent = sceneNode;
        setSelectedNode(sceneNode);
        int childID = Wizard.getObjectReferences().getID(newB3DChild.getUUID());
        newChild = (Spatial) Wizard.getObjects().getOriginalObject(childID);
    }

    public void cancelPairing()
    {
        newB3DChild = null;
    }

    public FilterPostProcessor getFilterPostProcessor()
    {
        return filterPostProcessor;
    }

    public InteractionType getInteractionType()
    {
        return interactionType;
    }

    public Node getMotionEventNode()
    {
        return motionEventNode;
    }

    public UUID getSelectedUUID()
    {
        return selectedUUID;
    }

    public Object getSelectedObject()
    {
        return selectedObject;
    }

    public Vector<LightModel> getLightModels()
    {
        return lightModels;
    }

    public AppSettings getSettings()
    {
        return settings;
    }

    /**
     *
     * @param insertAssetName
     */
    public void setInsertAssetName(String insertAssetName)
    {
        this.insertAssetName = insertAssetName;
    }

    public Vector<LightScatteringModel> getLightScatteringModels()
    {
        return lightScatteringModels;
    }

    public void attachCam()
    {
        flyingEditor.setEnabled(false);
        rootNode.attachChild(camNode);
    }

    public void detachCam()
    {
        flyingEditor.setEnabled(true);
        rootNode.detachChild(camNode);
    }

    public CameraNode getCamNode()
    {
        return camNode;
    }

    public SceneNode getSceneNode()
    {
        return sceneNode;
    }

    /**
     *
     * @param sceneNode
     */
    public void setSceneNode(SceneNode sceneNode)
    {
        this.sceneNode = sceneNode;
    }

    public Node getEditorNode()
    {
        return editorNode;
    }

    /**
     *
     * @param editorNode
     */
    public void setEditorNode(Node editorNode)
    {
        this.editorNode = editorNode;
    }

    public Geometry getGridGeometry()
    {
        return gridGeometry;
    }

    /**
     *
     * @param gridGeometry
     */
    public void setGridGeometry(Geometry gridGeometry)
    {
        this.gridGeometry = gridGeometry;
    }

    /**
     *
     * @param sync
     */
    public void setSyncTree(boolean sync)
    {
        treeSyncNeeded = sync;
    }

    public BulletAppState getBulletAppState()
    {
        return bulletAppState;
    }

    public void updateSelectedMotionPath()
    {
        updateMotionPath = true;
    }

    public Node getSelectedNode()
    {
        return selectedNode;
    }

    public void setSelectedNode(Node selectedNode)
    {
        this.selectedNode = selectedNode;
    }

    public Vector<NodeModel> getNodeModels()
    {
        return nodeModels;
    }

    public boolean isPhysicsPlaying()
    {
        return physicsPlaying;
    }

    public Node getNewParent()
    {
        return newParent;
    }

    public void createCam()
    {
        AdditionalCameraDialog acd = new AdditionalCameraDialog(cam.getLocation(), cam.getRotation(),
                CurrentData.getEditorWindow().getCanvasPanel().getSize().width / 2,
                CurrentData.getEditorWindow().getCanvasPanel().getSize().height / 2,
                renderer,
                renderManager,
                rootNode,
                assetManager,
                true);
    }

    public void addAdditionalCamera(final AdditionalCameraDialog acd)
    {
        enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                editorNode.attachChild(acd.getRepresentative());
                return null;
            }
        });
        additionalCameraDialogs.add(acd);
    }

    public void removeAdditionalCamera(final AdditionalCameraDialog acd)
    {
        enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                if (acd.equals(selectedObject))
                {
                    selectedObject = null;
                    CurrentData.getEditorWindow().getEditPane().arrange(true);
                }
                editorNode.detachChild(acd.getRepresentative());
                additionalCameraDialogs.remove(acd);
                return null;
            }
        });
    }

    public Vector<AdditionalCameraDialog> getAdditionalCameraDialogs()
    {
        return additionalCameraDialogs;
    }

    public Vector<MotionPathModel> getMotionPathModels()
    {
        return motionPathModels;
    }

    public void setTreeSyncNeeded(boolean treeSyncNeeded)
    {
        this.treeSyncNeeded = treeSyncNeeded;
    }

    public Vector<LightControl> getLightControls()
    {
        return lightControls;
    }
}