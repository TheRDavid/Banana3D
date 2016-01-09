/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.dialogs.keyframeAnimationEditor;

import b3dElements.B3D_Element;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import dialogs.ObserverDialog;
import general.CurrentData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import monkeyStuff.keyframeAnimation.KeyframeProperty;
import monkeyStuff.keyframeAnimation.KeyframeUpdater;
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
class AnimationElementTree extends JXTree
{

    private KeyframeUpdater keyframeUpdater;
    private Object object;
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(true);
    private DefaultMutableTreeNode selectedNode = rootNode;
    private DefaultTreeModel treeModel;
    private B3D_Element element;
    /**********POPUP**************/
    private JPopupMenu popupMenu = new JPopupMenu();
    private JMenu addAttributeMenu = new JMenu("Add Attribute");
    private JMenuItem removeItem = new JMenuItem("Remove Element");

    public AnimationElementTree(B3D_Element e)
    {
        element = e;
        object = Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(element.getUUID()));
        if (object instanceof Spatial)
        {
            keyframeUpdater = new SpatialUpdater((Spatial) object);
        }
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
                        popupMenu.show(AnimationElementTree.this, e.getX(), e.getY());

            }
        });
        treeModel = new DefaultTreeModel(rootNode, true);
        setModel(treeModel);
        setEditable(false);
    }

    private void initPopup()
    {
        popupMenu.add(addAttributeMenu);
        Vector<AnimationType> attributeTypes = CurrentData.getAttributes(element);
        for (AnimationType s : attributeTypes)
            addAttributeMenu.add(new AttributeItem(s.toString()));
        popupMenu.add(removeItem);
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
                    if (AnimationType.valueOf(getText()).equals(AnimationType.Translation))
                    {
                        try
                        {
                            Spatial spatial = (Spatial) object;
                            Vector3fProperty property = new Vector3fProperty(getText(), 200, spatial.getLocalTranslation(), new Vector3f(0, 10, 0));
                            keyframeUpdater.getKeyframeProperties().add(property);
                        } catch (Exception ex)
                        {
                            Logger.getLogger(AnimationElementTree.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
        }
    }

    public KeyframeUpdater getKeyframeUpdater()
    {
        return keyframeUpdater;
    }
}
