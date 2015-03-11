package DataRetrieving;

import org.apache.catalina.Store;
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
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.Connection;

public class SparqlQueryer {
	private static final Logger logger = LogManager.getLogger(SparqlQueryer.class);
	String service;
	RepositoryConnection connection;
	Connection stardogConnection;
	Store crdf;
	GraphQueryResult result;
	
	/**
	 * Constructor of the SparqlQueryer class
	 * @param service The sparql endpoint to query
	 * @param stardogConnection The connection to the stardog local repository
	 */
	public SparqlQueryer(String service, Connection stardogConnection) {
		this.service = service;
		this.stardogConnection = stardogConnection;
		connection = new SPARQLConnection(new SPARQLRepository(service));
	}
	
	/**
	 * Queries the service with the given query, and stores the result.
	 * @param sparqlConstructQuery The query
	 */
	void query(String sparqlConstructQuery){
		Query query;
		try {
			if(result != null)
				result.close();
			query = connection.prepareQuery(QueryLanguage.SPARQL, sparqlConstructQuery);
			if (query instanceof GraphQuery) {
			    result = ((GraphQuery) query).evaluate();
			}
		} catch (RepositoryException e) {
			logger.error("The DBPedia respository does not seem to be up.\n", e);
		} catch (MalformedQueryException e) {
			logger.error("The query is malformed : "+sparqlConstructQuery+"\n", e);
		} catch (QueryEvaluationException e) {
			logger.error("The query could not be evaluated.\n", e);
		}
	}
	
	/**
	 * Saves the result into the stardog local database
	 */
	void save(){
		try {
			while (result.hasNext()) {
				try {
					stardogConnection.add().statement(result.next());
				} catch (QueryEvaluationException | StardogException e) {
					logger.error("Unable to save a statement.\n", e);
				}
			}
			stardogConnection.commit();
		} catch (QueryEvaluationException e) {
			logger.error("The query could not be evaluated.\n", e);
		} catch (StardogException e) {
			logger.error("The commit of the new triples failed.\n", e);
		}
		
	}
}
