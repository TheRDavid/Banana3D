package gui.editPanes.spatials;

import gui.components.AssetButton;
import gui.dialogs.AssetChooserDialog;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import dialogs.ObserverDialog;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
        northButton = new SelectButton(0);
        southButton = new SelectButton(1);
        westButton = new SelectButton(2);
        eastButton = new SelectButton(3);
        topButton = new SelectButton(4);
        bottomButton = new SelectButton(5);
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

        public SelectButton(int nmbr)
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
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    setText(new AssetChooserDialog(AssetType.Texture, true).getSelectedAssetName());
                    final Spatial skyObject = skyBox;
                    UUID elementUUID = Wizard.getObjectReferences().getUUID(skyObject.hashCode());
                    final b3dElements.B3D_Element skyElement = Wizard.getObjects().getB3D_Element(elementUUID);
                    CurrentData.execDelete();
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            try
                            {
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
                                CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(skyBox);
                                //Just use hashcode skyBox.setUserData("index", skyElement.getUUID());
                                CurrentData.getEditorWindow().getTree().sync();
                                CurrentData.getEditorWindow().getB3DApp().setSelectedElement(skyBox);
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
                                Wizard.getObjects().add(skyObject, skyElement);
                                CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(skyObject);
                                CurrentData.getEditorWindow().getB3DApp().setSelectedElement(skyBox);
                            }
                            return 0;
                        }
                    });
                }
            });
        }
    }
}
