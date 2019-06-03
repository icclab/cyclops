package ch.icclab.cyclops.load.model;

public class GitCredentials {
    // These fields correspond with the configuration file
    private String gitRepo;
    private String gitUsername;
    private String gitPassword;
    private String gitProjectPath;

    //==== Getters and Setters
    public String getGitRepo() {
        return gitRepo;
    }
    public void setGitRepo(String gitRepo) {
        this.gitRepo = gitRepo;
    }

    public String getGitUsername() {
        return gitUsername;
    }
    public void setGitUsername(String gitUsername) {
        this.gitUsername = gitUsername;
    }

    public String getGitPassword() {
        return gitPassword;
    }
    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }

    public String getGitProjectPath() {
        return gitProjectPath;
    }
    public void setGitProjectPath(String gitProjectPath) {
        this.gitProjectPath = gitProjectPath;
    }

}
