package general;

import b3dElements.B3D_Element;
import b3dElements.filters.B3D_Filter;
import java.awt.Toolkit;
import java.io.Serializable;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;
import other.ElementToObjectConverter;
import other.ObjectToElementConverter;
import other.Wizard;

/**
 *
 * @author David
 */
public class UAManager implements Serializable
{

    private static B3D_Element current;
    private static Stack<UserAction> undoStack = new Stack<UserAction>(), redoStack = new Stack<UserAction>();

    public static void curr(Object o, UUID uuid)
    {
        if (o != null)
        {
            current = ObjectToElementConverter.convertToElement(o);
            current.setUuid(uuid);
        } else
            current = null;
    }

    public static void add(Object o, String name)
    {
        ObjectToElementConverter.convertMode = ObjectToElementConverter.ConvertMode.SAVING;
        //System.out.println("addState [ " + name + " ] \t(" + o + ")");
        redoStack.clear();
        B3D_Element e = null;
        if (o != null)
            e = ObjectToElementConverter.convertToElement(o);
        if (current != null && e != null)
            e.setUuid(current.getUUID());
        else if (e != null)
            e.setUuid(Wizard.getObjectReferences().getUUID(o.hashCode()));
        undoStack.push(new UserAction(current, e, name));
        if (e != null)
            curr(o, e.getUUID());
        else
            curr(o, null);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setEnabled(true);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setText("Undo: " + name);
    }

    public static void undo()
    {
        if (undoStack.isEmpty())
        {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        update(redoStack.push(undoStack.pop()), 0);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setEnabled(true);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setText("Redo: " + redoStack.peek().name);
        if (undoStack.isEmpty())
        {
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setEnabled(false);
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setText("Undo");
        } else
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setText("Undo: " + undoStack.peek().name);
    }

    public static void redo()
    {
        if (redoStack.isEmpty())
        {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        update(undoStack.push(redoStack.pop()), 1);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setEnabled(true);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setText(undoStack.peek().name);
        //Enable Undo?
        if (redoStack.isEmpty())
        {
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setEnabled(false);
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setText("Redo");
        } else
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setText(redoStack.peek().name);
    }

    // 0 = undo, 1 = redo
    private static void update(UserAction ua, int direction)
    {
        ObjectToElementConverter.convertMode = ObjectToElementConverter.ConvertMode.SAVING;
        final B3D_Element primaryElement = (direction == 0) ? ua.before : ua.after, secondaryElement = (direction == 1) ? ua.before : ua.after;
        if (primaryElement != null)
        {
            Object newObject = ElementToObjectConverter.convertToObject(primaryElement);
            if (secondaryElement == null)
            {
                // Recover Element
                //System.out.println("UPDATE RECOVER");
                if (primaryElement instanceof B3D_Filter)
                    ((B3D_Filter) primaryElement).changeFilterIndex(CurrentData.getEditorWindow().getB3DApp().getFilterPostProcessor().getFilterList().size());

                CurrentData.addToScene(newObject, primaryElement);
                CurrentData.getEditorWindow().getB3DApp().setSelectedUUID(primaryElement.getUUID());
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    public Void call() throws Exception
                    {
                        CurrentData.getEditorWindow().getEditPane().arrange(true);
                        return null;
                    }
                });
            } else
            {
                // Update Element
                final B3D_Element oldElement = Wizard.getObjects().getB3D_Element(primaryElement.getUUID());
                oldElement.set(primaryElement);
                CurrentData.getEditorWindow().getB3DApp().setSelectedUUID(oldElement.getUUID());
                Object oldObject = Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(oldElement.getUUID()));
                Wizard.completelyCopyValues(newObject, oldObject);
                CurrentData.getEditorWindow().getTree().sync();
            }
        } else if (secondaryElement != null)
        {
            //Delete Element
            //System.out.println("UPDATE DELETE");
            CurrentData.getEditorWindow().getB3DApp().setSelectedUUID(((direction == 1) ? ua.before : ua.after).getUUID());
            CurrentData.execDelete(false);
            CurrentData.getEditorWindow().getB3DApp().setSelectedUUID(null);
            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
            {
                public Void call() throws Exception
                {
                    CurrentData.getEditorWindow().getEditPane().arrange(true);
                    return null;
                }
            });
        }
    }

    static void reset()
    {
        current = null;
        undoStack.clear();
        redoStack.clear();
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setEnabled(false);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setText("Redo");
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setEnabled(false);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setText("Undo");
    }

    public static class UserAction<T extends B3D_Element> implements Serializable
    {

        private T before, after;
        private String name;

        public UserAction(T before, T after, String name)
        {
            this.before = before;
            this.after = after;
            this.name = name;
        }
    }
}
