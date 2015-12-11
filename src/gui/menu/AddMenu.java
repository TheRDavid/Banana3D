package gui.menu;

import b3dElements.filters.B3D_BasicSSAO;
import b3dElements.filters.B3D_Bloom;
import b3dElements.filters.B3D_Cartoon;
import b3dElements.filters.B3D_ColorScale;
import b3dElements.filters.B3D_Crosshatch;
import b3dElements.filters.B3D_DepthOfField;
import b3dElements.filters.B3D_Fog;
import b3dElements.filters.B3D_FrostedGlass;
import b3dElements.filters.B3D_LightScattering;
import b3dElements.filters.B3D_OldFilm;
import b3dElements.filters.B3D_Posterization;
import b3dElements.filters.B3D_SSAO;
import b3dElements.filters.B3D_Shadow;
import b3dElements.filters.B3D_Water;
import b3dElements.other.B3D_MotionEvent;
import b3dElements.other.B3D_MotionPath;
import gui.components.AssetButton;
import gui.dialogs.AssetChooserDialog;
import gui.dialogs.ChildMotionEventDialog;
import gui.dialogs.CreateParticleEmitterDialog;
import gui.dialogs.CreateSkyBoxDialog;
import general.CurrentData;
import monkeyStuff.LightScatteringModel;
import monkeyStuff.MotionPathModel;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.post.filters.CrossHatchFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.post.filters.PosterizationFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.shaderblow.filter.basicssao.BasicSSAO;
import com.shaderblow.filter.frostedglass.FrostedGlassFilter;
import com.shaderblow.filter.oldfilm.OldFilmFilter;
import com.simsilica.lemur.geom.MBox;
import dialogs.ObserverDialog;
import dialogs.SelectDialog;
import general.UAManager;
import other.Wizard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import javax.swing.*;
import monkeyStuff.ColorScaleFilterWithGetters;
import monkeyStuff.DirectionalLightShadowFilterWithGetters;
import other.ObjectToElementConverter;
import monkeyStuff.PointLightShadowFilterWithGetters;
import monkeyStuff.SpotLightShadowFilterWithGetters;
import monkeyStuff.WaterFilterWithGetters;

public class AddMenu extends JMenu
{

    private Object3DMenu object3DMenu = new Object3DMenu();
    private LightMenu lightMenu = new LightMenu();
    private FilterMenu filterMenu = new FilterMenu();
    private JMenuItem motionPathItem = new JMenuItem("Motion Path", new ImageIcon("dat//img//menu//motionPath.png"));

    public AddMenu()
    {
        setText("Add");
        add(object3DMenu);
        add(lightMenu);
        add(filterMenu);
        add(motionPathItem);
        motionPathItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UAManager.curr(null, null);
                b3dElements.B3D_Element element;
                ArrayList<String> objects = new ArrayList<String>();
                objects.add("Camera");
                Vector<Spatial> allSpatials = new Vector<Spatial>();
                for (Object o : Wizard.getObjects().getOriginalObjectsIterator())
                    if (o instanceof Spatial)
                        allSpatials.add((Spatial) o);
                Collections.sort(allSpatials, new Comparator<Spatial>()
                {
                    @Override
                    public int compare(Spatial o1, Spatial o2)
                    {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (Spatial spatial : allSpatials)
                    objects.add(spatial.getName());
                SelectDialog selectDialog = new SelectDialog("Moving Object", objects.toArray());
                if (selectDialog.isOk())
                {
                    MotionEvent motionEvent = null;
                    if (selectDialog.getSelectedIndex() == 0)
                    {
                        motionEvent = new MotionEvent(CurrentData.getEditorWindow().getB3DApp().getCamNode(), new MotionPath());
                        motionEvent.setLoopMode(LoopMode.DontLoop);
                        element = new B3D_MotionEvent(
                                "Camera Motion Path",
                                B3D_MotionEvent.Cam.CAM_ID,
                                new B3D_MotionPath(
                                null,
                                motionEvent.getPath().getCurveTension(),
                                motionEvent.getSpeed(),
                                ColorRGBA.Gray,
                                motionEvent.getPath().isCycle(),
                                motionEvent.getRotation(),
                                motionEvent.getDirectionType(),
                                motionEvent.getLoopMode()));
                    } else
                    {
                        motionEvent = new MotionEvent(allSpatials.get(selectDialog.getSelectedIndex() - 1), new MotionPath());
                        motionEvent.setLoopMode(LoopMode.DontLoop);
                        UUID elementUUID = Wizard.getObjectReferences().getUUID(allSpatials.get(selectDialog.getSelectedIndex() - 1).hashCode());
                        element = new B3D_MotionEvent(
                                allSpatials.get(selectDialog.getSelectedIndex() - 1).getName() + " Motion Path",
                                elementUUID,
                                new B3D_MotionPath(
                                null,
                                motionEvent.getPath().getCurveTension(),
                                motionEvent.getSpeed(),
                                ColorRGBA.Green,
                                motionEvent.getPath().isCycle(),
                                motionEvent.getRotation(),
                                motionEvent.getDirectionType(),
                                motionEvent.getLoopMode()));
                        allSpatials.get(selectDialog.getSelectedIndex() - 1).setUserData("motionEventName", element.getName());
                        allSpatials.get(selectDialog.getSelectedIndex() - 1).setUserData("motionEventLookAtName", element.getName());
                        if (CurrentData.getConfiguration().remindOfNodeChildrenAsMotionEventSpatial
                                && !allSpatials.get(selectDialog.getSelectedIndex() - 1).getParent().equals(
                                CurrentData.getEditorWindow().getB3DApp().getSceneNode()))
                            new ChildMotionEventDialog();
                    }
                    motionEvent.getPath().addWayPoint(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation());
                    motionEvent.getPath().addWayPoint(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(5, 0, 0));
                    Wizard.getObjects().add(motionEvent, element);
                    motionEvent.getPath().setPathSplineType(Spline.SplineType.CatmullRom);
                    final MotionPathModel mpm = new MotionPathModel(motionEvent);
                    CurrentData.getEditorWindow().getB3DApp().getMotionPathModels().add(mpm);
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            CurrentData.getEditorWindow().getB3DApp().getEditorNode().attachChild(mpm.getSymbol());
                            return null;
                        }
                    });
                    CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                    CurrentData.getEditorWindow().getB3DApp().updateSelectedMotionPath();
                    UAManager.add(motionEvent, "Add Motion Event");
                }
            }
        });
    }

    public class FilterMenu extends JMenu
    {

        private JMenuItem basicSSAOItem = new JMenuItem("Basic SSAO");
        private JMenuItem bloomItem = new JMenuItem("Bloom");
        private JMenuItem cartoonItem = new JMenuItem("Cartoon");
        private JMenuItem colorScaleItem = new JMenuItem("Color Scale");
        private JMenuItem crossHatchItem = new JMenuItem("Crosshatch");
        private JMenuItem dofItem = new JMenuItem("Depth of Field");
        private JMenuItem fogItem = new JMenuItem("Fog");
        private JMenuItem frostedGlassItem = new JMenuItem("Frosted Glass");
        private JMenuItem lightScatteringItem = new JMenuItem("Light Scattering");
        private JMenuItem oldFilmItem = new JMenuItem("Old Film");
        private JMenuItem posterizationItem = new JMenuItem("Posterization");
        private JMenu shadowMenu = new JMenu("Shadow");
        private JMenuItem dlShadowItem = new JMenuItem("Directionallight Shadow");
        private JMenuItem plShadowItem = new JMenuItem("Pointlight Shadow");
        private JMenuItem slShadowItem = new JMenuItem("Spotlight Shadow");
        private JMenuItem ssaoItem = new JMenuItem("SSAO");
        private JMenuItem waterItem = new JMenuItem("Water");

        public FilterMenu()
        {
            setText("Filter");
            setIcon(new ImageIcon("dat//img//menu//filter.png"));
            add(basicSSAOItem);
            add(bloomItem);
            add(cartoonItem);
            add(colorScaleItem);
            add(crossHatchItem);
            add(dofItem);
            add(fogItem);
            add(frostedGlassItem);
            add(lightScatteringItem);
            add(oldFilmItem);
            add(posterizationItem);
            add(shadowMenu);
            shadowMenu.add(dlShadowItem);
            shadowMenu.add(plShadowItem);
            shadowMenu.add(slShadowItem);
            add(ssaoItem);
            add(waterItem);
            initActions();
        }

        private void initActions()
        {
            basicSSAOItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            BasicSSAO basicSSAO = new BasicSSAO();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_BasicSSAO b3D_BasicSSAO = ObjectToElementConverter.convertBasicSSAO(
                                    basicSSAO, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(basicSSAO);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(basicSSAO, b3D_BasicSSAO);
                            UAManager.add(basicSSAO, "Add Basic SSAO");
                            return null;
                        }
                    });
                }
            });
            oldFilmItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    UAManager.curr(null, null);
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            OldFilmFilter oldFilmFilter = new OldFilmFilter();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_OldFilm b3D_OldFilm = ObjectToElementConverter.convertOldFilm(
                                    oldFilmFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(oldFilmFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(oldFilmFilter, b3D_OldFilm);
                            UAManager.add(oldFilmFilter, "Add Old Film");
                            return null;
                        }
                    });
                }
            });
            colorScaleItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            ColorScaleFilterWithGetters csfwg = new ColorScaleFilterWithGetters();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_ColorScale b3D_ColorScale = ObjectToElementConverter.convertColorScale(
                                    csfwg, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(csfwg);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(csfwg, b3D_ColorScale);
                            UAManager.add(colorScaleItem, "Add Color Scale");
                            return null;
                        }
                    });
                }
            });
            frostedGlassItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            FrostedGlassFilter frostedGlassFilter = new FrostedGlassFilter();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_FrostedGlass b3D_FrostedGlass = ObjectToElementConverter.convertFrostedGlass(
                                    frostedGlassFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(frostedGlassFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(frostedGlassFilter, b3D_FrostedGlass);
                            UAManager.add(frostedGlassFilter, "Add Frosted Glass");
                            return null;
                        }
                    });
                }
            });
            dlShadowItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Vector<String> names = new Vector<String>();
                    Vector<DirectionalLight> lights = new Vector<DirectionalLight>();
                    for (Light l : CurrentData.getEditorWindow().getB3DApp().getSceneNode().getWorldLightList())
                        if (l instanceof DirectionalLight)
                        {
                            names.add(l.getName());
                            lights.add((DirectionalLight) l);
                        }
                    if (!names.isEmpty())
                    {
                        SelectDialog selectResolutionDialog = new SelectDialog("Shadow Resolution", new String[]
                        {
                            "64", "128", "256", "512", "1024", "2048"
                        });
                        final DirectionalLightShadowFilterWithGetters shadowFilter = new DirectionalLightShadowFilterWithGetters(
                                CurrentData.getEditorWindow().getB3DApp().getAssetManager(),
                                Integer.parseInt(selectResolutionDialog.getSelectedValue()),
                                3);
                        Collections.sort(names, new Comparator<String>()
                        {
                            @Override
                            public int compare(String o1, String o2)
                            {
                                return o1.compareTo(o2);
                            }
                        });
                        Collections.sort(lights, new Comparator<DirectionalLight>()
                        {
                            @Override
                            public int compare(DirectionalLight o1, DirectionalLight o2)
                            {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        SelectDialog selectDialog = new SelectDialog("Select Light Source", names.toArray());
                        final DirectionalLight selectedLight = lights.get(selectDialog.getSelectedIndex());
                        shadowFilter.setLight(selectedLight);
                        shadowFilter.setName(selectedLight.getName() + " Shadow");
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                        {
                            @Override
                            public Integer call() throws Exception
                            {
                                UAManager.curr(null, null);
                                //ConvertMode does not matter here, the filter can not possibly have a LightControl
                                B3D_Shadow b3D_Shadow = ObjectToElementConverter.convertDirectionalLightShadow(
                                        shadowFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                                CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(shadowFilter);
                                CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                                Wizard.getObjects().add(shadowFilter, b3D_Shadow);
                                UAManager.add(shadowFilter, "Add Shadow");
                                return null;
                            }
                        });
                    } else
                        JOptionPane.showMessageDialog(CurrentData.getEditorWindow(), "You need a Directional Light for this");
                }
            });
            plShadowItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Vector<String> names = new Vector<String>();
                    Vector<PointLight> lights = new Vector<PointLight>();
                    for (Light l : CurrentData.getEditorWindow().getB3DApp().getSceneNode().getWorldLightList())
                        if (l instanceof PointLight)
                        {
                            names.add(l.getName());
                            lights.add((PointLight) l);
                        }
                    if (!names.isEmpty())
                    {
                        SelectDialog selectResolutionDialog = new SelectDialog("Shadow Resolution", new String[]
                        {
                            "64", "128", "256", "512", "1024", "2048"
                        });
                        final PointLightShadowFilterWithGetters shadowFilter = new PointLightShadowFilterWithGetters(
                                CurrentData.getEditorWindow().getB3DApp().getAssetManager(),
                                Integer.parseInt(selectResolutionDialog.getSelectedValue()));
                        Collections.sort(names, new Comparator<String>()
                        {
                            @Override
                            public int compare(String o1, String o2)
                            {
                                return o1.compareTo(o2);
                            }
                        });
                        Collections.sort(lights, new Comparator<PointLight>()
                        {
                            @Override
                            public int compare(PointLight o1, PointLight o2)
                            {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        SelectDialog selectDialog = new SelectDialog("Select Light Source", names.toArray());
                        final PointLight selectedLight = lights.get(selectDialog.getSelectedIndex());
                        shadowFilter.setLight(selectedLight);
                        shadowFilter.setName(selectedLight.getName() + " Shadow");
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                        {
                            @Override
                            public Integer call() throws Exception
                            {
                                UAManager.curr(null, null);
                                //ConvertMode does not matter here, the filter can not possibly have a LightControl
                                B3D_Shadow b3D_Shadow = ObjectToElementConverter.convertPointLightShadow(
                                        shadowFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                                CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(shadowFilter);
                                CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                                Wizard.getObjects().add(shadowFilter, b3D_Shadow);
                                UAManager.add(shadowFilter, "Add Shadow");
                                return null;
                            }
                        });
                    } else
                        JOptionPane.showMessageDialog(CurrentData.getEditorWindow(), "You need a Point Light for this");
                }
            });
            slShadowItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Vector<String> names = new Vector<String>();
                    Vector<SpotLight> lights = new Vector<SpotLight>();
                    for (Light l : CurrentData.getEditorWindow().getB3DApp().getSceneNode().getWorldLightList())
                    {
                        if (l instanceof SpotLight)
                        {
                            names.add(l.getName());
                            lights.add((SpotLight) l);
                        }
                    }
                    if (!names.isEmpty())
                    {
                        SelectDialog selectResolutionDialog = new SelectDialog("Shadow Resolution", new String[]
                        {
                            "64", "128", "256", "512", "1024", "2048"
                        });
                        final SpotLightShadowFilterWithGetters shadowFilter = new SpotLightShadowFilterWithGetters(
                                CurrentData.getEditorWindow().getB3DApp().getAssetManager(),
                                Integer.parseInt(selectResolutionDialog.getSelectedValue()));
                        Collections.sort(names, new Comparator<String>()
                        {
                            @Override
                            public int compare(String o1, String o2)
                            {
                                return o1.compareTo(o2);
                            }
                        });
                        Collections.sort(lights, new Comparator<SpotLight>()
                        {
                            @Override
                            public int compare(SpotLight o1, SpotLight o2)
                            {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        SelectDialog selectDialog = new SelectDialog("Select Light Source", names.toArray());
                        final SpotLight selectedLight = lights.get(selectDialog.getSelectedIndex());
                        shadowFilter.setLight(selectedLight);
                        shadowFilter.setName(selectedLight.getName() + " Shadow");
                        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                        {
                            @Override
                            public Integer call() throws Exception
                            {
                                UAManager.curr(null, null);
                                //ConvertMode does not matter here, the filter can not possibly have a LightControl
                                B3D_Shadow b3D_Shadow = ObjectToElementConverter.convertSpotLightShadow(
                                        shadowFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                                CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(shadowFilter);
                                CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                                Wizard.getObjects().add(shadowFilter, b3D_Shadow);
                                UAManager.add(shadowFilter, "Add Shadow");
                                return null;
                            }
                        });
                    } else
                        JOptionPane.showMessageDialog(CurrentData.getEditorWindow(), "You need a Spot Light for this");
                }
            });
            bloomItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            BloomFilter bloomFilter = new BloomFilter();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_Bloom b3D_Bloom = ObjectToElementConverter.convertBloom(
                                    bloomFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(bloomFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(bloomFilter, b3D_Bloom);
                            UAManager.add(bloomFilter, "Add Bloom");
                            return null;
                        }
                    });
                }
            });
            cartoonItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            CartoonEdgeFilter cartoonFilter = new CartoonEdgeFilter();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_Cartoon b3D_Cartoon = ObjectToElementConverter.convertCartoon(
                                    cartoonFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(cartoonFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(cartoonFilter, b3D_Cartoon);
                            UAManager.add(cartoonFilter, "Add Cartoon Edges");
                            return null;
                        }
                    });
                }
            });
            crossHatchItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            CrossHatchFilter crossHatchFilter = new CrossHatchFilter();
                            Float[] llevels = new Float[5];
                            llevels[0] = crossHatchFilter.getLuminance1();
                            llevels[1] = crossHatchFilter.getLuminance2();
                            llevels[2] = crossHatchFilter.getLuminance3();
                            llevels[3] = crossHatchFilter.getLuminance4();
                            llevels[4] = crossHatchFilter.getLuminance5();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_Crosshatch b3D_Crosshatch = ObjectToElementConverter.convertCrosshatch(
                                    crossHatchFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(crossHatchFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(crossHatchFilter, b3D_Crosshatch);
                            UAManager.add(crossHatchFilter, "Add Crosshatch");
                            return null;
                        }
                    });
                }
            });
            dofItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            DepthOfFieldFilter depthOfFieldFilter = new DepthOfFieldFilter();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_DepthOfField b3D_DepthOfField = ObjectToElementConverter.convertDOF(
                                    depthOfFieldFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(depthOfFieldFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(depthOfFieldFilter, b3D_DepthOfField);
                            UAManager.add(depthOfFieldFilter, "Add Depth of Field");
                            return null;
                        }
                    });
                }
            });
            fogItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            FogFilter fogFilter = new FogFilter();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_Fog b3D_Fog = ObjectToElementConverter.convertFog(
                                    fogFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(fogFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(fogFilter, b3D_Fog);
                            UAManager.add(fogFilter, "Add Fog");
                            return null;
                        }
                    });
                }
            });
            lightScatteringItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            LightScatteringFilter scatteringFilter = new LightScatteringFilter(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().clone());
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_LightScattering b3D_LightScattering = ObjectToElementConverter.convertLightScattering(
                                    scatteringFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getLightScatteringModels().add(new LightScatteringModel(scatteringFilter));
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(scatteringFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(scatteringFilter, b3D_LightScattering);
                            UAManager.add(scatteringFilter, "Add Light Scattering");
                            return null;
                        }
                    });
                }
            });
            posterizationItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            PosterizationFilter posterizationFilter = new PosterizationFilter();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_Posterization b3D_Posterization = ObjectToElementConverter.convertPosterization(
                                    posterizationFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(posterizationFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(posterizationFilter, b3D_Posterization);
                            UAManager.add(posterizationFilter, "Add Posterization");
                            return null;
                        }
                    });
                }
            });
            ssaoItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            SSAOFilter ssaoFilter = new SSAOFilter();
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_SSAO b3d_ssao = ObjectToElementConverter.convertSSAO(
                                    ssaoFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(ssaoFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(ssaoFilter, b3d_ssao);
                            UAManager.add(ssaoFilter, "Add SSAO");
                            return null;
                        }
                    });
                }
            });
            waterItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            WaterFilterWithGetters waterFilter = new WaterFilterWithGetters(CurrentData.getEditorWindow().getB3DApp().getSceneNode(), Vector3f.ZERO);
                            //ConvertMode does not matter here, the filter can not possibly have a LightControl
                            B3D_Water b3d_water = ObjectToElementConverter.convertWater(
                                    waterFilter, CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());
                            CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().addFilter(waterFilter);
                            CurrentData.getEditorWindow().getB3DApp().setSyncTree(true);
                            Wizard.getObjects().add(waterFilter, b3d_water);
                            UAManager.add(waterFilter, "Add Water");
                            return null;
                        }
                    });
                }
            });
        }
    }

    public class LightMenu extends JMenu
    {

        private JMenuItem ambientItem = new JMenuItem("Ambient Light", new ImageIcon("dat//img//menu//light.png"));
        private JMenuItem directionalItem = new JMenuItem("Directional Light", new ImageIcon("dat//img//menu//directionalLight.png"));
        private JMenuItem pointItem = new JMenuItem("Point Light", new ImageIcon("dat//img//menu//pointLight.png"));
        private JMenuItem spotItem = new JMenuItem("Spot Light", new ImageIcon("dat//img//menu//spotLight.png"));

        public LightMenu()
        {
            setText("Light");
            setIcon(new ImageIcon("dat//img//menu//light.png"));
            add(ambientItem);
            add(directionalItem);
            add(pointItem);
            add(spotItem);
            initActions();
        }

        private void initActions()
        {
            ambientItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            AmbientLight tempLight = new AmbientLight();
                            tempLight.setName("Ambient");
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().addLight(tempLight);
                            //ConvertMode does not matter here, the light can not possibly have a LightControl
                            Wizard.getObjects().add(tempLight, ObjectToElementConverter.convertLight(tempLight));
                            UAManager.add(tempLight, "Add Ambient Light");
                            return null;
                        }
                    });
                }
            });
            directionalItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            DirectionalLight tempLight = new DirectionalLight();
                            tempLight.setDirection(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection());
                            tempLight.setName("Directional");
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().addLight(tempLight);
                            //ConvertMode does not matter here, the light can not possibly have a LightControl
                            Wizard.getObjects().add(tempLight, ObjectToElementConverter.convertLight(tempLight));
                            UAManager.add(tempLight, "Add Directional Light");
                            return null;
                        }
                    });
                }
            });
            pointItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            PointLight tempLight = new PointLight();
                            tempLight.setPosition(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation());
                            tempLight.setRadius(10);
                            tempLight.setName("Point");
                            //ConvertMode does not matter here, the light can not possibly have a LightControl
                            Wizard.getObjects().add(tempLight, ObjectToElementConverter.convertLight(tempLight));
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().addLight(tempLight);
                            UAManager.add(tempLight, "Add Point Light");
                            return null;
                        }
                    });
                }
            });
            spotItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            SpotLight tempLight = new SpotLight();
                            tempLight.setPosition(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation());
                            tempLight.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD);
                            tempLight.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD);
                            tempLight.setSpotRange(100);
                            tempLight.setDirection(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection());
                            tempLight.setName("Spot");
                            //ConvertMode does not matter here, the light can not possibly have a LightControl
                            Wizard.getObjects().add(tempLight, ObjectToElementConverter.convertLight(tempLight));
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().addLight(tempLight);
                            UAManager.add(tempLight, "Add Spot Light");
                            return null;
                        }
                    });
                }
            });
        }
    }

    public class Object3DMenu extends JMenu
    {

        private JMenuItem nodeItem = new JMenuItem("Node", new ImageIcon("dat//img//menu//node.png"));
        private JMenu meshMenu = new JMenu("Mesh");
        private JMenuItem boxItem = new JMenuItem("Box", new ImageIcon("dat//img//menu//box.png"));
        private JMenuItem cylinderItem = new JMenuItem("Cylinder", new ImageIcon("dat//img//menu//cylinder.png"));
        private JMenuItem sphereItem = new JMenuItem("Sphere", new ImageIcon("dat//img//menu//circle.png"));
        private JMenuItem torusItem = new JMenuItem("Torus", new ImageIcon("dat//img//menu//torus.png"));
        private JMenuItem modelItem = new JMenuItem("Model", new ImageIcon("dat//img//menu//assets.png"));
        private JMenuItem particleEmitterItem = new JMenuItem("Particle Effect", new ImageIcon("dat//img//menu//effect.png"));
        private JMenuItem heightmapItem = new JMenuItem("Heightmap", new ImageIcon("dat//img//menu//terrain.png"));
        private JMenuItem skyBoxMenuItem = new JMenuItem("Sky Box", new ImageIcon("dat//img//menu//skybox.png"));

        public Object3DMenu()
        {
            meshMenu.setIcon(new ImageIcon("dat//img//menu//mesh.png"));
            setText("3D Object");
            setIcon(new ImageIcon("dat//img//menu//box.png"));
            add(nodeItem);
            add(meshMenu);
            meshMenu.add(boxItem);
            meshMenu.add(cylinderItem);
            meshMenu.add(sphereItem);
            meshMenu.add(torusItem);
            add(modelItem);
            add(particleEmitterItem);
            add(heightmapItem);
            add(skyBoxMenuItem);
            initActions();
        }

        private void initActions()
        {
            heightmapItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final String name = new AssetChooserDialog(AssetButton.AssetType.Texture, true).getSelectedAssetName();
                    if (name != null)
                    {
                        final int selection = JOptionPane.showOptionDialog(
                                object3DMenu,
                                "Should this object only link to the Heightmap-Image or store the actual values?",
                                "Select", JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new String[]
                        {
                            "Only link to asset file", "Store the height values"
                        },
                                "Only link to asset file");
                        if (selection != JOptionPane.CLOSED_OPTION)
                            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                            {
                                public Void call() throws Exception
                                {
                                    UAManager.curr(null, null);
                                    Texture heightMapImage = CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture(name);
                                    ImageBasedHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
                                    heightmap.load();
                                    TerrainQuad terrain = new TerrainQuad("Terrain", 65, heightmap.getSize() + 1, heightmap.getHeightMap());
                                    terrain.setUserData("angles", new Vector3f());
                                    terrain.setUserData("scale", new Vector3f(1, 1, 1));
                                    terrain.setMaterial(new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(),
                                            "Common/MatDefs/Terrain/TerrainLighting.j3md"));
                                    if (selection == 0)
                                        terrain.setUserData("heightmapLink", name);
                                    b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(terrain);
                                    Wizard.getObjects().add(terrain, element);
                                    CurrentData.getEditorWindow().getB3DApp().getSelectedNode().attachChild(terrain);
                                    UAManager.add(terrain, "Add Terrain");
                                    return null;
                                }
                            });
                    }
                }
            });
            nodeItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            UAManager.curr(null, null);
                            Node node = new Node("Node");
                            node.setUserData("angles", new Vector3f());
                            node.setUserData("scale", new Vector3f(1, 1, 1));
                            if (CurrentData.getEditorWindow().getB3DApp().getSelectedNode().equals(CurrentData.getEditorWindow().getB3DApp().getSceneNode()))
                                node.setLocalTranslation(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(10)));
                            //ConvertMode does not matter here, this spatial can not possibly have a LightControl already
                            b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(node);
                            Wizard.getObjects().add(node, element);
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(node);
                            UAManager.add(node, "Add Node");
                            return null;
                        }
                    });
                }
            });
            modelItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final String name = new AssetChooserDialog(AssetButton.AssetType.Model, true).getSelectedAssetName();
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            try
                            {
                                UAManager.curr(null, null);
                                /*Model itself*/
                                Spatial s = CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadModel(name);
                                s.setUserData("angles", new Vector3f());
                                s.setUserData("scale", new Vector3f(1, 1, 1));
                                s.setUserData("modelName", name);
                                s.setName(name);
                                if (CurrentData.getEditorWindow().getB3DApp().getSelectedNode().equals(CurrentData.getEditorWindow().getB3DApp().getSceneNode()))
                                    s.setLocalTranslation(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(10)));
                                /*Set UserData to all children*/
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
                                //ConvertMode does not matter here, this spatial can not possibly have a LightControl already
                                b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(s);
                                Wizard.getObjects().add(s, element);
                                //Just use hashcode s.setUserData("ID", element.getUUID());
                                CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(s);
                                UAManager.add(s, "Add " + name);
                            } catch (java.lang.NullPointerException npe)
                            {
                                System.err.println(npe);
                                ObserverDialog.getObserverDialog().printMessage("Fail: NullPointerException -> Trying to add Model: " + name);
                            }
                            return null;
                        }
                    });
                }
            });
            particleEmitterItem.addActionListener(
                    new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new CreateParticleEmitterDialog();
                }
            });
            skyBoxMenuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    new CreateSkyBoxDialog();
                }
            });
            boxItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            /*Lemur*/
                            UAManager.curr(null, null);
                            Geometry g = new Geometry("Box", new MBox(1, 1, 1f, 1, 1, 1));
                            g.setUserData("xSlices", 1);
                            g.setUserData("ySlices", 1);
                            g.setUserData("zSlices", 1);
                            g.setUserData("angles", new Vector3f());
                            g.setUserData("scale", new Vector3f(1, 1, 1));
                            if (CurrentData.getEditorWindow().getB3DApp().getSelectedNode().equals(CurrentData.getEditorWindow().getB3DApp().getSceneNode()))
                                g.setLocalTranslation(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(10)));
                            g.setMaterial(new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
                            //ConvertMode does not matter here, this spatial can not possibly have a LightControl already
                            b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(g);
                            Wizard.getObjects().add(g, element);
                            //Just use hashcode g.setUserData("ID", element.getUUID());
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(g);
                            UAManager.add(g, "Add Box");
                            return null;
                        }
                    });
                }
            });
            sphereItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            Geometry g = new Geometry("Sphere", new Sphere(10, 10, 1));
                            g.setUserData("angles", new Vector3f());
                            g.setUserData("scale", new Vector3f(1, 1, 1));
                            if (CurrentData.getEditorWindow().getB3DApp().getSelectedNode().equals(CurrentData.getEditorWindow().getB3DApp().getSceneNode()))
                                g.setLocalTranslation(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(10)));
                            g.setMaterial(new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
                            //ConvertMode does not matter here, this spatial can not possibly have a LightControl already
                            b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(g);
                            Wizard.getObjects().add(g, element);
                            //Just use hashcode g.setUserData("ID", element.getUUID());
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(g);
                            UAManager.add(g, "Add Sphere");
                            return null;
                        }
                    });
                }
            });
            cylinderItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            Geometry g = new Geometry("Cylinder", new Cylinder(10, 10, 1, 1, 1, true, false));
                            g.setUserData("angles", new Vector3f());
                            g.setUserData("scale", new Vector3f(1, 1, 1));
                            if (CurrentData.getEditorWindow().getB3DApp().getSelectedNode().equals(CurrentData.getEditorWindow().getB3DApp().getSceneNode()))
                                g.setLocalTranslation(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(10)));
                            g.setMaterial(new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
                            //ConvertMode does not matter here, this spatial can not possibly have a LightControl already
                            b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(g);
                            Wizard.getObjects().add(g, element);
                            //Just use hashcode g.setUserData("ID", element.getUUID());
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(g);
                            UAManager.add(g, "Add Cylinder");
                            return null;
                        }
                    });
                }
            });
            torusItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws Exception
                        {
                            UAManager.curr(null, null);
                            Geometry g = new Geometry("Torus", new Torus(10, 10, 1, 2));
                            g.setUserData("angles", new Vector3f());
                            g.setUserData("scale", new Vector3f(1, 1, 1));
                            if (CurrentData.getEditorWindow().getB3DApp().getSelectedNode().equals(CurrentData.getEditorWindow().getB3DApp().getSceneNode()))
                                g.setLocalTranslation(CurrentData.getEditorWindow().getB3DApp().getCamera().getLocation().add(CurrentData.getEditorWindow().getB3DApp().getCamera().getDirection().mult(10)));
                            g.setMaterial(new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
                            //ConvertMode does not matter here, this spatial can not possibly have a LightControl already
                            b3dElements.B3D_Element element = ObjectToElementConverter.convertToElement(g);
                            Wizard.getObjects().add(g, element);
                            //Just use hashcode  g.setUserData("ID", element.getUUID());
                            CurrentData.getEditorWindow().getB3DApp().getSceneNode().attachChild(g);
                            UAManager.add(g, "Add Torus");
                            return null;
                        }
                    });
                }
            });
        }
    }

    public Object3DMenu getObject3DMenu()
    {
        return object3DMenu;
    }
}
