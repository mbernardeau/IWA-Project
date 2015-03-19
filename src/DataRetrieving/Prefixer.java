package DataRetrieving;

import java.util.ArrayList;
import java.util.List;

/***
 * Prefixing singleton for prefixes in sparql queries
 * @author Mathias
 *
 */
public class Prefixer {
	public static Prefixer INSTANCE = new Prefixer();
	private List<Prefix> prefixes;
	
	/**
	 * Private constructor
	 */
	private Prefixer(){
		prefixes = new ArrayList<Prefix>();
		this.addPrefix("dbpedia", "http://dbpedia.org/resource/");
		this.addPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
		this.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		this.addPrefix("dbo", "http://dbpedia.org/ontology/");
		this.addPrefix("btl", "http://battles.com/");
		this.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
		this.addPrefix("foaf", "http://xmlns.com/foaf/0.1/");
		this.addPrefix("dbpedia-owl", "http://dbpedia.org/ontology/");
		this.addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		this.addPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
		this.addPrefix("dbpprop", "http://dbpedia.org/property/");
	}
	
	
	 public String getURL(String prefix){
		 if(prefixes.contains(new Prefix(prefix, null))){
			 return prefixes.get(prefixes.indexOf(new Prefix(prefix, null))).url;
		 }else{
			 return null;
		 }
	 }
	 
	/**
	 * Add the prefix to the list
	 * @param prefix Prefix
	 * @param url Full URL corresponding to the prefix
	 */
	public void addPrefix(String prefix, String url) {
		Prefix p = new Prefix(prefix, url);
		if(!prefixes.contains(p))
			prefixes.add(p);
		else if(!prefixes.get(prefixes.indexOf(p)).url.equals(url))
			throw new IllegalArgumentException("The prefix '"+prefix+"' you try to add already exists with a different URI.");
	}
	
	/**
	 * Remove the given prefix
	 * @param prefix The prefix
	 */
	public void removePrefix(String prefix){
		prefixes.remove(new Prefix(prefix, null));
	}
	
	/**
	 * Delete all the prefixes
	 */
	public void clearPrefixes(){
		prefixes.clear();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("\n");
		for(Prefix p : prefixes){
			sb.append(p.toString());
		}
		return sb.toString();
	}
	
	
	private class Prefix{
		public String prefix;
		public String url;
		
		public Prefix(String prefix, String url){
			this.prefix = prefix;
			this.url = url;
		}
		
		@Override
		public String toString(){
			return "PREFIX "+ prefix + ": <"+ url + ">\n";
		}
		
		public boolean equals(Object o){
			if(o instanceof Prefix){
				if(((Prefix)o).prefix.equals(this.prefix)){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}
	}
}
