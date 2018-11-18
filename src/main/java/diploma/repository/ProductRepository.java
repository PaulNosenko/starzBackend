package diploma.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import diploma.entity.Product;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	Page<Product> findAllByOrderByPriceDesc(Pageable pages);
	
	Page<Product> findAllByOrderByPriceAsc(Pageable pages);
	
	Page<Product> findByCategory(String category, Pageable pages);
	
}
