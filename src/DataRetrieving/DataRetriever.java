package DataRetrieving;

import java.io.File;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Mostly based on Stardog example
 * https://gist.github.com/mhgrove/1045573
 */
public class DataRetriever implements Job {
	private static final String LOCAL_DB_SERVER = "http://localhost:8181/openrdf-sesame";
	private static final String DB_NAME = "Battles";
	
	//private Collection<Namespace> namespaces =  new LinkedList<Namespace>();
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println(new java.io.File("").getAbsolutePath());
    	System.out.println(DataRetriever.class.getClassLoader().getResource("").getPath());
    	
			try {
				/*
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
				*/
				// Open a user connection
				/*Connection aConn = ConnectionConfiguration
		                   .to(DB_NAME)
		                   .credentials("admin", "admin")
		                   .server(LOCAL_DB_SERVER)
		                   .connect();
				*/
				Repository sesameServer = new HTTPRepository(LOCAL_DB_SERVER,DB_NAME);  
				sesameServer.initialize(); 
				
				RepositoryConnection rConn = sesameServer.getConnection();
				
				//aConn.begin();
				//final com.complexible.common.rdf.model.Namespaces ns = aConn.namespaces(); 
				//ns.add("btl", "http://battles.com/");
				
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
				CSVRetriever csv = new CSVRetriever(rConn);
				
				for(File f : files){
					if(f.getName().startsWith("enum_")){
						csv.importEnum(f.getAbsolutePath(), f.getName().substring(5).split("\\.")[0]);
					}
				}
				
				csv.importBattles(DataRetriever.class.getClassLoader().getResource("./DataRetrieving/battles.csv"));
				csv.importCommanders(DataRetriever.class.getClassLoader().getResource("./DataRetrieving/commanders.csv"));
				csv.importWeather(DataRetriever.class.getClassLoader().getResource("./DataRetrieving/weather.csv"));
				
				rConn.begin();
				TupleQueryResult res = rConn.prepareTupleQuery(QueryLanguage.SPARQL,Prefixer.INSTANCE.toString() + "\nSELECT ?dbentity WHERE{ ?battle a btl:Battle ; owl:sameAs ?dbentity . }").evaluate();
				rConn.commit();
				
				int i = 0;
				int j = 0;
				while(res.hasNext()){
					BindingSet b = res.next();
					j++;
					if(b != null){
						Value dbentity = b.getBinding("dbentity").getValue();
						if(dbentity.toString().startsWith("http://dbpedia.org/resource/")){
							i++;
							SparqlConstructQueryer DBPediaQueryer = new SparqlConstructQueryer("http://dbpedia.org/sparql", rConn);
						
							DBPediaQueryer.query("\nCONSTRUCT{ <"+dbentity+"> ?pred ?obj ; geo:lat ?lat ; geo:long ?long . }WHERE{ <"+dbentity+"> ?pred ?obj .  OPTIONAL { <"+dbentity+"> dbpedia-owl:place ?place. ?place geo:lat ?lat ; geo:long ?long . } FILTER(!isLiteral(?obj) || lang(?obj) = \"\" || langMatches(lang(?obj), \"EN\"))\nFILTER(?pred != owl:sameAs)}");
						
							DBPediaQueryer.save();
						}
					}else{
						/// TODO : try to find a dbpedia entity, even if not provided
					}
				}
				
				
				rConn.close();
				
				
			} catch (Exception e) {
				throw new JobExecutionException(e);
			}
	}

}
