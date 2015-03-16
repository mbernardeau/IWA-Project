package DataRetrieving;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

public class Initializer implements ServletContextListener{
	SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
	Scheduler sched;

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		try {
			sched = schedFact.getScheduler(); 
			sched.start();
		} catch (SchedulerException e) {

			e.printStackTrace();
		} 

		// define the job and tie it to our HelloJob class 
		JobDetail job = newJob(DataRetriever.class) 
				.withIdentity("myJob", "group1") 
				.build(); 

		// Trigger the job to run now, and then every 40 seconds 
		/*Trigger trigger = newTrigger() 
				.withIdentity("myTrigger", "group1") 
				.startNow()
				.withSchedule(simpleSchedule()
						.withIntervalInHours(1)
						.repeatForever())
						.build();
						
		//.withSchedule(simpleSchedule() 
		//		.withIntervalInSeconds(40) 
		//	.repeatForever()) 
		//.build(); 
		// Tell quartz to schedule the job using our trigger 
		try {
			sched.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	*/
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		/* Do Shutdown stuff. */
	}

}