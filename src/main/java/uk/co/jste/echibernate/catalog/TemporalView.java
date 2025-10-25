package uk.co.jste.echibernate.catalog;

import java.time.Instant;

public interface TemporalView<I extends TemporalView<I, O>, O> {

  O toSnapshotAt(Instant atInstance);
}
