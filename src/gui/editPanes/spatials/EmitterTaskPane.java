package gui.editPanes.spatials;

import gui.components.BColorButton;
import general.CurrentData;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterBoxShape;
import com.jme3.effect.shapes.EmitterPointShape;
import com.jme3.effect.shapes.EmitterShape;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.math.Vector3f;
import components.BSlider;
import components.BTextField;
import components.Checker;
import components.EditTaskPane;
import components.Float3Panel;
import general.UAManager;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTaskPane;
import monkeyStuff.CustomParticleEmitter;
import se.datadosen.component.RiverLayout;

public class EmitterTaskPane extends EditTaskPane
{

    private ButtonGroup typeGroup = new ButtonGroup();
    private JRadioButton pointTypeRadioButton = new JRadioButton("Point", false);
    private JRadioButton triangleTypeRadioButton = new JRadioButton("Triangle", true);
    private BTextField maxParticlesField = new BTextField("Integer");
    private BTextField particlesPerSecondField = new BTextField("float");
    private BTextField startSizeField = new BTextField("float");
    private BTextField endSizeField = new BTextField("float");
    private BTextField highLifeField = new BTextField("float");
    private BTextField lowLifeField = new BTextField("float");
    private BSlider velocityVariationSlider = new BSlider(Float.class, 0, 1, .25f);
    private Float3Panel gravityPanel = new Float3Panel(Vector3f.ZERO, Wizard.getCamera(), Float3Panel.HORIZONTAL);
    private Float3Panel directionVelocityPanel = new Float3Panel(Vector3f.ZERO, Wizard.getCamera(), Float3Panel.HORIZONTAL);
    private BColorButton startColorButton = new BColorButton(Color.white);
    private BColorButton endColorButton = new BColorButton(Color.white);
    private BTextField rotateSpeedField = new BTextField("float");
    private BTextField imgXField = new BTextField("Integer");
    private BTextField imgYField = new BTextField("Integer");
    private Checker firingChecker = new Checker();
    private Checker frozenChecker = new Checker();
    private Checker depthWriteChecker = new Checker();
    private Checker faceVelocityChecker = new Checker();
    private Float3Panel faceNormalPanel = new Float3Panel(null, Wizard.getCamera(), Float3Panel.HORIZONTAL);
    private StartShapeTaskPane startShapeTaskPane;

    /**
     *
     * @param particleEmitter
     */
    public EmitterTaskPane(final CustomParticleEmitter particleEmitter)
    {
        startShapeTaskPane = new StartShapeTaskPane(particleEmitter.getShape());
        depthWriteChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        particleEmitter.getMaterial().getAdditionalRenderState().setDepthWrite(depthWriteChecker.isChecked());
                        UAManager.add(particleEmitter, "Use Depth Write " + depthWriteChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        depthWriteChecker.setChecked(particleEmitter.getMaterial().getAdditionalRenderState().isDepthWrite());
        faceNormalPanel.setVector(particleEmitter.getFaceNormal());
        faceVelocityChecker.setChecked(particleEmitter.isFacingVelocity());
        typeGroup.add(triangleTypeRadioButton);
        typeGroup.add(pointTypeRadioButton);
        if (particleEmitter.getMeshType().equals(ParticleMesh.Type.Point))
            pointTypeRadioButton.setSelected(true);
        velocityVariationSlider._setValue(particleEmitter.getParticleInfluencer().getVelocityVariation());
        maxParticlesField.setText("" + particleEmitter.getMaxNumParticles());
        particlesPerSecondField.setText("" + particleEmitter.getParticlesPerSec());
        startSizeField.setText("" + particleEmitter.getStartSize());
        endSizeField.setText("" + particleEmitter.getEndSize());
        highLifeField.setText("" + particleEmitter.getHighLife());
        lowLifeField.setText("" + particleEmitter.getLowLife());
        gravityPanel.setVector(particleEmitter.getGravity());
        startColorButton.setColor(Wizard.makeColor(particleEmitter.getStartColor()));
        endColorButton.setColor(Wizard.makeColor(particleEmitter.getEndColor()));
        rotateSpeedField.setText("" + particleEmitter.getRotateSpeed());
        imgXField.setText("" + particleEmitter.getImagesX());
        imgYField.setText("" + particleEmitter.getImagesY());
        firingChecker.setChecked(particleEmitter.isFiring());
        frozenChecker.setChecked(!particleEmitter.isEnabled());
        directionVelocityPanel.setVector(particleEmitter.getParticleInfluencer().getInitialVelocity());
        taskPane.setLayout(new RiverLayout());
        firingChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                particleEmitter.setFiring(firingChecker.isChecked());
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        UAManager.add(particleEmitter, (firingChecker.isEnabled() ? "Activate" : "Deactivate " + particleEmitter.getName()));
                        return null;
                    }
                });
            }
        });
        frozenChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                particleEmitter.setEnabled(!frozenChecker.isChecked());
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        UAManager.add(particleEmitter, (firingChecker.isEnabled() ? "Freeze" : "Unfreeze " + particleEmitter.getName()));
                        return null;
                    }
                });
            }
        });
        velocityVariationSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                particleEmitter.getParticleInfluencer().setVelocityVariation(velocityVariationSlider._getValue());
            }
        });
        velocityVariationSlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        UAManager.add(particleEmitter, "Set Velocity Variation to " + velocityVariationSlider._getValue());
                        return null;
                    }
                });
            }
        });
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        particleEmitter.getMaterial().getAdditionalRenderState().setDepthWrite(depthWriteChecker.isChecked());
                        particleEmitter.setEndColor(Wizard.makeColorRGBA(endColorButton.getColor()));
                        particleEmitter.setEndSize(Float.parseFloat(endSizeField.getText()));
                        particleEmitter.setGravity(gravityPanel.getVector());
                        particleEmitter.setHighLife(Float.parseFloat(highLifeField.getText()));
                        particleEmitter.setImagesX(Integer.parseInt(imgXField.getText()));
                        particleEmitter.setImagesY(Integer.parseInt(imgYField.getText()));
                        particleEmitter.setLowLife(Float.parseFloat(lowLifeField.getText()));
                        if (faceNormalPanel.getVector() != null)
                            particleEmitter.setFaceNormal(faceNormalPanel.getVector());
                        particleEmitter.setFacingVelocity(faceVelocityChecker.isChecked());
                        particleEmitter.setStartColor(Wizard.makeColorRGBA(startColorButton.getColor()));
                        particleEmitter.getParticleInfluencer().setInitialVelocity(directionVelocityPanel.getVector());
                        particleEmitter.getParticleInfluencer().setVelocityVariation(velocityVariationSlider._getValue());
                        if (triangleTypeRadioButton.isSelected())
                            particleEmitter.setMeshType(ParticleMesh.Type.Triangle);
                        else
                            particleEmitter.setMeshType(ParticleMesh.Type.Point);
                        particleEmitter.setNumParticles(Integer.parseInt(maxParticlesField.getText()));
                        particleEmitter.setParticlesPerSec(Float.parseFloat(particlesPerSecondField.getText()));
                        particleEmitter.setRotateSpeed(Float.parseFloat(rotateSpeedField.getText()));
                        particleEmitter.setStartSize(Float.parseFloat(startSizeField.getText()));
                        if (startShapeTaskPane.getPropsPanel() instanceof StartShapeTaskPane.PointShapePanel)
                            particleEmitter.setShape(
                                    new EmitterPointShape(
                                    ((StartShapeTaskPane.PointShapePanel) startShapeTaskPane.getPropsPanel()).getPointPanel().getVector()));
                        else if (startShapeTaskPane.getPropsPanel() instanceof StartShapeTaskPane.BoxShapePanel)
                            particleEmitter.setShape(
                                    new EmitterBoxShape(
                                    ((StartShapeTaskPane.BoxShapePanel) startShapeTaskPane.getPropsPanel()).getMinPanel().getVector(),
                                    ((StartShapeTaskPane.BoxShapePanel) startShapeTaskPane.getPropsPanel()).getMaxPanel().getVector()));
                        else if (startShapeTaskPane.getPropsPanel() instanceof StartShapeTaskPane.SphereShapePanel)
                            particleEmitter.setShape(
                                    new EmitterSphereShape(
                                    ((StartShapeTaskPane.SphereShapePanel) startShapeTaskPane.getPropsPanel()).getCenterPanel().getVector(),
                                    Float.parseFloat(((StartShapeTaskPane.SphereShapePanel) startShapeTaskPane.getPropsPanel()).getRadiusField().getText())));
                        UAManager.add(particleEmitter, "Edit " + particleEmitter.getName());
                        return null;
                    }
                });
            }
        });
        taskPane.add("left", new JLabel("Mesh Type:"));
        taskPane.add("tab hfill", pointTypeRadioButton);
        taskPane.add("br tab tab hfill", triangleTypeRadioButton);
        taskPane.add("br left", new JLabel("Active:"));
        taskPane.add("tab", firingChecker);
        taskPane.add("br left", new JLabel("Frozen:"));
        taskPane.add("tab", frozenChecker);
        taskPane.add("br left", new JLabel("Max. Particles:"));
        taskPane.add("tab hfill", maxParticlesField);
        taskPane.add("br left", new JLabel("Particles per Sec.:"));
        taskPane.add("tab hfill", particlesPerSecondField);
        taskPane.add("br left", new JLabel("Velocity Variation:"));
        taskPane.add("tab hfill", velocityVariationSlider);
        taskPane.add("br left", new JLabel("Start Size:"));
        taskPane.add("tab hfill", startSizeField);
        taskPane.add("br left", new JLabel("End Size:"));
        taskPane.add("tab hfill", endSizeField);
        taskPane.add("br left", new JLabel("Start Color:"));
        taskPane.add("tab hfill", startColorButton);
        taskPane.add("br left", new JLabel("End Color:"));
        taskPane.add("tab hfill", endColorButton);
        taskPane.add("br left", new JLabel("High Life:"));
        taskPane.add("tab hfill", highLifeField);
        taskPane.add("br left", new JLabel("Low Life:"));
        taskPane.add("tab hfill", lowLifeField);
        taskPane.add("br left", new JLabel("Gravity:"));
        taskPane.add("tab hfill", gravityPanel);
        taskPane.add("br left", new JLabel("Direction Velocity:"));
        taskPane.add("tab hfill", directionVelocityPanel);
        taskPane.add("br left", new JLabel("Rotate Speed:"));
        taskPane.add("tab hfill", rotateSpeedField);
        taskPane.add("br left", new JLabel("Img X:"));
        taskPane.add("tab hfill", imgXField);
        taskPane.add("br left", new JLabel("Img Y:"));
        taskPane.add("tab hfill", imgYField);
        taskPane.add("br", new JLabel("Depth Write:"));
        taskPane.add("tab", depthWriteChecker);
        taskPane.add("br", new JLabel("Face Velocity:"));
        taskPane.add("tab", faceVelocityChecker);
        taskPane.add("br left", new JLabel("Face Normal:"));
        taskPane.add("tab hfill", faceNormalPanel);
        taskPane.add("br", startShapeTaskPane);
        taskPane.add("br right", applyButton);
        taskPane.setTitle("Particle Emitter");
        add(taskPane, BorderLayout.CENTER);
    }

    private class StartShapeTaskPane extends JXTaskPane
    {

        private JComboBox shapesComboBox = new JComboBox(new String[]
        {
            "Point Shape", "Box Shape", "Sphere Shape"
        });
        private JPanel propsPanel;

        public StartShapeTaskPane(EmitterShape shape)
        {
            shapesComboBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    switch (shapesComboBox.getSelectedIndex())
                    {
                        case 0:
                            arrangeProps(new EmitterPointShape(Vector3f.ZERO));
                            break;
                        case 1:
                            arrangeProps(new EmitterBoxShape(Vector3f.ZERO, new Vector3f(10, 10, 10)));
                            break;
                        case 2:
                            arrangeProps(new EmitterSphereShape(Vector3f.ZERO, 10));
                    }
                }
            });
            setTitle("Start Shape");
            add(shapesComboBox, BorderLayout.NORTH);
            arrangeProps(shape);
        }

        private void arrangeProps(EmitterShape shape)
        {
            removeAll();
            add(shapesComboBox, BorderLayout.NORTH);
            if (shape instanceof EmitterPointShape)
            {
                propsPanel = new PointShapePanel(((EmitterPointShape) shape).getPoint());
                add(propsPanel, BorderLayout.CENTER);
            } else if (shape instanceof EmitterSphereShape)
            {
                propsPanel = new SphereShapePanel(((EmitterSphereShape) shape).getCenter(), ((EmitterSphereShape) shape).getRadius());
                add(propsPanel, BorderLayout.CENTER);
            } else if (shape instanceof EmitterBoxShape)
            {
                propsPanel = new BoxShapePanel(
                        ((EmitterBoxShape) shape).getMin(),
                        ((EmitterBoxShape) shape).getMin().add(((EmitterBoxShape) shape).getLen()));
                add(propsPanel, BorderLayout.CENTER);
            }
        }

        public JPanel getPropsPanel()
        {
            return propsPanel;
        }

        private class PointShapePanel extends JPanel
        {

            private Float3Panel pointPanel;

            public PointShapePanel(Vector3f point)
            {
                pointPanel = new Float3Panel(point, Wizard.getCamera(), Float3Panel.HORIZONTAL);
                setLayout(new RiverLayout());
                add("left", new JLabel("Location:"));
                add("tab hfill", pointPanel);
            }

            public Float3Panel getPointPanel()
            {
                return pointPanel;
            }
        }

        private class BoxShapePanel extends JPanel
        {

            private Float3Panel minPanel, maxPanel;

            public BoxShapePanel(Vector3f min, Vector3f max)
            {
                minPanel = new Float3Panel(min, Wizard.getCamera(), Float3Panel.HORIZONTAL);
                maxPanel = new Float3Panel(max, Wizard.getCamera(), Float3Panel.HORIZONTAL);
                setLayout(new RiverLayout());
                add("left", new JLabel("Minimum:"));
                add("tab hfill", minPanel);
                add("br left", new JLabel("Maximum:"));
                add("tab hfill", maxPanel);
            }

            public Float3Panel getMinPanel()
            {
                return minPanel;
            }

            public Float3Panel getMaxPanel()
            {
                return maxPanel;
            }
        }

        private class SphereShapePanel extends JPanel
        {

            private Float3Panel centerPanel;
            private BTextField radiusField;

            public SphereShapePanel(Vector3f center, float radius)
            {
                centerPanel = new Float3Panel(center, Wizard.getCamera(), Float3Panel.HORIZONTAL);
                radiusField = new BTextField("Integer", radius + "");
                setLayout(new RiverLayout());
                add("left", new JLabel("Center:"));
                add("tab hfill", centerPanel);
                add("br left", new JLabel("Radius:"));
                add("tab hfill", radiusField);
            }

            public Float3Panel getCenterPanel()
            {
                return centerPanel;
            }

            public BTextField getRadiusField()
            {
                return radiusField;
            }
        }
    }
}
