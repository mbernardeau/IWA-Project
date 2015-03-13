package DataRetrieving;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResult;

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
	 * Saves the result into the stardog local database
	 */
	void save(){
		int i=0;
		try {
			for (Statement s : this.getResult()) {
				try {
					stardogConnection.add().statement(s);
				} catch (StardogException e) {
					logger.error("Unable to save a statement.\n", e);
				}
				if(i%1000 == 0){
					stardogConnection.commit();
					stardogConnection.begin();
					System.out.println(i+" triples saved to database.");
				}
				i++;
				
			}
			stardogConnection.commit();
			stardogConnection.begin();
		} catch (StardogException e) {
			logger.error("The commit of the new triples failed.\n", e);
		}
		
	}


	@Override
	protected QueryResult<Statement> queryWithLimit(String query, int limit, int offset) {
		QueryResult<Statement> res = null;
		try {
			this.prepareQuery(query, limit, offset);
			
			if (this.query instanceof GraphQuery) {
			    res = ((GraphQuery) this.query).evaluate();
			}
		} catch (QueryEvaluationException e) {
			logger.error("The query could not be evaluated.\n", e);
		}
		return res;
	}
}
