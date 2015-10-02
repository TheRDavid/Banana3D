package gui.dialogs;

import files.SkyFile;
import gui.components.AssetButton;
import general.CurrentData;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import components.BButton;
import components.CancelButton;
import components.OKButton;
import dialogs.BasicDialog;
import dialogs.ObserverDialog;
import java.awt.BasicStroke;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jdesktop.swingx.VerticalLayout;
import other.ObjectToElementConverter;
import se.datadosen.component.RiverLayout;

public class CreateSkyBoxDialog extends BasicDialog
{

    private JTabbedPane tabbedPane = new JTabbedPane();

    public CreateSkyBoxDialog()
    {
        setTitle("Create Sky Box");
        add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.addTab("DDS / Singe Texture", new SingleFilePanel());
        tabbedPane.addTab("Multiple Textures", new MultipleTexturesPanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class SingleFilePanel extends JPanel
    {

        private AssetChooserDialog acd;
        private JLabel previewLabel = new JLabel()
        {
            @Override
            public void paint(Graphics g)
            {
                if (acd != null && acd.getSelectedAssetName() != null)
                {
                    if (acd.getSelectedAssetName().toLowerCase().endsWith(".dds"))
                    {
                        String displayString = "SkyFile: " + acd.getSelectedAssetName();
                        g.drawString(displayString, previewLabel.getWidth() / 2 - g.getFontMetrics().stringWidth(displayString) / 2, 100);
                        createButton.setEnabled(true);
                    } else
                    {
                        ImageIcon icon = new ImageIcon(CurrentData.getProject().getAssetsFolder().getAbsolutePath() + "\\" + acd.getSelectedAssetName());
                        Image img = Wizard.resizeImageI(icon.getImage(), 500, 500);
                        g.drawImage(img, 0, 0, null);
                        createButton.setEnabled(true);
                    }
                }
            }
        };
        private BButton browseButton = new BButton("Load...", new ImageIcon("dat//img//menu//open.png"));
        private CancelButton cancelButton;
        private OKButton createButton = new OKButton("Create");
        private JPanel buttonPanel = new JPanel(new VerticalLayout(0));

        public SingleFilePanel()
        {
            createButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CreateSkyBoxDialog.this.dispose();
                    final Spatial skyBoxSpatial = SkyFactory.createSky(
                            CurrentData.getEditorWindow().getB3DApp().getAssetManager(),
                            CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(acd.getSelectedAssetName()),
                            false);
                    skyBoxSpatial.setUserData("north", acd.getSelectedAssetName());
                    skyBoxSpatial.setUserData("angles", new Vector3f());
                    skyBoxSpatial.setUserData("scale", new Vector3f(1, 1, 1));
                    skyBoxSpatial.setName("SkyBox");
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            //ConvertMode does not matter here, the skybox can not possibly have a LightControl
                            b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(skyBoxSpatial);
                            Wizard.getObjects().add(skyBoxSpatial, element);
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(skyBoxSpatial);
                            return null;
                        }
                    });
                }
            });
            createButton.setEnabled(false);
            setLayout(new RiverLayout(0, 0));
            cancelButton = new CancelButton(CreateSkyBoxDialog.this);
            previewLabel.setPreferredSize(new Dimension(500, 500));
            browseButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    acd = new AssetChooserDialog(AssetButton.AssetType.Texture_DDS, true);
                    CreateSkyBoxDialog.this.repaint();
                }
            });
            buttonPanel.add(createButton);
            buttonPanel.add(browseButton);
            buttonPanel.add(cancelButton);
            add("left", buttonPanel);
            add("tab hfill", previewLabel);
        }
    }

    private class MultipleTexturesPanel extends JPanel
    {

        private OKButton okButton = new OKButton("Create");
        private BButton okAndSaveButton = new BButton("Create and save");
        private BButton loadButton = new BButton("Load...", new ImageIcon("dat//img//menu//open.png"));
        private CancelButton cancelButton;
        private JPanel buttonPanel = new JPanel(new VerticalLayout());
        private SelectImageLabel northLabel = new SelectImageLabel("North");
        private SelectImageLabel southLabel = new SelectImageLabel("South");
        private SelectImageLabel westLabel = new SelectImageLabel("West");
        private SelectImageLabel eastLabel = new SelectImageLabel("East");
        private SelectImageLabel topLabel = new SelectImageLabel("Top");
        private SelectImageLabel bottomLabel = new SelectImageLabel("Bottom");

        public MultipleTexturesPanel()
        {
            cancelButton = new CancelButton(CreateSkyBoxDialog.this);
            buttonPanel.add(loadButton);
            buttonPanel.add(okButton);
            buttonPanel.add(okAndSaveButton);
            buttonPanel.add(cancelButton);
            okAndSaveButton.setEnabled(false);
            okButton.setEnabled(false);
            loadButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    AssetChooserDialog acs = new AssetChooserDialog(AssetButton.AssetType.Sky, true);
                    if (acs.getSelectedAssetName() != null)
                    {
                        northLabel.setAcs(new AssetChooserDialog(AssetButton.AssetType.Texture, false));
                        southLabel.setAcs(new AssetChooserDialog(AssetButton.AssetType.Texture, false));
                        westLabel.setAcs(new AssetChooserDialog(AssetButton.AssetType.Texture, false));
                        eastLabel.setAcs(new AssetChooserDialog(AssetButton.AssetType.Texture, false));
                        topLabel.setAcs(new AssetChooserDialog(AssetButton.AssetType.Texture, false));
                        bottomLabel.setAcs(new AssetChooserDialog(AssetButton.AssetType.Texture, false));
                        SkyFile loadedFile = (SkyFile) Wizard.loadFile(CurrentData.getProject().getAssetsFolder() + "//" + acs.getSelectedAssetName());
                        northLabel.getChooser().setSelectedAssetName(loadedFile.getNorthAsset());
                        southLabel.getChooser().setSelectedAssetName(loadedFile.getSouthAsset());
                        westLabel.getChooser().setSelectedAssetName(loadedFile.getWestAsset());
                        eastLabel.getChooser().setSelectedAssetName(loadedFile.getEastAsset());
                        topLabel.getChooser().setSelectedAssetName(loadedFile.getTopAsset());
                        bottomLabel.getChooser().setSelectedAssetName(loadedFile.getBottomAsset());
                        checkIfDoneDeal();
                    }
                    repaint();
                }
            });
            okAndSaveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    create();
                    String name = JOptionPane.showInputDialog("Sky Name:");
                    Wizard.saveFile(CurrentData.getProject().getAssetsFolder() + "\\" + name + ".sky", new SkyFile(
                            northLabel.getChooser().getSelectedAssetName(),
                            southLabel.getChooser().getSelectedAssetName(),
                            westLabel.getChooser().getSelectedAssetName(),
                            eastLabel.getChooser().getSelectedAssetName(),
                            topLabel.getChooser().getSelectedAssetName(),
                            bottomLabel.getChooser().getSelectedAssetName()));
                }
            });
            okButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    create();
                }
            });
            setLayout(new RiverLayout(0, 0));
            add("left", buttonPanel);
            add("tab", topLabel);
            add("br left", westLabel);
            add("tab", northLabel);
            add("tab", eastLabel);
            add("tab", southLabel);
            add("br left tab", bottomLabel);
        }

        private class SelectImageLabel extends JLabel
        {

            AssetChooserDialog acs;
            private Dimension iconDimension = new Dimension(150, 150);

            public SelectImageLabel(String text)
            {
                setPreferredSize(iconDimension);
                setHorizontalTextPosition(JLabel.CENTER);
                setText(text);
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
                        acs = new AssetChooserDialog(AssetButton.AssetType.Texture, true);
                        checkIfDoneDeal();
                        CreateSkyBoxDialog.this.repaint();
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
            }

            public AssetChooserDialog getChooser()
            {
                return acs;
            }

            public void setAcs(AssetChooserDialog acs)
            {
                this.acs = acs;
            }

            @Override
            public void paint(Graphics g)
            {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (acs != null && acs.getSelectedAssetName() != null)
                {
                    ImageIcon icon = new ImageIcon(acs.getSelectedFile().getAbsolutePath());
                    Image img = icon.getImage();
                    if (icon.getIconWidth() != icon.getIconHeight())
                    {
                        acs.setSelectedAssetName(null);
                        setText("Must be quadratic!");
                    } else
                    {
                        img = Wizard.resizeImageI(img, (int) iconDimension.getWidth(), (int) iconDimension.getHeight());
                        g2d.drawImage(img, 0, 0, null);
                    }
                } else
                {
                    g2d.setColor(Color.cyan);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 12, 12);
                }
                g2d.drawString(getText(), getWidth() / 2 - getText().length() * 2, getHeight() / 2 - 2);
                g2d.drawString("[Click here]", getWidth() / 2 - "[Click here]".length() * 2, getHeight() / 2 + 14);
            }
        }

        private void checkIfDoneDeal()
        {
            if (northLabel.getChooser() != null
                    && southLabel.getChooser() != null
                    && westLabel.getChooser() != null
                    && eastLabel.getChooser() != null
                    && topLabel.getChooser() != null
                    && bottomLabel.getChooser() != null)
            {
                okButton.setEnabled((northLabel.getChooser().getSelectedAssetName() != null
                        && southLabel.getChooser().getSelectedAssetName() != null
                        && westLabel.getChooser().getSelectedAssetName() != null
                        && eastLabel.getChooser().getSelectedAssetName() != null
                        && topLabel.getChooser().getSelectedAssetName() != null
                        && bottomLabel.getChooser().getSelectedAssetName() != null));
                okAndSaveButton.setEnabled((northLabel.getChooser().getSelectedAssetName() != null
                        && southLabel.getChooser().getSelectedAssetName() != null
                        && westLabel.getChooser().getSelectedAssetName() != null
                        && eastLabel.getChooser().getSelectedAssetName() != null
                        && topLabel.getChooser().getSelectedAssetName() != null
                        && bottomLabel.getChooser().getSelectedAssetName() != null));
            }
        }

        private void create()
        {
            CreateSkyBoxDialog.this.dispose();
            try
            {
                final Spatial skyBoxSpatial = SkyFactory.createSky(CurrentData.getEditorWindow().getB3DApp().getAssetManager(),
                        CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(westLabel.getChooser().getSelectedAssetName()),
                        CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(eastLabel.getChooser().getSelectedAssetName()),
                        CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(northLabel.getChooser().getSelectedAssetName()),
                        CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(southLabel.getChooser().getSelectedAssetName()),
                        CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(topLabel.getChooser().getSelectedAssetName()),
                        CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(bottomLabel.getChooser().getSelectedAssetName()));
                skyBoxSpatial.setUserData("north", northLabel.getChooser().getSelectedAssetName());
                skyBoxSpatial.setUserData("south", southLabel.getChooser().getSelectedAssetName());
                skyBoxSpatial.setUserData("west", westLabel.getChooser().getSelectedAssetName());
                skyBoxSpatial.setUserData("east", eastLabel.getChooser().getSelectedAssetName());
                skyBoxSpatial.setUserData("top", topLabel.getChooser().getSelectedAssetName());
                skyBoxSpatial.setUserData("bottom", bottomLabel.getChooser().getSelectedAssetName());
                skyBoxSpatial.setUserData("angles", new Vector3f());
                skyBoxSpatial.setUserData("scale", new Vector3f(1, 1, 1));
                skyBoxSpatial.setName("SkyBox");
                //ConvertMode does not matter here, the skybox can not possibly have a LightControl
                b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(skyBoxSpatial);
                Wizard.getObjects().add(skyBoxSpatial, element);
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        //Just use hashcode skyBoxSpatial.setUserData("ID", element.getUUID());
                        CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(skyBoxSpatial);
                        return null;
                    }
                });
            } catch (IllegalArgumentException iae)
            {
                JOptionPane.showMessageDialog(this, "Images must have the same format!\n" + iae, "Could not create SkyBox", JOptionPane.ERROR_MESSAGE);
                ObserverDialog.getObserverDialog().printMessage("Fail: Images must have the same format!\n" + iae);
            }
        }
    }
}
