package com.store.api.mongo.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.store.api.mongo.entity.User;

public interface UserRepository extends MongoRepository<User, Long>{
	
	public User findByUserName(String userName);

}
