package gui.dialogs;

import b3dElements.B3D_Element;
import components.OKButton;
import dialogs.BasicDialog;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JList;
import javax.swing.JScrollPane;
import other.Wizard;

/**
 *
 * @author David
 */
public class SelectElementDialog extends BasicDialog implements ActionListener
{

    private JList<Object> list;
    private ArrayList<B3D_Element> elements = new ArrayList<B3D_Element>();
    private ArrayList<String> elementNames = new ArrayList<String>();
    private B3D_Element selectedElement;
    private OKButton selectButton = new OKButton("Select");

    public SelectElementDialog(Point p)
    {
        for (B3D_Element e : Wizard.getObjects().getB3D_ElementsIterator())
            elements.add(e);
        Collections.sort(elements, new Comparator<B3D_Element>()
        {
            public int compare(B3D_Element o1, B3D_Element o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (B3D_Element e : elements)
            elementNames.add(e.getName());
        list = new JList<Object>(elementNames.toArray());
        list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    if (list.getSelectedIndex() != -1)
                    {
                        selectedElement = elements.get(list.getSelectedIndex());
                        dispose();
                    }
                }
            }
        });
        add(new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);
        selectButton.addActionListener(this);
        setTitle("Select");
        setSize(400, 600);
        setLocation(p);
        setVisible(true);
    }

    public SelectElementDialog(Point p, ArrayList<B3D_Element> exclude)
    {
        for (B3D_Element e : Wizard.getObjects().getB3D_ElementsIterator())
        {
            boolean accept = true;
            for (B3D_Element e2 : exclude)
                if (e2.equals(e))
                {
                    accept = false;
                    break;
                }
            if (accept)
                elements.add(e);
        }
        Collections.sort(elements, new Comparator<B3D_Element>()
        {
            public int compare(B3D_Element o1, B3D_Element o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (B3D_Element e : elements)
            elementNames.add(e.getName());
        list = new JList<Object>(elementNames.toArray());
        list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    if (list.getSelectedIndex() != -1)
                    {
                        selectedElement = elements.get(list.getSelectedIndex());
                        dispose();
                    }
                }
            }
        });
        add(new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);
        selectButton.addActionListener(this);
        setTitle("Select");
        setSize(400, 600);
        setLocation(p);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        selectedElement = elements.get(list.getSelectedIndex());
        dispose();
    }

    public B3D_Element getSelectedElement()
    {
        return selectedElement;
    }
}
