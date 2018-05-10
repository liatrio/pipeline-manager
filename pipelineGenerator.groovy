import groovy.json.JsonSlurper


def parsedJson = new JsonSlurper().parseText(readFileFromWorkspace('config.json'))
def gitRepos = parsedJson.gitRepos
def gitLocation = parsedJson.bitbucketUrl
def gitProjects = parsedJson.gitProjects
gitProjects.each {
    bitbucketProject->
    organizationFolder(bitbucketProject.name) {
        configure {
            node ->
            node / 'navigators' / 'com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator' / 'traits' / 'com.cloudbees.jenkins.plugins.bitbucket.BranchDiscoveryTrait' {
                strategyId '1'
            }
            node / 'navigators' / 'com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator' {
                serverUrl parsedJson.bitbucketUrl
                credentialsId parsedJson.credentialId
                repoOwner bitbucketProject.name
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
