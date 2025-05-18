package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

import es.ulpgc.dacd.bitfomo.businessunit.domain.DatamartEntry;

public interface DatamartWriter {
    void writeEntry(DatamartEntry entry);
}