package com.springproject.eshop.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.springproject.eshop.form.ProductForm;
import com.springproject.eshop.model.Authority;
import com.springproject.eshop.model.Basket;
import com.springproject.eshop.model.Product;
import com.springproject.eshop.security.Role;
import com.springproject.eshop.security.SecurityConfig;
import com.springproject.eshop.service.ProductService;
import com.springproject.eshop.service.UserDetailsServiceImpl;
import com.springproject.eshop.web.ProductController;
import com.springproject.eshop.model.User;

@RunWith(SpringRunner.class)
@WebMvcTest(ProductController.class)
@ContextConfiguration(classes = { UserDetailsServiceImpl.class, ProductController.class, ProductService.class,
		SecurityConfig.class })
public class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService productService;

	@MockBean
	private UserDetailsServiceImpl userService;

	@Before
	public void setup() {
		Set<Authority> authorities = new HashSet<>();
		authorities.add(new Authority(1L, Role.ROLE_EMPLOYEE));
		when(userService.loadUserByUsername("testUser"))
				.thenReturn(new User(1L, "testUser", "testPassword", new Basket(), authorities));
	}

	@Test
	public void testThatGetProductsReturnsTheProductPageWithProducts() throws Exception {
		List<Product> products = new ArrayList<>();
		products.add(new Product());
		products.add(new Product());
		products.add(new Product());
		products.forEach(product -> {
			product.setImage(new byte[1]);
		});

		when(productService.getAllProducts()).thenReturn(products);
		mockMvc.perform(get("/products")).andExpect(status().isOk()).andExpect(view().name("index"))
				.andExpect(model().attribute("products", products));
	}

	@Test
	public void TestThatCreateProductReturnsCreateProductPageAndFormObject() throws Exception {
		mockMvc.perform(get("/product/create")).andExpect(status().isOk()).andExpect(view().name("createProduct"))
				.andExpect(model().attribute("productForm", new ProductForm()));
	}

	@Test
	public void TestThatPostCreateProductCorrectlyProcessesTheFormAndReturnsTheUserToTheProductsPage()
			throws Exception {

		ProductForm productForm = new ProductForm();
		productForm.setName("name");
		productForm.setDescription("description");
		productForm.setCategory("category");
		productForm.setPrice(new BigDecimal(1.00));
		productForm.setImage(new MockMultipartFile("data", "other-file-name.data", "text/plain", "image".getBytes()));

		when(productService.create(productForm.toProduct())).thenReturn(productForm.toProduct());
		mockMvc.perform(post("/product/create").flashAttr("productForm", productForm).with(csrf()))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/products"));

	}

	@Test
	public void testThatPostCreateProductReturnsErrorsWhenDataIsInvalid() throws Exception {
		ProductForm productForm = new ProductForm();
		mockMvc.perform(post("/product/create").flashAttr("productForm", productForm).with(csrf()))
				.andExpect(status().isOk()).andExpect(view().name("createProduct"));
	}

}
