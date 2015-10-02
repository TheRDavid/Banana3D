package files;

import java.io.Serializable;

/**
 * A file storing all texture references so it can be loaded again
 *
 * @author David
 */
public class SkyFile implements Serializable
{

    private String northAsset, southAsset, westAsset, eastAsset, topAsset, bottomAsset;

    /**
     * Initializing by setting all the textures
     *
     * @param north
     * @param south
     * @param west
     * @param east
     * @param top
     * @param bottom
     */
    public SkyFile(String north, String south, String west, String east, String top, String bottom)
    {
        northAsset = north;
        southAsset = south;
        westAsset = west;
        eastAsset = east;
        topAsset = top;
        bottomAsset = bottom;
    }

    public String getNorthAsset()
    {
        return northAsset;
    }

    public void setNorthAsset(String northAsset)
    {
        this.northAsset = northAsset;
    }

    public String getSouthAsset()
    {
        return southAsset;
    }

    public void setSouthAsset(String southAsset)
    {
        this.southAsset = southAsset;
    }

    public String getWestAsset()
    {
        return westAsset;
    }

    public void setWestAsset(String westAsset)
    {
        this.westAsset = westAsset;
    }

    public String getEastAsset()
    {
        return eastAsset;
    }

    public void setEastAsset(String eastAsset)
    {
        this.eastAsset = eastAsset;
    }

    public String getTopAsset()
    {
        return topAsset;
    }

    public void setTopAsset(String topAsset)
    {
        this.topAsset = topAsset;
    }

    public String getBottomAsset()
    {
        return bottomAsset;
    }

    public void setBottomAsset(String bottomAsset)
    {
        this.bottomAsset = bottomAsset;
    }
}
