package files;

import general.CurrentData;
import java.awt.Point;
import other.Wizard;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Stores all configurations of the editor
 *
 * @author David
 */
public class Configuration implements Serializable
{

    public ArrayList<String> recentProjectPaths = new ArrayList<String>();
    public boolean exitwithoutprompt = false, fullscreen = false, assetbrowsershown = true,
            showgrid = true, showscenery = false, showfilters = true, showwire = false,
            showallmotionpaths = false, remindOfNodeChildrenAsMotionEventSpatial = true,
            assetbrowserontop = true, animationDialogVisible = false, saveXML = true, vSync = false;
    public float camspeed = 100, gridgap = 20;
    public int gridx = 100, gridy = 100;
    public String treesort = "a-z(no_cs)";
    public int editorWidth = 1650, editorHeight = 960;
    public Point animationScriptDialogPosition = new Point(0, 0);
    public static final int SLOW_GUI = 250, DEFAULT_GUI = 100, FAST_GUI = 4;
    public int framerate = 100, guiSPeed = DEFAULT_GUI;
    public int colorDepth = 8, mutlisampling = 0, depthBits = 24;

    /**
     * Saves settings to the config-file
     */
    public void save()
    {
        Wizard.saveFile("dat//config.cfg", CurrentData.getConfiguration());
    }

    public void setEditorSize(int width, int height)
    {
        editorWidth = width;
        editorHeight = height;
        save();
    }

    /**
     *
     * @param entry
     */
    public void addRecentlyOpenedEntry(String entry)
    {
        if (recentProjectPaths.contains(entry))
            recentProjectPaths.remove(entry);
        recentProjectPaths.add(0, entry);
        if (recentProjectPaths.size() > 15)
        {
            recentProjectPaths.remove(recentProjectPaths.size() - 1);
        }
        save();
    }

    public void setExitwithoutprompt(boolean exitwithoutprompt)
    {
        this.exitwithoutprompt = exitwithoutprompt;
        save();
    }

    public void setvSync(boolean vSync)
    {
        this.vSync = vSync;
        save();
    }

    public void setMutlisampling(int mutlisampling)
    {
        this.mutlisampling = mutlisampling;
        save();
    }

    public void setDepthBits(int depthBits)
    {
        this.depthBits = depthBits;
        save();
    }

    public void setColorDepth(int colorDepth)
    {
        this.colorDepth = colorDepth;
        save();
    }

    public void setFullscreen(boolean fullscreen)
    {
        this.fullscreen = fullscreen;
        save();
    }

    public void setDefaultEditorSize(int width, int height)
    {
        this.editorHeight = height;
        this.editorWidth = width;
        save();
    }

    public void setFramerate(int framerate)
    {
        this.framerate = framerate;
        save();
    }

    public void setAssetbrowsershown(boolean assetbrowsershown)
    {
        this.assetbrowsershown = assetbrowsershown;
        save();
    }

    public void setShowgrid(boolean showgrid)
    {
        this.showgrid = showgrid;
        save();
    }

    public void setShowscenery(boolean showscenery)
    {
        this.showscenery = showscenery;
        save();
    }

    public void setShowfilters(boolean showfilters)
    {
        this.showfilters = showfilters;
        save();
    }

    public void setShowwire(boolean showwire)
    {
        this.showwire = showwire;
        save();
    }

    public void setShowallmotionpaths(boolean showallmotionpaths)
    {
        this.showallmotionpaths = showallmotionpaths;
        save();
    }

    public void setRemindOfNodeChildrenAsMotionEventSpatial(boolean remindOfNodeChildrenAsMotionEventSpatial)
    {
        this.remindOfNodeChildrenAsMotionEventSpatial = remindOfNodeChildrenAsMotionEventSpatial;
        save();
    }

    public void setAssetbrowserontop(boolean assetbrowserontop)
    {
        this.assetbrowserontop = assetbrowserontop;
        save();
    }

    public void setCamspeed(float camspeed)
    {
        this.camspeed = camspeed;
        save();
    }

    public void setGridgap(float gridgap)
    {
        this.gridgap = gridgap;
        save();
    }

    public void setGridx(int gridx)
    {
        this.gridx = gridx;
        save();
    }

    public void setGridy(int gridy)
    {
        this.gridy = gridy;
        save();
    }

    public void setTreesort(String treesort)
    {
        this.treesort = treesort;
        save();
    }

    public void setAnimationDialogVisible(boolean animationDialogVisible)
    {
        this.animationDialogVisible = animationDialogVisible;
        save();
    }

    public void setAnimationScriptDialogPosition(Point animationScriptDialogPosition)
    {
        this.animationScriptDialogPosition = animationScriptDialogPosition;
        save();
    }

    public void setGuiSPeed(int guiSPeed)
    {
        this.guiSPeed = guiSPeed;
        save();
    }
}