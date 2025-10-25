package uk.co.jste.echibernate.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductTest {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void shouldCreateProductWithPrice() {
    UUID newProductsId = createNewProductWithInitialPrice();
    var theNewProduct = loadNewProduct(newProductsId);

    assertThat(theNewProduct.productPriceHistory()).hasSize(1);
    assertThat(theNewProduct.effectivePriceAsOf(Instant.now())).isPresent();
    assertThat(theNewProduct.effectivePriceAsOf(Instant.now()).get()).isEqualTo(new BigDecimal("10"));
  }

  @Test
  void shouldCascadeRemovals() {
    // a JpaSystemException with a route cause of HibernateException
    // will be thrown if the adapter is not unwrapped
    assertDoesNotThrow(() -> {
      UUID savedId = createNewProductWithInitialPrice();

      transactionTemplate.executeWithoutResult(_ -> {
        var saved = loadNewProduct(savedId);
        saved.mutableProductPriceHistory().removeFirst();
        productRepository.saveAndFlush(saved);
      });

      transactionTemplate.executeWithoutResult(_ -> {
        var save = loadNewProduct(savedId);
        assertNotNull(save);
        assertNotNull(save.getId());
        assertThat(save.productPriceHistory()).isEmpty();
        assertThat(save.effectivePriceAsOf(Instant.now())).isEmpty();
      });
    });
  }

  @Test
  void shouldBeAbleToUpdatePrice() {
    UUID savedId = createNewProductWithInitialPrice();
    var saved = loadNewProduct(savedId);

    // One initial price should be present. Create a new price:
    var firstPriceUpdate = Instant.now();
    saved.updatePriceEffective(firstPriceUpdate, new BigDecimal("20.00"));

    // And another 10 days later
    var tenDaysLater = firstPriceUpdate.plus(10, ChronoUnit.DAYS);
    saved.updatePriceEffective(tenDaysLater, new BigDecimal("30.00"));

    // And another ten days after the last update
    var anotherTenDaysLater = tenDaysLater.plus(10, ChronoUnit.DAYS);
    saved.updatePriceEffective(anotherTenDaysLater, new BigDecimal("40.00"));

    Product save = productRepository.saveAndFlush(saved);

    assertAll(
        // Expected size
        () -> assertThat(save.productPriceHistory()).hasSize(4),
        // First price
        () -> assertThat(save.effectivePriceAsOf(firstPriceUpdate)).isPresent(),
        () -> assertThat(save.effectivePriceAsOf(firstPriceUpdate).get()).isEqualTo(new BigDecimal("20.00")),
        // Second price
        () -> assertThat(save.effectivePriceAsOf(tenDaysLater)).isPresent(),
        () -> assertThat(save.effectivePriceAsOf(tenDaysLater).get()).isEqualTo(new BigDecimal("30.00")),
        // Third price
        () -> assertThat(save.effectivePriceAsOf(anotherTenDaysLater)).isPresent(),
        () -> assertThat(save.effectivePriceAsOf(anotherTenDaysLater).get()).isEqualTo(new BigDecimal("40.00"))
    );
  }

  //region Test helpers
  private UUID createNewProductWithInitialPrice() {
    return transactionTemplate.execute(tx -> {
      Product product = Product.builder()
          .withName("Test Product")
          .withInitialPrice(BigDecimal.TEN)
          .build();

      Product save = productRepository.saveAndFlush(product);

      assertNotNull(save);
      assertNotNull(save.getId());

      return save.getId();
    });
  }

  private Product loadNewProduct(UUID newProductsId) {
    return productRepository.findById(newProductsId).orElseThrow();
  }
  //endregion
}