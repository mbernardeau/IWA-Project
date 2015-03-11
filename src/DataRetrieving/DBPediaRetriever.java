package DataRetrieving;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.complexible.stardog.api.Connection;

public class DBPediaRetriever {
	private static final Logger logger = LogManager.getLogger(DBPediaRetriever.class);
	String service = "http://dbpedia.org/sparql";
	RepositoryConnection connection;
	
	public DBPediaRetriever() {
		// TODO Auto-generated constructor stub
	}
	
	void queryAndInsert(String sparqlConstructQuery, Connection stardogConnection){
		Query query;
		try {
			query = connection.prepareQuery(QueryLanguage.SPARQL, sparqlConstructQuery);
			if (query instanceof GraphQuery) {
			    GraphQueryResult result = ((GraphQuery) query).evaluate();

			    while (result.hasNext()) {
			        // The result is an iterator of Statement, which is a RDF triple or quad.
			        System.out.println(result.next());
			    }

			    // Do not forget!
			    result.close();
			}
		} catch (RepositoryException e) {
			logger.error("The DBPedia respository does not seem to be up.\n", e);
		} catch (MalformedQueryException e) {
			logger.error("The query to DBPedia is malformed : "+sparqlConstructQuery+"\n", e);
		} catch (QueryEvaluationException e) {
			logger.error("The query to DBPedia could not be evaluated.\n", e);
		}
		
	}
}
