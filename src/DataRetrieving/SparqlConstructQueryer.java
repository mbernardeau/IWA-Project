package DataRetrieving;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryEvaluationException;

import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.Connection;

public class SparqlConstructQueryer extends SparqlQueryer<Statement> {
	private static final Logger logger = LogManager.getLogger(SparqlConstructQueryer.class);
	Connection stardogConnection;
	
	/**
	 * Constructor of the SparqlQueryer class
	 * @param service The sparql endpoint to query
	 * @param stardogConnection The connection to the stardog local repository
	 */
	public SparqlConstructQueryer(String service, Connection stardogConnection) {
		super(service);
		this.stardogConnection = stardogConnection;
	}
	
	/**
	 * Queries the service with the given query, and stores the result.
	 * @param sparqlConstructQuery The query
	 */
	@Override
	public void query(String sparqlConstructQuery){
		try {
			if(result != null)
				result.close();
				this.prepareQuery( sparqlConstructQuery);
			if (query instanceof GraphQuery) {
			    result = ((GraphQuery) query).evaluate();
			}
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
