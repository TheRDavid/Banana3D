package files;

import general.CurrentData;
import general.Preference;
import other.B3D_Scene;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import other.Wizard;

/**
 * Stores the path of the folders and the current scene as well as
 * AutosaveOptions
 *
 * @author David
 */
public class Project implements Serializable
{

    private File projectFile;
    private File mainFolder;
    private File assetsFolder;
    private B3D_Scene currentScene;
    private AutosaveOptions autosaveOptions = new AutosaveOptions("autosave.b3ds", 1);

    /**
     * Creates a new Project, automatically puts it as the current project
     * (there can be only one)
     *
     * @param directoryPath
     * @param projectName
     */
    public Project(String directoryPath, final String projectName)
    {
        CurrentData.setProject(this);
        //New scene
        currentScene = new B3D_Scene(projectName);
        mainFolder = new File(directoryPath + "//" + projectName);
        assetsFolder = new File(mainFolder.getAbsolutePath() + "//assets");
        //Creates directories
        create();
        this.projectFile = new File(mainFolder.getAbsolutePath() + "\\" + projectName + ".b3dp");
        Wizard.saveFile(projectFile.getAbsolutePath(), this);
        //Ask for a name of the new B3D_Scene
        String sceneName = JOptionPane.showInputDialog("New Scene name:");
        File sceneFile = new File(mainFolder.getAbsolutePath() + "\\" + sceneName + ".b3ds");
        currentScene.setName(sceneName);
        Wizard.saveFile(sceneFile.getAbsolutePath(), currentScene);
        boolean check = true;
        while (check)
        {
            if (sceneFile.exists())
            {
                setScene(sceneFile, new File(mainFolder.getAbsolutePath() + "\\" + projectName + ".b3dp"));
                check = false;
            }
        }
    }

    /**
     * Opens an existing project, automatically puts it as the current project
     * (there can be only one)
     *
     * @param directoryPath
     * @param projectName
     */
    public Project(File projectFile, File sceneFile)
    {
        this.projectFile = projectFile;
        CurrentData.setProject(this);
        mainFolder = projectFile.getParentFile();
        assetsFolder = new File(mainFolder.getAbsolutePath() + "//assets");
        setScene(sceneFile, projectFile);
    }

    /**
     * Loads scene file, inits threads
     *
     * @param sceneFile
     * @param projectFile
     */
    private void setScene(File sceneFile, File projectFile)
    {
        this.projectFile = projectFile;
        if (currentScene == null)
        {
            currentScene = (B3D_Scene) Wizard.loadFile(sceneFile.getAbsolutePath());
        }
        currentScene.setName(sceneFile.getName().replaceAll(".b3ds", ""));
        autosaveOptions = ((Project) Wizard.loadFile(projectFile.getAbsolutePath())).getAutosaveOptions();
        if (autosaveOptions == null)
        {
            autosaveOptions = new AutosaveOptions(mainFolder + "\\autosave.b3ds", 1);
        }
        CurrentData.startAutosaveThread();
        updateRecentProjects();
        CurrentData.getEditorWindow().setTitle(sceneFile.getName() + " - Banana3D");
    }

    public File getMainFolder()
    {
        return mainFolder;
    }

    public File getProjectFile()
    {
        return projectFile;
    }

    /**
     *
     * @param directory
     */
    public void setMainFolder(File directory)
    {
        this.mainFolder = directory;
    }

    public File getAssetsFolder()
    {
        return assetsFolder;
    }

    /**
     *
     * @param assetsFolder
     */
    public void setAssetsFolder(File assetsFolder)
    {
        this.assetsFolder = assetsFolder;
    }

    public B3D_Scene getScene()
    {
        return currentScene;
    }

    /**
     *
     * @param world
     */
    public void setWorld(B3D_Scene world)
    {
        this.currentScene = world;
    }

    /**
     * Creates main - and asset-folder
     */
    public void create()
    {
        mainFolder.mkdir();
        assetsFolder.mkdir();
        setWorld(currentScene);
    }

    public AutosaveOptions getAutosaveOptions()
    {
        return autosaveOptions;
    }

    public void setAutosaveOptions(AutosaveOptions autosaveOptions)
    {
        this.autosaveOptions = autosaveOptions;
    }

    /**
     * Updates the list of recent Projects (recently opened in file-menu)
     */
    private void updateRecentProjects()
    {
        ArrayList<String> projectPaths = (ArrayList<String>) CurrentData.getPrefs().get(Preference.RECENT_PROJECT_PATHS);
        String path = mainFolder.getAbsolutePath() + "\\" + mainFolder.getName() + ".b3dp";
        if (!projectPaths.contains(path))
            projectPaths.add(path);
        CurrentData.getPrefs().set(
                Preference.RECENT_PROJECT_PATHS,
                projectPaths);
        //CurrentData.getPrefs().addRecentlyOpenedEntry(mainFolder.getAbsolutePath() + "\\" + mainFolder.getName() + ".b3dp");
    }

    public void setCurrentScene(B3D_Scene currentScene)
    {
        this.currentScene = currentScene;
    }
}
