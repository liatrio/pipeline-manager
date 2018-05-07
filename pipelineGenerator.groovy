import groovy.json.JsonSlurper

def gitLocation = 'http://bitbucket.liatr.io/'

def parsedJson = new JsonSlurper().parseText(readFileFromWorkspace('config.json'))
def gitRepos = parsedJson.gitRepos
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
            node / 'properties' / 'org.jenkinsci.plugins.workflow.libs.FolderLibraries' / 'libraries' / 'org.jenkinsci.plugins.workflow.libs.LibraryConfiguration' {
                defaultVersion 'master'
                implicit 'true'
                name 'Pipeline-Library'
            }
            node / 'properties' / 'org.jenkinsci.plugins.workflow.libs.FolderLibraries' / 'libraries' / 'org.jenkinsci.plugins.workflow.libs.LibraryConfiguration' / retriever (class:'org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever') / scm (class:'jenkins.plugins.git.GitSCMSource') {
                remote parsedJson.pipelineLibrary
                credentialsId parsedJson.credentialId
            }
        }
    }
}
