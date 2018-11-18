package diploma.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

//import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import diploma.entity.Product;
import diploma.entity.User;
import diploma.entity.UserProduct;
import diploma.repository.ProductRepository;
import diploma.repository.UserProductRepository;
import diploma.repository.UserRepository;
import diploma.responses.Response;
import diploma.security.jwt.JwtUtil;
import diploma.service.ProductService;
import diploma.service.UserService;

@RequestMapping("/api/auth")
@RestController
@CrossOrigin(origins = "*")
public class UserController {

	@Autowired
	UserRepository userDao;

	@Autowired
	ProductRepository productDao;

	@Autowired
	UserProductRepository userProductDao;

	@Autowired
	ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	ProductRepository productRepository;

	@Autowired
	UserProductRepository userProductRepository;
	
	//DELETE FROM CART					//DELETE FROM CART					//DELETE FROM CART
	@GetMapping(value="/deleteFromCart/{userId}/{productId}")
	public void deleteFromCart(@PathVariable long userId, @PathVariable long productId) {
		for(UserProduct userPro : userProductRepository.findAll()) {
			if(userPro.getUser().getId() == userId && userPro.getProduct().getId() == productId) {
				userProductRepository.delete(userPro);
			}
		}
	}
	//DELETE FROM CART					//DELETE FROM CART					//DELETE FROM CART
	//F	  I	 L	T	E	R	I	N	G
	@GetMapping(value = "/products/asc/{page}/{count}")
	public ResponseEntity<Response<Page<Product>>> findAllProductsAsc(@PathVariable int page, @PathVariable int count) {
		Pageable pages = new PageRequest(page, count);
		Response<Page<Product>> response = new Response<Page<Product>>();
		Page<Product> prods = productRepository.findAllByOrderByPriceAsc(pages);
		response.setData(prods);
		return ResponseEntity.ok(response);
	}

	
	
	@GetMapping(value = "/products/desc/{page}/{count}")
	public ResponseEntity<Response<Page<Product>>> findAllProductsDesc(@PathVariable int page, @PathVariable int count) {
		Pageable pages = new PageRequest(page, count);
		Response<Page<Product>> response = new Response<Page<Product>>();
		Page<Product> prods = productRepository.findAllByOrderByPriceDesc(pages);
		response.setData(prods);
		return ResponseEntity.ok(response);
	}
	//F	  I	 L	T	E	R	I	N	G

	//@GetMapping(value = "/products/{category}/{page}/{count}")
	@GetMapping(value = "/products/category/{category}/{page}/{count}")
	public ResponseEntity<Response<Page<Product>>> findByCategory(
			@PathVariable String category,
			@PathVariable int page, 
			@PathVariable int count) {
		Pageable pages = new PageRequest(page, count);
		Response<Page<Product>> response = new Response<Page<Product>>();
		Page<Product> prods = productRepository.findByCategory(category, pages);
		response.setData(prods);
		return ResponseEntity.ok(response);
	}
	
	//F	  I	 L	T	E	R	I	N	G
	
	
	
	
	@PostMapping
	@RequestMapping(value = "/newUser")
	public ResponseEntity<Response<User>> create(HttpServletRequest request, @RequestBody User user,
			BindingResult result) {
		Response<User> response = new Response<User>();
		try {
			validateCreateUser(user, result);
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User userPersisted = (User) userService.createUser(user);
			response.setData(userPersisted);
		} catch (DuplicateKeyException dE) {
			response.getErrors().add("E-mail already registered");
			return ResponseEntity.badRequest().body(response);
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
		return ResponseEntity.ok(response);
	}

	private void validateCreateUser(User user, BindingResult result) {
		if (user.getEmail() == null && user.getPassword() == null) {
			result.addError(new ObjectError("User", "Email no information"));
			result.addError(new ObjectError("User", "Password no information"));
		} else if (user.getPassword() == null) {
			result.addError(new ObjectError("User", "Password no information"));
		} else if (user.getEmail() == null) {
			result.addError(new ObjectError("User", "Email no information"));
		}
	}


	@GetMapping("/addToCart/{productId}")
	//@PreAuthorize("hasAnyRole('USER')")
	public ResponseEntity<Response<UserProduct>> addToCart(@PathVariable long productId, HttpServletRequest request) {
		Response<UserProduct> response = new Response<UserProduct>();
		String authToken = null;
		try {			
			authToken = request.getHeader("Authorization");
		}catch (NullPointerException e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
		String email = jwtUtil.getUsernameFromToken(authToken);
		User user = userDao.findByEmail(email);
		response.setData(userProductDao.save(new UserProduct(null, userDao.findOne(user.getId()), productDao.findOne(productId))));
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/users/{page}/{count}")
	public ResponseEntity<Response<Page<User>>> findAllUsers(@PathVariable int page, @PathVariable int count) {
		Response<Page<User>> response = new Response<Page<User>>();
		Page<User> users = userService.findAll(page, count);
		response.setData(users);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/products/{page}/{count}")
	public ResponseEntity<Response<Page<Product>>> findAllProducts(@PathVariable int page, @PathVariable int count) {
		Response<Page<Product>> response = new Response<Page<Product>>();
		Page<Product> products = productService.findAll(page, count);
		response.setData(products);
		return ResponseEntity.ok(response);
	}

	// ALL PRODUCTS OF USERS
	@GetMapping("/allCart")
	public List<UserProduct> findUserProducts() {
		return userProductDao.findAll();
	}

	// RETURNS ALL PRODUCTS OF SPECIFIC USER
	/*@GetMapping("/cart")
	@PreAuthorize("hasAnyRole('USER')")
	public List<Product> findProductsOfUser(HttpServletRequest request) {
		String authToken = request.getHeader("Authorization");
		String email = jwtUtil.getUsernameFromToken(authToken);
		User user = userDao.findByEmail(email);
		return productService.findProductsOfUser(user.getId());
	}
	*/
			
	// RETURNS ALL PRODUCTS OF SPECIFIC USER not redundant
	@GetMapping("/cart")
	@PreAuthorize("hasAnyRole('USER')")
	public HashSet<Product> findProductsOfUser(HttpServletRequest request) {
		String authToken = request.getHeader("Authorization");
		String email = jwtUtil.getUsernameFromToken(authToken);
		User user = userDao.findByEmail(email);
		//TEST
		HashSet<Product> clean = new HashSet<>(productService.findProductsOfUser(user.getId()));
		//TEST
		return clean;
	}

	// RETURNS ALL THE PRODUCTS
	@GetMapping("/products")
	public List<Product> findProducts() {
		return productDao.findAll();
	}

	// RETURNS ONE PRODUCT
	@GetMapping("/products/{id}")
	public Product findProduct(@PathVariable long id) {
		return productDao.findOne(id);
	}

}
