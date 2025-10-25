package uk.co.jste.echibernate.catalog;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.co.jste.echibernate.time.TimeDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Access(AccessType.PROPERTY)
class ProductPrice {

  @NonNull
  private UUID id = UUID.randomUUID();

  @NonNull
  private BigDecimal price = BigDecimal.ZERO;

  @NonNull
  private Instant effectiveFrom = TimeDefaults.START_OF_TIME;

  @NonNull
  private Instant effectiveTo = TimeDefaults.END_OF_TIME;

  private Product product;

  boolean isEffectiveAt(Instant instant) {
    return !instant.isBefore(effectiveFrom) && effectiveTo.isAfter(instant);
  }

  //region accessors for Hibernate mappings
  @Id
  private @NonNull UUID getId() {
    return id;
  }

  private void setId(@NonNull UUID productId) {
    this.id = productId;
  }

  @NonNull BigDecimal getPrice() {
    return price;
  }

  public void setPrice(@NonNull BigDecimal price) {
    this.price = price;
  }

  @NonNull
  Instant getEffectiveFrom() {
    return effectiveFrom;
  }

  void setEffectiveFrom(@NonNull Instant effectiveFrom) {
    if ( effectiveFrom.isBefore(TimeDefaults.START_OF_TIME) ) {
      throw new IllegalArgumentException("effectiveFrom cannot be before " + TimeDefaults.START_OF_TIME);
    }
    this.effectiveFrom = effectiveFrom;
  }

  @NonNull
  Instant getEffectiveTo() {
    return effectiveTo;
  }

  void setEffectiveTo(@NonNull Instant effectiveTo) {
    if (effectiveTo.isBefore(effectiveFrom)) {
      throw new IllegalArgumentException("effectiveTo must be before effectiveFrom");
    }

    if (effectiveTo.isAfter(TimeDefaults.END_OF_TIME)) {
      throw new IllegalArgumentException("effectiveTo must be before " +  TimeDefaults.END_OF_TIME);
    }

    this.effectiveTo = effectiveTo;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  private @Nullable Product getProduct() {
    return product;
  }

  void setProduct(@Nullable Product product) {
    this.product = product;
  }

  public record ProductPriceSnapshot(UUID id, BigDecimal price, Instant effectiveFrom, Instant effectiveTo) {
  }

  //endregion
}
