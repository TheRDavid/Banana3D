package files;

import java.io.Serializable;

/**
 * Stores options of the autosave-process (path, minutesPerSave, enabled)
 * @author David
 */
public class AutosaveOptions implements Serializable
{

    private String path;
    private int minutesPerSave;
    private boolean enabled;

    /**
     * Disabled by default
     * @param filePath
     * @param minutes 
     */
    public AutosaveOptions(String filePath, int minutes)
    {
        path = filePath;
        minutesPerSave = minutes;
        enabled = false;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public int getMinutesPerSave()
    {
        return minutesPerSave;
    }

    public void setMinutesPerSave(int minutesPerSave)
    {
        this.minutesPerSave = minutesPerSave;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
