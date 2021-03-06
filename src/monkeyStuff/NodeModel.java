package monkeyStuff;

import general.CurrentData;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;
import general.Preference;
import other.Wizard;
import java.util.Vector;

public class NodeModel
{

    private Spatial model;
    private Node node;
    private Node lineNode = new Node("Connections");
    private Material lineMaterial, nodeMaterial;
    private Geometry lineGeometry;

    public NodeModel(Node n)
    {
        lineMaterial = new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        lineMaterial.setColor("Color", ColorRGBA.Cyan);
        node = n;
        model = CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadModel("Models/node.j3o");
        model.setName("nodeModel");
        nodeMaterial = new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        nodeMaterial.setTexture("DiffuseMap", CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Textures/nodeTexture.PNG"));
        model.setMaterial(nodeMaterial);
        Vector<Geometry> geometrys = new Vector<Geometry>();
        Wizard.insertAllGeometrys((Node) model, geometrys);
        model.setLocalTransform(node.getLocalTransform());
        CurrentData.getEditorWindow().getB3DApp().getNodeModels().add(this);
        CurrentData.getEditorWindow().getB3DApp().getEditorNode().attachChild(lineNode);
    }

    public Spatial getModel()
    {
        return model;
    }

    public void setModel(Spatial model)
    {
        this.model = model;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }

    void update(boolean selected, boolean further)
    {
        if (!(Boolean) CurrentData.getPrefs().get(Preference.SHOW_NODE_HIERARCHY))
            return;
        lineNode.detachAllChildren();
        model.setLocalTransform(node.getWorldTransform());
        model.setLocalScale(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().distance(model.getWorldTranslation()) / 60);
        if (selected)
        {
            CurrentData.getEditorWindow().getB3DApp().getEditorNode().attachChild(model);
            for (Spatial spatial : node.getChildren())
            {
                if (spatial instanceof Node && further)
                    for (NodeModel nm : CurrentData.getEditorWindow().getB3DApp().getNodeModels())
                        if (nm.getNode().equals(spatial))
                            nm.update(true, false);
                Vector3f lineDifference = spatial.getWorldTranslation().subtract(model.getWorldTranslation());
                lineGeometry = new Geometry("connection", new Line(Vector3f.ZERO, lineDifference));
                lineGeometry.setLocalTranslation(model.getWorldTranslation());
                lineGeometry.setMaterial(lineMaterial);
                lineNode.attachChild(lineGeometry);
            }
            CurrentData.getEditorWindow().getB3DApp().getEditorNode().attachChild(lineNode);
        }
    }

    public Node getLineNode()
    {
        return lineNode;
    }
}
