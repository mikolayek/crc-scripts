import de.hybris.platform.servicelayer.config.ConfigurationService
import java.lang.String
import java.lang.System
import groovy.json.JsonOutput
 
results = []


def joblogs = new FlexibleSearchQuery("SELECT COUNT(*) AS entries FROM {JobLog}")
flexibleSearchService.search(joblogs)
def savedvalues = new FlexibleSearchQuery("SELECT COUNT(*) AS entries from {SavedValues}")
def savedvalueentries = new FlexibleSearchQuery("SELECT COUNT(*) AS entries FROM {SavedValueEntry}")
def modifiedValues = configurationService.getConfiguration().getProperty("hmc.storing.modifiedvalues.size")
// fetch property hmc.storing.modifiedvalues.size=20 // 20 is factory default
def sqlProps = new FlexibleSearchQuery("SELECT COUNT(*) AS entries FROM props")
def processtasklogs = new FlexibleSearchQuery("SELECT COUNT(*) AS entries FROM {ProcessTaskLog}")
def sqlTasklogs = new FlexibleSearchQuery("SELECT COUNT(*) AS entries FROM tasklogs")
def carts = new FlexibleSearchQuery("SELECT COUNT(*) AS entries FROM {cart}")
def cartEntries = new FlexibleSearchQuery("SELECT COUNT(*) AS entries FROM {cartentry}")
// then check if OldCartRemovalCronJob exists
// check their trigger. query below:
def cartcronjob = new FlexibleSearchQuery("SELECT * FROM {Trigger AS t JOIN OldCartRemovalCronJob AS o ON {o.pk} = {t.cronJob} }")

JsonOutput.prettyPrint(JsonOutput.toJson(results))