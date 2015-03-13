package DataRetrieving;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;

/**
 * 
 * @author Mathias
 *
 * @param <T>
 */
public abstract class SparqlQueryer<T> {
	private static final Logger logger = LogManager.getLogger(SparqlQueryer.class);
	private final int LIMIT_PER_ITERATION  = 1000;
	
	protected String service;
	protected Query query;
	protected RepositoryConnection connection;
	protected NtripleSPARQLRepository repository;
	protected List<T> results;
	
	public SparqlQueryer(String service){
		this.service = service;
		repository = new NtripleSPARQLRepository(service);
		connection = new SPARQLConnection(repository);
		results = new LinkedList<T>();
	}
	
	protected void prepareQuery(String query, int limit, int offset){
		try {
			this.query = connection.prepareQuery(QueryLanguage.SPARQL, Prefixer.INSTANCE.toString()+ query + "\nLIMIT "+limit+"\nOFFSET "+offset+"\n");
		} catch (RepositoryException e) {
			logger.error("The DBPedia respository does not seem to be up.\n", e);
		} catch (MalformedQueryException e) {
			logger.error("The query is malformed : "+query+"\n", e);
		}
	}
	
	public List<T> getResult(){
		return results;
	}
	
	
	public void query(String query){
		int numberOfResults;
		int i = 0;
		QueryResult<T> result;
		do{
			System.out.print("Querying iteration "+i+"\n");
			numberOfResults = 0;
			result = queryWithLimit(query, LIMIT_PER_ITERATION, LIMIT_PER_ITERATION * i);
			try {
				while(result.hasNext()){
					numberOfResults++;
					results.add(result.next());
				}
				System.out.print("   Found "+numberOfResults+" results.\n");
			} catch (QueryEvaluationException e) {
				logger.error("Could not execute query : "+query, e);
				// Skipping the rest of this iteration
				numberOfResults = LIMIT_PER_ITERATION;
				i++;
				continue;
			}
			
			i++;
		}while(numberOfResults >= LIMIT_PER_ITERATION-1);
		System.out.print("Query done. Total of "+results.size()+" results found.");
	}
	
	/**
	 * Queries the service with the given query, and returns the result.
	 * @param query The query
	 * @return the number of results
	 */
	protected abstract QueryResult<T> queryWithLimit(String query, int limit, int offset);
}
