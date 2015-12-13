package gui.editor;

import b3dElements.filters.B3D_Filter;
import b3dElements.lights.B3D_Light;
import b3dElements.spatials.geometries.B3D_Geometry;
import b3dElements.spatials.B3D_Spatial;
import b3dElements.other.B3D_MotionEvent;
import b3dElements.spatials.B3D_Heightmap;
import b3dElements.spatials.B3D_Terrain;
import gui.dialogs.AdditionalCameraDialog;
import gui.editPanes.filters.BasicSSAOTaskPane;
import gui.editPanes.filters.BloomTaskPane;
import gui.editPanes.filters.CartoonEdgeTaskPane;
import gui.editPanes.filters.ColorScaleTaskPane;
import gui.editPanes.filters.CrosshatchTaskPane;
import gui.editPanes.filters.DepthOfFieldTaskPane;
import gui.editPanes.filters.FogTaskPane;
import gui.editPanes.filters.FrostedGlassTaskPane;
import gui.editPanes.filters.LightScatteringTaskPane;
import gui.editPanes.filters.OldFilmTaskPane;
import gui.editPanes.filters.PosterizationTaskPane;
import gui.editPanes.filters.SSAOTaskPane;
import gui.editPanes.filters.ShadowTaskPane;
import gui.editPanes.filters.WaterTaskPane;
import gui.editPanes.lights.AmbientLightTaskPane;
import gui.editPanes.lights.DirectionalLightTaskPane;
import gui.editPanes.lights.PointLightTaskPane;
import gui.editPanes.lights.SpotLightTaskPane;
import gui.editPanes.others.AdditionalCameraTaskPane;
import gui.editPanes.others.AnimationsTaskPane;
import gui.editPanes.others.MotionPathTaskPane;
import gui.editPanes.spatials.ShadowModeTaskPane;
import gui.editPanes.spatials.Basic3DTaskPane;
import gui.editPanes.spatials.EmitterTaskPane;
import gui.editPanes.spatials.MaterialTaskPane;
import gui.editPanes.spatials.MeshTaskPane;
import gui.editPanes.spatials.OtherTaskPane;
import gui.editPanes.spatials.PhysicsTaskPane;
import gui.editPanes.spatials.SkyBoxTaskPane;
import general.CurrentData;
import monkeyStuff.LightModel;
import monkeyStuff.MotionPathModel;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.SpotLight;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.post.filters.CrossHatchFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.post.filters.PosterizationFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.AbstractShadowFilter;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.shaderblow.filter.basicssao.BasicSSAO;
import com.shaderblow.filter.frostedglass.FrostedGlassFilter;
import com.shaderblow.filter.oldfilm.OldFilmFilter;
import components.EditTaskPane;
import dialogs.ObserverDialog;
import general.Preference;
import gui.editPanes.spatials.TQMaterialTaskPane;
import java.io.File;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.VerticalLayout;
import monkeyStuff.ColorScaleFilterWithGetters;
import monkeyStuff.CustomParticleEmitter;
import monkeyStuff.WaterFilterWithGetters;
import other.Wizard;

public class EditPane extends JScrollPane
{

    private JPanel panel = new JPanel(new VerticalLayout());
    private EditTaskPane currentEditPane = null;
    private Vector<EditTaskPane> taskPanes = new Vector<EditTaskPane>();
    private long lastTime = 0;

    /**
     * Initializing the GUI and the update-Thread
     */
    public EditPane()
    {
        setViewportView(panel);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep((Integer) CurrentData.getPrefs().get(Preference.GUI_SPEED));
                    } catch (InterruptedException ex)
                    {
                        Logger.getLogger(EditPane.class.getName()).log(Level.SEVERE, null, ex);
                        ObserverDialog.getObserverDialog().printError("Thread.sleep in EditPane interrupted", ex);
                    }
                    synchronized (taskPanes)
                    {
                        for (EditTaskPane editTaskPane : taskPanes)
                        {
                            editTaskPane.updateData(false);
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * Adds an EditTaskPane to the list and sets it as the currentTaskPane
     *
     * @param taskPane
     */
    public void addTaskPane(EditTaskPane taskPane)
    {
        if (taskPane instanceof MaterialTaskPane)
        {
            if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Geometry)
                ((MaterialTaskPane) taskPane).arrange(
                        new File("matD//" + ((Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getMaterial().getMaterialDef().getAssetName()));
            else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof TerrainQuad)
                ((TQMaterialTaskPane) taskPane).arrange(
                        new File("matD//" + ((TerrainQuad) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getMaterial().getMaterialDef().getAssetName()));
        }
        panel.add(taskPane);
        currentEditPane = taskPane;
    }

    /**
     * This one is tricky. Add the RIGHT EditPanes, regarding the type of the
     * currently selected Element / Object
     *
     * @param arrangeWHATEVERITTAKES
     */
    public void arrange(boolean arrangeWHATEVERITTAKES)
    {
        if (System.currentTimeMillis() - lastTime > 100)
        {
            lastTime = System.currentTimeMillis();
            //Clear
            for (EditTaskPane etp : taskPanes)
                etp.lastAction();
            taskPanes.removeAllElements();
            panel.removeAll();
            if (CurrentData.getEditorWindow().getB3DApp().getSelectedUUID() != Wizard.NULL_SELECTION)
            {
                b3dElements.B3D_Element selectedElement = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
                //Okay, there is an element selected.
                //Now we need about 3000 if-statements to add all the necessary TaskPanes IN THE RIGHT ORDER
                if (selectedElement instanceof B3D_Spatial
                        && CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Spatial)
                {
                    //It's a spatial.
                    Spatial tempSpatial = (Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                    if (tempSpatial.getParent() != null
                            && !tempSpatial.getParent().equals(CurrentData.getEditorWindow().getB3DApp().getEditorNode()))
                    {
                        //It's an element of the 3D-Scene (no Editor-Element)
                        //Add the Transformation-, Shadow-, Physics- and Other-Pane
                        Basic3DTaskPane basic3DTaskPane = new Basic3DTaskPane();
                        addTaskPane(basic3DTaskPane);
                        taskPanes.add(basic3DTaskPane);

                        ShadowModeTaskPane shadowModeTaskPane = new ShadowModeTaskPane();
                        addTaskPane(shadowModeTaskPane);
                        taskPanes.add(shadowModeTaskPane);

                        PhysicsTaskPane physicsTaskPane = new PhysicsTaskPane();
                        addTaskPane(physicsTaskPane);
                        taskPanes.add(physicsTaskPane);

                        if (selectedElement instanceof B3D_Geometry)
                        {
                            //It's not a node? Great, check if it's a ParticleEmitter.
                            Geometry tempGeometry = (Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                            if ((tempGeometry instanceof CustomParticleEmitter))
                            {
                                EmitterTaskPane emitterTaskPane = new EmitterTaskPane((CustomParticleEmitter) tempGeometry);
                                addTaskPane(emitterTaskPane);
                                taskPanes.add(emitterTaskPane);
                            }
                            //If the Geometry has a special mesh (unlike Emitters and Boxes) and is NOT a SkyBox (no userData at "north"),
                            //add a Mesh-Editing TaskPane.
                            if (!(tempGeometry instanceof CustomParticleEmitter)
                                    && !(tempGeometry.getMesh() instanceof Box)
                                    && tempGeometry.getUserData("north") == null)
                            {
                                MeshTaskPane meshTaskPane = new MeshTaskPane(tempGeometry.getMesh());
                                addTaskPane(meshTaskPane);
                                taskPanes.add(meshTaskPane);
                            } else if (tempGeometry.getUserData("north") != null && tempGeometry.getUserData("south") != null)
                            {
                                //Seems to be a SkyBox.
                                SkyBoxTaskPane skyBoxTaskPane = new SkyBoxTaskPane(tempSpatial);
                                addTaskPane(skyBoxTaskPane);
                                taskPanes.add(skyBoxTaskPane);
                            }
                            if (!(tempGeometry instanceof CustomParticleEmitter) && tempGeometry.getUserData("north") == null)
                            {
                                //If it's not a SkyBox and not an Emitter, add a MaterialTaskPane
                                MaterialTaskPane materialTaskPane = new MaterialTaskPane(tempGeometry.getMaterial().getMaterialDef().getAssetName());
                                addTaskPane(materialTaskPane);
                                taskPanes.add(materialTaskPane);
                            }
                        } else if (selectedElement instanceof B3D_Terrain || selectedElement instanceof B3D_Heightmap)
                        {
                            TerrainQuad tq = (TerrainQuad) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                            TQMaterialTaskPane materialTaskPane = new TQMaterialTaskPane(tq.getMaterial().getMaterialDef().getAssetName());
                            addTaskPane(materialTaskPane);
                            taskPanes.add(materialTaskPane);
                        }

                        //Editing UserData and LightControls here
                        OtherTaskPane otherTaskPane = new OtherTaskPane(tempSpatial);
                        addTaskPane(otherTaskPane);
                        taskPanes.add(otherTaskPane);

                        AnimationsTaskPane animationScriptTaskPane = new AnimationsTaskPane();
                        addTaskPane(animationScriptTaskPane);
                        taskPanes.add(animationScriptTaskPane);
                    }
                } else if (selectedElement instanceof B3D_Light)
                {
                    //Appears to be a light
                    LightModel tempLightModel = null;
                    //Find the actual Light
                    int tempLightID = Wizard.getObjectReferences().getID(selectedElement.getUUID());
                    Light tempLight = (Light) Wizard.getObjects().getOriginalObject(tempLightID);
                    //Find the LightModel (representative)
                    for (int i = 0; i < CurrentData.getEditorWindow().getB3DApp().getLightModels().size(); i++)
                    {
                        if (CurrentData.getEditorWindow().getB3DApp().getLightModels().get(i).getLight().equals(tempLight))
                        {
                            tempLightModel = CurrentData.getEditorWindow().getB3DApp().getLightModels().get(i);
                        }
                    }
                    //Add a TaskPane depending on the type of the light
                    if (tempLight instanceof AmbientLight)
                    {
                        AmbientLightTaskPane ambientLightTaskPane = new AmbientLightTaskPane((AmbientLight) tempLight);
                        addTaskPane(ambientLightTaskPane);
                        taskPanes.add(ambientLightTaskPane);
                    } else if (tempLight instanceof DirectionalLight)
                    {
                        DirectionalLightTaskPane directionalLightTaskPane = new DirectionalLightTaskPane(tempLightModel);
                        addTaskPane(directionalLightTaskPane);
                        taskPanes.add(directionalLightTaskPane);
                    } else if (tempLight instanceof SpotLight)
                    {
                        SpotLightTaskPane spotLightTaskPane = new SpotLightTaskPane(tempLightModel);
                        addTaskPane(spotLightTaskPane);
                        taskPanes.add(spotLightTaskPane);
                    } else
                    {
                        PointLightTaskPane pointLightTaskPane = new PointLightTaskPane(tempLightModel);
                        addTaskPane(pointLightTaskPane);
                        taskPanes.add(pointLightTaskPane);
                    }

                    AnimationsTaskPane animationScriptTaskPane = new AnimationsTaskPane();
                    addTaskPane(animationScriptTaskPane);
                    taskPanes.add(animationScriptTaskPane);
                } else if (selectedElement instanceof B3D_Filter)
                {
                    //Appears to be a filter... what kind?
                    if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof BasicSSAO)
                    {
                        BasicSSAOTaskPane basicSSAOTaskPane = new BasicSSAOTaskPane((BasicSSAO) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(basicSSAOTaskPane);
                        taskPanes.add(basicSSAOTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof BloomFilter)
                    {
                        BloomTaskPane bloomTaskPane = new BloomTaskPane((BloomFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(bloomTaskPane);
                        taskPanes.add(bloomTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof CartoonEdgeFilter)
                    {
                        CartoonEdgeTaskPane cartoonEdgeTaskPane = new CartoonEdgeTaskPane((CartoonEdgeFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(cartoonEdgeTaskPane);
                        taskPanes.add(cartoonEdgeTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof ColorScaleFilterWithGetters)
                    {
                        ColorScaleTaskPane colorScaleTaskPane = new ColorScaleTaskPane((ColorScaleFilterWithGetters) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(colorScaleTaskPane);
                        taskPanes.add(colorScaleTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof CrossHatchFilter)
                    {
                        CrosshatchTaskPane crosshatchTaskPane = new CrosshatchTaskPane((CrossHatchFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(crosshatchTaskPane);
                        taskPanes.add(crosshatchTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof DepthOfFieldFilter)
                    {
                        DepthOfFieldTaskPane depthOfFieldTaskPane = new DepthOfFieldTaskPane((DepthOfFieldFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(depthOfFieldTaskPane);
                        taskPanes.add(depthOfFieldTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof FogFilter)
                    {
                        FogTaskPane fogTaskPane = new FogTaskPane((FogFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(fogTaskPane);
                        taskPanes.add(fogTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof FrostedGlassFilter)
                    {
                        FrostedGlassTaskPane frostedGlassTaskPane = new FrostedGlassTaskPane((FrostedGlassFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(frostedGlassTaskPane);
                        taskPanes.add(frostedGlassTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof LightScatteringFilter)
                    {
                        LightScatteringTaskPane lightScatteringTaskPane = new LightScatteringTaskPane((LightScatteringFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(lightScatteringTaskPane);
                        taskPanes.add(lightScatteringTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof OldFilmFilter)
                    {
                        OldFilmTaskPane oldFilmTaskPane = new OldFilmTaskPane((OldFilmFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(oldFilmTaskPane);
                        taskPanes.add(oldFilmTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof PosterizationFilter)
                    {
                        PosterizationTaskPane posterizationTaskPane = new PosterizationTaskPane((PosterizationFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(posterizationTaskPane);
                        taskPanes.add(posterizationTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof SSAOFilter)
                    {
                        SSAOTaskPane sSAOTaskPane = new SSAOTaskPane((SSAOFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(sSAOTaskPane);
                        taskPanes.add(sSAOTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof AbstractShadowFilter)
                    {
                        ShadowTaskPane shadowTaskPane = new ShadowTaskPane((AbstractShadowFilter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(shadowTaskPane);
                        taskPanes.add(shadowTaskPane);
                    } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof WaterFilterWithGetters)
                    {
                        WaterTaskPane waterTaskPane = new WaterTaskPane((WaterFilterWithGetters) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                        addTaskPane(waterTaskPane);
                        taskPanes.add(waterTaskPane);
                    }
                } else if (selectedElement instanceof B3D_MotionEvent)
                {
                    //Appears to be a MotionEvent
                    int motionEventID = Wizard.getObjectReferences().getID(selectedElement.getUUID());
                    MotionEvent motionEvent = (MotionEvent) Wizard.getObjects().getOriginalObject(motionEventID);
                    MotionPathModel mpm = null;
                    //Find the MotionPathModel
                    for (MotionPathModel m : CurrentData.getEditorWindow().getB3DApp().getMotionPathModels())
                    {
                        if (m.getMotionEvent().equals(motionEvent))
                        {
                            mpm = m;
                        }
                    }
                    //Create the TaskPane
                    MotionPathTaskPane motionPathTaskPane = new MotionPathTaskPane(mpm,
                            (B3D_MotionEvent) selectedElement);
                    addTaskPane(motionPathTaskPane);
                    taskPanes.add(motionPathTaskPane);
                }
            } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() != null)
            {
                //Element is null, but the object is not. Can only be an AdditionalCamera...
                if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof AdditionalCameraDialog)
                {
                    //Yup...
                    AdditionalCameraTaskPane additionalCameraTaskPane = new AdditionalCameraTaskPane((AdditionalCameraDialog) CurrentData.getEditorWindow().getB3DApp().getSelectedObject());
                    addTaskPane(additionalCameraTaskPane);
                    taskPanes.add(additionalCameraTaskPane);
                }
            }
            repaint();
            revalidate();
            repaint();
        }
    }

    public EditTaskPane getCurrentEditPane()
    {
        return currentEditPane;
    }

    public void refresh()
    {
        for (EditTaskPane etp : taskPanes)
            etp.updateData(true);
    }
}
