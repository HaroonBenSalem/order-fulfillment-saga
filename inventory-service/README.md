# inventory-service
Construit en **Phase 2**. Consomme OrderCreated, réserve le stock dans
inventory_db, publie StockReserved/StockRejected. Consomme aussi
ReleaseStockCommand pour la compensation (Phase 3).
