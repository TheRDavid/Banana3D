package gui.elementTree;

import b3dElements.B3D_Element;
import b3dElements.filters.B3D_Filter;
import b3dElements.lights.B3D_Light;
import b3dElements.spatials.B3D_Node;
import b3dElements.spatials.B3D_Spatial;
import b3dElements.other.B3D_MotionEvent;
import gui.editPanes.others.MotionPathTaskPane;
import gui.menu.AddMenu;
import general.CurrentData;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.cinematic.PlayState;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import dialogs.ObserverDialog;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jme3tools.optimize.GeometryBatchFactory;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.search.TreeSearchable;
import other.ObjectToElementConverter;
import other.Wizard;

public class ElementTree extends JXTree
{

    private DefaultTreeModel treeModel;
    private HashMap<DefaultMutableTreeNode, UUID> nodeIndexes = new HashMap<DefaultMutableTreeNode, UUID>();
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Scene", true);
    private DefaultMutableTreeNode spatialsNode = new DefaultMutableTreeNode("Objects");
    private DefaultMutableTreeNode lightsNode = new DefaultMutableTreeNode("Global Lights");
    private DefaultMutableTreeNode filtersNode = new DefaultMutableTreeNode("Filters");
    private DefaultMutableTreeNode othersNode = new DefaultMutableTreeNode("Other");
    private DefaultMutableTreeNode selectedNode = null;
    private JPopupMenu addPopupMenu = new JPopupMenu();
    private AddMenu addMenu = new AddMenu();
    private String oldName;
    private boolean nameChangingLegit = false;
    private ControlsPopupMenu controlsPopupMenu = new ControlsPopupMenu();
    private boolean codeSelect = false;
    private Insets autoscrollInsets = new Insets(20, 20, 20, 20);

    public void autoscroll(Point cursorLocation)
    {
        Insets insets = getAutoscrollInsets();
        Rectangle outer = getVisibleRect();
        Rectangle inner = new Rectangle(outer.x + insets.left, outer.y + insets.top, outer.width - (insets.left + insets.right), outer.height - (insets.top + insets.bottom));
        if (!inner.contains(cursorLocation))
        {
            Rectangle scrollRect = new Rectangle(cursorLocation.x - insets.left, cursorLocation.y - insets.top, insets.left + insets.right, insets.top + insets.bottom);
            scrollRectToVisible(scrollRect);
        }
    }

    public Insets getAutoscrollInsets()
    {
        return autoscrollInsets;
    }

    public void init()
    {
        setRolloverEnabled(true);
        setSearchable(new TreeSearchable(this));
        setRowHeight(25);
        new DefaultTreeTransferHandler(this, DnDConstants.ACTION_MOVE);
        setAutoscrolls(true);
        addMenu.setEnabled(true);
        addPopupMenu.add(addMenu);
        setEnabled(false);
        setEditable(true);
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
                        oldName = (String) tempNode.getUserObject();
                        selectedNode = tempNode;
                        try
                        {
                            if (nodeIndexes.get(tempNode) == null)
                                CurrentData.getEditorWindow().getB3DApp().setSelectedNode(CurrentData.getEditorWindow().getB3DApp().getSceneNode());
                            else
                                CurrentData.getEditorWindow().getB3DApp().setSelectedUUID(nodeIndexes.get(tempNode));
                        } catch (NullPointerException npe)
                        {
                            npe.printStackTrace();
                            ObserverDialog.getObserverDialog().printError("Error selecting Element chosen in Tree", npe);
                        }
                    }
                } catch (NullPointerException npe)
                {
                    ObserverDialog.getObserverDialog().printError("Error in TreeSelectionListener", npe);
                }
            }
        });
        addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    setSelectionRow(getClosestRowForLocation(e.getX(), e.getY()));
                }
                if (e.getButton() == MouseEvent.BUTTON3)  //controls
                {
                    if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() != null)
                    {
                        if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Node && !(CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof TerrainQuad))
                        {
                            addMenu.remove(addMenu.getObject3DMenu());
                            controlsPopupMenu.add(addMenu.getObject3DMenu());
                        } else
                        {
                            controlsPopupMenu.remove(addMenu.getObject3DMenu());
                            addMenu.add(addMenu.getObject3DMenu(), 0);
                        }
                    } else
                    {
                        controlsPopupMenu.remove(addMenu.getObject3DMenu());
                        addMenu.add(addMenu.getObject3DMenu(), 0);
                    }
                    controlsPopupMenu.popup(selectedNode, e.getX(), e.getY());
                } else if (e.getButton() == MouseEvent.BUTTON2)
                {
                    controlsPopupMenu.remove(addMenu.getObject3DMenu());
                    addMenu.add(addMenu.getObject3DMenu(), 0);
                    addPopupMenu.show(ElementTree.this, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
            }
        });
        rootNode.removeAllChildren();
        rootNode.add(spatialsNode);
        rootNode.add(lightsNode);
        rootNode.add(filtersNode);
        rootNode.add(othersNode);
        treeModel = new DefaultTreeModel(rootNode, true);
        treeModel.addTreeModelListener(new RenameListener());
        setModel(treeModel);
        setCellRenderer(new ElementTreeCellRenderer(this));
    }
    private HashMap<UUID, Boolean> expansionStates;

    public void sync()
    {
        synchronized (nodeIndexes)
        {
            expansionStates = new HashMap<UUID, Boolean>(nodeIndexes.size());
            for (Map.Entry<DefaultMutableTreeNode, UUID> entry : nodeIndexes.entrySet())
            {
                expansionStates.put(entry.getValue(), isExpanded(new TreePath(entry.getKey().getPath())));
            }
            nodeIndexes.clear();
            spatialsNode.removeAllChildren();
            lightsNode.removeAllChildren();
            filtersNode.removeAllChildren();
            othersNode.removeAllChildren();
            List<B3D_Element> list = new ArrayList<B3D_Element>();
            for (B3D_Element e : Wizard.getObjects().getB3D_ElementsIterator())
                list.add(e);
            Collections.sort(list, new Comparator<B3D_Element>()
            {
                @Override
                public int compare(B3D_Element o1, B3D_Element o2)
                {
                    if (o1 != null && o2 != null && o1.getName() != null && o2.getName() != null)
                    {
                        if (CurrentData.getConfiguration().treesort.equals("a-z(cs)"))
                        {
                            return o1.getName().compareTo(o2.getName());
                        } else if (CurrentData.getConfiguration().treesort.equals("a-z(no_cs)"))
                        {
                            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                        } else if (CurrentData.getConfiguration().treesort.equals("z-a(cs)"))
                        {
                            return -o1.getName().compareTo(o2.getName());
                        } else
                        {
                            return -o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                        }
                    } else
                    {
                        return 0;
                    }
                }
            });
            List<B3D_Filter> filterList = new ArrayList<B3D_Filter>();
            for (B3D_Element element : list)
                if (element != null)
                    if (element instanceof B3D_Spatial)
                    {
                        if (((B3D_Spatial) element).getParentUUID().equals(Wizard.NULL_SELECTION))
                        {
                            System.out.println("Handling " + element.getName() + " (" + element.getUUID() + ")");
                            handleChild(spatialsNode, element);
                        }
                    } else if (element instanceof B3D_Light)
                    {
                        System.out.println(element + " - " + element.getUUID());
                        DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode(element.getName(), false);
                        nodeIndexes.put(tempNode, element.getUUID());
                        lightsNode.add(tempNode);
                    } else if (element instanceof B3D_Filter)
                    {
                        //Sorting filters by FilterIndex of the elements and adding them to the treenode after in a loop
                        filterList.add((B3D_Filter) element);
                    } else
                    {
                        DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode(element.getName(), false);
                        nodeIndexes.put(tempNode, element.getUUID());
                        othersNode.add(tempNode);
                    }
            Collections.sort(filterList, new Comparator<B3D_Filter>()
            {
                @Override
                public int compare(B3D_Filter o1, B3D_Filter o2)
                {
                    return o1.getFilterIndex() - o2.getFilterIndex();
                }
            });
            for (B3D_Filter filter : filterList)
            {
                DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode(filter.getName() + " - " + filter.getFilterIndex(), false);
                nodeIndexes.put(tempNode, filter.getUUID());
                filtersNode.add(tempNode);
            }
            treeModel.reload();
            expandPath(new TreePath(spatialsNode.getPath()));
            expandPath(new TreePath(lightsNode.getPath()));
            expandPath(new TreePath(filtersNode.getPath()));
            expandPath(new TreePath(othersNode.getPath()));
            for (Map.Entry<UUID, Boolean> entry : expansionStates.entrySet())
            {
                if (entry.getValue())
                {
                    for (Map.Entry<DefaultMutableTreeNode, UUID> e : nodeIndexes.entrySet())
                    {
                        if (entry.getKey().equals(e.getValue()))
                        {
                            expandPath(new TreePath(e.getKey().getPath()));
                        }
                    }
                }
            }
            updateSelection();
        }
        /*synchronized (nodeIndexes)
         {
         for (B3D_Element e : Wizard.getObjects().getB3D_ElementsIterator())
         {
         System.out.println("At " + e.getUUID() + " is " + e.getName().toString());
         }
         }*/
    }

    private void handleChild(DefaultMutableTreeNode parentNode, B3D_Element element)
    {
        synchronized (nodeIndexes)
        {
            int spatialID = Wizard.getObjectReferences().getID(element.getUUID());
            Spatial spatial = (Spatial) Wizard.getObjects().getOriginalObject(spatialID);
            DefaultMutableTreeNode tempNode = new DefaultMutableTreeNode(element.getName(),
                    (spatial instanceof Node && !(spatial instanceof TerrainQuad))
                    && (spatial.getUserData("modelName") == null));
            parentNode.add(tempNode);
            nodeIndexes.put(tempNode, element.getUUID());
            if (spatial instanceof Node && !(CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof TerrainQuad)
                    && (spatial.getUserData("modelName") == null))
            {
                Node node = (Node) spatial;
                for (Spatial child : node.getChildren())
                {
                    UUID childElementUUID = Wizard.getObjectReferences().getUUID(child.hashCode());
                    B3D_Element ele = Wizard.getObjects().getB3D_Element(childElementUUID);
                    if (ele != null)
                        handleChild(tempNode, ele);
                }
            }
        }
    }

    public void updateSelection()
    {
        synchronized (nodeIndexes)
        {
            if (!codeSelect)
            {
                if (CurrentData.getEditorWindow().getB3DApp().getSelectedUUID() != Wizard.NULL_SELECTION)
                {
                    for (Map.Entry<DefaultMutableTreeNode, UUID> entry : nodeIndexes.entrySet())
                    {
                        if (entry.getValue().equals(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID()))
                        {
                            TreePath tPath = new TreePath(treeModel.getPathToRoot(entry.getKey()));
                            setSelectionPath(tPath);
                        }
                    }
                }
            } else
            {
                codeSelect = false;
            }
            repaint();
        }
    }

    private class ControlsPopupMenu extends JPopupMenu
    {

        private JMenuItem renameItem = new JMenuItem("Rename", new ImageIcon("dat//img//menu//edit.png"));
        private JMenuItem twinItem = new JMenuItem("Duplicate", new ImageIcon("dat//img//menu//duplicate.png"));
        private JMenuItem deleteItem = new JMenuItem("Delete", new ImageIcon("dat//img//menu//delete.png"));
        private JMenuItem jumpToItem = new JMenuItem("Jump to", new ImageIcon("dat//img//menu//jumpTo.png"));
        private JMenuItem lookAtItem = new JMenuItem("Look at", new ImageIcon("dat//img//menu//lookAt.png"));
        private JCheckBoxMenuItem enableItem = new JCheckBoxMenuItem("Enabled", new ImageIcon("dat//img//menu//eye.png"));
        private JMenuItem dissolveItem = new JMenuItem("Dissolve", new ImageIcon("dat//img//menu//dissolve.png"));
        private JMenuItem connectItem = new JMenuItem("Dissolve", new ImageIcon("dat//img//menu//connect.png"));
        private JMenuItem batchNodeItem = new JMenuItem("Export as optimized Model", new ImageIcon("dat//img//menu//batch.png"));
        private JMenuItem filterUpItem = new JMenuItem("Up", new ImageIcon("dat//img//menu//arrowUp.png"));
        private JMenuItem filterDownItem = new JMenuItem("Down", new ImageIcon("dat//img//menu//arrowDown.png"));
        private JMenuItem playItem = new JMenuItem(new ImageIcon("dat//img//menu//play.png"));
        private JMenuItem stopItem = new JMenuItem("Stop", new ImageIcon("dat//img//menu//stop.png"));
        private JMenuItem numberElementsItem = new JMenuItem("Number elements", new ImageIcon("dat//img//menu//number.png"));
        private JCheckBoxMenuItem showAllMotionPathsItem = new JCheckBoxMenuItem("Show all MotionPaths", new ImageIcon("dat//img//menu//motionPath.png"),
                CurrentData.getConfiguration().showallmotionpaths);
        private JMenuItem animateItem = new JMenuItem("Animate", new ImageIcon("dat//img//menu//random.png"));
        private JMenuItem copyIDItem = new JMenuItem("Copy ID to clipboard", new ImageIcon("dat//img//menu//duplicate.png"));

        public ControlsPopupMenu()
        {
            //Damit es vor dem Canvas gezeichnet wird
            setLightWeightPopupEnabled(false);
            copyIDItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                            new StringSelection(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID().toString()), null);
                }
            });
            batchNodeItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    JFileChooser jfc = new JFileChooser(CurrentData.getProject().getAssetsFolder() + "//");
                    jfc.setDialogTitle("Save Model");
                    //Optimize and save Model
                    if (jfc.showSaveDialog(ElementTree.this) == JFileChooser.APPROVE_OPTION)
                    {
                        final String modelName = jfc.getSelectedFile().getName();
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                        {
                            public Void call() throws Exception
                            {
                                Node newNode = GeometryBatchFactory.optimize(CurrentData.getEditorWindow().getB3DApp().getSelectedNode().clone(true), true);
                                BinaryExporter.getInstance().save(newNode, new File(CurrentData.getProject().getAssetsFolder() + "//" + modelName + ".j3o"));
                                /*
                                 //Keep track of old B3D_Element
                                 B3D_Node node = ((B3D_Node) Wizard.getObjects().getB3D_Element(
                                 Wizard.getObjectReferences().getUUID(CurrentData.getEditorWindow().getB3DApp().getSelectedNode().hashCode())));
                                 //Remove old Node
                                 CurrentData.execDelete();
                                 //Load new Model
                                 //Model itself
                                 Spatial s = CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadModel(modelName + ".j3o");
                                 s.setUserData("angles", new Vector3f());
                                 s.setUserData("scale", new Vector3f(1, 1, 1));
                                 s.setUserData("modelName", modelName);
                                 s.setName(modelName);
                                 //Set UserData to all children
                                 if (s instanceof Node)
                                 {
                                 Vector<Spatial> geoms = new Vector<Spatial>();
                                 Wizard.insertAllSpatials((Node) s, geoms);
                                 for (Spatial g : geoms)
                                 {
                                 g.setUserData("angles", new Vector3f());
                                 g.setUserData("scale", new Vector3f(1, 1, 1));
                                 g.setUserData("modelChild", "yup");
                                 g.setUserData("modelName", "modelChild");
                                 g.setShadowMode(RenderQueue.ShadowMode.Inherit);
                                 g.setName("modelChild");
                                 }
                                 }
                                 ObjectToElementConverter.convertMode= ObjectToElementConverter.ConvertMode.CREATING_TWIN;
                                 b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(s);
                                 element.setUuid(node.getUUID());
                                 Wizard.getObjects().add(s, element);
                                 //Just use hashcode s.setUserData("ID", element.getUUID());
                                 CurrentData.getEditorWindow().getB3DApp().setSelectedNode(CurrentData.getEditorWindow().getB3DApp().getSceneNode());
                                 CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(s);
                                 * */
                                return null;
                            }
                        });
                    }
                }
            });
            enableItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ((Filter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).setEnabled(enableItem.isSelected());
                }
            });
            numberElementsItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int number = 1;
                    for (Spatial spatial : ((Node) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getChildren())
                    {
                        spatial.setName(spatial.getName() + " [" + number + "]");
                        UUID elementUUID = Wizard.getObjectReferences().getUUID(spatial.hashCode());
                        Wizard.getObjects().getB3D_Element(elementUUID).setName(spatial.getName());
                        number++;
                    }
                    sync();
                }
            });
            showAllMotionPathsItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getConfiguration().setShowallmotionpaths(showAllMotionPathsItem.isSelected());
                    CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
                }
            });
            lookAtItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.execLookAt();
                }
            });
            twinItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.execCreateTwin();
                }
            });
            animateItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getAnimationScriptDialog().openTab(Wizard.getObjects().getB3D_Element(nodeIndexes.get(selectedNode)));
                    CurrentData.getAnimationScriptDialog().setVisible(true);
                }
            });
            jumpToItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Spatial)
                            {
                                CurrentData.getEditorWindow().getB3DApp().getCamera().setLocation(((Spatial) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getWorldTranslation());
                            } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Light)
                            {
                                /*Only Spot-And PointLight have that Item*/
                                if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof PointLight)
                                {
                                    CurrentData.getEditorWindow().getB3DApp().getCamera().setLocation(((PointLight) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getPosition());
                                } else
                                {
                                    CurrentData.getEditorWindow().getB3DApp().getCamera().setLocation(((SpotLight) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getPosition());
                                }
                            } else if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof MotionEvent)
                            {
                                CurrentData.getEditorWindow().getB3DApp().getCamera().setLocation(((MotionEvent) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getPath().getWayPoint(0));
                            }
                            return null;
                        }
                    });
                }
            });
            deleteItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.execDelete(true);
                }
            });
            playItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.execPlayPauseMotionPath(playItem);
                }
            });
            stopItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ((MotionPathTaskPane) CurrentData.getEditorWindow().getEditPane().getCurrentEditPane()).stop();
                    playItem.setIcon(new ImageIcon("dat//img//menu//play.png"));
                }
            });
            renameItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.execRename();
                }
            });
            filterDownItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    B3D_Element selectedElement = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
                    int oldIndex = ((B3D_Filter) selectedElement).getFilterIndex();
                    int newIndex = ((B3D_Filter) selectedElement).getFilterIndex() + 1;
                    int filterID = CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().get(newIndex).hashCode();
                    UUID b3d_filterUUID = Wizard.getObjectReferences().getUUID(
                            filterID);
                    B3D_Filter lowerFilter = (B3D_Filter) Wizard.getObjects().getB3D_Element(b3d_filterUUID);
                    //Upper one will be moved down -> selected one
                    B3D_Filter upperFilter = ((B3D_Filter) selectedElement);
                    lowerFilter.indexUp();
                    upperFilter.indexDown();
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            CurrentData.getEditorWindow().getB3DApp().sortFilters();
                            return null;
                        }
                    });
                    sync();
                }
            });
            filterUpItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    B3D_Element selectedElement = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
                    int oldIndex = ((B3D_Filter) selectedElement).getFilterIndex();
                    int newIndex = ((B3D_Filter) selectedElement).getFilterIndex() - 1;
                    int filterID = CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().get(newIndex).hashCode();
                    UUID b3d_filterUUID = Wizard.getObjectReferences().getUUID(
                            filterID);
                    B3D_Filter upperFilter = (B3D_Filter) Wizard.getObjects().getB3D_Element(b3d_filterUUID);
                    //Upper one will be moved down -> selected one
                    B3D_Filter lowerFilter = ((B3D_Filter) selectedElement);
                    lowerFilter.indexUp();
                    upperFilter.indexDown();
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            CurrentData.getEditorWindow().getB3DApp().sortFilters();
                            return null;
                        }
                    });
                    sync();
                }
            });
            dissolveItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            //B3D_Side
                            B3D_Node element = (B3D_Node) Wizard.getObjects().getB3D_Element(
                                    Wizard.getObjectReferences().getUUID(
                                    CurrentData.getEditorWindow().getB3DApp().getSelectedNode().hashCode()));
                            for (B3D_Spatial spat : element.getChildren())
                            {
                                spat.setParentUUID(element.getParentUUID());
                                if (!element.getParentUUID().equals(Wizard.NULL_SELECTION))
                                {
                                    B3D_Node parent = (B3D_Node) Wizard.getObjects().getB3D_Element(element.getParentUUID());
                                    parent.getChildren().add(spat);
                                }
                            }
                            element.getChildren().clear();
                            //"Real"_Side
                            Node node = ((Node) CurrentData.getEditorWindow().getB3DApp().getSelectedNode());
                            CurrentData.getEditorWindow().getB3DApp().setSelectedNode(node.getParent());
                            ArrayList<Spatial> spatials = new ArrayList<Spatial>();
                            for (Spatial spatial : node.getChildren())
                            {
                                spatials.add(spatial);
                            }
                            for (Spatial spatial : spatials)
                            {
                                CurrentData.getEditorWindow().getB3DApp().setSelectedNode(node.getParent());
                                node.getParent().attachChild(spatial);
                            }
                            node.removeFromParent();
                            if (node.getControl(RigidBodyControl.class) != null)
                            {
                                CurrentData.getEditorWindow()
                                        .getB3DApp().getBulletAppState().getPhysicsSpace().remove(node);
                            }
                            Wizard.getObjects().remove(node.hashCode());
                            CurrentData.getEditorWindow().getB3DApp().setSelectedElement(Wizard.NULL_SELECTION, null);
                            CurrentData.getEditorWindow().getEditPane().arrange(false);
                            sync();
                            return null;
                        }
                    });
                }
            });
        }

        private void popup(DefaultMutableTreeNode selectedNode, int x, int y)
        {
            B3D_Element selectedElement = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
            removeAll();
            if (nodeIndexes.get(selectedNode) != null)
            {
                if (Wizard.getObjects().getB3D_Element(nodeIndexes.get(selectedNode)) instanceof B3D_Spatial)
                {
                    if (CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof Node && !(CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof TerrainQuad))
                    {
                        add(addMenu.getObject3DMenu());
                        add(dissolveItem);
                        add(numberElementsItem);
                        add(batchNodeItem);
                        add(new JSeparator());
                    }
                    add(twinItem);
                    add(lookAtItem);
                    add(jumpToItem);
                    add(renameItem);
                    add(deleteItem);
                    add(animateItem);
                    add(copyIDItem);
                } else if (selectedNode.getParent().equals(lightsNode))
                {
                    add(twinItem);
                    if (!(CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof AmbientLight)
                            && !(CurrentData.getEditorWindow().getB3DApp().getSelectedObject() instanceof DirectionalLight))
                    {
                        add(lookAtItem);
                        add(jumpToItem);
                    }
                    add(renameItem);
                    add(deleteItem);
                    add(animateItem);
                    add(copyIDItem);
                } else if (selectedNode.getParent().equals(filtersNode))
                {
                    enableItem.setSelected(((Filter) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).isEnabled());
                    add(enableItem);
                    add(renameItem);
                    if (((B3D_Filter) selectedElement).getFilterIndex() > 0)
                    {
                        add(filterUpItem);
                    }
                    int index = ((B3D_Filter) selectedElement).getFilterIndex();
                    int size = CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size();
                    if (index < (size - 1))
                    {
                        add(filterDownItem);
                    }
                    add(deleteItem);
                    add(animateItem);
                    add(copyIDItem);
                } else if (selectedElement instanceof B3D_MotionEvent)
                {
                    int motivionEventID = Wizard.getObjectReferences().getID(selectedElement.getUUID());
                    if (((MotionEvent) Wizard.getObjects().getOriginalObject(motivionEventID)).getPlayState().equals(PlayState.Playing))
                    {
                        playItem.setText("Pause");
                    } else
                    {
                        playItem.setText("Play");
                    }
                    add(playItem);
                    if (!((MotionEvent) Wizard.getObjects().getOriginalObject(motivionEventID)).getPlayState().equals(PlayState.Stopped))
                    {
                        add(stopItem);
                    }
                    add(new JSeparator(JSeparator.HORIZONTAL));
                    add(twinItem);
                    add(lookAtItem);
                    add(jumpToItem);
                    add(renameItem);
                    add(deleteItem);
                    add(animateItem);
                    add(copyIDItem);
                } else
                {
                    add(showAllMotionPathsItem);
                }
            } else
            {
                add(showAllMotionPathsItem);
            }
            show(ElementTree.this, x, y);
        }
    }

    class RenameListener implements TreeModelListener
    {

        @Override
        public void treeNodesChanged(TreeModelEvent e)
        {
            B3D_Element selectedElement = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
            DefaultMutableTreeNode tempNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();
            if (!tempNode.equals(rootNode)
                    && !tempNode.equals(spatialsNode)
                    && !tempNode.equals(lightsNode)
                    && !tempNode.equals(filtersNode)
                    && !tempNode.equals(othersNode))
            {
                try
                {
                    if (tempNode.getUserObject().toString().equals("LightModel") || tempNode.getUserObject().toString().equals("LightSymbol") || tempNode.getUserObject().toString().equals("SkyBox"))
                    {
                        nameChangingLegit = false;
                        tempNode.setUserObject(oldName);
                    } else
                    {
                        selectedElement.setName(tempNode.getUserObject().toString());
                        int objectID = Wizard.getObjectReferences().getID(selectedElement.getUUID());
                        Object actualObject = Wizard.getObjects().getOriginalObject(objectID);
                        if (actualObject instanceof Light)
                        {
                            ((Light) actualObject).setName(tempNode.getUserObject().toString());
                        } else if (actualObject instanceof Filter)
                        {
                            ((Filter) actualObject).setName(tempNode.getUserObject().toString());
                        } else if (actualObject instanceof Spatial)
                        {
                            ((Spatial) actualObject).setName(tempNode.getUserObject().toString());
                        }
                        nameChangingLegit = true;
                        sync();
                    }
                    // CurrentData.getEditorWindow().getB3DApp().setSelectedElement(nodeIDs.get(tempNode));
                } catch (NullPointerException npe)
                {
                    ObserverDialog.getObserverDialog().printError("Error while renaming", npe);
                }
            } else
            {
                rootNode.setUserObject("Scene");
                spatialsNode.setUserObject("Objects");
                lightsNode.setUserObject("Lights");
                filtersNode.setUserObject("Filters");
                othersNode.setUserObject("Other");
            }
            if (!nameChangingLegit)
            {
                JOptionPane.showMessageDialog(null, "Name already taken for In-Editor stuff!", "Nope", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e)
        {
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e)
        {
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e)
        {
        }
    }

    public DefaultMutableTreeNode getSelectedNode()
    {
        return selectedNode;
    }

    public DefaultMutableTreeNode getSpatialsNode()
    {
        return spatialsNode;
    }

    public DefaultMutableTreeNode getLightsNode()
    {
        return lightsNode;
    }

    public DefaultMutableTreeNode getFiltersNode()
    {
        return filtersNode;
    }

    public DefaultMutableTreeNode getOthersNode()
    {
        return othersNode;
    }

    public void setCodeSelect(boolean codeSelect)
    {
        this.codeSelect = codeSelect;
    }

    public HashMap<DefaultMutableTreeNode, UUID> getNodeIds()
    {
        return nodeIndexes;
    }

    public DefaultMutableTreeNode getRootNode()
    {
        return rootNode;
    }
}
