package nopipeline
import nopipeline.utils.Config

class Integration extends Config {

    Integration(def script) {
        super(script)
    }

    /**
    * Used to initialize this Class
    *   calls "super" to instantiate and inherit the Config class objects.  See Config.initialize() for param definitions
    */
    public String init(Map args) {
        this.initialize(args)
    }

    /**
    * Docker Runner to implement containerized python app.
    * More details: See Docker Containers, Python Apps and Groovy references.
    *
    * @param module - The module in the container.
    * @param function - The function of the module.
    * @param arguments - The arguments for the module and function required for the code to run.
    * @param loglevel - The level of the log (DEBUG, INFO, WARNING, ERROR or CRITICAL) used for Jenkins Shell and the Python App.
    *
    */
    String dockerRunner(Map args){
        // Set log level, defaults to this.loglevel value
        def loglevel
        if (args.loglevel != null){ loglevel = args.loglevel } else { loglevel = this.logLevel }

        // Build the command stack
        def cmd = """#!/bin/bash -e
            cd ${this.workPath}
            if [ "${loglevel}" == "DEBUG" ]; then set -x; else set +x; fi
            docker run -t --rm \
                -v \"\$(pwd):/artifacts\" \
                ${container} \
                --module \"${args.module}\" \
                --function \"${args.function}\" \
                --loglevel \"${loglevel}\" \
                --payload '${this.escapeQuotes(data: args.payload, quote: 'double')}' \
                ${arguments}
        """
        this.script.sh(script: cmd)
    }
}