/**
 * OrderOfferServiceImpl.java
 *
 * Copyright 2014 redmz, Inc. All Rights Reserved.
 *
 * created by vincent 2014年11月15日
 */
package com.store.api.mongo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.store.api.mongo.dao.OrderOfferRepository;
import com.store.api.mongo.entity.OrderOffer;
import com.store.api.mongo.service.OrderOfferService;
import com.store.api.mongo.service.SequenceService;

/**
 * 
 * Revision History
 * 
 * 2014年11月15日,vincent,created it
 */
public class OrderOfferServiceImpl implements OrderOfferService {
    
    @Autowired
    private SequenceService sequenceService;
    
    @Autowired
    private OrderOfferRepository repository;

    @Override
    public void save(OrderOffer entity) {
        if (null == entity.getId()) {
            entity.setId(this.sequenceService.getNextSequence(entity));
        }
        repository.save(entity);
    }

    @Override
    public void save(List<OrderOffer> entitys) {
        for (OrderOffer entity : entitys) {
            if (null == entity.getId()) {
                entity.setId(sequenceService.getNextSequence(entity));
            }
        }
        repository.save(entitys);
    }

    @Override
    public void remove(Long id) {
        repository.delete(id);
    }

    @Override
    public void remove(List<OrderOffer> entitys) {
        repository.delete(entitys);
    }

    @Override
    public List<OrderOffer> findByOrderId(Long id) {
        return repository.findByOrderId(id);
    }

    @Override
    public List<OrderOffer> findByMerchantsId(Long id) {
        return repository.findByMerchantsId(id);
    }

}