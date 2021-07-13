import java.util.concurrent.TimeUnit

import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.LocalDateTime

import de.hybris.platform.core.PK
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery
import de.hybris.platform.cronjob.model.CronJobModel
import de.hybris.platform.servicelayer.cronjob.CronJobHistoryService;

import org.quartz.CronExpression

import groovy.json.JsonOutput
import groovy.transform.Field

@Field final int MAX_EXECUTIONS = 1000
@Field final int MAX_DAYS = 30
@Field final int DEFAULT_CRONJOB_DURATION_SECS = 60


def query = new FlexibleSearchQuery('SELECT {c.pk}, {c.code}, {c.active}, {c.nodeID}, {c.nodeGroup}, {t.cronExpression} FROM {CronJob as c JOIN Trigger AS t ON {t.cronjob} = {c.PK}} WHERE {t.cronExpression} is not null')
query.setResultClassList([Long.class, String.class, Boolean.class, Integer.class, String.class, String.class])

def result = flexibleSearchService.search(query)

def cronjobExecutionsMap = [:]
results = []

result.result.each { r -> 

  def cj = modelService.get(new PK(r[0]));
  def cronExpStr = r[5];
  def averageDuration = getCronJobAverageExecutionTime(cj);
  def cronExpression = new CronExpression (cronExpStr)
  def cronSummary = cronExpression.expressionSummary
  def executionList = getCronJobExecutions(cronExpression, averageDuration, LocalDateTime.now())

  results << [ pk: r[0], code: r[1], active: r[2], nodeID: r[3], nodeGroup: r[4], cronExpression: r[5], cronSummary: cronSummary, averageDuration: averageDuration, executions: executionList]
}

// scan for overlapping executions in each cronjob and their executions
def overlapsMap = [:]
results.each{r -> 
	overlaps = checkForCronJobOverlaps(r.code, r.executions, results);
	// add the overlaps to the results.
	r.overlaps = overlaps;
	// remove the executions as no longer needed
	r.remove('executions')
}
JsonOutput.prettyPrint(JsonOutput.toJson(results))



       
def checkForCronJobOverlaps(currentCronJobCode, currentCronjobExecutions, allResults) {
	def overlaps = []

	currentCronjobExecutions.each { execution ->

		allResults.each { otherCronJobResult ->
			otherCronJobCode = otherCronJobResult.code

			// skip checking executions against itself
			if (!otherCronJobCode.equals(currentCronJobCode) && !overlaps.contains(otherCronJobCode)) {
			
				// check for any intersections in the executions
				if (executionsOverlap(execution, otherCronJobResult.executions)) {
					overlaps << otherCronJobCode
				}
			}
		}
	}
	return overlaps
}

// determine if the given execution overlaps any executions in the other execution list.
def executionsOverlap( execution, otherExecutions ) {
	return otherExecutions.any {it.intersects(execution)};
}


// given a cron expression, and average duration, and a start time
// compute the next X execution ranges for the cronjob up to a maximum of MAX EXECUTIONS or MAX_DAYS in the future
def getCronJobExecutions(CronExpression cron, long averageDuration, LocalDateTime startTime) {

	def executionList = []
    def lastEndTime = startTime;
    def loopCount = 0; 
    while (lastEndTime.isBefore(startTime.plus(MAX_DAYS, ChronoUnit.DAYS)) && loopCount < MAX_EXECUTIONS) {
    	loopCount++
      	def nextStartTime = getNextExecutionTime(cron, lastEndTime)
    	def nextEndTime = nextStartTime.plus(averageDuration, ChronoUnit.SECONDS)

    	def newRange = new DateTimeRange(nextStartTime, nextEndTime)
        executionList << newRange
    	lastEndTime = nextEndTime
    }
    return executionList;
}

// retrieve the Average execution time for the specified job.
// Assume a default of DEFAULT_CRONJOB_DURATION_SECS if no history for execution, or a duration of 0 is returned.
def getCronJobAverageExecutionTime(CronJobModel cronjob) {
   
  def durationInSeconds = cronJobHistoryService.getAverageExecutionTime(cronjob, TimeUnit.SECONDS);	
  if (durationInSeconds == null || durationInSeconds == 0) {
  	// set a default duration for those cronjobs with no history
  	durationInSeconds = DEFAULT_CRONJOB_DURATION_SECS;
  }
  return durationInSeconds;
}
  
def getNextExecutionTime(CronExpression cron, LocalDateTime startDateTime) {
  def startDate = toDate(startDateTime);
  return toDateTime(cron.getNextValidTimeAfter(startDate));
}

def toDate(LocalDateTime localDateTime) {
  return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
}

def toDateTime(Date date) {
  return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault() )
}

class DateTimeRange {
	long start;
	long end;

	DateTimeRange(LocalDateTime start, LocalDateTime end) {
		this.start = start.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()
		this.end = end.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()
	}

	public boolean intersects(DateTimeRange otherRange) {
		return (this.start <= otherRange.end) && (this.end >= otherRange.start)
	}
}
