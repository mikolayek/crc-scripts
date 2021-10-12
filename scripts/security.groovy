import de.hybris.platform.servicelayer.config.ConfigurationService
import java.lang.String
import java.lang.System
import groovy.json.JsonOutput
 
String[] array = "hmc.default.login hmc.default.password cockpit.default.login cockpit.default.password printcockpit.default.login printcockpit.default.password cscockpit.default.login cscockpit.default.password importcockpit.default.login importcockpit.default.password cmscockpit.default.login cmscockpit.default.password productcockpit.default.login productcockpit.default.password backoffice.default.login backoffice.default.password".split()

results = []

array.each { r ->
	def value = configurationService.getConfiguration().getProperty(r)
	def arr = [ key: r, value:  configurationService.getConfiguration().getProperty(r)]
	if (value != null && value.length() > 0) {
		arr.error = true
	}
	
  results << arr
}

JsonOutput.prettyPrint(JsonOutput.toJson(results))
