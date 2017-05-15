package hello;

/**
 * Search query object. 
 * Consists of a query type (author, title, year etc.) and search term.
 */
public class Query {	
	private QueryType queryType;
	private String searchTerm;
	
	public Query() {}
	public Query(QueryType queryType, String searchTerm) {
		this.queryType = queryType;
		this.searchTerm = searchTerm;
	}
	
	public QueryType getQueryType() {
		return queryType;
	}
	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}
	public String getSearchTerm() {
		return searchTerm;
	}
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	
	@Override
	public String toString() {
		return "Query Type: " + queryType + " Search Term: " + searchTerm;
	}
}
