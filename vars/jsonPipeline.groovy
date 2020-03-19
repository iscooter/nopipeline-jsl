def call(Map args) {
    // startup class integrations: jenkins shared library
    jsl = args.library.nopipeline.Integration.new(this)

    pipeline {

        agent any

        options {
            timeout(time: 60, unit: 'MINUTES')
            ansiColor('xterm')
        }

        stages {
            stage("Initialize") {
                steps {
                    script {
                        // initialize gitops shared library integration
                        jsl.init(jsonPipeline: args.json)

                        // display for testing
                        jsl.printFormattedText(
                            label: "section",
                            text: """\
                                Starting JSON noPipeline""".stripIndent()
                        )
                    }
                }
            }

            stage("JSON No Pipeline") {
                steps {
                    script {
                        jsl.clientConfig.each { pipeline, junk ->
                            println(pipeline)
                        }
                    }
                }
            }
        }
    }
}