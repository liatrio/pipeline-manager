import groovy.json.JsonSlurper

def parsedJson = new JsonSlurper().parseText(readFileFromWorkspace('config.json'))
def gitLocation = parsedJson.bitbucketUrl
def topLevelFolders = parsedJson.folders
topLevelFolders.each {
    topLevelFolder->
    folder(topLevelFolder.name)
    topLevelFolder.projects.each {
        bitbucketProject->
        def organizationInFolder = topLevelFolder.name + "/" + bitbucketProject
        organizationFolder(organizationInFolder) {
            configure {
                node ->
                node / 'navigators' / 'com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator' {
                    serverUrl parsedJson.bitbucketUrl
                    credentialsId parsedJson.credentialId
                    repoOwner bitbucketProject
                }
                node / 'navigators' / 'com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator' / 'traits' / 'com.cloudbees.jenkins.plugins.bitbucket.BranchDiscoveryTrait' {
                    strategyId '1'
                }
                node / 'navigators' / 'com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator' / 'traits' / 'com.cloudbees.jenkins.plugins.bitbucket.OriginPullRequestDiscoveryTrait' {
                    strategyId '1'
                }
                node / 'navigators' / 'com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator' / 'traits' / 'com.cloudbees.jenkins.plugins.bitbucket.ForkPullRequestDiscoveryTrait' {
                    strategyId '1'
                    trust(class:'com.cloudbees.jenkins.plugins.bitbucket.ForkPullRequestDiscoveryTrait$TrustEveryone')
                }
                parsedJson.pipelineLibraries.each {
                    libraryUrl ->
                    def gitRepoName = (libraryUrl =~ /[a-z,-]*(?:\.git)/)[0] - '.git'
                    node / 'properties' / 'org.jenkinsci.plugins.workflow.libs.FolderLibraries' / 'libraries' << 'org.jenkinsci.plugins.workflow.libs.LibraryConfiguration' {
                            retriever (class:'org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever') / scm (class:'jenkins.plugins.git.GitSCMSource') {
                            remote libraryUrl
                            credentialsId parsedJson.credentialId
                        }
                        defaultVersion 'master'
                        implicit 'true'
                        name gitRepoName
                    }
                }
            }
        }
    }
}
