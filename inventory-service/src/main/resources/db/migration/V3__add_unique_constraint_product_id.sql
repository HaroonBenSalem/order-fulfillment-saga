ALTER TABLE stock
    ADD CONSTRAINT uk_stock_product_id UNIQUE (product_id);
