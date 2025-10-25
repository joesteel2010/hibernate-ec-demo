package uk.co.jste.echibernate;

import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.jste.echibernate.catalog.Product;
import uk.co.jste.echibernate.catalog.ProductRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
public class EcHibernateApplication {

  @Configuration
  public static class DataBootstrapper {
    private final Faker faker = new Faker();
    private final Instant currentInstant = Instant.now();
    private final ProductRepository productRepository;
    private final long toGenerate;

    public DataBootstrapper(ProductRepository productRepository, @Value("${app.products-to-generate:100}") long toGenerate) {
      this.productRepository = productRepository;
      this.toGenerate = toGenerate;
    }

    @PostConstruct
    @Transactional
    public void init() {
      MutableList<Product> allProducts = Stream.generate(this::createProduct)
          .limit(toGenerate)
          .collect(Collectors2.toList());
      productRepository.saveAllAndFlush(allProducts);
    }

    private Product createProduct() {
      return Product.builder()
          .withName(faker.book().title())
          .withInitialPrice(BigDecimal.valueOf(faker.number().numberBetween(1d, 100d)))
          .withPriceEffective(faker.timeAndDate().between(currentInstant.minus(5 * 365, ChronoUnit.DAYS), currentInstant))
          .build();
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(EcHibernateApplication.class, args);
  }
}
