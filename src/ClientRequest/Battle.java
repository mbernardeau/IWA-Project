package ClientRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
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

import com.complexible.common.base.DateTime;

import DataRetrieving.Prefixer;

/**
 * Servlet implementation class Battle
 */
@WebServlet("/Battle")
public class Battle extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String LOCAL_DB_SERVER = "http://localhost:8181/openrdf-sesame";
	private static final String DB_NAME = "Battles";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Battle() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		
		try {
			/*Connection aConn = ConnectionConfiguration
			        .to(DB_NAME)
			        .credentials("admin", "admin")
			        .server(LOCAL_DB_SERVER)
			        .reasoning(ReasoningType.SL)
			        .connect();
			*/
			Repository sesameServer = new HTTPRepository(LOCAL_DB_SERVER,DB_NAME);  
			sesameServer.initialize(); 
			
			RepositoryConnection rConn = sesameServer.getConnection();
			
			GraphQuery query = rConn.prepareGraphQuery(QueryLanguage.SPARQL, createSparqlRequest(request));
			query.setIncludeInferred(true);
			GraphQueryResult res = query.evaluate();
			
			//List<Statement> result = new ArrayList<Statement>();
			//ByteArrayOutputStream b = new ByteArrayOutputStream();
			//JSONLDWriter p = new JSONLDWriter(b);
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
			
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private String createSparqlRequest(HttpServletRequest request){
		boolean shortBattle = false;
		int isqno = -1;
		int year = 0;
		boolean hasDate = false;
		LocalDate minDate = null;
		LocalDate maxDate = LocalDate.now();
		int limit;
		
		if(request.getParameter("limit") != null){
			try{
				limit = Integer.valueOf(request.getParameter("limit"));
			}catch(NumberFormatException e){
				limit = 1000;
			}
		}else{
			limit = 1000;
		}

		if(request.getParameter("short") != null){
			try{
				shortBattle = BooleanUtils.toBoolean(request.getParameter("short"));
			}catch(NumberFormatException e){
				
			}
		}
		
		if(request.getParameter("year") != null){
			try{
				year = Integer.valueOf(request.getParameter("year"));
				hasDate=true;
				minDate= LocalDate.of(year, 1, 1);
				maxDate= LocalDate.of(year, 12, 31);
			}catch(NumberFormatException e){
				
			}
		}
		if(request.getParameter("minyear") != null){
			try{
				int minyear = Integer.valueOf(request.getParameter("minyear"));
				hasDate=true;
				minDate= LocalDate.of(minyear, 1, 1);
				
			}catch(NumberFormatException e){
				
			}
		}
		if(request.getParameter("maxyear") != null){
			try{
				int maxyear = Integer.valueOf(request.getParameter("maxyear"));
				hasDate=true;
				maxDate= LocalDate.of(maxyear, 12, 31);
				
			}catch(NumberFormatException e){
				
			}
		}
		
		if(request.getParameter("isqno") != null){
			try{
				isqno = Integer.valueOf(request.getParameter("isqno"));
			}catch(NumberFormatException e){
				isqno = -1;
				// Wrong format, doing normal request
			}
		}
		if(request.getParameter("year") != null){
			try{
				year = Integer.valueOf(request.getParameter("year"));
			}catch(NumberFormatException e){
				year = -1;
				// Wrong format, doing normal request
			}
		}
		String result = Prefixer.INSTANCE.toString() + "\nCONSTRUCT{ "+
						(shortBattle ? "?entity rdfs:label ?label ; geo:lat ?lat ; geo:long ?long ; btl:isqno ?isqno . " : " ?entity ?rel ?obj . " )+
						"}\nWHERE {" ;
		
		String subRequest = "?entity a btl:Battle ; \n";
		
		if(isqno != -1)
			subRequest += "\nbtl:isqno \""+isqno+"\"^^xsd:int ;\n";
		
		result += 		subRequest + 
							(shortBattle ? " rdfs:label ?label ; geo:lat ?lat ; geo:long ?long ; btl:isqno ?isqno " : "?rel ?obj " )+
							(hasDate ? ";\n dbpedia-owl:date ?date" : "")+
							"."+
							(hasDate ? "\nFILTER(?date >= \""+minDate+"\"^^xsd:date)\nFILTER(?date <= \""+maxDate+"\"^^xsd:date)\n" : "")
							+"}"+
						"LIMIT "+limit;
		
		return result;
	}

}
