package gui.menu;

import javax.swing.*;

public class MainMenu extends JMenuBar
{

    private FileMenu fileMenu = new FileMenu();
    private AddMenu addMenu = new AddMenu();
    private EditMenu editMenu = new EditMenu();
    private ViewMenu viewMenu = new ViewMenu();
    private PhysicsMenu physicsMenu = new PhysicsMenu();
    private ExtrasMenu extrasMenu = new ExtrasMenu();
    private HelpMenu helpsMenu = new HelpMenu();

    public MainMenu()
    {
        add(fileMenu);
        add(addMenu);
        add(editMenu);
        add(viewMenu);
        add(physicsMenu);
        add(extrasMenu);
        add(helpsMenu);
        setDisabled(true);
    }

    public void setDisabled(boolean disable)
    {
        fileMenu.getSaveAsItem().setEnabled(!disable);
        fileMenu.getSaveItem().setEnabled(!disable);
        fileMenu.getSwitchSceneItem().setEnabled(!disable);
        editMenu.setEnabled(!disable);
        addMenu.setEnabled(!disable);
        viewMenu.setEnabled(!disable);
        physicsMenu.setEnabled(!disable);
        extrasMenu.getScreenshotItem().setEnabled(!disable);
        //extrasMenu.getKeyframeAnimationItem().setEnabled(!disable);
        extrasMenu.getRecordItem().setEnabled(!disable);
        extrasMenu.getAutosaveMenu().setEnabled(!disable);
        extrasMenu.getOpenFolderItem().setEnabled(!disable);
        for (ExtrasMenu.AppItem item : extrasMenu.getAppItems())
            item.setEnabled(!disable);
    }

    public ViewMenu getViewMenu()
    {
        return viewMenu;
    }

    public FileMenu getFileMenu()
    {
        return fileMenu;
    }

    public AddMenu getAddMenu()
    {
        return addMenu;
    }

    public EditMenu getEditMenu()
    {
        return editMenu;
    }

    public PhysicsMenu getPhysicsMenu()
    {
        return physicsMenu;
    }

    public ExtrasMenu getExtrasMenu()
    {
        return extrasMenu;
    }

    public HelpMenu getHelpsMenu()
    {
        return helpsMenu;
    }
}