package com.example.WatchesStoreV2.service.component.impl;

import com.example.WatchesStoreV2.entity.ProductItem;
import com.example.WatchesStoreV2.repository.ProductItemRepository;
import com.example.WatchesStoreV2.service.component.ProductItemService;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductItemImpl implements ProductItemService {

    private final ProductItemRepository productItemRepository;

    @Override
    public List<ProductItem> findAllUserItem(ObjectId user) {
        try {
            return this.productItemRepository.findAllByUser(user);
        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Can't find user's item");
        }
    }

    @Override
    public ProductItem saveProductItem(ProductItem productItem) {
        try {
            if (productItem.getId() == null) {
                productItem.setId(new ObjectId());
            }

            this.productItemRepository.save(productItem);

            return productItem;
        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Can't save user's item");
        }
    }

    @Override
    public void deleteProductItem(ObjectId itemId) {
        try {
            this.productItemRepository.deleteById(itemId);
        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Can't save user's item");
        }
    }
}
