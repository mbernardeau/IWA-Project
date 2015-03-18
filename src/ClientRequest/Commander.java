package ClientRequest;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import DataRetrieving.Prefixer;

/**
 * Servlet implementation class Commander
 */
@WebServlet("/Commander")
public class Commander extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String LOCAL_DB_SERVER = "http://localhost:8181/openrdf-sesame";
	private static final String DB_NAME = "Battles";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Commander() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
		Repository sesameServer = new HTTPRepository(LOCAL_DB_SERVER,DB_NAME);  
		sesameServer.initialize(); 
		
		RepositoryConnection rConn = sesameServer.getConnection();
		
		GraphQuery query = rConn.prepareGraphQuery(QueryLanguage.SPARQL, createSparqlRequest(request));
		query.setIncludeInferred(true);
		GraphQueryResult res = query.evaluate();
		
		Map<String, JSONObject> map = new LinkedHashMap<String, JSONObject>();
		while(res.hasNext()){
			Statement s = res.next();
			if(!map.containsKey(s.getSubject().toString())){
				map.put(s.getSubject().toString(), new JSONObject());
			}
			
			if(map.get(s.getSubject().toString()).containsKey(s.getPredicate().toString())){
				// Value of the given property already defined, let's keep the longest one (more precise for floats)
				if(s.getObject().toString().length() > map.get(s.getSubject().toString()).get(s.getPredicate().toString()).toString().length() ){
					map.get(s.getSubject().toString()).put(s.getPredicate().toString(), s.getObject().toString());
				}
			}else{
				map.get(s.getSubject().toString()).put(s.getPredicate().toString(), s.getObject().toString());
			}
		}
		
		response.getWriter().append(JSONValue.toJSONString(map));
		
		}catch(QueryEvaluationException e){
			
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private String createSparqlRequest(HttpServletRequest request) {
		String s = "";
		if(request.getParameter("s") != null){
			s = request.getParameter("s");
		}
		String result = Prefixer.INSTANCE.toString() +  "\nCONSTRUCT{ ?commander ?pred ?obj } "+
				"WHERE{ ?commander a btl:Commander ; ?pred ?obj ; rdfs:label ?name . FILTER(regex(?name, \""+s+"\", \"i\")) }"
				;
		return result;
	}


}
