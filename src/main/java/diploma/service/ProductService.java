package diploma.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import diploma.entity.Product;
import diploma.entity.UserProduct;
import diploma.repository.ProductRepository;
import diploma.repository.UserProductRepository;

@Service
public class ProductService {

	@Autowired
	private UserProductRepository userProductDao;

	@Autowired
	private ProductRepository productDao;
	
	public List<Product> findProductsOfUser(long id){
		ArrayList<Product> products = new ArrayList<>();
		for(UserProduct ur : userProductDao.findAll()) {
			if(ur.getUser().getId() == id) {
				products.add(ur.getProduct());
			}
		}
		return products;
	}
	
	public Page<Product> findAll(int page, int count){
		Pageable pages = new PageRequest(page, count);
		return productDao.findAll(pages); 
	}
	
}
