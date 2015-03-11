package DataRetrieving;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;

/**
 * Mostly based on Stardog example
 * https://gist.github.com/mhgrove/1045573
 */
public class DataRetriever implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
			try {
				// Connect as an admin to the server
				AdminConnection aAdminConnection = AdminConnectionConfiguration.toServer("snarl://localhost:5820")
                        .credentials("admin", "admin")
                        .connect();
				
				// Drop the database if exists
				if (aAdminConnection.list().contains("testConnectionAPI")) {
					aAdminConnection.drop("testConnectionAPI");
				}
				
				// Create a permanant database
				aAdminConnection.disk("testConnectionAPI").create();
				
				
				// Close the admin connection
				aAdminConnection.close();
				
				// Open a user connection
				Connection aConn = ConnectionConfiguration
		                   .to("testConnectionAPI")
		                   .credentials("admin", "admin")
		                   .server("snarl://localhost:5820")
		                   .connect();
				aConn.begin();
				
				
				/// Example to add data from a Turtle File
				//	aConn.add().io()
			    // .format(RDFFormat.TURTLE)
			     //.stream(new FileInputStream("data/sp2b_10k.n3"));
				
				
				/// Example to create a new statement
				/*Graph aGraph = (Graph) Graphs.newGraph(ValueFactoryImpl.getInstance()
                        .createStatement(ValueFactoryImpl.getInstance().createURI("urn:subj"),
                                         ValueFactoryImpl.getInstance().createURI("urn:pred"),
                                         ValueFactoryImpl.getInstance().createURI("urn:obj")));
				Resource aContext = ValueFactoryImpl.getInstance().createURI("urn:test:context");
				 
				// With our newly created `Graph`, we can easily add that to the database as well.  You can also
				// easily specify the context the data should be added to.  This will insert all of the statements
				// in the `Graph` into the given context.
				aConn.add().graph(aGraph, aContext);
				*/
				// To close the connection
				
				Prefixer.INSTANCE.addPrefix("dbpedia", "http://dbpedia.org/resource/");
				Prefixer.INSTANCE.addPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
				Prefixer.INSTANCE.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
				
				SparqlConstructQueryer DBPediaQueryer = new SparqlConstructQueryer("http://dbpedia.org/sparql", aConn);
				
				/*
				DBPediaQueryer.query(
					"\nCONSTRUCT{ dbpedia:Battle_of_Kursk ?relation ?data . dbpedia:Battle_of_Kursk geo:lat ?lat . dbpedia:Battle_of_Kursk geo:long ?long . } WHERE{   dbpedia:Battle_of_Kursk ?relation ?data. dbpedia:Battle_of_Kursk dbpedia-owl:place ?place .?place geo:lat ?lat ; geo:long ?long.}"
				);
				*/
				DBPediaQueryer.query("CONSTRUCT{ ?battle ?relation ?data . } WHERE{  ?battle 	rdf:type 	yago:Battle100953559 ; 	?relation 	?data	.  FILTER(?relation != owl:sameAs)}");
				
				DBPediaQueryer.save();
				
				/*SparqlSelectQueryer DBPediaSelectQueryer = new SparqlSelectQueryer("http://dbpedia.org/sparql");
				DBPediaSelectQueryer.query("SELECT ?battle WHERE {  ?battle 	rdf:type 	yago:Battle100953559 .}");
				
				while(DBPediaSelectQueryer.getResult().hasNext()){
					System.out.println(DBPediaSelectQueryer.getResult().next().toString());
				}
				*/
				aConn.close();
				
				
			} catch (Exception e) {
				throw new JobExecutionException(e);
			}
	}

}
