package gui.elementTree;

import b3dElements.spatials.B3D_Node;
import b3dElements.spatials.B3D_Spatial;
import general.CurrentData;
import java.awt.*;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.util.UUID;
import other.Wizard;

public class DefaultTreeTransferHandler extends AbstractTreeTransferHandler
{

    public DefaultTreeTransferHandler(ElementTree tree, int action)
    {
        super(tree, action, true);
    }

    @Override
    public boolean canPerformAction(ElementTree elementTree, DefaultMutableTreeNode draggedNode, int action, Point location)
    {
        if (checkIfSpatial(draggedNode))
        {
            TreePath pathTarget = elementTree.getPathForLocation(location.x, location.y);
            if (pathTarget == null)
            {
                elementTree.setSelectionPath(null);
                CurrentData.getEditorWindow().getB3DApp().cancelPairing();
                return (false);
            }
            elementTree.setSelectionPath(pathTarget);
            if (action == DnDConstants.ACTION_MOVE)
            {
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) pathTarget.getLastPathComponent();
                if (draggedNode.isRoot()
                        || parentNode == draggedNode.getParent()
                        || parentNode.equals(draggedNode)
                        || parentNode.isNodeAncestor(draggedNode)
                        || !((DefaultMutableTreeNode) pathTarget.getLastPathComponent()).getAllowsChildren())
                {
                    CurrentData.getEditorWindow().getB3DApp().cancelPairing();
                    return false;
                } else
                {
                    /*Removing the dragged Spatial from the ElementList, because it will be added again when put into the new parent
                     CurrentData.getEditorWindow().getB3DApp().getElementList().removeByElement(
                     CurrentData.getEditorWindow().getB3DApp().getElementList().getElement(
                     CurrentData.getEditorWindow().getTree().getNodeIds().get(draggedNode))); OR NOT!!!!!!!!!!!!!!!!!!!!*/
                    UUID childUUID = CurrentData.getEditorWindow().getTree().getNodeIds().get(draggedNode);
                    B3D_Spatial b3d_child = (B3D_Spatial) Wizard.getObjects().getB3D_Element(childUUID);
                    if (parentNode.equals(elementTree.getSpatialsNode()))
                    {
                        CurrentData.getEditorWindow().getB3DApp().prepairSceneNodePairing(
                                b3d_child);
                        return true;
                    } else
                    {
                        UUID parentUUID = CurrentData.getEditorWindow().getTree().getNodeIds().get((DefaultMutableTreeNode) pathTarget.getLastPathComponent());
                        B3D_Node b3d_parent = (B3D_Node) Wizard.getObjects().getB3D_Element(parentUUID);
                        CurrentData.getEditorWindow().getB3DApp().prepairPairing(
                                b3d_parent, b3d_child);
                        return (true);
                    }
                }
            } else
            {
                CurrentData.getEditorWindow().getB3DApp().cancelPairing();
                return (false);
            }
        } else
        {
            CurrentData.getEditorWindow().getB3DApp().cancelPairing();
            return false;
        }
    }

    private boolean checkIfSpatial(DefaultMutableTreeNode node)
    {
        if (CurrentData.getEditorWindow().getTree().getNodeIds().containsKey(node))
        {
            UUID elementUUID = CurrentData.getEditorWindow().getTree().getNodeIds().get(node);
            if (Wizard.getObjects().getB3D_Element(elementUUID) instanceof B3D_Spatial)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean executeDrop(ElementTree target, DefaultMutableTreeNode draggedNode, DefaultMutableTreeNode newParentNode, int action)
    {
        if (action == DnDConstants.ACTION_MOVE)
        {
            draggedNode.removeFromParent();
            ((DefaultTreeModel) target.getModel()).insertNodeInto(draggedNode, newParentNode, newParentNode.getChildCount());
            TreePath treePath = new TreePath(draggedNode.getPath());
            target.scrollPathToVisible(treePath);
            target.setSelectionPath(treePath);
            return (true);
        }
        return (false);
    }
}