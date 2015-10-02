/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package monkeyStuff;

import general.CurrentData;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import java.util.concurrent.Callable;

/**
 *
 * @author David
 */
public class LightModel
{

    private Node node = new Node("LightModel");
    private Light light;
    private Material symbolMaterial;
    private Material representativeMaterial;
    private Geometry representative = null;
    private Geometry symbol = null;

    /**
     *
     * @param l
     */
    public LightModel(Light l)
    {
        light = l;
        if (!(l instanceof AmbientLight))
        {
            symbolMaterial = new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            symbolMaterial.setColor("Color", l.getColor());
            representativeMaterial = new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
            if (l instanceof DirectionalLight)
            {
                /*Light*/
                DirectionalLight dLight = (DirectionalLight) l;
                /*Representativ*/
                representativeMaterial.setTexture("DiffuseMap", CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Textures/showDirectionalLightTexture.PNG"));
                representative = new Geometry("LightModel", new Box(1, 1, 1));
                representative.setMaterial(representativeMaterial);
                /*Symbol*/
                Vector3f end = CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(
                        CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(20)).add(
                        dLight.getDirection().mult(100));
                Line symbolMesh = new Line(representative.getWorldTranslation(), end);
                symbolMesh.setPointSize(5);
                symbol = new Geometry("LightSymbol", symbolMesh);
                symbol.setMaterial(symbolMaterial);
            } else if (l instanceof PointLight)
            {
                PointLight pLight = (PointLight) l;
                representativeMaterial.setTexture("DiffuseMap", CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Textures/showPointLightTexture.PNG"));
                representative = new Geometry("LightModel", new Box(1, 1, 1));
                representative.setMaterial(representativeMaterial);
                representative.setLocalTranslation(pLight.getPosition());
                symbol = new Geometry("LightSymbol", new Sphere(15, 15, pLight.getRadius()));
                symbol.setMaterial(symbolMaterial);
                symbolMaterial.getAdditionalRenderState().setWireframe(true);
            } else if (l instanceof SpotLight)
            {
                /*Light*/
                SpotLight sLight = (SpotLight) l;
                /*Representativ*/
                representativeMaterial.setTexture("DiffuseMap", CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Textures/showSpotLightTexture.PNG"));
                representative = new Geometry("LightModel", new Box(1, 1, 1));
                representative.setLocalTranslation(sLight.getPosition());
                representative.setMaterial(representativeMaterial);
                /*Symbol*/
                Vector3f end = sLight.getPosition().add(sLight.getDirection().mult(sLight.getSpotRange()));
                Line symbolMesh = new Line(representative.getWorldTranslation(), end);
                symbolMesh.setPointSize(5);
                symbol = new Geometry("LightSymbol", symbolMesh);
                symbol.setMaterial(symbolMaterial);
            }
            node.attachChild(representative);
            if (symbol != null)
            {
                node.attachChild(symbol);
            }
        }
    }

    public Node getNode()
    {
        return node;
    }

    /**
     *
     * @param model
     */
    public void setNode(Node model)
    {
        this.node = model;
    }

    public Geometry getRepresentative()
    {
        return representative;
    }

    /**
     *
     * @param representative
     */
    public void setRepresentative(Geometry representative)
    {
        this.representative = representative;
    }

    public Geometry getSymbol()
    {
        return symbol;
    }

    /**
     *
     * @param symbol
     */
    public void setSymbol(Geometry symbol)
    {
        this.symbol = symbol;
    }

    /**
     *
     * @return
     */
    public Light getLight()
    {
        return light;
    }

    /**
     *
     * @param light
     */
    public void setLight(Light light)
    {
        this.light = light;
    }

    /**
     *
     * @param visible
     */
    public void setSymbolVisible(boolean visible)
    {
        if (visible)
        {
            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    if (light instanceof DirectionalLight)
                    {
                        node.setLocalTranslation(
                                CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().clone().add(
                                CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(10)));
                    }
                    CurrentData.getEditorWindow().getB3DApp().getEditorNode().attachChild(node);
                    return null;
                }
            });
        } else
        {
            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    CurrentData.getEditorWindow().getB3DApp().getEditorNode().detachChild(node);
                    return null;
                }
            });
        }
    }
}
