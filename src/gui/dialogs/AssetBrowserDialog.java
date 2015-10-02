package gui.dialogs;

import general.CurrentData;
import monkeyStuff.B3DApp;
import components.BButton;
import components.CancelButton;
import dialogs.BasicDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Callable;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class AssetBrowserDialog extends BasicDialog
{

    private JCheckBox alwaysOnTopCheckBox = new JCheckBox("Always on top");
    private BButton refreshBButton = new BButton("Refresh");
    private JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    private JTree assetTree = new JTree();
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Asset Files");

    public AssetBrowserDialog()
    {
        setModal(false);
        setAlwaysOnTop(CurrentData.getConfiguration().assetbrowserontop);
        alwaysOnTopCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                AssetBrowserDialog.this.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
                CurrentData.getConfiguration().setAssetbrowserontop(alwaysOnTopCheckBox.isSelected());
            }
        });
        refreshBButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                refresh(false);
            }
        });
        assetTree.setModel(new FileTreeModel(CurrentData.getProject().getAssetsFolder()));
        assetTree.setEditable(true);
        assetTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                if (assetTree.getLastSelectedPathComponent() != null)
                {
                    final String name = assetTree.getLastSelectedPathComponent().toString().toLowerCase();
                    if (name.contains("."))
                    {
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                        {
                            @Override
                            public Void call() throws Exception
                            {
                                if (name.endsWith(".scene") || name.endsWith(".obj") || name.endsWith(".blend") || name.endsWith(".j3o"))
                                {
                                    CurrentData.getEditorWindow().getB3DApp().setInteractionType(B3DApp.InteractionType.InsertModel, assetTree.getLastSelectedPathComponent().toString());
                                } else if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".png"))
                                {
                                    CurrentData.getEditorWindow().getB3DApp().setInteractionType(B3DApp.InteractionType.InsertTexture, assetTree.getLastSelectedPathComponent().toString());
                                }
                                return null;
                            }
                        });
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(assetTree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        controlsPanel.add(alwaysOnTopCheckBox);
        controlsPanel.add(refreshBButton);
        CancelButton cancelButton = new CancelButton(this, "Close");
        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getConfiguration().setAssetbrowsershown(false);
                CurrentData.getEditorWindow().getMainMenu().getViewMenu().getShowAssetBrowserItem().setSelected(false);
            }
        });
        alwaysOnTopCheckBox.setSelected(CurrentData.getConfiguration().assetbrowserontop);
        controlsPanel.add(alwaysOnTopCheckBox);
        controlsPanel.add(refreshBButton);
        controlsPanel.add(cancelButton);
        add(controlsPanel, BorderLayout.SOUTH);
        setSize(600, 430);
        setLocation(0, 50);
        setTitle("Assets");
        setVisible(CurrentData.getConfiguration().assetbrowsershown);
        refresh(false);
    }

    protected void refresh(boolean resetInteractionType)
    {
        assetTree.setModel(new FileTreeModel(CurrentData.getProject().getAssetsFolder()));
        for (int i = 0; i < assetTree.getRowCount(); i++)
        {
            assetTree.expandRow(i);
        }
        if (resetInteractionType)
        {
            CurrentData.getEditorWindow().getB3DApp().setInteractionType(B3DApp.InteractionType.Default, null);
        }
    }

    class FileTreeModel implements TreeModel
    {

        private File root;
        private Vector listeners = new Vector();
        private FilenameFilter filter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                name = name.toLowerCase();
                return !name.contains(".") || name.endsWith(".scene") || name.endsWith(".obj") || name.endsWith(".blend") || name.endsWith(".j3o")
                        || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif") || name.endsWith(".png");
            }
        };

        public FileTreeModel(File rootDirectory)
        {
            root = rootDirectory;
            addTreeModelListener(new TreeModelListener()
            {
                @Override
                public void treeNodesChanged(TreeModelEvent e)
                {
                    refresh(true);
                }

                @Override
                public void treeNodesInserted(TreeModelEvent e)
                {
                    refresh(true);
                }

                @Override
                public void treeNodesRemoved(TreeModelEvent e)
                {
                    refresh(true);
                }

                @Override
                public void treeStructureChanged(TreeModelEvent e)
                {
                    refresh(true);
                }
            });
        }

        @Override
        public Object getRoot()
        {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index)
        {
            File directory = (File) parent;
            String[] children = directory.list(filter);
            return new TreeFile(directory, children[index]);
        }

        @Override
        public int getChildCount(Object parent)
        {
            File file = (File) parent;
            if (file.isDirectory())
            {
                String[] fileList = file.list(filter);
                if (fileList != null)
                {
                    return file.list(filter).length;
                }
            }
            return 0;
        }

        @Override
        public boolean isLeaf(Object node)
        {
            File file = (File) node;
            return file.isFile();
        }

        @Override
        public int getIndexOfChild(Object parent, Object child)
        {
            File directory = (File) parent;
            File file = (File) child;
            String[] children = directory.list(filter);
            for (int i = 0; i < children.length; i++)
            {
                if (file.getName().equals(children[i]))
                {
                    return i;
                }
            }
            return -1;

        }

        @Override
        public void valueForPathChanged(TreePath path, Object value)
        {
            File oldFile = (File) path.getLastPathComponent();
            String fileParentPath = oldFile.getParent();
            String newFileName = (String) value;
            File targetFile = new File(fileParentPath, newFileName);
            oldFile.renameTo(targetFile);
            File parent = new File(fileParentPath);
            int[] changedChildrenIndices =
            {
                getIndexOfChild(parent, targetFile)
            };
            Object[] changedChildren =
            {
                targetFile
            };
            fireTreeNodesChanged(path.getParentPath(), changedChildrenIndices, changedChildren);

        }

        private void fireTreeNodesChanged(TreePath parentPath, int[] indices, Object[] children)
        {
            TreeModelEvent event = new TreeModelEvent(this, parentPath, indices, children);
            Iterator iterator = listeners.iterator();
            TreeModelListener listener = null;
            while (iterator.hasNext())
            {
                listener = (TreeModelListener) iterator.next();
                listener.treeNodesChanged(event);
            }
        }

        @Override
        public void addTreeModelListener(TreeModelListener listener)
        {
            listeners.add(listener);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener listener)
        {
            listeners.remove(listener);
        }

        private class TreeFile extends File
        {

            public TreeFile(File parent, String child)
            {
                super(parent, child);
            }

            @Override
            public String toString()
            {
                return getName();
            }
        }
    }
}
