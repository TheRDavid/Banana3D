package gui.elementTree;

import static gui.elementTree.TransferableNode.NODE_FLAVOR;
import java.awt.datatransfer.*;
import javax.swing.tree.*;
import java.util.*;

public class TransferableNode implements Transferable
{

    public static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Node");
    private DefaultMutableTreeNode node;
    private DataFlavor[] flavors =
    {
        NODE_FLAVOR
    };

    public TransferableNode(DefaultMutableTreeNode nd)
    {
        node = nd;
    }

    @Override
    public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor == NODE_FLAVOR)
        {
            return node;
        } else
        {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return Arrays.asList(flavors).contains(flavor);
    }
}