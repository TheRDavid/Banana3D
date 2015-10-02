package gui.elementTree;

import general.CurrentData;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTreeTransferHandler implements DragGestureListener, DragSourceListener, DropTargetListener
{

    private ElementTree tree;
    private DragSource dragSource; // dragsource
    private DropTarget dropTarget; //droptarget
    private static DefaultMutableTreeNode draggedNode;
    private DefaultMutableTreeNode draggedNodeParent;
    private static BufferedImage image = null; //buff image
    private Rectangle rect2D = new Rectangle();
    private boolean drawImage;

    protected AbstractTreeTransferHandler(ElementTree tree, int action, boolean drawIcon)
    {
        this.tree = tree;
        drawImage = drawIcon;
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(tree, action, this);
        dropTarget = new DropTarget(tree, action, this);
    }

    /* Methods for DragSourceListener */
    @Override
    public void dragDropEnd(DragSourceDropEvent dsde)
    {
        if (dsde.getDropSuccess() && dsde.getDropAction() == DnDConstants.ACTION_MOVE && draggedNodeParent != null)
        {
            ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(draggedNodeParent);
        }
        CurrentData.getEditorWindow().getB3DApp().startPairing();
        CurrentData.getEditorWindow().getTree().sync();
    }

    @Override
    public final void dragEnter(DragSourceDragEvent dsde)
    {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_MOVE)
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
        } else
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        }
    }

    @Override
    public final void dragOver(DragSourceDragEvent dsde)
    {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_MOVE)
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
        } else
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        }
    }

    @Override
    public final void dropActionChanged(DragSourceDragEvent dsde)
    {
        int action = dsde.getDropAction();
        if (action == DnDConstants.ACTION_MOVE)
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
        } else
        {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        }
    }

    @Override
    public final void dragExit(DragSourceEvent dse)
    {
        dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    /* Methods for DragGestureListener */
    @Override
    public final void dragGestureRecognized(DragGestureEvent dge)
    {
        TreePath path = tree.getSelectionPath();
        if (path != null)
        {
            draggedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            draggedNodeParent = (DefaultMutableTreeNode) draggedNode.getParent();
            if (drawImage)
            {
                Rectangle pathBounds = tree.getPathBounds(path); //getpathbounds of selectionpath
                JComponent lbl = (JComponent) tree.getCellRenderer().getTreeCellRendererComponent(tree, draggedNode, false, tree.isExpanded(path), ((DefaultTreeModel) tree.getModel()).isLeaf(path.getLastPathComponent()), 0, false);//returning the label
                lbl.setBounds(pathBounds);//setting bounds to lbl
                image = new BufferedImage(lbl.getWidth(), lbl.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);//buffered image reference passing the label's ht and width
                Graphics2D graphics = image.createGraphics();//creating the graphics for buffered image
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));     //Sets the Composite for the Graphics2D context
                lbl.setOpaque(false);
                lbl.paint(graphics); //painting the graphics to label
                graphics.dispose();
            }
            dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, image, new Point(0, 0), new TransferableNode(draggedNode), this);
        }
    }

    /* Methods for DropTargetListener */
    @Override
    public final void dragEnter(DropTargetDragEvent dtde)
    {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        if (drawImage)
        {
            paintImage(pt);
        }
        if (canPerformAction(tree, draggedNode, action, pt))
        {
            dtde.acceptDrag(action);
        } else
        {
            dtde.rejectDrag();
        }
    }

    @Override
    public final void dragExit(DropTargetEvent dte)
    {
        if (drawImage)
        {
            clearImage();
        }
    }

    @Override
    public final void dragOver(DropTargetDragEvent dtde)
    {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        tree.autoscroll(pt);
        if (drawImage)
        {
            paintImage(pt);
        }
        if (canPerformAction(tree, draggedNode, action, pt))
        {
            dtde.acceptDrag(action);
        } else
        {
            dtde.rejectDrag();
        }
    }

    @Override
    public final void dropActionChanged(DropTargetDragEvent dtde)
    {
        Point pt = dtde.getLocation();
        int action = dtde.getDropAction();
        if (drawImage)
        {
            paintImage(pt);
        }
        if (canPerformAction(tree, draggedNode, action, pt))
        {
            dtde.acceptDrag(action);
        } else
        {
            dtde.rejectDrag();
        }
    }

    @Override
    public final void drop(DropTargetDropEvent dtde)
    {
        try
        {
            if (drawImage)
            {
                clearImage();
            }
            int action = dtde.getDropAction();
            Transferable transferable = dtde.getTransferable();
            Point pt = dtde.getLocation();
            if (transferable.isDataFlavorSupported(TransferableNode.NODE_FLAVOR) && canPerformAction(tree, draggedNode, action, pt))
            {
                TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) transferable.getTransferData(TransferableNode.NODE_FLAVOR);
                DefaultMutableTreeNode newParentNode = (DefaultMutableTreeNode) pathTarget.getLastPathComponent();
                if (executeDrop(tree, node, newParentNode, action))
                {
                    dtde.acceptDrop(action);
                    dtde.dropComplete(true);
                    return;
                }
            }
            dtde.rejectDrop();
            dtde.dropComplete(false);
        } catch (UnsupportedFlavorException e)
        {
            dtde.rejectDrop();
            dtde.dropComplete(false);
        } catch (IOException ex)
        {
            Logger.getLogger(AbstractTreeTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final void paintImage(Point pt)
    {
        tree.paintImmediately(rect2D.getBounds());
        rect2D.setRect((int) pt.getX(), (int) pt.getY(), image.getWidth(), image.getHeight());
        tree.getGraphics().drawImage(image, (int) pt.getX(), (int) pt.getY(), tree);
    }

    private final void clearImage()
    {
        tree.paintImmediately(rect2D.getBounds());
    }

    public abstract boolean canPerformAction(ElementTree target, DefaultMutableTreeNode draggedNode, int action, Point location);

    public abstract boolean executeDrop(ElementTree tree, DefaultMutableTreeNode draggedNode, DefaultMutableTreeNode newParentNode, int action);
}