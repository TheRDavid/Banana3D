package gui.elementTree;

import general.CurrentData;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.post.Filter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import monkeyStuff.CustomParticleEmitter;
import other.Wizard;

public class ElementTreeCellRenderer extends DefaultTreeCellRenderer
{

    private ImageIcon boxIcon, terrainIcon;
    private ImageIcon nodeIcon, meshIcon, skyboxIcon, effectIcon;
    private ImageIcon lightIcon, directionalLightIcon, pointLightIcon, spotLightIcon;
    private ImageIcon filterIcon, disabledFilterIcon;
    private ImageIcon sceneIcon, motionPathIcon, cameraIcon;
    private ElementTree elementTree;

    public ElementTreeCellRenderer(ElementTree tree)
    {
        elementTree = tree;
        setForeground(UIManager.getColor("Button.background"));
        boxIcon = new ImageIcon("dat//img//menu//c_box.png");
        nodeIcon = new ImageIcon("dat//img//menu//c_node.png");
        lightIcon = new ImageIcon("dat//img//menu//c_light.png");
        filterIcon = new ImageIcon("dat//img//menu//c_filter_full.png");
        sceneIcon = new ImageIcon("dat//img//other//logo.png");
        meshIcon = new ImageIcon("dat//img//menu//c_mesh.png");

        skyboxIcon = new ImageIcon("dat//img//menu//c_skybox.png");
        terrainIcon = new ImageIcon("dat//img//menu//c_terrain.png");
        effectIcon = new ImageIcon("dat//img//menu//c_effect.png");
        directionalLightIcon = new ImageIcon("dat//img//menu//c_directionalLight.png");
        pointLightIcon = new ImageIcon("dat//img//menu//c_pointLight.png");

        spotLightIcon = new ImageIcon("dat//img//menu//c_spotLight.png");
        effectIcon = new ImageIcon("dat//img//menu//c_effect.png");
        disabledFilterIcon = new ImageIcon("dat//img//menu//c_filter_empty.png");
        motionPathIcon = new ImageIcon("dat//img//menu//c_motionPath.png");
        cameraIcon = new ImageIcon("dat//img//menu//c_camera.png");
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row, hasFocus);
        ElementTree eTree = (ElementTree) tree;
        DefaultMutableTreeNode tempNode = (DefaultMutableTreeNode) value;
        Integer id = Wizard.getObjectReferences().getID(eTree.getNodeIds().get(tempNode));
        if (id != null)
        {
            Object object = Wizard.getObjects().getOriginalObject(id);
            if (object instanceof Spatial)
            {
                Spatial spatial = (Spatial) Wizard.getObjects().getOriginalObject(id);
                if (spatial instanceof Geometry)
                {
                    if (spatial instanceof CustomParticleEmitter)
                        setIcon(effectIcon);
                    else if (spatial.getUserData("north") != null)
                        setIcon(skyboxIcon);
                    else
                        setIcon(meshIcon);
                } else if (spatial instanceof TerrainQuad)
                {
                    setIcon(terrainIcon);
                } else if (spatial instanceof Node)
                    setIcon(nodeIcon);
                else
                    setIcon(boxIcon);
            } else if (object instanceof Light)
            {
                Light light = (Light) Wizard.getObjects().getOriginalObject(id);
                if (light instanceof PointLight)
                    setIcon(pointLightIcon);
                else if (light instanceof DirectionalLight)
                    setIcon(directionalLightIcon);
                else if (light instanceof SpotLight)
                    setIcon(spotLightIcon);
                else
                    setIcon(lightIcon);
            } else if (object instanceof Filter)
            {
                Filter filter = (Filter) Wizard.getObjects().getOriginalObject(id);
                if (filter.isEnabled())
                    setIcon(filterIcon);
                else
                    setIcon(disabledFilterIcon);
            } else
            {
                if (object instanceof MotionEvent)
                    setIcon(motionPathIcon);
            }
        }
        return this;
    }
}