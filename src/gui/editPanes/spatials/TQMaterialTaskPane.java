package gui.editPanes.spatials;

import b3dElements.spatials.geometries.B3D_Geometry;
import b3dElements.other.B3D_MaterialPropertyList;
import b3dElements.spatials.B3D_Terrain;
import gui.components.AssetButton;
import gui.components.BColorButton;
import general.CurrentData;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import components.BComboBox;
import components.BTextField;
import components.Checker;
import components.EditTaskPane;
import components.Float2Panel;
import components.Float3Panel;
import components.Float4Panel;
import dialogs.ObserverDialog;
import general.UAManager;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import se.datadosen.component.RiverLayout;
import javax.swing.JOptionPane;

public class TQMaterialTaskPane extends EditTaskPane
{

    private BComboBox materialComboBox = new BComboBox();
    private HashMap<String, String> properties = new HashMap<String, String>();
    private HashMap<String, String> propsANDValues = new HashMap<String, String>();
    private HashMap<String, Component> propertyControls = new HashMap<String, Component>();
    private Vector<String> matFiles = new Vector<String>();

    /**
     *
     * @param assetName
     */
    public TQMaterialTaskPane(String assetName)
    {
        AutoCompleteDecorator.decorate(materialComboBox);
        taskPane.setLayout(new RiverLayout(5, 5));
        taskPane.setTitle("Material");
        initComboBox(assetName);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateMaterial();
            }
        });
        materialComboBox.getEditor().getEditorComponent().addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                if (e.getKeyChar() == '\n')
                {
                    updateMaterial();
                }
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    arrange(new File("matD//" + matFiles.get(materialComboBox.getSelectedIndex())));
                    materialComboBox.getEditor().selectAll();
                }
            }
        });
        materialComboBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                arrange(new File("matD//" + matFiles.get(materialComboBox.getSelectedIndex())));
                materialComboBox.getEditor().selectAll();
                ((B3D_Geometry) Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID()))
                        .getMaterial().getPropertyList().getProperties().clear();
            }
        });
        add(taskPane, BorderLayout.CENTER);
        taskPane.add(applyButton, BorderLayout.SOUTH);
        arrange(new File("matD//" + assetName));
    }

    public void updateMaterial()
    {
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                TerrainQuad g = (TerrainQuad) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                boolean wired = g.getMaterial().getAdditionalRenderState().isWireframe();
                UUID elementUUID = Wizard.getObjectReferences().getUUID(g.hashCode());
                B3D_Terrain element = (B3D_Terrain) Wizard.getObjects().getB3D_Element(elementUUID);
                B3D_MaterialPropertyList propertyList = element.getMaterial().getPropertyList();
                Material mat = new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), matFiles.get(materialComboBox.getSelectedIndex()));
                if (!g.getMaterial().getMaterialDef().getAssetName().equals(mat.getMaterialDef().getAssetName()))
                {
                    propertyList = new B3D_MaterialPropertyList();
                }
                synchronized (propertyControls)
                {
                    try
                    {
                        for (Map.Entry<String, Component> e : propertyControls.entrySet())
                        {
                            if (e.getValue() instanceof Checker)
                            {
                                Checker checkBox = (Checker) e.getValue();
                                mat.setBoolean(e.getKey(), checkBox.isChecked());
                                propsANDValues.put(e.getKey(), "" + checkBox.isChecked());
                                if (propertyList.has(e.getKey()))
                                {
                                    propertyList.change(e.getKey(), Boolean.toString(checkBox.isChecked()));
                                } else
                                {
                                    propertyList.add(e.getKey(), "Boolean", Boolean.toString(checkBox.isChecked()));
                                }
                            } else if (e.getValue() instanceof BColorButton)
                            {
                                BColorButton colorButton = (BColorButton) e.getValue();
                                if (colorButton.getColor() != null)
                                {
                                    mat.setColor(e.getKey(), Wizard.makeColorRGBA(colorButton.getColor()));
                                    propsANDValues.put(e.getKey(), "" + colorButton.getColor());
                                    if (propertyList.has(e.getKey()))
                                    {
                                        propertyList.change(e.getKey(), colorButton.getColor().getRed() + "," + colorButton.getColor().getGreen() + "," + colorButton.getColor().getBlue() + "," + colorButton.getColor().getAlpha());
                                    } else
                                    {
                                        propertyList.add(e.getKey(), "Color", colorButton.getColor().getRed() + "," + colorButton.getColor().getGreen() + "," + colorButton.getColor().getBlue() + "," + colorButton.getColor().getAlpha());
                                    }
                                    System.out.println("saving as " + colorButton.getColor().getRed() + "," + colorButton.getColor().getGreen() + "," + colorButton.getColor().getBlue() + "," + colorButton.getColor().getAlpha());
                                }
                            } else if (e.getValue() instanceof AssetButton)
                            {
                                AssetButton assetButton = (AssetButton) e.getValue();
                                if (assetButton.getAssetType().equals(AssetButton.AssetType.Texture) && assetButton.getText().contains("."))
                                {
                                    mat.setTexture(e.getKey(), (Texture) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(assetButton.getText()));
                                    propsANDValues.put(e.getKey(), "" + assetButton.getText());
                                    if (propertyList.has(e.getKey()))
                                    {
                                        propertyList.change(e.getKey(), assetButton.getText());
                                    } else
                                    {
                                        propertyList.add(e.getKey(), "Texture", assetButton.getText());
                                    }
                                }
                            } else if (e.getValue() instanceof BTextField)
                            {
                                BTextField bTextField = (BTextField) e.getValue();
                                if (bTextField.getValueType().equals("Float"))
                                {
                                    try
                                    {
                                        mat.setFloat(e.getKey(), Float.parseFloat(bTextField.getText()));
                                        if (propertyList.has(e.getKey()))
                                        {
                                            propertyList.change(e.getKey(), bTextField.getText());
                                        } else
                                        {
                                            propertyList.add(e.getKey(), "Float", bTextField.getText());
                                        }
                                    } catch (NumberFormatException nfe)
                                    {
                                        ObserverDialog.getObserverDialog().printMessage("Fail: NumberFormatException -> Invalid input in FloatTextField (MaterialTaskPane)");
                                    }
                                } else if (bTextField.getValueType().equals("Int"))
                                {
                                    try
                                    {
                                        mat.setInt(e.getKey(), Integer.parseInt(bTextField.getText()));
                                        if (propertyList.has(e.getKey()))
                                        {
                                            propertyList.change(e.getKey(), bTextField.getText());
                                        } else
                                        {
                                            propertyList.add(e.getKey(), "Int", bTextField.getText());
                                        }
                                    } catch (NumberFormatException nfe)
                                    {
                                        ObserverDialog.getObserverDialog().printMessage("Fail: NumberFormatException -> Invalid input in IntTextField (MaterialTaskPane)");
                                    }
                                }
                                propsANDValues.put(e.getKey(), "" + bTextField.getText());
                            } else if (e.getValue() instanceof Float2Panel)
                            {
                                Float2Panel float2Panel = (Float2Panel) e.getValue();
                                mat.setVector2(e.getKey(), float2Panel.getVector());
                                propsANDValues.put(e.getKey(), "" + float2Panel.getVector());
                                if (propertyList.has(e.getKey()))
                                {
                                    propertyList.change(e.getKey(), float2Panel.getVector().toString());
                                } else
                                {
                                    propertyList.add(e.getKey(), "Vector2", float2Panel.getVector().toString());
                                }
                            } else if (e.getValue() instanceof Float3Panel)
                            {
                                Float3Panel float3Panel = (Float3Panel) e.getValue();
                                mat.setVector3(e.getKey(), float3Panel.getVector());
                                propsANDValues.put(e.getKey(), "" + float3Panel.getVector());
                                if (propertyList.has(e.getKey()))
                                {
                                    propertyList.change(e.getKey(), float3Panel.getVector().toString());
                                } else
                                {
                                    propertyList.add(e.getKey(), "Vector3", float3Panel.getVector().toString());
                                }
                            } else if (e.getValue() instanceof Float4Panel)
                            {
                                Float4Panel float4Panel = (Float4Panel) e.getValue();
                                mat.setVector4(e.getKey(), float4Panel.getVector());
                                propsANDValues.put(e.getKey(), "" + float4Panel.getVector());
                                if (propertyList.has(e.getKey()))
                                {
                                    propertyList.change(e.getKey(), float4Panel.getVector().toString());
                                } else
                                {
                                    propertyList.add(e.getKey(), "Vector2", float4Panel.getVector().toString());
                                }
                            }
                        }
                    } catch (Exception whatever)
                    {
                        ObserverDialog.getObserverDialog().printError("Error updating Material / PropertyList", whatever);
                    }
                    mat.getAdditionalRenderState().setWireframe(wired);
                    g.setMaterial(mat);
                    UAManager.add(g, "Edit Material of " + g.getName());
                    return null;
                }
            }
        });
    }

    public void arrange(File materialFile)
    {
        try
        {
            try
            {
                TerrainQuad g = (TerrainQuad) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                if (g.getUserData("north") == null)
                {
                    UUID elementUUID = Wizard.getObjectReferences().getUUID(g.hashCode());
                    B3D_Terrain element = (B3D_Terrain) Wizard.getObjects().getB3D_Element(elementUUID);
                    B3D_MaterialPropertyList propertyList = element.getMaterial().getPropertyList();
                    propertyControls.clear();
                    taskPane.removeAll();
                    taskPane.add("br left", new JLabel("Type:"));
                    taskPane.add("tab hfill", materialComboBox);
                    ObserverDialog.getObserverDialog().printMessage(materialFile.getAbsolutePath());
                    properties = Wizard.readMat(materialFile);
                    List<String> tempList = new ArrayList<String>(properties.keySet());
                    Collections.sort(tempList);
                    for (String key : tempList)
                    {
                        if (properties.get(key).length() != 0 && !properties.get(key).equals("TextureCubeMap") && !properties.get(key).contains("Matrix"))
                        {
                            // key = name
                            Component newComponent = null;
                            if (properties.get(key).equals("Boolean"))
                            {
                                if (propertyList.has(key))
                                {
                                    newComponent = new Checker();
                                    ((Checker) newComponent).setChecked(Boolean.parseBoolean(propertyList.getProperty(key).getPropertyValue()));
                                } else
                                {
                                    newComponent = new Checker();
                                }
                            } else if (properties.get(key).equals("Float"))
                            {
                                if (propertyList.has(key))
                                {
                                    newComponent = new BTextField("Float", propertyList.getProperty(key).getPropertyValue());
                                } else
                                {
                                    newComponent = new BTextField("Float");
                                }
                            } else if (properties.get(key).equals("Int"))
                            {
                                if (propertyList.has(key))
                                {
                                    newComponent = new BTextField("Int", propertyList.getProperty(key).getPropertyValue());
                                } else
                                {
                                    newComponent = new BTextField("Int");
                                }
                            } else if (properties.get(key).equals("Color"))
                            {
                                if (propertyList.has(key))
                                {
                                    System.out.println("Reading color: " + propertyList.getProperty(key).getPropertyValue());
                                    StringTokenizer tokenizer;
                                    try
                                    {//Try to read as ints
                                        tokenizer = new StringTokenizer(propertyList.getProperty(key).getPropertyValue().replaceAll(" ", ""), ",");
                                        int r = Integer.parseInt(tokenizer.nextToken());
                                        int gr = Integer.parseInt(tokenizer.nextToken());
                                        int b = Integer.parseInt(tokenizer.nextToken());
                                        int a = Integer.parseInt(tokenizer.nextToken());
                                        System.out.println("Ints Converted to: \nr: " + r + "\ng: " + gr + "\nb: " + b + "\na: " + a);
                                        newComponent = new BColorButton(new Color(r, gr, b, a));
                                    } catch (NumberFormatException nfe)
                                    {//read from floats
                                        tokenizer = new StringTokenizer(propertyList.getProperty(key).getPropertyValue().replaceAll(" ", ""), ",");
                                        int r = (int) (Float.parseFloat(tokenizer.nextToken()) * 255);
                                        int gr = (int) (Float.parseFloat(tokenizer.nextToken()) * 255);
                                        int b = (int) (Float.parseFloat(tokenizer.nextToken()) * 255);
                                        if (tokenizer.hasMoreElements())
                                        {
                                            int a = (int) (Float.parseFloat(tokenizer.nextToken()) * 255);
                                            System.out.println("Floats Converted to: \nr: " + r + "\ng: " + gr + "\nb: " + b + "\na: " + a);
                                            newComponent = new BColorButton(new Color(r, gr, b, a));
                                        } else
                                        {
                                            System.out.println("Floats Converted to: \nr: " + r + "\ng: " + gr + "\nb: " + b);
                                            newComponent = new BColorButton(new Color(r, gr, b));
                                        }
                                    }
                                } else
                                {
                                    newComponent = new BColorButton(null);
                                }
                            } else if (properties.get(key).equals("Texture"))
                            {
                                if (propertyList.has(key))
                                {
                                    newComponent = new AssetButton(AssetButton.AssetType.Texture, propertyList.getProperty(key).getPropertyValue());
                                } else
                                {
                                    newComponent = new AssetButton(AssetButton.AssetType.Texture);
                                }
                            } else if (properties.get(key).equals("Texture2D"))
                            {
                                if (propertyList.has(key))
                                {
                                    newComponent = new AssetButton(AssetButton.AssetType.Texture, propertyList.getProperty(key).getPropertyValue());
                                } else
                                {
                                    newComponent = new AssetButton(AssetButton.AssetType.Texture);
                                }
                            } else if (properties.get(key).equals("Vector2"))
                            {
                                if (propertyList.has(key))
                                {
                                    String value1 = null, value2 = null;
                                    try
                                    {
                                        StringTokenizer tokenizer = new StringTokenizer(propertyList.getProperty(key).getPropertyValue().substring(1, propertyList.getProperty(key).getPropertyValue().length() - 1));
                                        value1 = tokenizer.nextToken(",");
                                        value2 = tokenizer.nextToken(",");
                                        newComponent = new Float2Panel(new Vector2f(Float.parseFloat(value1), Float.parseFloat(value2)));
                                    } catch (NumberFormatException nfe)
                                    {
                                        JOptionPane.showMessageDialog(null, "Exception:\n" + nfe + "\nValue1: " + value1 + "\nValue2: " + value2);
                                        ObserverDialog.getObserverDialog().printMessage("Fail: NumberFormatException -> Trying to convert String to Vector2");
                                    }
                                } else
                                {
                                    newComponent = new Float2Panel(null);
                                }
                            } else if (properties.get(key).equals("Vector3"))
                            {
                                if (propertyList.has(key))
                                {
                                    String value1 = null, value2 = null, value3 = null;
                                    try
                                    {
                                        StringTokenizer tokenizer = new StringTokenizer(propertyList.getProperty(key).getPropertyValue().substring(1, propertyList.getProperty(key).getPropertyValue().length() - 1));
                                        value1 = tokenizer.nextToken(",");
                                        value2 = tokenizer.nextToken(",");
                                        value3 = tokenizer.nextToken(",");
                                        newComponent = new Float3Panel(new Vector3f(Float.parseFloat(value1), Float.parseFloat(value2), Float.parseFloat(value3)), Wizard.getCamera(), Float3Panel.HORIZONTAL);
                                    } catch (NumberFormatException nfe)
                                    {
                                        JOptionPane.showMessageDialog(null, "Exception:\n" + nfe + "\nValue1: " + value1 + "\nValue2: " + value2 + "\nValue3: " + value3);
                                        ObserverDialog.getObserverDialog().printMessage("Fail: NumberFormatException -> Trying to convert String to Vector3");
                                    }
                                } else
                                {
                                    newComponent = new Float3Panel(null, Wizard.getCamera(), Float3Panel.HORIZONTAL);
                                }
                            } else if (properties.get(key).equals("Vector4"))
                            {
                                if (propertyList.has(key))
                                {
                                    StringTokenizer tokenizer = new StringTokenizer(propertyList.getProperty(key).getPropertyValue().substring(1, propertyList.getProperty(key).getPropertyValue().length() - 1));
                                    newComponent = new Float4Panel(new Vector4f(Float.parseFloat(tokenizer.nextToken(",")), Float.parseFloat(tokenizer.nextToken(",")), Float.parseFloat(tokenizer.nextToken(",")), Float.parseFloat(tokenizer.nextToken(","))));
                                } else
                                {
                                    newComponent = new Float4Panel(null);
                                }
                            }
                            if (newComponent != null)
                            {
                                taskPane.add("br hfill", new JLabel(key));
                                if (newComponent instanceof Checker)
                                    taskPane.add("tab", newComponent);
                                else
                                {
                                    newComponent.setPreferredSize(new Dimension(newComponent.getPreferredSize().width, 25));
                                    taskPane.add("tab hfill", newComponent);
                                }
                                propertyControls.put(key, newComponent);
                            }
                        }
                    }
                    taskPane.add("br right", applyButton);
                }
            } catch (NullPointerException npe)
            {
                ObserverDialog.getObserverDialog().printError("Nullpointer because of File: " + materialFile.getAbsolutePath() + " in MaterialTaskPane", npe);
                throw npe;
            }
        } catch (ClassCastException cce)
        {
            ObserverDialog.getObserverDialog().printMessage("Weird Error in MaterialTaskPane when arranging...");
        }
    }

    private void initComboBox(String assetName)
    {
        ArrayList<String> materials = (ArrayList<String>) CurrentData.getAllMaterialFiles();
        int selectedIndex = 0;
        Collections.sort(materials, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });
        Collections.sort(materials, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });
        for (String e : materials)
        {
            ObserverDialog.getObserverDialog().printMessage("Adding to matFiles: " + e);
            matFiles.add(e);
            Collections.sort(matFiles, new Comparator<String>()
            {
                @Override
                public int compare(String o1, String o2)
                {
                    return configureName(o1).compareTo(configureName(o2));
                }
            });
            if (e.equals(assetName))
            {
                selectedIndex = materialComboBox.getItemCount();
            }
            e = configureName(e);
            materialComboBox.addItem(e);
        }
        materialComboBox.setSelectedIndex(selectedIndex);
        materialComboBox.sort();
    }

    private String configureName(String e)
    {
        e = e.replaceAll(".j3md", "");
        e = e.substring(2);
        while (e.contains("/"))
        {
            e = e.substring(e.indexOf("/") + 1);
        }
        return e;
    }
}