package hello;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.DigitStringValue;
import org.jbibtex.StringValue;
import org.jbibtex.StringValue.Style;
import org.jbibtex.Key;
import org.jbibtex.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(path="/demo")
public class MainController {
	@Autowired // Get referenceRepository bean (auto-generated by Spring)
	private ReferenceRepository referenceRepository;
	
	// Directory to import/export BibTeX files
	private static String UPLOADED_FOLDER = 
			"/Users/Ken/Documents/workspace/Jabref/gs-accessing-data-mysql/complete/bibtex/import/";
	private static String EXPORT_FOLDER = 
			"/Users/Ken/Documents/workspace/Jabref/gs-accessing-data-mysql/complete/bibtex/export/";
	
	// Home page
	@GetMapping(path="/biblio")
	public String create(Model model) {
		try {
			Iterable<Reference> references = referenceRepository.findAll();
			model.addAttribute("reference", new Reference());
			model.addAttribute("references", references);
		}
		catch (Exception ex) {
			return "Error creating reference: " + ex.toString();
		}
	
		return "biblio";
	}
	
	
	// Add entry
	@PostMapping(path="/biblio", params="add")
	public String biblioAdd(@Valid @ModelAttribute Reference reference, BindingResult bindingResult) {
		// Form validation
		if (bindingResult.hasErrors()) {
			return "biblio";
		}
		
		// Check if entry already exists
		List<Reference> references = referenceRepository.findByTitle(reference.getTitle());
		if (references.isEmpty()) {
			referenceRepository.save(reference);
		}
		else {
			boolean duplicate = false;
			for (Reference r : references) {
				if (r.equals(reference)) {
					duplicate = true;
				}
			}
			if (!duplicate) {
				referenceRepository.save(reference);
			}
		}
		
		return "redirect:biblio";
	}
	
	// Delete entry
	@PostMapping(path="/biblio", params="delete")
	public String biblioDelete(@RequestParam("entryNumber") Integer entryNumber) {
		Iterable<Reference> references = referenceRepository.findAll();
		List<Reference> referenceList = Lists.newArrayList(references);
		referenceRepository.delete(referenceList.get(entryNumber - 1)); // Display begins at 1
		return "redirect:biblio";
	}
	
	// Edit entry
	@GetMapping(path="/edit")
	public String edit(Model model, @RequestParam("entryNumber") Integer entryNumber) {
		Iterable<Reference> references = referenceRepository.findAll();
		List<Reference> referenceList = Lists.newArrayList(references);
		Reference reference = referenceList.get(entryNumber - 1); // Display begins at 1
		model.addAttribute("reference", reference);
		return "edit";
	}
	
	// Process edit
	@PostMapping(path="/edit/{id}")
	public String makeChanges(Model model, @ModelAttribute Reference reference, @PathVariable("id") Integer id) {
		Reference edit = referenceRepository.findById(id);
		edit.setAuthor(reference.getAuthor());
		edit.setTitle(reference.getTitle());
		edit.setYear(reference.getYear());
		edit.setJournal(reference.getJournal());
		referenceRepository.save(edit);
		model.addAttribute(edit);
		return "edit";
	}
	
	// Search entry
	@GetMapping(path="/search")
	public String getByAuthor(Model model) {
		try {
			model.addAttribute("query", new Query());
		}
		catch (Exception ex) {
			return "Error searching: " + ex.toString();
		}
		return "search";
	}
	
	// Process search
	@PostMapping(path="/search")
	public String showResults(Model model, @ModelAttribute Query query) {
		List<Reference> references;
		switch (query.getQueryType()) {
			case AUTHOR:
				references = referenceRepository.findByAuthor(query.getSearchTerm());
				model.addAttribute("references", references);
				break;
			case TITLE:
				references = referenceRepository.findByTitle(query.getSearchTerm());
				model.addAttribute("references", references);
				break;
			case YEAR:
				references = referenceRepository.findByYear(Integer.valueOf(query.getSearchTerm()));
				model.addAttribute("references", references);
				break;
			case JOURNAL:
				references = referenceRepository.findByJournal(query.getSearchTerm());
				model.addAttribute("references", references);
				break;
			default:
				break;
		}
				
		return "result";
	}
	
	// Import BibTeX file
	@GetMapping(path="/import")
    public String importBibTex() {
        return "import";
    }
	
	// Process imported file
	@PostMapping("/import")
    public String processImportBibTex(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Get the file and save
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);

            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
            parseFile(UPLOADED_FOLDER + file.getOriginalFilename());

        } 
        catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:importStatus";
    }
	
	// Display import status
	@GetMapping("/importStatus")
    public String importStatus() {
        return "importStatus";
    }
	
	// Export BibTeX File
	@GetMapping("/export")
	public String exportBibTex() {
		return "export";
	}
	
	// Process exported file
	@PostMapping("/export") 
	public String processExportBibTex(@RequestParam("fileName") String fileName,
									RedirectAttributes redirectAttributes) {
		try {
			// setup
            File output = new File(EXPORT_FOLDER + fileName + ".bib");
            Writer writer = new FileWriter(output);
            BibTeXFormatter formatter = new BibTeXFormatter();
            
            // make database
            Iterable<Reference> references = referenceRepository.findAll();
            Iterator<Reference> it = references.iterator();
            BibTeXDatabase database = new BibTeXDatabase();
            while (it.hasNext()) {
            	Reference reference = it.next();
            	String author = reference.getAuthor();
            	String title = reference.getTitle();
            	String year = reference.getYear().toString();
            	String journal = reference.getJournal();
            	String key = generateKey(author, year);
            	BibTeXEntry entry = new BibTeXEntry(new Key("article"), new Key(key));
                entry.addField(new Key("author"), new StringValue(author, Style.BRACED));
                entry.addField(new Key("title"), new StringValue(title, Style.BRACED));
                entry.addField(new Key("year"), new DigitStringValue(year.toString()));
                entry.addField(new Key("journal"), new StringValue(journal, Style.BRACED));
                database.addObject(entry);
            }
            
            formatter.format(database, writer);
			writer.close();
			
            redirectAttributes.addFlashAttribute("message",
                    "You successfully exported '" + fileName + ".bib'");
    
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
		
		return "redirect:exportStatus";
	}
	 
	// Display export status
	@GetMapping("/exportStatus")
	public String exportStatus() {
		return "exportStatus";
	}
	
	// Convert MySQL repository to BibTeX database
	private void parseFile(String filePath) {
		try {
			Reader reader = new FileReader(filePath);
			BibTeXParser parser = new BibTeXParser();
			BibTeXDatabase database = parser.parse(reader);
			reader.close();
			
			Map<Key, BibTeXEntry> entryMap = database.getEntries();
			Collection<org.jbibtex.BibTeXEntry> entries = entryMap.values();
			
			for (BibTeXEntry entry : entries) {
				Value author = entry.getField(BibTeXEntry.KEY_AUTHOR);
				Value title = entry.getField(BibTeXEntry.KEY_TITLE);
				Value year = entry.getField(BibTeXEntry.KEY_YEAR);
				Value journal = entry.getField(BibTeXEntry.KEY_JOURNAL);
				
				Reference reference = new Reference();
				reference.setAuthor(author.toUserString());
				reference.setTitle(title.toUserString());
				reference.setYear(Integer.valueOf(year.toUserString()));
				reference.setJournal(journal.toUserString());
				referenceRepository.save(reference);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	// Generate citation key (part of BibTeX standard)
	// First author followed by year
	private String generateKey(String author, String year) {
		int i = author.indexOf(' ');
		if (i != -1) {
			String aut = author.substring(0, i);
			return aut + "-" + year;
		}
		else {
			return author + "-" + year;
		}
	}
}
