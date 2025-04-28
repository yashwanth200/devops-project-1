package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category","categoryId",categoryId));
        Product productFromDb = productRepository.findByProductName(productDTO.getProductName());
        if (productFromDb != null)
            throw new APIException("Product name with  "+ productFromDb.getProductName() + " already exists");
        Product product = modelMapper.map(productDTO,Product.class);
        product.setImage("default.png");
        product.setCategory(category);
        double specialPrice = product.getPrice() - (product.getDiscount() * 0.01)*product.getPrice();
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,String sortBy,String sortOrder) {
        Sort sortByAndOrder= sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);

        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize,String sortBy,String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category","categoryId",categoryId));
        Sort sortByAndOrder= sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category ,pageDetails);
        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize,String sortBy,String sortOrder) {
        Sort sortByAndOrder= sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);
        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();
        ProductResponse productResponse= new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        //mapping ProductDTO object to Product class
        Product product = modelMapper.map(productDTO,Product.class);
        Product existingproduct= productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product","Product",productId));
        existingproduct.setProductName(product.getProductName());
        existingproduct.setDescription(product.getDescription());
        existingproduct.setQuantity(product.getQuantity());
        existingproduct.setPrice(product.getPrice());
        existingproduct.setPrice(product.getPrice()-(product.getDiscount()*0.01*product.getPrice()));
        Product updatedProduct = productRepository.save(existingproduct);
        List<Cart> carts = cartRepository.findCartByProductId(productId);
        List<CartDTO> cartDTOS = carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
                    List<ProductDTO> products = cart.getCartItems().stream()
                            .map(p -> modelMapper.map(p.getProduct(),ProductDTO.class))
                            .collect(Collectors.toList());
                    cartDTO.setProducts(products);
                    return cartDTO;
                }).collect(Collectors.toList());

        cartDTOS.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(),productId));


        return modelMapper.map(updatedProduct,ProductDTO.class);
    }



    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // DELETE
        List<Cart> carts = cartRepository.findCartByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {

        //Get the Product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","Product",productId));

        //upload image to server
        //Get the file name of upload image
        String fileName = fileService.uploadImage(path,image);
        //Updating the new file name to the product
        productFromDb.setImage(fileName);
        //Save Updated product
        Product updatedProduct= productRepository.save(productFromDb);
        //return DTO after mapping product to DTO
        return  modelMapper.map(updatedProduct,ProductDTO.class);
    }


}
