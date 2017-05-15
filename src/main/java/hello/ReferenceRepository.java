package hello;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import hello.Reference;

// This will be AUTO IMPLEMENTED by Spring into a Bean called referenceRepository
// CRUD refers Create, Read, Update, Delete

@Transactional
public interface ReferenceRepository extends CrudRepository<Reference, Long> {
	public Reference findById(Integer id);
	public List<Reference> findByAuthor(String author);
	public List<Reference> findByTitle(String title);
	public List<Reference> findByYear(Integer year);
	public List<Reference> findByJournal(String journal);
}
