package nopipeline.utils
import net.sf.json.JSONObject

class Config implements Serializable {

    public final def script
    static String workPath = "nopipelinework"
    static String basePathConfig = "resources/config"
    static JSONObject clientConfig

    Config(def script) {
        this.script = script
    }

    /**
    * Used to initialize this Class
    *
    * @param workPath - The working directory used by this integration library.
    * @param basePath - This defines the top level directory name used for resources; artifacts, dockerfiles, policies, bin, documents and src.
    * @returns [String] response - the output from running the shell script
    *
    */
    public String initialize(Map args){
        if (args != null) {
            if (args.workPath != null){ this.workPath = args.workPath }
            if (args.basePath != null){ this.basePath = args.basePath }
            if (args.jsonPipeline != null) {
                this.script.sh(script: """#!/bin/bash
                    mkdir -p resources/
                    mkdir -p resources/config/
                """)
                this.script.writeFile file:"resources/config/nopipeline.json", text:this.script.libraryResource("nopipelines/${args.jsonPipeline}.json")
            }
        }

        def response = this.script.sh(script: """#!/bin/bash
            if [ ! -z "${this.workPath}" ]
            then
            if test -d "${this.workPath}"
            then
            rm -rf ./${this.workPath}/*
            echo "JSL Work Path contents removed"
            else
            mkdir ${this.workPath}
            echo "JSL Work Path created"
            fi
            else
            echo "workPath cannot be null"
            exit 1
            fi
        """, returnStdout:true).trim()

        this.clientConfig = this.jsonReader()

        return response
    }

    /**
    * Due to the way jenkins interpolates strings in single, double and triple quoted objects, we need to have a way
    * to escape interal quotes too.  Primarly done to preserve JSON data structure throughout integration from JSON -> JSL -> Docker -> Python.
    *
    * @param data - The string object with quotes to escape
    * @returns [String] - The args.data with double quotes escaped by one backslash
    *
    */
    public String escapeQuotes(Map args) {
        def response

        if (args != null) {
            if (args.data != null) {
                if (args.quote != null) {
                    if (args.quote == "single") {
                        response = args.data.toString().replaceAll("'", "\'")
                    }
                    if (args.quote == "double") {
                        if (args.depth != null) {
                            response = args.data.toString().replaceAll('"', '\\\\\\"')
                        } else {
                            response = args.data.toString().replaceAll('"', '\"')
                        }
                        
                    }
                }
            }
        }
        return response
    }

    /**
    * Parse JSON configuration file and return the value for the name element. The name (or key) must exist in the json data structure.
    *
    * @param configFile - The name of the json file under basePath/basePathConfig.
    * @returns [JSONObject] respose - the json object generated by readJSON. 
    *
    */
    public JSONObject jsonReader(Map args) {
        def config_file = "${this.basePathConfig}/nopipeline.json"

        if (args != null) {
            if (args.configFile != null) {
                def file = this.script.fileExists file: "${this.basePathConfig}/${args.configFile}"
                if (file) {
                    config_file = "${this.basePathConfig}/${args.configFile}"
                } else {
                    this.script.println("${file}: the file does not exist.")
                    this.script.currentBuild.result = 'SUCCESS'
                    return
                }
            }
        }

        def response = this.script.readJSON file:config_file
        
        return response
    }

    /**
    * method built to facilitate printing text with formatting
    *
    * @param label - the type of formatting to perform; colorize or block, etc.
    * @param text - the content to print
    *
    */
    public void printFormattedText(Map args) {
        // Didn't find a better was outside of ansicolor...
        def text = args.text
        this.script.sh """#!/bin/bash +x
            case "${args.label}" in
                section)
                    echo "\n"
                    echo "\u001B[1m" # Bold
                    echo "##############################################################"
                    echo "${text}"
                    echo "##############################################################"
                    echo "\u001B[0m"
                    ;;
                red)
                    echo "\u001B[31m\u001B[1m${text}\u001B[0m"
                    ;;
                magenta)
                    echo "\u001B[35m\u001B[1m${text}\u001B[0m"
                    ;;
                cyan)
                    echo "\u001B[36m\u001B[1m${text}\u001B[0m"
                    ;;
                blue)
                    echo "\u001B[34m\u001B[1m${text}\u001B[0m"
                    ;;
                yellow)
                    echo "\u001B[33m\u001B[1m${text}\u001B[0m"
                    ;;
                green)
                    echo "\u001B[32m\u001B[1m${text}\u001B[0m"
                    ;;
                bold)
                    echo "\u001B[1m${text}\u001B[0m"
                    ;;
                *)
                    echo "\u001B[1mDidn't find kind or color for: ${args.label}\u001B[0m"
                    echo "\u001B[1mProvided text is: ${text}\u001B[0m"
                    ;;
            esac
        """
    }
}