package general;

import b3dElements.B3D_Element;
import java.io.Serializable;
import java.util.Stack;
import java.util.UUID;
import other.ElementToObjectConverter;
import other.ObjectToElementConverter;
import other.Wizard;

/**
 *
 * @author David
 */
public class UserActionManager implements Serializable
{

    private static B3D_Element current;
    private static Stack<UserAction> undoStack = new Stack<UserAction>(), redoStack = new Stack<UserAction>();

    public static void setCurrentElement(Object o, UUID uuid)
    {
        System.out.println("setCurrentElement " + o);
        if (o != null)
        {
            current = ObjectToElementConverter.convertToElement(o);
            current.setUuid(uuid);
        } else
            current = null;
    }

    public static void addState(Object o, String name)
    {
        System.out.println("addState");
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
            setCurrentElement(o, e.getUUID());
        else
            setCurrentElement(o, null);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setEnabled(true);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setText(name);
    }

    public static void undo()
    {
        if (undoStack.isEmpty())
            return;
        update(redoStack.push(undoStack.pop()), 0);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setEnabled(true);
        CurrentData.getEditorWindow().getMainMenu().getEditMenu().getRedoItem().setText(redoStack.peek().name);
        if (undoStack.isEmpty())
        {
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setEnabled(false);
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setText("Undo");
        } else
            CurrentData.getEditorWindow().getMainMenu().getEditMenu().getUndoItem().setText(undoStack.peek().name);
    }

    public static void redo()
    {
        if (redoStack.isEmpty())
            return;
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
        B3D_Element primaryElement = (direction == 0) ? ua.before : ua.after, secondaryElement = (direction == 1) ? ua.before : ua.after;
        if (primaryElement != null)
        {
            Object newObject = ElementToObjectConverter.convertToObject(primaryElement);
            if (secondaryElement == null)
                // Recover Element
                CurrentData.addToScene(newObject, primaryElement);
            else
            {
                B3D_Element oldElement = Wizard.getObjects().getB3D_Element(primaryElement.getUUID());
                Object oldObject = Wizard.getObjects().getOriginalObject(Wizard.getObjectReferences().getID(oldElement.getUUID()));
                Wizard.completelyCopyValues(newObject, oldObject);
                oldElement.set(primaryElement);
            }
        } else if (direction == 0)
        {
            //Delete Element
            CurrentData.getEditorWindow().getB3DApp().setSelectedUUID(((direction == 1) ? ua.before : ua.after).getUUID());
            CurrentData.execDelete(false);
        }
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
