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
import javax.swing.JScrollPane;
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
    private JMenuItem removeAttributeItem = new JMenuItem("Remove Attribute");
    private boolean expanded = true;

    public AnimationElementTree(B3D_Element e, LiveKeyframeUpdater lku)
    {
        element = e;
        object = Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(element.getUUID()));
        if (lku == null)
        {
            if (object instanceof Spatial)
                keyframeUpdater = new LiveSpatialUpdater((Spatial) object);
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
                if (SwingUtilities.isRightMouseButton(e))
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
        Vector<AnimationType> attributeTypes = CurrentData.getAttributes(element, attributeNodes);
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
        addAttributeItems();
        rootPopup.add(removeElementItem);
        leafPopup.add(valueListItem);
        leafPopup.add(removeAttributeItem);
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
            System.out.println("Removing Attribute!");
            keyframeUpdater.getKeyframeProperties().remove(((AttributeNode) selectedNode).getProperty());
            attributeNodes.remove(selectedNode);
            keyframeUpdater.getKeyframeProperties().remove(((AttributeNode) selectedNode).getProperty());
            expanded = true;
            updateElements();
            CurrentData.getEditorWindow().getKeyframeAnimationEditor().arrangeScrollbars();
            repaint();
        }
    }

    class ValueObserverDialog extends BasicDialog
    {

        private JList valuesList;

        public ValueObserverDialog(AttributeNode aNode)
        {
            valuesList = new JList();
            DefaultListModel dlm = new DefaultListModel();
            int i = 0;
            for (Serializable s : aNode.getProperty().getValues())
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
                            if (AnimationType.valueOf(getText()).equals(AnimationType.Translation))
                            {
                                property = new Vector3fProperty(AnimationType.valueOf(getText()),
                                        10, new Vector3f(spatial.getLocalTranslation()), keyframeUpdater);
                            } else if (AnimationType.valueOf(getText()).equals(AnimationType.Rotation))
                            {
                                property = new QuaternionProperty(AnimationType.valueOf(getText()),
                                        10, new Quaternion(spatial.getLocalRotation()), keyframeUpdater);
                            } else if (AnimationType.valueOf(getText()).equals(AnimationType.Scale))
                            {
                                property = new Vector3fProperty(AnimationType.valueOf(getText()),
                                        10, new Vector3f(spatial.getLocalScale()), keyframeUpdater);
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
