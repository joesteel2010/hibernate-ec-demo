package uk.co.jste.echibernate.catalog;

import jakarta.persistence.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.jspecify.annotations.NonNull;
import uk.co.jste.echibernate.misc.EcAdapterUnwrapper;
import uk.co.jste.echibernate.time.TimeDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static jakarta.persistence.AccessType.PROPERTY;

@Entity
@Access(PROPERTY) // Vital for allowing us to adapt the collection types to EC types
public class Product implements TemporalView<Product, Product.ProductSnapshot> {

  private UUID id = UUID.randomUUID();

  @NonNull
  private String name = "";

  @NonNull
  private MutableList<ProductPrice> productPrices = Lists.mutable.empty();

  private Product(Builder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.updatePriceEffective(builder.priceEffective, builder.initialPrice);
  }

  @Id
  public @NonNull UUID getId() {
    return id;
  }

  ImmutableList<ProductPrice> productPriceHistory() {
    return productPrices.toImmutable();
  }

  MutableList<ProductPrice> mutableProductPriceHistory() {
    return productPrices;
  }

  public void updatePriceEffective(Instant newPriceEffectiveFrom, BigDecimal theNewPrice) {
    endCurrentEffectivePriceAt(newPriceEffectiveFrom);
    ProductPrice productPrice = new ProductPrice();
    productPrice.setProduct(this);
    productPrice.setEffectiveFrom(newPriceEffectiveFrom);
    productPrice.setPrice(theNewPrice);
    productPrices.add(productPrice);
  }

  private void endCurrentEffectivePriceAt(Instant givenInstant) {
    Optional<ProductPrice> optCurrentPrice = effectivePrice(givenInstant);
    optCurrentPrice.ifPresent(theCurrentPrice -> theCurrentPrice.setEffectiveTo(givenInstant));
  }

  Optional<ProductPrice> effectivePrice(Instant asOfInstant) {
    return productPrices
        .detectWithOptional(ProductPrice::isEffectiveAt, asOfInstant);
  }

  public Optional<BigDecimal> effectivePriceAsOf(Instant asOfInstant) {
    return effectivePrice(asOfInstant).map(ProductPrice::getPrice);
  }

  @Override
  public ProductSnapshot toSnapshotAt(Instant atInstance) {
    return new ProductSnapshot(id, name, effectivePriceAsOf(atInstance));
  }

  public static Builder builder() {
    return new Builder();
  }

  //region Snapshot record
  public record ProductSnapshot(UUID id, String name, Optional<BigDecimal> productPrice) {
  }
  //endregion

  //region Product builder
  public static class Builder {
    @NonNull
    private UUID id = UUID.randomUUID();

    @NonNull
    private String name = "";

    @NonNull
    private Instant priceEffective = TimeDefaults.START_OF_TIME;

    @NonNull
    private BigDecimal initialPrice = BigDecimal.ZERO;

    Builder() {}

    public Builder withId(UUID id) {
      this.id = Objects.requireNonNull(id, "id must not be null");
      return this;
    }

    public Builder withName(String name) {
      this.name = Objects.requireNonNull(name, "name must not be null");
      return this;
    }

    public Builder withPriceEffective(Instant asOfInstant) {
      this.priceEffective = Objects.requireNonNull(asOfInstant, "priceEffective must not be null");
      return this;
    }

    public Builder withInitialPrice(BigDecimal initialPrice) {
      this.initialPrice = Objects.requireNonNull(initialPrice, "initialPrice must not be null");
      return this;
    }

    public Product  build() {
      return new Product(this);
    }
  }
  //endregion

  //region Constructor and accessors for Hibernate mappings
  protected Product() {}

  private void setId(@NonNull UUID id) {
    this.id = id;
  }

  public @NonNull String getName() {
    return name;
  }

  public void setName(@NonNull String name) {
    this.name = name;
  }

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private @NonNull List<ProductPrice> getProductPrices() {
    List<ProductPrice> unwrapped = EcAdapterUnwrapper.unwrap(productPrices);
    return Objects.requireNonNullElse(unwrapped, productPrices);
  }

  private void setProductPrices(@NonNull List<ProductPrice> productPrices) {
    this.productPrices = ListAdapter.adapt(productPrices);
  }
  //endregion
}
