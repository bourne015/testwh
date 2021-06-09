import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ant
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.python
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.1"

project {

    vcsRoot(HttpsGithubComFantao015testtcRefsHeadsMain1)

    buildType(Build)
    buildType(CheckBuild)
    //buildType(FastTest)
    //buildType(SlowTest)
    //buildType(UnitTest)
    buildType(Build2)

    sequential {
        buildType(Build)
        buildType(CheckBuild)
        /*
        parallel {
            buildType(FastTest)
            buildType(SlowTest)
            buildType(UnitTest)
        }
         */
        buildType(Build2)
    }
}

object Build : BuildType({
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        ant {
            mode = antFile {
            }
            targets = "compile"
        }
        script {
            enabled = false
            scriptContent = "sh build.sh"
        }
    }
})

object CheckBuild : BuildType({
    name = "Check Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        python {
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            command = file {
                filename = ".teamcity/rest.py"
            }
        }
    }
})

object Build2 : BuildType({
    name = "Build 2"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        ant {
            mode = antFile {
            }
            targets = "jar"
        }
    }
    triggers {
        vcs {
        }
    }
/*
    features {
        feature {
            type = "SinCity"
            param("sincity.tagNameForBuildsTriggeredBySinCity","sincity-tag")
        }
    }
 */
})

object FastTest : BuildType({
    name = "Fast Test"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        ant {
            mode = antFile {
            }
            targets = "jar"
        }
    }
})

object SlowTest : BuildType({
    name = "Slow Test"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        ant {
            mode = antFile {
            }
            targets = "jar"
        }
    }
})

object UnitTest : BuildType({
    name = "Unit Test"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        ant {
            mode = antFile {
            }
            targets = "jar"
        }
    }
})

object HttpsGithubComFantao015testtcRefsHeadsMain1 : GitVcsRoot({
    name = "https://github.com/fantao015/testtc#refs/heads/main (1)"
    url = "https://github.com/fantao015/testtc"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "fantao015"
        password = "credentialsJSON:3f864950-94c9-41da-9d8f-fbde166b2017"
    }
})
