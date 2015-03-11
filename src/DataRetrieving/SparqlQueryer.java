package DataRetrieving;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

public abstract class SparqlQueryer<T> {
	private static final Logger logger = LogManager.getLogger(SparqlQueryer.class);
	protected String service;
	protected Query query;
	protected RepositoryConnection connection;
	protected QueryResult<T> result;
	
	public SparqlQueryer(String service){
		this.service = service;
		connection = new SPARQLConnection(new SPARQLRepository(service));
	}
	
	protected void prepareQuery(String query){
		try {
			this.query = connection.prepareQuery(QueryLanguage.SPARQL, Prefixer.INSTANCE.toString()+ query);
		} catch (RepositoryException e) {
			logger.error("The DBPedia respository does not seem to be up.\n", e);
		} catch (MalformedQueryException e) {
			logger.error("The query is malformed : "+query+"\n", e);
		}
	}
	
	public  QueryResult<T> getResult(){
		return result;
	}
	
	public abstract void query(String query);
}
