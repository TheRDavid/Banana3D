package gui.components;

import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

/**
 * This FileView highlights .b3ds files. That's it.
 * @author David
 */
public class B3DSFileView extends FileView
{

    private ImageIcon icon = null;
    private File[] allFiles = null;

    @Override
    public Icon getIcon(File f)
    {
        if (f.getAbsolutePath().endsWith(".b3ds"))
        {
            icon = new ImageIcon("dat//img//other//sceneIcon.png");
        } else
        {
            icon = null;
        }
        return icon;
    }
}