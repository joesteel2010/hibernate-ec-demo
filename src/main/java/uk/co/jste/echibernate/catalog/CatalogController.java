package uk.co.jste.echibernate.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class CatalogController {

  private final ProductRepository productRepository;

  public CatalogController(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @GetMapping
  public List<Product.ProductSnapshot> getProducts() {
    return productRepository.productSnapshots(Instant.now());
  }
}
