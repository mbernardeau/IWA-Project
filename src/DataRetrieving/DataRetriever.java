package DataRetrieving;

import java.io.File;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
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
	private static final String LOCAL_DB_SERVER = "snarl://localhost:5820";
	private static final String DB_NAME = "Battles3";
	
	//private Collection<Namespace> namespaces =  new LinkedList<Namespace>();
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println(new java.io.File("").getAbsolutePath());
    	System.out.println(DataRetriever.class.getClassLoader().getResource("").getPath());
    	
			try {
				// Connect as an admin to the server
				AdminConnection aAdminConnection = AdminConnectionConfiguration.toServer(LOCAL_DB_SERVER)
                        .credentials("admin", "admin")
                        .connect();
				
				// Drop the database if exists
				if (aAdminConnection.list().contains(DB_NAME)) {
					aAdminConnection.drop(DB_NAME);
				}
				
				// Create a permanant database
				aAdminConnection.disk(DB_NAME).create();
				
				
				// Close the admin connection
				aAdminConnection.close();
				
				// Open a user connection
				Connection aConn = ConnectionConfiguration
		                   .to(DB_NAME)
		                   .credentials("admin", "admin")
		                   .server(LOCAL_DB_SERVER)
		                   .connect();
				//aConn.begin();
				
				
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
				Prefixer.INSTANCE.addPrefix("dbo", "http://dbpedia.org/ontology/");
				Prefixer.INSTANCE.addPrefix("btl", "http://battles.com/");
				Prefixer.INSTANCE.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
				Prefixer.INSTANCE.addPrefix("foaf", "http://xmlns.com/foaf/0.1/");
				/*
				SparqlConstructQueryer DBPediaQueryer = new SparqlConstructQueryer("http://dbpedia.org/sparql", aConn);
				
				/*
				DBPediaQueryer.query(
					"\nCONSTRUCT{ dbpedia:Battle_of_Kursk ?relation ?data . dbpedia:Battle_of_Kursk geo:lat ?lat . dbpedia:Battle_of_Kursk geo:long ?long . } WHERE{   dbpedia:Battle_of_Kursk ?relation ?data. dbpedia:Battle_of_Kursk dbpedia-owl:place ?place .?place geo:lat ?lat ; geo:long ?long.}"
				);
				*/
				/*
				DBPediaQueryer.query("CONSTRUCT{ ?battle ?relation ?data ; dbo:date ?date_str .  }\n WHERE{  ?battle 	rdf:type 	yago:Battle100953559 ;\n 	?relation 	?data	; dbo:date ?date . \nBIND(STR(?date) AS ?date_str)\n  FILTER(?relation != owl:sameAs) FILTER(!isLiteral(?data) || lang(?data) = "" || langMatches(lang(?data), "EN"))}");
				
				DBPediaQueryer.save();
				
				/*
				
				
				/*SparqlSelectQueryer DBPediaSelectQueryer = new SparqlSelectQueryer("http://dbpedia.org/sparql");
				DBPediaSelectQueryer.query("SELECT ?battle WHERE {  ?battle 	rdf:type 	yago:Battle100953559 .}");
				
				while(DBPediaSelectQueryer.getResult().hasNext()){
					System.out.println(DBPediaSelectQueryer.getResult().next().toString());
				}
				*/
				//
				//final String classPath = System.getProperty("java.class.path", ".");
				
				File[] files = new File(DataRetriever.class.getClassLoader().getResource("").getFile().replace("%20", " ")+"DataRetrieving/").listFiles();
				CSVRetriever csv = new CSVRetriever(aConn);
				
				for(File f : files){
					if(f.getName().startsWith("enum_")){
						csv.importEnum(f.getAbsolutePath(), f.getName().substring(5).split("\\.")[0]);
					}
				}
				
				csv.importBattles(DataRetriever.class.getClassLoader().getResource("./DataRetrieving/battles.csv"));
				csv.importCommanders(DataRetriever.class.getClassLoader().getResource("./DataRetrieving/commanders.csv"));
				csv.importWeather(DataRetriever.class.getClassLoader().getResource("./DataRetrieving/weather.csv"));
				
				aConn.begin();
				TupleQueryResult res = aConn.select(Prefixer.INSTANCE.toString() + "\nSELECT ?dbentity WHERE{ ?battle a btl:Battle ; owl:sameAs ?dbentity . }").execute();
				aConn.commit();
				
				
				while(res.hasNext()){
					BindingSet b = res.next();
					
					
					if(b != null){
						Value dbentity = b.getBinding("dbentity").getValue();
						SparqlConstructQueryer DBPediaQueryer = new SparqlConstructQueryer("http://dbpedia.org/sparql", aConn);
					
						DBPediaQueryer.query("\nCONSTRUCT{ <"+dbentity+"> ?pred ?obj . }WHERE{ <"+dbentity+"> ?pred ?obj . FILTER(!isLiteral(?obj) || lang(?obj) = \"\" || langMatches(lang(?obj), \"EN\"))}");
					
						DBPediaQueryer.save();
					}else{
						/// TODO : try to find a dbpedia entity, even if not provided
					}
				}
				
				aConn.close();
				
				
			} catch (Exception e) {
				throw new JobExecutionException(e);
			}
	}

}
