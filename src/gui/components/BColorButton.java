package gui.components;

import b3dElements.other.B3D_MotionEvent;
import components.ColorButton;
import other.Wizard;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A BButton, used to set the Color of an Object
 */
public class BColorButton extends ColorButton
{

    private Color color;

    public BColorButton(Color initColor)
    {
        super(initColor);
    }

    /**
     *
     * @param initColor
     * @param b3D_Spatial_MotionPath
     */
    public BColorButton(Color initColor, final B3D_MotionEvent b3D_Spatial_MotionPath)
    {
        super(initColor);
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                b3D_Spatial_MotionPath.getMotionPath().setColor(Wizard.makeColorRGBA(color));
            }
        });
    }
}
