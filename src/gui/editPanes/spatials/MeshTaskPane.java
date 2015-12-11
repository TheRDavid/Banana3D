package gui.editPanes.spatials;

import b3dElements.spatials.geometries.B3D_Cylinder;
import b3dElements.spatials.geometries.B3D_Sphere;
import b3dElements.spatials.geometries.B3D_Torus;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.simsilica.lemur.geom.MBox;
import components.BTextField;
import components.Checker;
import components.Float3Panel;
import general.UAManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class MeshTaskPane extends EditTaskPane
{

    private BoxPropertiesPanel boxPropertiesPanel;
    private SpherePropertiesPanel spherePropertiesPanel;
    private CylinderPropertiesPanel cylinderPropertiesPanel;
    private TorusPropertiesPanel torusPropertiesPanel;
    private Mesh mesh;

    /**
     *
     * @param m
     */
    public MeshTaskPane(Mesh m)
    {
        mesh = m;
        if (mesh instanceof MBox)
        {
            boxPropertiesPanel = new BoxPropertiesPanel();
            taskPane.add(boxPropertiesPanel);
            add(taskPane, BorderLayout.CENTER);
        } else if (mesh instanceof Sphere)
        {
            spherePropertiesPanel = new SpherePropertiesPanel();
            taskPane.add(spherePropertiesPanel);
            add(taskPane, BorderLayout.CENTER);
        } else if (mesh instanceof Cylinder)
        {
            cylinderPropertiesPanel = new CylinderPropertiesPanel();
            taskPane.add(cylinderPropertiesPanel);
            add(taskPane, BorderLayout.CENTER);
        } else if (mesh instanceof Torus)
        {
            torusPropertiesPanel = new TorusPropertiesPanel();
            taskPane.add(torusPropertiesPanel);
            add(taskPane, BorderLayout.CENTER);
        }
        taskPane.setTitle("Mesh Properties");
        taskPane.add("br right", applyButton);
    }

    private class BoxPropertiesPanel extends JPanel
    {

        private Float3Panel slicesPanel;

        public BoxPropertiesPanel()
        {
            setLayout(new RiverLayout(5, 5));
            slicesPanel = new Float3Panel(new Vector3f((Integer) ((Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getUserData("xSlices"),
                    (Integer) ((Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getUserData("ySlices"),
                    (Integer) ((Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject()).getUserData("zSlices")), Wizard.getCamera());
            add("br left", new JLabel("Slices (integers!):"));
            add("tab hfill", slicesPanel);
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
                            MBox tempMBox = new MBox(1, 1, 1, (int) slicesPanel.getVector().getX(), (int) slicesPanel.getVector().getY(), (int) slicesPanel.getVector().getZ());
                            Geometry tempGeometry = (Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                            tempGeometry.setMesh(tempMBox);
                            tempGeometry.setUserData("xSlices", (int) slicesPanel.getVector().getX());
                            tempGeometry.setUserData("ySlices", (int) slicesPanel.getVector().getY());
                            tempGeometry.setUserData("zSlices", (int) slicesPanel.getVector().getZ());
                            UAManager.add(tempGeometry, "Edit Mesh of " + tempGeometry.getName());
                            return null;
                        }
                    });
                }
            });
        }
    }

    private class SpherePropertiesPanel extends JPanel
    {

        private BTextField radialSamplesTextField, zSamplesTextField, radiusTextField;

        public SpherePropertiesPanel()
        {
            setLayout(new RiverLayout(5, 5));
            Sphere tempSphere = (Sphere) mesh;
            radialSamplesTextField = new BTextField("Int", Integer.toString(tempSphere.getRadialSamples()));
            zSamplesTextField = new BTextField("Int", Integer.toString(tempSphere.getZSamples()));
            radiusTextField = new BTextField("Float", Float.toString(tempSphere.getRadius()));
            add("br left", new JLabel("Radial Samples:"));
            add("tab hfill", radialSamplesTextField);
            add("br left", new JLabel("Z Samples:"));
            add("tab hfill", zSamplesTextField);
            add("br left", new JLabel("Radius:"));
            add("tab hfill", radiusTextField);
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
                            Sphere tempSphere = new Sphere(Integer.parseInt(zSamplesTextField.getText()),
                                    Integer.parseInt(radialSamplesTextField.getText()),
                                    Float.parseFloat(radiusTextField.getText()));
                            Geometry tempGeometry = (Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                            tempGeometry.setMesh(tempSphere);
                            B3D_Sphere b3D_Sphere = (B3D_Sphere) Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
                            b3D_Sphere.setRadialSamples(Integer.parseInt(radialSamplesTextField.getText()));
                            b3D_Sphere.setRadius(Float.parseFloat(radiusTextField.getText()));
                            b3D_Sphere.setzSamples(Integer.parseInt(zSamplesTextField.getText()));
                            UAManager.add(tempGeometry, "Edit Mesh of " + tempGeometry.getName());
                            return null;
                        }
                    });
                }
            });
        }
    }

    private class CylinderPropertiesPanel extends JPanel
    {

        private BTextField axisSamplesTextField, radialSamplesTextField, heightTextField, radiusTextField, radius2TextField;
        private Checker isClosedChecker, isInvertedChecker;

        public CylinderPropertiesPanel()
        {
            setLayout(new RiverLayout(5, 5));
            final Cylinder tempCylinder = (Cylinder) mesh;
            axisSamplesTextField = new BTextField("Int", Integer.toString(tempCylinder.getAxisSamples()));
            radialSamplesTextField = new BTextField("Int", Integer.toString(tempCylinder.getRadialSamples()));
            heightTextField = new BTextField("Float", Float.toString(tempCylinder.getHeight()));
            radiusTextField = new BTextField("Float", Float.toString(tempCylinder.getRadius()));
            radius2TextField = new BTextField("Float", Float.toString(tempCylinder.getRadius2()));
            isClosedChecker = new Checker();
            isInvertedChecker = new Checker();
            isClosedChecker.setChecked(tempCylinder.isClosed());
            isInvertedChecker.setChecked(tempCylinder.isInverted());
            add("br left", new JLabel("Axis Samples:"));
            add("tab hfill", axisSamplesTextField);
            add("br left", new JLabel("Radial Samples:"));
            add("tab hfill", radialSamplesTextField);
            add("br left", new JLabel("Height:"));
            add("tab hfill", heightTextField);
            add("br left", new JLabel("Radius:"));
            add("tab hfill", radiusTextField);
            add("br left", new JLabel("Radius 2:"));
            add("tab hfill", radius2TextField);
            add("br left", new JLabel("Closed:"));
            add("tab", isClosedChecker);
            add("br left", new JLabel("Inverted:"));
            add("tab", isInvertedChecker);
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
                            Geometry tempGeometry = (Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                            Cylinder tempCylinder = new Cylinder(Integer.parseInt(axisSamplesTextField.getText()),
                                    Integer.parseInt(radialSamplesTextField.getText()),
                                    Float.parseFloat(radiusTextField.getText()),
                                    Float.parseFloat(radius2TextField.getText()),
                                    Float.parseFloat(heightTextField.getText()),
                                    isClosedChecker.isChecked(),
                                    isInvertedChecker.isChecked());
                            tempGeometry.setMesh(tempCylinder);
                            B3D_Cylinder b3D_Cylinder = (B3D_Cylinder) Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
                            b3D_Cylinder.setAxisSamples(Integer.parseInt(axisSamplesTextField.getText()));
                            b3D_Cylinder.setRadialSamples(Integer.parseInt(radialSamplesTextField.getText()));
                            b3D_Cylinder.setRadius(Float.parseFloat(radiusTextField.getText()));
                            b3D_Cylinder.setRadius2(Float.parseFloat(radius2TextField.getText()));
                            b3D_Cylinder.setHeight(Float.parseFloat(heightTextField.getText()));
                            b3D_Cylinder.setIsClosed(isClosedChecker.isChecked());
                            b3D_Cylinder.setIsInverted(isInvertedChecker.isChecked());
                            UAManager.add(tempGeometry, "Edit Mesh of " + tempGeometry.getName());
                            return null;
                        }
                    });
                }
            });
        }
    }

    private class TorusPropertiesPanel extends JPanel
    {

        private BTextField circleSamplesTextField, radialSamplesTextField, innerRadiusTextField, outerRadiusTextField;

        public TorusPropertiesPanel()
        {
            setLayout(new RiverLayout(5, 5));
            Torus tempCylinder = (Torus) mesh;
            circleSamplesTextField = new BTextField("Int", Integer.toString(tempCylinder.getCircleSamples()));
            radialSamplesTextField = new BTextField("Int", Integer.toString(tempCylinder.getRadialSamples()));
            innerRadiusTextField = new BTextField("Float", Float.toString(tempCylinder.getInnerRadius()));
            outerRadiusTextField = new BTextField("Float", Float.toString(tempCylinder.getOuterRadius()));
            add("br left", new JLabel("Circle Samples:"));
            add("tab hfill", circleSamplesTextField);
            add("br left", new JLabel("Radial Samples:"));
            add("tab hfill", radialSamplesTextField);
            add("br left", new JLabel("Inner Radius:"));
            add("tab hfill", innerRadiusTextField);
            add("br left", new JLabel("Outer Radius:"));
            add("tab hfill", outerRadiusTextField);
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
                            Geometry tempGeometry = (Geometry) CurrentData.getEditorWindow().getB3DApp().getSelectedObject();
                            Torus tempTorus = new Torus(Integer.parseInt(circleSamplesTextField.getText()),
                                    Integer.parseInt(radialSamplesTextField.getText()),
                                    Float.parseFloat(innerRadiusTextField.getText()),
                                    Float.parseFloat(outerRadiusTextField.getText()));
                            tempGeometry.setMesh(tempTorus);
                            B3D_Torus b3D_Torus = (B3D_Torus) Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
                            b3D_Torus.setCircleSamples(Integer.parseInt(circleSamplesTextField.getText()));
                            b3D_Torus.setRadialSamples(Integer.parseInt(radialSamplesTextField.getText()));
                            b3D_Torus.setInnerRadius(Float.parseFloat(innerRadiusTextField.getText()));
                            b3D_Torus.setOuterRadius(Float.parseFloat(outerRadiusTextField.getText()));
                            UAManager.add(tempGeometry, "Edit Mesh of " + tempGeometry.getName());
                            return null;
                        }
                    });
                }
            });
        }
    }
}
