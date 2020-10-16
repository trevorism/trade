package com.trevorism.gcloud

import com.trevorism.secure.validator.Validators
import org.glassfish.jersey.server.ResourceConfig

import java.util.logging.Logger

class StartupSecurity extends ResourceConfig {

    private static final Logger log = Logger.getLogger(StartupSecurity.class.name)

    StartupSecurity(){
        log.info("Ensuring only token based auth...")
        Validators.allValidators = [Validators.bearerTokenValidator, Validators.cookieValidator]
    }
}
