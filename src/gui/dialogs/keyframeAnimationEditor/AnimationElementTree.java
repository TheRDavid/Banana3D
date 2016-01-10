/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.dialogs.keyframeAnimationEditor;

import b3dElements.B3D_Element;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import dialogs.ObserverDialog;
import general.CurrentData;
import gui.dialogs.keyframeAnimationEditor.KeyframeAnimationFrame.AttributesPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import monkeyStuff.keyframeAnimation.KeyframeProperty;
import monkeyStuff.keyframeAnimation.KeyframeUpdater;
import monkeyStuff.keyframeAnimation.Properties.QuaternionProperty;
import monkeyStuff.keyframeAnimation.Properties.Vector3fProperty;
import monkeyStuff.keyframeAnimation.Updaters.AnimationType;
import monkeyStuff.keyframeAnimation.Updaters.SpatialUpdater;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.search.TreeSearchable;
import other.Wizard;

/**
 *
 * @author User
 */
public class AnimationElementTree extends JXTree
{

    private KeyframeUpdater keyframeUpdater;
    private Object object;
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(true);
    private DefaultMutableTreeNode selectedNode = rootNode;
    private ArrayList<AttributeNode> attributeNodes = new ArrayList<AttributeNode>();
    private DefaultTreeModel treeModel;
    private B3D_Element element;
    /**
     * ********POPUP*************
     */
    private JPopupMenu popupMenu = new JPopupMenu();
    private JMenu addAttributeMenu = new JMenu("Add Attribute");
    private JMenuItem removeItem = new JMenuItem("Remove Element");
    private boolean expanded = false;

    public AnimationElementTree(B3D_Element e)
    {
        element = e;
        object = Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(element.getUUID()));
        if (object instanceof Spatial)
        {
            keyframeUpdater = new SpatialUpdater((Spatial) object);
        }
        addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                super.focusLost(e); //To change body of generated methods, choose Tools | Templates.
                clearSelection();
            }
        });
        initPopup();
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
                if (SwingUtilities.isRightMouseButton(e))
                    if (selectedNode == rootNode)
                    {
                        addAttributeItems();
                        popupMenu.show(AnimationElementTree.this, e.getX(), e.getY());
                    }

            }
        });
        treeModel = new DefaultTreeModel(rootNode, true);
        addTreeExpansionListener(new BTreeExpansionListener());
        setModel(treeModel);
        setEditable(false);
    }

    private void updateElements()
    {
        rootNode.removeAllChildren();
        for (AttributeNode an : attributeNodes)
        {
            rootNode.add(an);
        }
        ((AttributesPanel) getParent()).updateAttributes();
        treeModel.reload();
        repaint();
    }

    private void initPopup()
    {
        popupMenu.add(addAttributeMenu);
        addAttributeItems();
        popupMenu.add(removeItem);
    }

    private void addAttributeItems()
    {
        addAttributeMenu.removeAll();
        Vector<AnimationType> attributeTypes = CurrentData.getAttributes(element, attributeNodes);
        for (AnimationType s : attributeTypes)
            addAttributeMenu.add(new AttributeItem(s.toString()));
    }

    public B3D_Element getElement()
    {
        return element;
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
                        KeyframeProperty property = null;
                        if (object instanceof Spatial)
                        {
                            Spatial spatial = (Spatial) object;
                            if (AnimationType.valueOf(getText()).equals(AnimationType.Translation))
                            {
                                property = new Vector3fProperty(AnimationType.valueOf(getText()),
                                        10, new Vector3f(spatial.getLocalTranslation()));
                            } else if (AnimationType.valueOf(getText()).equals(AnimationType.Rotation))
                            {
                                property = new QuaternionProperty(AnimationType.valueOf(getText()),
                                        10, new Quaternion(spatial.getLocalRotation()));
                            } else if (AnimationType.valueOf(getText()).equals(AnimationType.Scale))
                            {
                                property = new Vector3fProperty(AnimationType.valueOf(getText()),
                                        10, new Vector3f(spatial.getLocalScale()));
                            }
                        }
                        attributeNodes.add(new AttributeNode(property));
                        keyframeUpdater.getKeyframeProperties().add(property);
                        expanded = true;
                        updateElements();
                    } catch (Exception ex)
                    {
                        Logger.getLogger(AnimationElementTree.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }

    public KeyframeUpdater getKeyframeUpdater()
    {
        return keyframeUpdater;
    }

    public ArrayList<AttributeNode> getAttributeNodes()
    {
        return attributeNodes;
    }

    public class AttributeNode extends DefaultMutableTreeNode
    {

        private KeyframeProperty property;

        public AttributeNode(KeyframeProperty p)
        {
            property = p;
            setAllowsChildren(false);
            setUserObject(p.type);
        }

        public KeyframeProperty getProperty()
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
            ((AttributesPanel) getParent()).updateAttributes();

        }

        public void treeExpanded(TreeExpansionEvent event)
        {
            expanded = true;
            ((AttributesPanel) getParent()).updateAttributes();
        }
    }

    public boolean isExpanded()
    {
        return expanded;
    }
}
