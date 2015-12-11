package gui.dialogs;

import gui.components.AssetButton;
import general.CurrentData;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import components.BButton;
import components.CancelButton;
import dialogs.BasicDialog;
import general.UAManager;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import monkeyStuff.CustomParticleEmitter;
import other.ObjectToElementConverter;
import se.datadosen.component.RiverLayout;

public class CreateParticleEmitterDialog extends BasicDialog
{

    private CancelButton cancelButton;
    private BButton okButton = new BButton("Create");
    private JLabel texturePreviewLabel = new JLabel();
    private ButtonGroup buttonGroup = new ButtonGroup();
    private JRadioButton pointType = new JRadioButton("Point", true);
    private JRadioButton triangleType = new JRadioButton("Triangle", false);
    private BButton selectTextureButton = new BButton("Select Texture");
    private JPanel texturePanel = new JPanel(new RiverLayout(0, 5));
    private JPanel controlsPanel = new JPanel(new GridLayout(0, 1));
    private AssetChooserDialog asc;
    private CustomParticleEmitter emitter;

    public CreateParticleEmitterDialog()
    {
        buttonGroup.add(pointType);
        buttonGroup.add(triangleType);
        cancelButton = new CancelButton(this);
        texturePreviewLabel.setPreferredSize(new Dimension(100, 100));
        okButton.setEnabled(false);
        selectTextureButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                asc = new AssetChooserDialog(AssetButton.AssetType.Texture, true);
                if (asc.getSelectedAssetName() != null)
                {
                    add(texturePanel, BorderLayout.CENTER);
                    ImageIcon notResizedIcon = new ImageIcon(asc.getSelectedFile().getAbsolutePath());
                    texturePreviewLabel.setIcon(Wizard.resizeImage(notResizedIcon.getImage(), 100, 100, false));
                    okButton.setEnabled(true);
                    pack();
                }
            }
        });
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UAManager.curr(null, null);
                dispose();
                if (pointType.isSelected())
                    emitter = new CustomParticleEmitter("Particle Emitter", ParticleMesh.Type.Point, 1500);
                else
                    emitter = new CustomParticleEmitter("Particle Emitter", ParticleMesh.Type.Triangle, 1500);
                Material particleMat = new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(),
                        "Common/MatDefs/Misc/Particle.j3md");
                particleMat.setTexture("Texture", CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(
                        asc.getSelectedAssetName()));
                emitter.setMaterial(particleMat);
                emitter.getMaterial().getAdditionalRenderState().setDepthWrite(true);
                emitter.setUserData("angles", new Vector3f());
                emitter.setUserData("scale", new Vector3f(1, 1, 1));
                if (CurrentData.getEditorWindow().getB3DApp().getSelectedNode().equals(CurrentData.getEditorWindow().getB3DApp().getSceneNode()))
                    emitter.setLocalTranslation(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(10)));
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        //ConvertMode does not matter here, the emitter can not possibly have a LightControl
                        b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(emitter);
                        Wizard.getObjects().add(emitter, element);
                        // Just use hashcode  emitter.setUserData("ID", element.getUUID());
                        CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(emitter);
                        UAManager.add(emitter, "Add Particle Effect");
                        return null;
                    }
                });
            }
        });
        texturePanel.add(texturePreviewLabel);
        controlsPanel.add(selectTextureButton);
        controlsPanel.add(pointType);
        controlsPanel.add(triangleType);
        controlsPanel.add(okButton);
        controlsPanel.add(cancelButton);
        add(controlsPanel, BorderLayout.EAST);
        setTitle("New Effect");
        pack();
        setSize(140, getHeight());
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
