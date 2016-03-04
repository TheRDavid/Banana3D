/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.dialogs.keyframeAnimationEditor;

import b3dElements.B3D_Element;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import dialogs.BasicDialog;
import dialogs.ObserverDialog;
import general.CurrentData;
import gui.dialogs.keyframeAnimationEditor.KeyframeAnimationFrame.AttributesPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import monkeyStuff.keyframeAnimation.LiveKeyframeProperty;
import monkeyStuff.keyframeAnimation.LiveKeyframeUpdater;
import b3dElements.animations.keyframeAnimations.Properties.QuaternionProperty;
import b3dElements.animations.keyframeAnimations.Properties.Vector3fProperty;
import b3dElements.animations.keyframeAnimations.AnimationType;
import b3dElements.animations.keyframeAnimations.Properties.BoolProperty;
import b3dElements.animations.keyframeAnimations.Properties.ColorRGBAProperty;
import b3dElements.animations.keyframeAnimations.Properties.IntProperty;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import monkeyStuff.CustomParticleEmitter;
import monkeyStuff.keyframeAnimation.Updaters.LiveALightUpdater;
import monkeyStuff.keyframeAnimation.Updaters.LiveDLightUpdater;
import monkeyStuff.keyframeAnimation.Updaters.LivePLightUpdater;
import monkeyStuff.keyframeAnimation.Updaters.LiveParticleEmitterUpdater;
import monkeyStuff.keyframeAnimation.Updaters.LiveSLightUpdater;
import monkeyStuff.keyframeAnimation.Updaters.LiveSpatialUpdater;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.search.TreeSearchable;
import other.Wizard;

/**
 *
 * @author User
 */
public class AnimationElementTree extends JXTree implements ActionListener
{

    private LiveKeyframeUpdater keyframeUpdater;
    private Object object;
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(true);
    private DefaultMutableTreeNode selectedNode = rootNode;
    private ArrayList<AttributeNode> attributeNodes = new ArrayList<AttributeNode>();
    private DefaultTreeModel treeModel;
    private B3D_Element element;
    /**
     * ********POPUP*************
     */
    private JPopupMenu rootPopup = new JPopupMenu();
    private JMenu addAttributeMenu = new JMenu("Add Attribute");
    private JMenuItem removeElementItem = new JMenuItem("Remove Element");
    private JPopupMenu leafPopup = new JPopupMenu();
    private JMenuItem valueListItem = new JMenuItem("Current Values");
    private JMenuItem copyAttributeItem = new JMenuItem("Copy Attribute");
    private JMenuItem insertAttributeItem = new JMenuItem("Insert Attribute");
    private JMenuItem removeAttributeItem = new JMenuItem("Remove Attribute");
    private boolean expanded = true;

    public AnimationElementTree(B3D_Element e, LiveKeyframeUpdater lku)
    {
        element = e;
        object = Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(element.getUUID()));
        if (lku == null)
        {
            if (object instanceof CustomParticleEmitter)
                keyframeUpdater = new LiveParticleEmitterUpdater((CustomParticleEmitter) object);
            else if (object instanceof Spatial)
                keyframeUpdater = new LiveSpatialUpdater((Spatial) object);
            else if (object instanceof AmbientLight)
                keyframeUpdater = new LiveALightUpdater((AmbientLight) object);
            else if (object instanceof DirectionalLight)
                keyframeUpdater = new LiveDLightUpdater((DirectionalLight) object);
            else if (object instanceof SpotLight)
                keyframeUpdater = new LiveSLightUpdater((SpotLight) object);
            else if (object instanceof PointLight)
                keyframeUpdater = new LivePLightUpdater((PointLight) object);
        } else
            keyframeUpdater = lku;
        addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                super.focusLost(e); //To change body of generated methods, choose Tools | Templates.
                clearSelection();
            }
        });
        initPopups();
        rootNode.setUserObject(element.getName());
        setRolloverEnabled(true);
        setSearchable(new TreeSearchable(this));
        setRowHeight(25);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                try
                {
                    final DefaultMutableTreeNode tempNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();
                    if (tempNode != null)
                    {
                        selectedNode = tempNode;
                    }
                } catch (NullPointerException npe)
                {
                    ObserverDialog.getObserverDialog().printError("Error in TreeSelectionListener", npe);
                }
            }
        });
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e) && CurrentData.getEditorWindow().getKeyframeAnimationEditor().isEditable())
                    if (selectedNode == rootNode)
                    {
                        addAttributeItems();
                        rootPopup.show(AnimationElementTree.this, e.getX(), e.getY());
                    } else
                        leafPopup.show(AnimationElementTree.this, e.getX(), e.getY());

            }
        });
        treeModel = new DefaultTreeModel(rootNode, true);
        addTreeExpansionListener(new BTreeExpansionListener());
        setModel(treeModel);
        setEditable(false);
        for (Object lkp : keyframeUpdater.getKeyframeProperties())
            attributeNodes.add(new AttributeNode((LiveKeyframeProperty) lkp));
    }

    public void updateElements()
    {
        rootNode.removeAllChildren();
        for (AttributeNode an : attributeNodes)
        {
            rootNode.add(an);
        }
        ((AttributesPanel) getParent().getParent()).updateAttributes();
        treeModel.reload();
        repaint();
    }

    private void addAttributeItems()
    {
        addAttributeMenu.removeAll();
        Vector<AnimationType> attributeTypes = new Vector<AnimationType>();
        insertAttributeItem.setEnabled(CurrentData.insertAttributes(attributeTypes, element, attributeNodes));
        for (AnimationType s : attributeTypes)
            addAttributeMenu.add(new AttributeItem(s.toString()));
    }

    public B3D_Element getElement()
    {
        return element;
    }

    private void initPopups()
    {
        rootPopup.add(addAttributeMenu);
        rootPopup.add(insertAttributeItem);
        addAttributeItems();
        rootPopup.add(removeElementItem);
        leafPopup.add(valueListItem);
        leafPopup.add(copyAttributeItem);
        leafPopup.add(new JSeparator());
        leafPopup.add(removeAttributeItem);
        copyAttributeItem.addActionListener(this);
        insertAttributeItem.addActionListener(this);
        valueListItem.addActionListener(this);
        removeAttributeItem.addActionListener(this);
        removeElementItem.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == valueListItem)
            new ValueObserverDialog((AttributeNode) selectedNode);
        else if (e.getSource() == removeElementItem)
            CurrentData.getEditorWindow().getKeyframeAnimationEditor().removeElement(this);
        else if (e.getSource() == removeAttributeItem)
        {
            // System.out.println("Removing Attribute!");
            keyframeUpdater.getKeyframeProperties().remove(((AttributeNode) selectedNode).getProperty());
            attributeNodes.remove(selectedNode);
            keyframeUpdater.getKeyframeProperties().remove(((AttributeNode) selectedNode).getProperty());
            expanded = true;
            updateElements();
            CurrentData.getEditorWindow().getKeyframeAnimationEditor().arrangeScrollbars();
            repaint();
        } else if (e.getSource() == copyAttributeItem)
            CurrentData.clipboardData = ((AttributeNode) selectedNode).getProperty().createNew(null);
        else if (e.getSource() == insertAttributeItem)
        {
            LiveKeyframeProperty lkp = ((LiveKeyframeProperty) CurrentData.clipboardData).createNew(keyframeUpdater);
            attributeNodes.add(new AttributeNode(lkp));
            keyframeUpdater.getKeyframeProperties().add(lkp);
            expanded = true;
            updateElements();
        }
    }

    class ValueObserverDialog extends BasicDialog
    {

        private JList valuesList;

        public ValueObserverDialog(AttributeNode aNode)
        {
            LiveKeyframeProperty lkp = aNode.getProperty().createNew(null);
            valuesList = new JList();
            DefaultListModel dlm = new DefaultListModel();
            int i = 0;
            lkp.calcValues();
            for (Serializable s : lkp.getValues())
                dlm.add(i, i++ + ": " + s);
            valuesList.setModel(dlm);
            add(new JScrollPane(valuesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
            setModal(false);
            setTitle(aNode.getUserObject().toString() + " - Values");
            setSize(250, 600);
            setLocationByPlatform(true);
            setVisible(true);
        }
    }

    class AttributeItem extends JMenuItem
    {

        public AttributeItem(String text)
        {
            super(text);
            addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        LiveKeyframeProperty property = null;
                        if (object instanceof Spatial)
                        {
                            Spatial spatial = (Spatial) object;
                            if (AnimationType.valueOfString(getText()).equals(AnimationType.Translation))
                                property = new Vector3fProperty(AnimationType.valueOfString(getText()),
                                        61, new Vector3f(spatial.getLocalTranslation()), keyframeUpdater);
                            else if (AnimationType.valueOfString(getText()).equals(AnimationType.Rotation))
                                property = new QuaternionProperty(AnimationType.valueOfString(getText()),
                                        61, new Quaternion(spatial.getLocalRotation()), keyframeUpdater);
                            else if (AnimationType.valueOfString(getText()).equals(AnimationType.Scale))
                                property = new Vector3fProperty(AnimationType.valueOfString(getText()),
                                        61, new Vector3f(spatial.getLocalScale()), keyframeUpdater);

                        }
                        if (object instanceof CustomParticleEmitter)
                        {
                            CustomParticleEmitter emitter = (CustomParticleEmitter) object;
                            if (AnimationType.valueOfString(getText()).equals(AnimationType.Frozen))
                                property = new BoolProperty(AnimationType.valueOfString(getText()),
                                        61, emitter.isEnabled(), keyframeUpdater);
                            else if (AnimationType.valueOfString(getText()).equals(AnimationType.Particles_Per_Second))
                                property = new IntProperty(AnimationType.valueOfString(getText()),
                                        61, (int) emitter.getParticlesPerSec(), keyframeUpdater);
                            else if (AnimationType.valueOfString(getText()).equals(AnimationType.Emit_All))
                                property = new BoolProperty(AnimationType.valueOfString(getText()),
                                        61, true, keyframeUpdater);
                            else if (AnimationType.valueOfString(getText()).equals(AnimationType.End_Color_Blend))
                                property = new ColorRGBAProperty(AnimationType.valueOfString(getText()),
                                        61, emitter.getEndColor(), keyframeUpdater);
                            else if (AnimationType.valueOfString(getText()).equals(AnimationType.Start_Color_Blend))
                                property = new ColorRGBAProperty(AnimationType.valueOfString(getText()),
                                        61, emitter.getStartColor(), keyframeUpdater);
                        }
                        if (object instanceof Light)
                        {
                            Light light = (Light) object;
                            if (AnimationType.valueOfString(getText()).equals(AnimationType.Light_Color_Blend))
                                property = new ColorRGBAProperty(AnimationType.valueOfString(getText()),
                                        61, new ColorRGBA(light.getColor()), keyframeUpdater);
                            else if (light instanceof DirectionalLight)
                            {
                                if (AnimationType.valueOfString(getText()).equals(AnimationType.Direction))
                                    property = new Vector3fProperty(AnimationType.valueOfString(getText()),
                                            61, ((DirectionalLight) light).getDirection(), keyframeUpdater);
                            } else if (light instanceof SpotLight)
                            {
                                SpotLight sLight = (SpotLight) light;
                                if (AnimationType.valueOfString(getText()).equals(AnimationType.Direction))
                                    property = new Vector3fProperty(AnimationType.valueOfString(getText()),
                                            61, sLight.getDirection(), keyframeUpdater);
                                else if (AnimationType.valueOfString(getText()).equals(AnimationType.Position))
                                    property = new Vector3fProperty(AnimationType.valueOfString(getText()),
                                            61, sLight.getPosition(), keyframeUpdater);
                            } else if (light instanceof PointLight)
                                if (AnimationType.valueOfString(getText()).equals(AnimationType.Position))
                                    property = new Vector3fProperty(AnimationType.valueOfString(getText()),
                                            61, ((PointLight) light).getPosition(), keyframeUpdater);
                        }
                        attributeNodes.add(new AttributeNode(property));
                        keyframeUpdater.getKeyframeProperties().add(property);
                        expanded = true;
                        updateElements();
                    } catch (Exception ex)
                    {
                        Logger.getLogger(AnimationElementTree.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    CurrentData.getEditorWindow().getKeyframeAnimationEditor().arrangeScrollbars();
                }
            });
        }
    }

    public LiveKeyframeUpdater getKeyframeUpdater()
    {
        return keyframeUpdater;
    }

    public ArrayList<AttributeNode> getAttributeNodes()
    {
        return attributeNodes;
    }

    public class AttributeNode extends DefaultMutableTreeNode
    {

        private LiveKeyframeProperty property;

        public AttributeNode(LiveKeyframeProperty p)
        {
            property = p;
            setAllowsChildren(false);
            setUserObject(p.type);
        }

        public LiveKeyframeProperty getProperty()
        {
            return property;
        }
    }

    public DefaultMutableTreeNode getRootNode()
    {
        return rootNode;
    }

    class BTreeExpansionListener implements TreeExpansionListener
    {

        public void treeCollapsed(TreeExpansionEvent event)
        {
            expanded = false;
            ((AttributesPanel) getParent().getParent()).updateAttributes();

        }

        public void treeExpanded(TreeExpansionEvent event)
        {
            expanded = true;
            ((AttributesPanel) getParent().getParent()).updateAttributes();
        }
    }

    public boolean isExpanded()
    {
        return expanded;
    }
}
