package com.store.api.mongo.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.store.api.mongo.entity.Order;

public interface OrderRepository extends MongoRepository<Order, Long> {
    
    public Page<Order> findByCustomerId(Long id,Pageable pr);
    
    public Page<Order> findByMerchantsId(Long id,Pageable pr);
    
    /**
     * 查询今天商户错过的订单数
     * @param id
     * @param date
     * @return
     */
    @Query(value="{'offers':{'$elemMatch':{'merchantsId':?0,'createDate':{$gte:?1},'status':{$ne:1}}}}",count=true)
    public int findTadayLostByUserId(Long id,Long date);
    
    /**
     * 分页查询推送给商户的订单(top)
     * @param mercId
     * @param orderId
     * @return
     */
//    @Query(value="{'offers':{'$elemMatch':{'merchantsId':?0,'status':{'$ne':1}}},'id':{'$gt':?1}}")
    @Query(value="{'offers':{'$elemMatch':{'merchantsId':?0}},'id':{'$gt':?1}}")
    public Page<Order> findTopOrderWithMercPush(Long mercId, Long orderId,Pageable pr);
    
    /**
     * 分页查询推送给商户的订单(tail)
     * @param mercId
     * @param orderId
     * @return
     */
//    @Query(value="{'offers':{'$elemMatch':{'merchantsId':?0,'status':{'$ne':1}}},'id':{'$lt':?1}}")
    @Query(value="{'offers':{'$elemMatch':{'merchantsId':?0}},'id':{'$lt':?1}}")
    public Page<Order> findTailOrderWithMercPush(Long mercId, Long orderId,Pageable pr);
    
    /**
     * 分页查询商户的订单(top)
     * @param mercId
     * @param orderId
     * @param pr
     * @return
     */
    @Query(value="{'merchantsId':?0,'id':{'$gt':?1}}")
    public Page<Order> findTopOrderWithMerc(Long mercId, Long orderId,Pageable pr);
    
    /**
     * 分页查询商户的订单(tail)
     * @param mercId
     * @param orderId
     * @return
     */
    @Query(value="{'merchantsId':?0,'id':{'$lt':?1}}")
    public Page<Order> findTailOrderWithMerc(Long mercId, Long orderId,Pageable pr);
    
    @Query(value="{'customerId':?0,'id':{'$gt':?1}}")
    public Page<Order> findTopOrderWithCustomer(Long customerId, Long orderId,Pageable pr);
    
    @Query(value="{'customerId':?0,'id':{'$lt':?1}}")
    public Page<Order> findTailOrderWithCustomer(Long customerId, Long orderId,Pageable pr);

}
