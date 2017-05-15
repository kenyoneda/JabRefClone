package hello;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

@Entity // Make table using this class
@Table(name="ref_example")
public class Reference {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    
	@NotEmpty
    @Size(max=50)
    private String author;
    
	@NotEmpty
    @Size(max=100)
    private String title;
    
	@NotNull
    @Min(1800)
    @Max(2017)
    private Integer year;
    
	@NotEmpty
    @Size(max=50)
    private String journal;
    
    public Integer getId() {
    	return id;
    }
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getYear() {
		return year;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public String getJournal() {
		return journal;
	}
	public void setJournal(String journal) {
		this.journal = journal;
	}
	
	@Override
	public String toString() {
		return "Author: " + author + " Title: " + title + 
				" Year: " + year + " Journal: " + journal;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (!(obj instanceof Reference)) {
			return false;
		}
		
		Reference r = (Reference) obj;
		return r.author.equals(author) &&
				r.title.equals(title) &&
				r.year.equals(year) &&
				r.journal.equals(journal);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(author, title, year, journal);
	}
}

