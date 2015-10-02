package monkeyStuff;

import general.CurrentData;
import com.jme3.material.Material;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import java.util.concurrent.Callable;

public class LightScatteringModel
{

    private LightScatteringFilter scatteringFilter;
    private Geometry symbol = new Geometry("LightScatteringSymbol", new Box(1, 1, 1));

    public LightScatteringModel(LightScatteringFilter l)
    {
        scatteringFilter = l;
        symbol.setLocalTranslation(scatteringFilter.getLightPosition());
        Material symbolMaterial = new Material(CurrentData.getEditorWindow().getB3DApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        symbolMaterial.setTexture("ColorMap", CurrentData.getEditorWindow().getB3DApp().getAssetManager().loadTexture("Textures/showLightScatteringFilterTexture.PNG"));
        symbol.setMaterial(symbolMaterial);
        CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                CurrentData.getEditorWindow().getB3DApp().getEditorNode().attachChild(symbol);
                return null;
            }
        });
    }

    public Geometry getSymbol()
    {
        return symbol;
    }

    /**
     *
     * @param symbol
     */
    public void setSymbol(Geometry symbol)
    {
        this.symbol = symbol;
    }

    public LightScatteringFilter getScatteringFilter()
    {
        return scatteringFilter;
    }

    /**
     *
     * @param scatteringFilter
     */
    public void setScatteringFilter(LightScatteringFilter scatteringFilter)
    {
        this.scatteringFilter = scatteringFilter;
    }
}
