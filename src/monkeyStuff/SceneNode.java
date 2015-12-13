package monkeyStuff;

import b3dElements.spatials.B3D_Spatial;
import general.CurrentData;
import com.jme3.light.Light;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import general.Preference;
import other.Wizard;

public class SceneNode extends Node
{

    public SceneNode()
    {
        setName("Scene");
    }

    /**
     *
     * @param spatial
     * @return
     */
    @Override
    public int detachChild(Spatial spatial)
    {
        super.detachChild(spatial);
        Spatial empty = null;
        CurrentData.getEditorWindow().getB3DApp().setSelectedElement(empty);
        CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
        return children.size();
    }

    @Override
    public void removeLight(Light light)
    {
        for (LightModel lm : CurrentData.getEditorWindow().getB3DApp().getLightModels())
        {
            if (light.equals(lm.getLight()))
            {
                CurrentData.getEditorWindow().getB3DApp().getLightModels().remove(lm);
                CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(lm.getNode());
                break;
            }
        }
        super.removeLight(light);
        Spatial empty = null;
        CurrentData.getEditorWindow().getB3DApp().setSelectedElement(empty);
        CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
    }

    @Override
    public void addLight(Light light)
    {
        LightModel newLightModel = new LightModel(light);
        CurrentData.getEditorWindow().getB3DApp().getLightModels().add(newLightModel);
        CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
        System.out.println("Added to LightModels");
        super.addLight(light);
    }

    /**
     *
     * @param spatial
     * @return
     */
    @Override
    public int attachChild(Spatial spatial)
    {
        Wizard.setWireframe(spatial, (Boolean) CurrentData.getPrefs().get(Preference.SHOW_WIREFRAME));
        if (spatial instanceof Node && !(spatial instanceof TerrainQuad))
            CurrentData.getEditorWindow().getB3DApp().getNodeModels().add(new NodeModel((Node) spatial));
        if (CurrentData.getEditorWindow().getB3DApp().getSelectedNode() == this || CurrentData.getEditorWindow().getB3DApp().getSelectedNode() == spatial)
        {
            System.out.println("Adding " + spatial + " to sceneNode -> " + spatial.getVertexCount());
            //Just in case
            ((B3D_Spatial) Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(spatial.hashCode()))).setParentUUID(Wizard.NULL_SELECTION);
            super.attachChild(spatial);
        } else
        {
            B3D_Spatial b3D_Spatial = (B3D_Spatial) Wizard.getObjects().getB3D_Element(Wizard.getObjectReferences().getUUID(spatial.hashCode()));
            CurrentData.getEditorWindow().getB3DApp().getSelectedNode().attachChild(spatial);
            b3D_Spatial.setParentUUID(
                    Wizard.getObjectReferences().getUUID(
                    CurrentData.getEditorWindow().getB3DApp().getSelectedNode().hashCode()));
            CurrentData.getEditorWindow().getB3DApp().getSelectedNode().attachChild(spatial);
        }
        CurrentData.getEditorWindow().getTree().sync();
        return children.size();
    }
}