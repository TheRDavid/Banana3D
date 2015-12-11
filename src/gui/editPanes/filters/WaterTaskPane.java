package gui.editPanes.filters;

import gui.components.AssetButton;
import gui.components.BColorButton;
import gui.editPanes.EditTaskPane;
import general.CurrentData;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture2D;
import components.BButton;
import components.BSlider;
import components.BTextField;
import components.Checker;
import components.Float3Panel;
import components.OKButton;
import general.UAManager;
import other.Wizard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTaskPane;
import monkeyStuff.WaterFilterWithGetters;
import se.datadosen.component.RiverLayout;

public class WaterTaskPane extends EditTaskPane
{

    private WaterFilterWithGetters waterFilter;
    private BTextField heightField = new BTextField("Float");
    private BTextField speedField = new BTextField("Float");
    private BSlider waveScaleSlider = new BSlider(Float.class, 0, 50, 5);
    private BSlider maxAmplitudeSlider = new BSlider(Float.class, 0, 30, 1);
    private BSlider resolutionSlider = new BSlider(Integer.class, 1, 1024, 512);
    private BSlider transparencySlider = new BSlider(Float.class, -1, 0, .1f);
    private BColorButton waterColorButton = new BColorButton(Color.white);
    private BColorButton deepWaterColorButton = new BColorButton(Color.white);
    private BTextField normalScaleField = new BTextField("Float");
    private BSlider windXSlider = new BSlider(Float.class, -1, 1, 0);
    private BSlider windZSlider = new BSlider(Float.class, -1, 1, 0);
    private BTextField sunScaleField = new BTextField("Float");
    private BColorButton sunColorButton = new BColorButton(Color.white);
    private Float3Panel sunDirectionField = new Float3Panel(Vector3f.NAN, Wizard.getCamera());
    private BTextField reflectionDisplaceField = new BTextField("Float");
    private BTextField refractionStrengthField = new BTextField("Float");
    private BTextField foamHardnessField = new BTextField("Float");
    private BTextField foamIntensityField = new BTextField("Float");
    private BTextField causticsIntensityField = new BTextField("Float");
    private BTextField underWaterFogDistanceField = new BTextField("Float");
    private BTextField shoreHardnessField = new BTextField("Float");
    private BTextField shininessField = new BTextField("Float");
    private Float3Panel foamExistencePanel = new Float3Panel(Vector3f.NAN, Wizard.getCamera());
    private Float3Panel colorExtinctionPanel = new Float3Panel(Vector3f.NAN, Wizard.getCamera());
    private Checker useCausistsChecker, useFoamChecker, useHQShoreLineChecker, useRefractionChecker, useRipplesChecker, useSpecularChecker;
    private TexturesTaskPane texturesTaskPane;
    private boolean heightAltered = false;

    public WaterTaskPane(final WaterFilterWithGetters wf)
    {
        waterFilter = wf;
        texturesTaskPane = new TexturesTaskPane();
        speedField.setText("" + waterFilter.getSpeed());
        foamHardnessField.setText("" + waterFilter.getFoamHardness());
        foamIntensityField.setText("" + waterFilter.getFoamIntensity());
        reflectionDisplaceField.setText("" + waterFilter.getReflectionDisplace());
        refractionStrengthField.setText("" + waterFilter.getRefractionStrength());
        sunScaleField.setText("" + waterFilter.getSunScale());
        underWaterFogDistanceField.setText("" + waterFilter.getUnderWaterFogDistance());
        normalScaleField.setText("" + waterFilter.getNormalScale());
        causticsIntensityField.setText("" + waterFilter.getCausticsIntensity());
        shininessField.setText("" + waterFilter.getShininess());
        shoreHardnessField.setText("" + waterFilter.getShoreHardness());
        resolutionSlider.setValue(waterFilter.getReflectionMapSize());
        resolutionSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                waterFilter.setReflectionMapSize(resolutionSlider.getValue());
            }
        });
        resolutionSlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(waterFilter, "Set Reflection Resolution to " + resolutionSlider.getValue());
            }
        });
        heightField.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                heightAltered = true;
            }

            @Override
            public void focusLost(FocusEvent e)
            {
            }
        });
        waveScaleSlider._setValue(waterFilter.getWaveScale() * 1000);
        waveScaleSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                waterFilter.setWaveScale(waveScaleSlider._getValue() / 1000);
            }
        });
        waveScaleSlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(waterFilter, "Set Wave Scale to " + waveScaleSlider._getValue() / 1000);
            }
        });
        maxAmplitudeSlider._setValue(waterFilter.getMaxAmplitude());
        maxAmplitudeSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                waterFilter.setMaxAmplitude(maxAmplitudeSlider._getValue());
            }
        });
        maxAmplitudeSlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(waterFilter, "Set max. Amplitude to " + maxAmplitudeSlider._getValue());
            }
        });
        transparencySlider._setValue(-waterFilter.getWaterTransparency());
        transparencySlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                waterFilter.setWaterTransparency(-transparencySlider._getValue());
            }
        });
        transparencySlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(waterFilter, "Set Transparency to " + -transparencySlider._getValue());
            }
        });
        windXSlider._setValue(waterFilter.getWindDirection().getX());
        windZSlider._setValue(waterFilter.getWindDirection().getY());
        windXSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                waterFilter.setWindDirection(new Vector2f(windXSlider._getValue(), windZSlider._getValue()));
            }
        });
        windZSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                waterFilter.setWindDirection(new Vector2f(windXSlider._getValue(), windZSlider._getValue()));
            }
        });

        windZSlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(waterFilter, "Change Wind Direction of " + waterFilter.getName());
            }
        });
        windXSlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                UAManager.add(waterFilter, "Change Wind Direction of " + waterFilter.getName());
            }
        });
        colorExtinctionPanel.setVector(waterFilter.getColorExtinction());
        foamExistencePanel.setVector(waterFilter.getFoamExistence());
        sunDirectionField.setVector(waterFilter.getLightDirection());

        waterColorButton.setColor(Wizard.makeColor(waterFilter.getWaterColor()));
        deepWaterColorButton.setColor(Wizard.makeColor(waterFilter.getDeepWaterColor()));
        sunColorButton.setColor(Wizard.makeColor(waterFilter.getLightColor()));

        useCausistsChecker = new Checker();
        useFoamChecker = new Checker();
        useHQShoreLineChecker = new Checker();
        useRefractionChecker = new Checker();
        useRipplesChecker = new Checker();
        useSpecularChecker = new Checker();
        useCausistsChecker.setChecked(waterFilter.isUseCaustics());
        useFoamChecker.setChecked(waterFilter.isUseFoam());
        useHQShoreLineChecker.setChecked(waterFilter.isUseHQShoreline());
        useRefractionChecker.setChecked(waterFilter.isUseRefraction());
        useRipplesChecker.setChecked(waterFilter.isUseRipples());
        useSpecularChecker.setChecked(waterFilter.isUseSpecular());

        initCheckerActions();

        taskPane.setLayout(new RiverLayout(5, 5));
        taskPane.setTitle("Water Filter");
        add(taskPane, BorderLayout.CENTER);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                waterFilter.setCausticsIntensity(Float.parseFloat(causticsIntensityField.getText()));
                waterFilter.setColorExtinction(colorExtinctionPanel.getVector());
                waterFilter.setDeepWaterColor(Wizard.makeColorRGBA(deepWaterColorButton.getColor()));
                waterFilter.setFoamExistence(foamExistencePanel.getVector());
                waterFilter.setFoamHardness(Float.parseFloat(foamHardnessField.getText()));
                waterFilter.setFoamIntensity(Float.parseFloat(foamIntensityField.getText()));
                waterFilter.setLightColor(Wizard.makeColorRGBA(sunColorButton.getColor()));
                waterFilter.setLightDirection(sunDirectionField.getVector());
                waterFilter.setMaxAmplitude(maxAmplitudeSlider._getValue());
                waterFilter.setNormalScale(Float.parseFloat(normalScaleField.getText()));
                //waterFilter.setReflectionDisplace(Float.parseFloat(reflectionDisplaceField.getText()));
                waterFilter.setReflectionMapSize(resolutionSlider.getValue());
                waterFilter.setRefractionStrength(Float.parseFloat(refractionStrengthField.getText()));
                waterFilter.setShininess(Float.parseFloat(shininessField.getText()));
                waterFilter.setShoreHardness(Float.parseFloat(shoreHardnessField.getText()));
                waterFilter.setSpeed(Float.parseFloat(speedField.getText()));
                waterFilter.setSunScale(Float.parseFloat(sunScaleField.getText()));
                waterFilter.setUnderWaterFogDistance(Float.parseFloat(underWaterFogDistanceField.getText()));
                waterFilter.setUseCaustics(useCausistsChecker.isChecked());
                waterFilter.setUseFoam(useFoamChecker.isChecked());
                waterFilter.setUseHQShoreline(useHQShoreLineChecker.isChecked());
                waterFilter.setUseRefraction(useRefractionChecker.isChecked());
                waterFilter.setUseRipples(useRipplesChecker.isChecked());
                waterFilter.setUseSpecular(useSpecularChecker.isChecked());
                waterFilter.setWaterColor(Wizard.makeColorRGBA(waterColorButton.getColor()));
                waterFilter.setWaterHeight(Float.parseFloat(heightField.getText()));
                waterFilter.setWaterTransparency(transparencySlider._getValue());
                waterFilter.setWaveScale(-waveScaleSlider._getValue() / 1000);
                waterFilter.setWindDirection(new Vector2f(windXSlider._getValue(), windZSlider._getValue()));
                heightAltered = false;
                UAManager.add(waterFilter, "Edit " + waterFilter.getName());
            }
        });
        taskPane.add("left", new JLabel("Height:"));
        taskPane.add("tab hfill", heightField);
        taskPane.add("br left", new JLabel("Speed:"));
        taskPane.add("tab hfill", speedField);
        taskPane.add("br left", new JLabel("Wave Scale:"));
        taskPane.add("tab hfill", waveScaleSlider);
        taskPane.add("br left", new JLabel("Max. Amplitude:"));
        taskPane.add("tab hfill", maxAmplitudeSlider);
        taskPane.add("br left", new JLabel("Resolution:"));
        taskPane.add("tab hfill", resolutionSlider);
        taskPane.add("br left", new JLabel("Transparency:"));
        taskPane.add("tab hfill", transparencySlider);
        taskPane.add("br left", new JLabel("Water Color:"));
        taskPane.add("tab hfill", waterColorButton);
        taskPane.add("br left", new JLabel("Deep-Water Color:"));
        taskPane.add("tab hfill", deepWaterColorButton);
        taskPane.add("br left", new JLabel("Normal Scale:"));
        taskPane.add("tab hfill", normalScaleField);
        taskPane.add("br left", new JLabel("Sun Scale:"));
        taskPane.add("tab hfill", sunScaleField);
        taskPane.add("br left", new JLabel("Sun Color:"));
        taskPane.add("tab hfill", sunColorButton);
        taskPane.add("br left", new JLabel("Sun Direction:"));
        taskPane.add("tab hfill", sunDirectionField);
        taskPane.add("br left", new JLabel("Reflection Displace:"));
        taskPane.add("tab hfill", reflectionDisplaceField);
        taskPane.add("br left", new JLabel("Refraction Strength:"));
        taskPane.add("tab hfill", refractionStrengthField);
        taskPane.add("br left", new JLabel("Foam Hardness:"));
        taskPane.add("tab hfill", foamHardnessField);
        taskPane.add("br left", new JLabel("Foam Intensity:"));
        taskPane.add("tab hfill", foamIntensityField);
        taskPane.add("br left", new JLabel("Caustics Intensity:"));
        taskPane.add("tab hfill", causticsIntensityField);
        taskPane.add("br left", new JLabel("Underwater Fog Distance:"));
        taskPane.add("tab hfill", underWaterFogDistanceField);
        taskPane.add("br left", new JLabel("Shore Hardness:"));
        taskPane.add("tab hfill", shoreHardnessField);
        taskPane.add("br left", new JLabel("Shininess:"));
        taskPane.add("tab hfill", shininessField);
        taskPane.add("br left", new JLabel("Wind Direction:"));
        taskPane.add("br left", new JLabel("X:"));
        taskPane.add("tab hfill", windXSlider);
        taskPane.add("br left", new JLabel("Z:"));
        taskPane.add("tab hfill", windZSlider);
        taskPane.add("br left", new JLabel("Foam Existence:"));
        taskPane.add("tab hfill", foamExistencePanel);
        taskPane.add("br left", new JLabel("Color Extinction:"));
        taskPane.add("tab hfill", colorExtinctionPanel);
        taskPane.add("br left", new JLabel("Use Specular:"));
        taskPane.add("tab", useSpecularChecker);
        taskPane.add("br left", new JLabel("Use Refraction:"));
        taskPane.add("tab", useRefractionChecker);
        taskPane.add("br left", new JLabel("Use Foam:"));
        taskPane.add("tab", useFoamChecker);
        taskPane.add("br left", new JLabel("Use Causists:"));
        taskPane.add("tab", useCausistsChecker);
        taskPane.add("br left", new JLabel("HQ Shoreline:"));
        taskPane.add("tab", useHQShoreLineChecker);
        taskPane.add("br left", new JLabel("Use Ripples:"));
        taskPane.add("tab", useRipplesChecker);
        taskPane.add("br right", applyButton);
        add(texturesTaskPane, BorderLayout.SOUTH);
    }

    @Override
    public void updateData(boolean urgent)
    {
        if (!heightAltered)
        {
            heightField.setText("" + waterFilter.getWaterHeight());
        }
    }

    private void initCheckerActions()
    {
        useCausistsChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        waterFilter.setUseCaustics(useCausistsChecker.isChecked());
                        UAManager.add(waterFilter, "Use Caustics " + useCausistsChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useFoamChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        waterFilter.setUseFoam(useFoamChecker.isChecked());
                        UAManager.add(waterFilter, "Use Foam " + useFoamChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useHQShoreLineChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        waterFilter.setUseHQShoreline(useHQShoreLineChecker.isChecked());
                        UAManager.add(waterFilter, "Use HQ Shoreline " + useHQShoreLineChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useRefractionChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        waterFilter.setUseRefraction(useRefractionChecker.isChecked());
                        UAManager.add(waterFilter, "Use Refraction " + useRefractionChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useRipplesChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        waterFilter.setUseRipples(useRipplesChecker.isChecked());
                        UAManager.add(waterFilter, "Use Ripples " + useRipplesChecker.isChecked());
                        return null;
                    }
                });
            }
        });
        useSpecularChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        waterFilter.setUseSpecular(useSpecularChecker.isChecked());
                        UAManager.add(waterFilter, "Use Specular " + useSpecularChecker.isChecked());
                        return null;
                    }
                });
            }
        });
    }

    private class TexturesTaskPane extends JXTaskPane
    {

        private AssetButton causticsButton, foamButton, heightButton, normalButton;
        private DefaultButton causticsDefault, foamDefault, heightDefault, normalDefault;
        private OKButton applyButton1 = new OKButton("Apply");

        public TexturesTaskPane()
        {
            if (waterFilter.getCausticsTexture() != null
                    && !waterFilter.getCausticsTexture().getKey().getName().equals("Common/MatDefs/Water/Textures/caustics.jpg"))
                causticsButton = new AssetButton(AssetButton.AssetType.Texture, waterFilter.getCausticsTexture().getKey().getName());
            else
                causticsButton = new AssetButton(AssetButton.AssetType.Texture, "Default Texture");
            if (waterFilter.getFoamTexture() != null
                    && !waterFilter.getFoamTexture().getKey().getName().equals("Common/MatDefs/Water/Textures/foam.jpg"))
                foamButton = new AssetButton(AssetButton.AssetType.Texture, waterFilter.getFoamTexture().getKey().getName());
            else
                foamButton = new AssetButton(AssetButton.AssetType.Texture, "Default Texture");
            if (waterFilter.getHeightTexture() != null
                    && !waterFilter.getHeightTexture().getKey().getName().equals("Common/MatDefs/Water/Textures/heightmap.jpg"))
                heightButton = new AssetButton(AssetButton.AssetType.Texture, waterFilter.getHeightTexture().getKey().getName());
            else
                heightButton = new AssetButton(AssetButton.AssetType.Texture, "Default Texture");
            if (waterFilter.getNormalTexture() != null
                    && !waterFilter.getNormalTexture().getKey().getName().equals("Common/MatDefs/Water/Textures/water_normalmap.dds"))
                normalButton = new AssetButton(AssetButton.AssetType.Texture, waterFilter.getNormalTexture().getKey().getName());
            else
                normalButton = new AssetButton(AssetButton.AssetType.Texture, "Default Texture");
            applyButton1.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (causticsButton.getText().equals("Default Texture"))
                        waterFilter.setCausticsTexture((Texture2D) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Common/MatDefs/Water/Textures/caustics.jpg"));
                    else
                        waterFilter.setCausticsTexture((Texture2D) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(causticsButton.getText()));
                    if (foamButton.getText().equals("Default Texture"))
                        waterFilter.setFoamTexture((Texture2D) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Common/MatDefs/Water/Textures/foam.jpg"));
                    else
                        waterFilter.setFoamTexture((Texture2D) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(foamButton.getText()));
                    if (heightButton.getText().equals("Default Texture"))
                        waterFilter.setHeightTexture((Texture2D) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Common/MatDefs/Water/Textures/heightmap.jpg"));
                    else
                        waterFilter.setHeightTexture((Texture2D) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(heightButton.getText()));
                    if (normalButton.getText().equals("Default Texture"))
                        waterFilter.setNormalTexture((Texture2D) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Common/MatDefs/Water/Textures/water_normalmap.dds"));
                    else
                        waterFilter.setNormalTexture((Texture2D) CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(normalButton.getText()));
                    UAManager.add(waterFilter, "Edit Textures of " + waterFilter.getName());
                }
            });
            causticsButton.setPreferredSize(new Dimension(220, 20));
            foamButton.setPreferredSize(new Dimension(220, 20));
            heightButton.setPreferredSize(new Dimension(220, 20));
            normalButton.setPreferredSize(new Dimension(220, 20));
            causticsDefault = new DefaultButton(causticsButton);
            foamDefault = new DefaultButton(foamButton);
            heightDefault = new DefaultButton(heightButton);
            normalDefault = new DefaultButton(normalButton);
            setTitle("Textures");
            setLayout(new RiverLayout());
            add("left", new JLabel("Caustics Texture:"));
            add("tab", causticsButton);
            add("tab", causticsDefault);
            add("br left", new JLabel("Foam Texture:"));
            add("tab", foamButton);
            add("tab", foamDefault);
            add("br left", new JLabel("Height Texture:"));
            add("tab", heightButton);
            add("tab", heightDefault);
            add("br left", new JLabel("Normal Texture:"));
            add("tab", normalButton);
            add("tab", normalDefault);
            add("br right", applyButton1);
        }

        private class DefaultButton extends BButton
        {

            public DefaultButton(final AssetButton assetButton)
            {
                setText("Default");
                addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        if (assetButton.getAssetChooser() != null)
                            assetButton.getAssetChooser().setSelectedAssetName(null);
                        assetButton.setText("Default Texture");
                    }
                });
            }
        }
    }
}
