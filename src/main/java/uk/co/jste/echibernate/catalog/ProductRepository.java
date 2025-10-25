package uk.co.jste.echibernate.catalog;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  default MutableList<Product.ProductSnapshot> productSnapshots(Instant at) {
    return ListAdapter.adapt(findAll()).collectWith(Product::toSnapshotAt, at);
  };
}
