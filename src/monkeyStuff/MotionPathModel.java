package monkeyStuff;

import general.CurrentData;
import com.jme3.asset.AssetManager;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.util.concurrent.Callable;

public class MotionPathModel
{

    private MotionEvent motionEvent;
    private Geometry symbol;

    public MotionPathModel(MotionEvent mE)
    {
        motionEvent = mE;
        AssetManager assetManager = CurrentData.getEditorWindow().getB3DApp().getAssetManager();
        symbol = new Geometry("motionPathSymbol", new Box(1, 1, 1));
        symbol.setMaterial(new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"));
        symbol.setLocalTranslation(motionEvent.getPath().getWayPoint(0).add(0, 5, 0));
        symbol.getMaterial().setTexture("DiffuseMap", assetManager.loadTexture("Textures/mpIcon.PNG"));
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

    public MotionEvent getMotionEvent()
    {
        return motionEvent;
    }

    public Spatial getSymbol()
    {
        return symbol;
    }
}
