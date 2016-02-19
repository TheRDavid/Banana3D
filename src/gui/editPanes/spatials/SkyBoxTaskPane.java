package gui.editPanes.spatials;

import b3dElements.spatials.geometries.B3D_MultipleTextureSkyBox;
import gui.components.AssetButton;
import gui.dialogs.AssetChooserDialog;
import general.CurrentData;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import components.EditTaskPane;
import dialogs.ObserverDialog;
import general.UAManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import other.ObjectToElementConverter;
import other.Wizard;
import se.datadosen.component.RiverLayout;

/**
 *
 * @author David
 */
public class SkyBoxTaskPane extends EditTaskPane
{

    private Spatial skyBox;
    private SelectButton northButton, southButton, westButton, eastButton, topButton, bottomButton;

    public SkyBoxTaskPane(Spatial sky)
    {
        skyBox = sky;
        northButton = new SelectButton("Loading...", 0);
        southButton = new SelectButton("Loading...", 1);
        westButton = new SelectButton("Loading...", 2);
        eastButton = new SelectButton("Loading...", 3);
        topButton = new SelectButton("Loading...", 4);
        bottomButton = new SelectButton("Loading...", 5);
        taskPane.setLayout(new RiverLayout());
        taskPane.setTitle("Sky Box");
        taskPane.add("br", new JLabel("North Texture: "));
        taskPane.add("tab hfill", northButton);
        taskPane.add("br", new JLabel("South Texture: "));
        taskPane.add("tab hfill", southButton);
        taskPane.add("br", new JLabel("West Texture: "));
        taskPane.add("tab hfill", westButton);
        taskPane.add("br", new JLabel("East Texture: "));
        taskPane.add("tab hfill", eastButton);
        taskPane.add("br", new JLabel("Top Texture: "));
        taskPane.add("tab hfill", topButton);
        taskPane.add("br", new JLabel("Bottom Texture: "));
        taskPane.add("tab hfill", bottomButton);
        add(taskPane, BorderLayout.CENTER);
    }

    private class SelectButton extends AssetButton
    {

        public SelectButton(String text, final int nmbr)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    setAssetType(AssetType.Texture);
                    switch (nmbr)
                    {
                        case 0:
                            getAssetChooser().setSelectedAssetName((String) skyBox.getUserData("north"));
                            setText((String) skyBox.getUserData("north"));
                            break;
                        case 1:
                            getAssetChooser().setSelectedAssetName((String) skyBox.getUserData("south"));
                            setText((String) skyBox.getUserData("south"));
                            break;
                        case 2:
                            getAssetChooser().setSelectedAssetName((String) skyBox.getUserData("west"));
                            setText((String) skyBox.getUserData("west"));
                            break;
                        case 3:
                            getAssetChooser().setSelectedAssetName((String) skyBox.getUserData("east"));
                            setText((String) skyBox.getUserData("east"));
                            break;
                        case 4:
                            getAssetChooser().setSelectedAssetName((String) skyBox.getUserData("top"));
                            setText((String) skyBox.getUserData("top"));
                            break;
                        case 5:
                            getAssetChooser().setSelectedAssetName((String) skyBox.getUserData("bottom"));
                            setText((String) skyBox.getUserData("bottom"));
                    }
                }
            }).start();
            setText(text);
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    setText(new AssetChooserDialog(AssetType.Texture, true).getSelectedAssetName());
                    final UUID oldUUID = Wizard.getObjectReferences().getUUID(skyBox.hashCode());
                    CurrentData.execDelete(false);
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            try
                            {
                                CurrentData.getEditorWindow().getB3DApp().setSelectedElement(null);
                                String name = skyBox.getName();
                                skyBox = SkyFactory.createSky(
                                        Wizard.getAssetManager(),
                                        Wizard.getAssetManager().loadTexture(northButton.getText()),
                                        Wizard.getAssetManager().loadTexture(southButton.getText()),
                                        Wizard.getAssetManager().loadTexture(westButton.getText()),
                                        Wizard.getAssetManager().loadTexture(eastButton.getText()),
                                        Wizard.getAssetManager().loadTexture(topButton.getText()),
                                        Wizard.getAssetManager().loadTexture(bottomButton.getText()));
                                skyBox.setUserData("north", northButton.getText());
                                skyBox.setUserData("south", southButton.getText());
                                skyBox.setUserData("west", westButton.getText());
                                skyBox.setUserData("east", eastButton.getText());
                                skyBox.setUserData("top", topButton.getText());
                                skyBox.setUserData("bottom", bottomButton.getText());
                                skyBox.setUserData("angles", new Vector3f());
                                skyBox.setUserData("scale", new Vector3f(1, 1, 1));
                                skyBox.setName(name);
                                B3D_MultipleTextureSkyBox skyElement = (B3D_MultipleTextureSkyBox) ObjectToElementConverter.convertToElement(skyBox);
                                skyElement.setUuid(oldUUID);
                                Wizard.getObjects().add(skyBox, skyElement);
                                CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(skyBox);
                                CurrentData.getEditorWindow().getB3DApp().setSelectedElement(skyBox);
                                //Just use hashcode skyBox.setUserData("index", skyElement.getUUID());
                                //CurrentData.getEditorWindow().getTree().sync();
                                UAManager.add(skyBox, "Edit Texture of " + skyBox.getName());
                                CurrentData.getEditorWindow().getEditPane().arrange(true);
                            } catch (java.lang.IllegalArgumentException iae)
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        JOptionPane.showMessageDialog(SkyBoxTaskPane.this, "Invalid Texture!", "Now that didn't work...", JOptionPane.ERROR_MESSAGE);
                                        ObserverDialog.getObserverDialog().printMessage("Invalid Texture in SkyBoxTaskPane");
                                    }
                                });
                            }
                            return 0;
                        }
                    });
                }
            });
        }
    }
}
