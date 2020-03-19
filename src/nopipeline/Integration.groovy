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
}