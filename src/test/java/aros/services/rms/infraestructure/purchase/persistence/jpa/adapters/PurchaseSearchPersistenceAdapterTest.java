/* (C) 2026 */

package aros.services.rms.infraestructure.purchase.persistence.jpa.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import aros.services.rms.core.purchase.domain.PurchaseOrder;
import aros.services.rms.core.purchase.domain.Supplier;
import aros.services.rms.core.systemconfig.domain.port.output.CurrencyProvider;
import aros.services.rms.infraestructure.purchase.persistence.PurchaseOrderEntity;
import aros.services.rms.infraestructure.purchase.persistence.SupplierEntity;
import aros.services.rms.infraestructure.purchase.persistence.jpa.PurchaseOrderJpaRepository;
import aros.services.rms.infraestructure.purchase.persistence.jpa.PurchaseOrderMapper;
import aros.services.rms.infraestructure.purchase.persistence.jpa.SupplierJpaRepository;
import aros.services.rms.infraestructure.purchase.persistence.jpa.SupplierMapper;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Query;

/**
 * Tests for {@link PurchaseSearchPersistenceAdapter}.
 *
 * <p><b>Feature:</b> Purchase search persistence adapter
 *
 * <p><b>Expected behavior:</b>
 *
 * <ul>
 *   <li>should Find Suppliers By Partial Name Ignoring Case
 *   <li>should Find Purchase Orders By Notes Or Supplier Name Ignoring Case
 *   <li>should Define Purchase Search Query For Notes And Supplier Name
 * </ul>
 *
 * <p><b>How this test works:</b>
 *
 * <ul>
 *   <li>Mocks service-layer dependencies via Mockito
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PurchaseSearchPersistenceAdapterTest {

  @Mock private SupplierJpaRepository supplierRepository;
  @Mock private SupplierMapper supplierMapper;
  @Mock private PurchaseOrderJpaRepository purchaseOrderRepository;
  @Mock private PurchaseOrderMapper purchaseOrderMapper;
  @Mock private CurrencyProvider currencyProvider;

  @Test
  void shouldFindSuppliersByPartialNameIgnoringCase() {
    SupplierEntity entity = SupplierEntity.builder().id(1L).name("Distribuidora Norte").build();
    Supplier supplier = Supplier.builder().id(1L).name("Distribuidora Norte").build();
    when(supplierRepository.findByNameContainingIgnoreCase("norte")).thenReturn(List.of(entity));
    when(supplierMapper.toDomain(entity)).thenReturn(supplier);
    SupplierPersistenceAdapter adapter =
        new SupplierPersistenceAdapter(supplierRepository, supplierMapper);

    List<Supplier> result = adapter.findByNameContainingIgnoreCase("norte");

    assertEquals(List.of(supplier), result);
    verify(supplierRepository).findByNameContainingIgnoreCase("norte");
  }

  @Test
  void shouldFindPurchaseOrdersByNotesOrSupplierNameIgnoringCase() {
    PurchaseOrderEntity entity =
        PurchaseOrderEntity.builder().id(5L).notes("Fresh produce").build();
    PurchaseOrder order = PurchaseOrder.builder().id(5L).notes("Fresh produce").build();
    Currency cop = Currency.getInstance("COP");
    when(currencyProvider.getCurrency()).thenReturn(cop);
    when(purchaseOrderRepository.findByNotesOrSupplierNameContainingIgnoreCase("produce"))
        .thenReturn(List.of(entity));
    when(purchaseOrderMapper.toDomain(entity, cop)).thenReturn(order);
    PurchaseOrderPersistenceAdapter adapter =
        new PurchaseOrderPersistenceAdapter(
            purchaseOrderRepository, purchaseOrderMapper, currencyProvider);

    List<PurchaseOrder> result =
        adapter.findByNotesContainingIgnoreCaseOrSupplierNameContainingIgnoreCase("produce");

    assertEquals(List.of(order), result);
    verify(purchaseOrderRepository).findByNotesOrSupplierNameContainingIgnoreCase("produce");
  }

  @Test
  void shouldDefinePurchaseSearchQueryForNotesAndSupplierName() throws NoSuchMethodException {
    var method =
        PurchaseOrderJpaRepository.class.getMethod(
            "findByNotesOrSupplierNameContainingIgnoreCase", String.class);
    Query query = method.getAnnotation(Query.class);

    assertNotNull(query);
    assertTrue(query.value().contains("LOWER(po.notes)"));
    assertTrue(query.value().contains("LOWER(s.name)"));
    assertTrue(query.value().contains(":search"));
  }
}
