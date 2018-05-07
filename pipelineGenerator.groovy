import groovy.json.JsonSlurper

def gitLocation = 'http://bitbucket.liatr.io/'

def parsedJson = new JsonSlurper().parseText(readFileFromWorkspace('config.json'))
def gitRepos = parsedJson.gitRepos
def gitProjects = parsedJson.gitProjects
// gitRepos.each { repo ->
//     def repoName = repo.name
//     def projectName = repo.project
//     def repoUrl = "${gitLocation}${repoName}.git"
//     multibranchPipelineJob("${repoName}") {
//         triggers {
//             periodic(0)
//         }
//         configure {
//             def bitbucketSource = it / sources(class:'jenkins.branch.MultiBranchProject$BranchSourceList') / 'data' / 'jenkins.branch.BranchSource' / 'source' / sources(class:'jenkins.branch.MultiBranchProject$BranchSourceList')
//             bitbucketSource {
//                     repoOwner 'PNC'
//              }
//              bitbucketSource {
//                      repository 'credit-cards'
//               }
//             bitbucketSource {
//                 serverUrl "http://bitbucket.liatr.io"
//             }
//             bitbucketSource {
//                 credentialsId 'bitbucket'
//             }
//             bitbucketSource << 'traits' << 'com.cloudbees.jenkins.plugins.bitbucket.BranchDiscoveryTrait' {
//                  strategyId '1'
//             }
//         }
//     }
// }
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
                    node / 'properties' / 'org.jenkinsci.plugins.workflow.libs.FolderLibraries' / 'libraries' / 'org.jenkinsci.plugins.workflow.libs.LibraryConfiguration' / retriever('class:org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever') / scm('class:jenkins.plugins.git.GitSCMSource') {
                        remote 'https://github.com/liatrio/pipeline-library'
                        credentialsId parsedJson.credentialId
                    }
                }
    }
}
