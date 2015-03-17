package DataRetrieving;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.complexible.stardog.StardogException;

public class SparqlConstructQueryer extends SparqlQueryer<Statement> {
	private static final Logger logger = LogManager.getLogger(SparqlConstructQueryer.class);
	RepositoryConnection stardogConnection;
	
	/**
	 * Constructor of the SparqlQueryer class
	 * @param service The sparql endpoint to query
	 * @param stardogConnection The connection to the stardog local repository
	 */
	public SparqlConstructQueryer(String service, RepositoryConnection stardogConnection) {
		super(service);
		this.stardogConnection = stardogConnection;
	}
	
	
	/**
	 * Saves the result into the stardog local database
	 * @throws StardogException 
	 * @throws RepositoryException 
	 */
	void save() throws RepositoryException{
		stardogConnection.begin();
		int i=0;
		for (Statement s : this.getResult()) {
			stardogConnection.add(s);
			if(i%1000 == 0 && i!=0){
				stardogConnection.commit();
				stardogConnection.begin();
				System.out.println("\n"+i+" triples saved to database.");
			}
			i++;
			
		}
		stardogConnection.commit();
		System.out.println("\nSaving Operation done : total of "+ i+" triples saved to database.");
		
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
