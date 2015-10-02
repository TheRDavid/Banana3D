package gui.editPanes.filters;

import b3dElements.filters.B3D_BasicSSAO;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import com.shaderblow.filter.basicssao.BasicSSAO;
import components.BTextField;
import components.Checker;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import other.ObjectToElementConverter;
import other.Wizard;
import se.datadosen.component.RiverLayout;

public class BasicSSAOTaskPane extends EditTaskPane
{

    private BTextField biasField, detailBiasField, detailIntensityField, detailSampleRadiusField, detailScaleField, falloffRateField,
            falloffStartDistanceField, intensityField, sampleRadiusField, scaleField;
    private Checker smoothMoreChecker, useAOChecker, useOnlyAOChecker, useDetailPassChecker, useDistanceFalloffChecker, useSmoothingChecker;

    public BasicSSAOTaskPane(final BasicSSAO basicSSAO)
    {
        biasField = new BTextField("Float", Float.toString(basicSSAO.getBias()));
        detailBiasField = new BTextField("Float", Float.toString(basicSSAO.getDetailBias()));
        detailIntensityField = new BTextField("Float", Float.toString(basicSSAO.getDetailIntensity()));
        detailSampleRadiusField = new BTextField("Float", Float.toString(basicSSAO.getDetailSampleRadius()));
        detailScaleField = new BTextField("Float", Float.toString(basicSSAO.getDetailScale()));
        falloffRateField = new BTextField("Float", Float.toString(basicSSAO.getFalloffRate()));
        falloffStartDistanceField = new BTextField("Float", Float.toString(basicSSAO.getFalloffStartDistance()));
        intensityField = new BTextField("Float", Float.toString(basicSSAO.getIntensity()));
        sampleRadiusField = new BTextField("Float", Float.toString(basicSSAO.getSampleRadius()));
        scaleField = new BTextField("Float", Float.toString(basicSSAO.getScale()));
        smoothMoreChecker = new Checker();
        smoothMoreChecker.setChecked(basicSSAO.isSmoothMore());
        useAOChecker = new Checker();
        useAOChecker.setChecked(basicSSAO.isUseAo());
        useOnlyAOChecker = new Checker();
        useOnlyAOChecker.setChecked(basicSSAO.isUseOnlyAo());
        useDetailPassChecker = new Checker();
        useDetailPassChecker.setChecked(basicSSAO.getUseDetailPass());
        useDistanceFalloffChecker = new Checker();
        useDistanceFalloffChecker.setChecked(basicSSAO.getUseDistanceFalloff());
        useSmoothingChecker = new Checker();
        useSmoothingChecker.setChecked(basicSSAO.isUseSmoothing());
        smoothMoreChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        basicSSAO.setSmoothMore(smoothMoreChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useAOChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        basicSSAO.setUseAo(useAOChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useOnlyAOChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        basicSSAO.setUseOnlyAo(useOnlyAOChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useDetailPassChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        basicSSAO.setUseDetailPass(useDetailPassChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useDistanceFalloffChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        basicSSAO.setUseDistanceFalloff(useDistanceFalloffChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useSmoothingChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        basicSSAO.setUseSmoothing(useSmoothingChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        applyButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().removeFilter(
                                basicSSAO);
                        UUID elementUUID = Wizard.getObjectReferences().getUUID(basicSSAO.hashCode());
                        B3D_BasicSSAO b3D_BasicSSAO = (B3D_BasicSSAO) Wizard.getObjects().getB3D_Element(elementUUID);
                        Wizard.getObjects().remove(basicSSAO.hashCode(), elementUUID);
                        CurrentData.getEditorWindow().getB3DApp().setSelectedElement(Wizard.NULL_SELECTION, null);
                        BasicSSAO newBasicSSAO = new BasicSSAO();
                        newBasicSSAO.setBias(Float.parseFloat(biasField.getText()));
                        newBasicSSAO.setDetailBias(Float.parseFloat(detailBiasField.getText()));
                        newBasicSSAO.setDetailIntensity(Float.parseFloat(detailIntensityField.getText()));
                        newBasicSSAO.setDetailSampleRadius(Float.parseFloat(detailSampleRadiusField.getText()));
                        newBasicSSAO.setDetailScale(Float.parseFloat(detailScaleField.getText()));
                        newBasicSSAO.setIntensity(Float.parseFloat(intensityField.getText()));
                        newBasicSSAO.setSampleRadius(Float.parseFloat(sampleRadiusField.getText()));
                        newBasicSSAO.setScale(Float.parseFloat(scaleField.getText()));
                        newBasicSSAO.setSmoothMore(smoothMoreChecker.isChecked());
                        newBasicSSAO.setUseAo(useAOChecker.isChecked());
                        newBasicSSAO.setUseDetailPass(useDetailPassChecker.isChecked());
                        newBasicSSAO.setUseDistanceFalloff(useDistanceFalloffChecker.isChecked());
                        newBasicSSAO.setUseOnlyAo(useOnlyAOChecker.isChecked());
                        newBasicSSAO.setUseSmoothing(useSmoothingChecker.isChecked());
                        newBasicSSAO.setFalloffRate(Float.parseFloat(falloffRateField.getText()));
                        newBasicSSAO.setFalloffStartDistance(Float.parseFloat(falloffStartDistanceField.getText()));
                        CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(
                                newBasicSSAO);
                        //ConvertMode does not matter here, the filter can not possibly have a LightControl
                        B3D_BasicSSAO newB3D_BasicSSAO = ObjectToElementConverter.convertBasicSSAO(
                                newBasicSSAO, b3D_BasicSSAO.getFilterIndex());
                        Wizard.getObjects().add(newBasicSSAO, newB3D_BasicSSAO);
                        CurrentData.getEditorWindow().getB3DApp().setSelectedUUID(newB3D_BasicSSAO.getUUID());
                        CurrentData.getEditorWindow().getTree().sync();
                        CurrentData.getEditorWindow().getEditPane().arrange(true);
                        return null;
                    }
                });
            }
        });
        taskPane.setLayout(new RiverLayout());
        taskPane.add("left", new JLabel("Bias:"));
        taskPane.add("tab hfill", biasField);
        taskPane.add("br left", new JLabel("Detail Bias:"));
        taskPane.add("tab hfill", detailBiasField);
        taskPane.add("br left", new JLabel("Detail Intensity:"));
        taskPane.add("tab hfill", detailIntensityField);
        taskPane.add("br left", new JLabel("Detail Sample Radius:"));
        taskPane.add("tab hfill", detailSampleRadiusField);
        taskPane.add("br left", new JLabel("Detail Scale:"));
        taskPane.add("tab hfill", detailScaleField);
        taskPane.add("br left", new JLabel("Falloff Rate:"));
        taskPane.add("tab hfill", falloffRateField);
        taskPane.add("br left", new JLabel("Falloff Start Distance:"));
        taskPane.add("tab hfill", falloffStartDistanceField);
        taskPane.add("br left", new JLabel("Intensity:"));
        taskPane.add("tab hfill", intensityField);
        taskPane.add("br left", new JLabel("Sample Radius:"));
        taskPane.add("tab hfill", sampleRadiusField);
        taskPane.add("br left", new JLabel("Scale:"));
        taskPane.add("tab hfill", scaleField);
        taskPane.add("br left", new JLabel("Smooth More:"));
        taskPane.add("tab", smoothMoreChecker);
        taskPane.add("br left", new JLabel("Use AO:"));
        taskPane.add("tab", useAOChecker);
        taskPane.add("br left", new JLabel("Use only AO:"));
        taskPane.add("tab", useOnlyAOChecker);
        taskPane.add("br left", new JLabel("Use Detail Pass:"));
        taskPane.add("tab", useDetailPassChecker);
        taskPane.add("br left", new JLabel("Use Distance Falloff:"));
        taskPane.add("tab", useDistanceFalloffChecker);
        taskPane.add("br left", new JLabel("Use Smoothing:"));
        taskPane.add("tab", useSmoothingChecker);
        taskPane.add("br right", applyButton);
        taskPane.setTitle("Basic SSAO");
        add(taskPane, BorderLayout.CENTER);
    }
}
