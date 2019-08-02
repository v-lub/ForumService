package telran.java29.forum.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.java29.forum.domain.Post;

public interface PostRepository extends MongoRepository<Post, String> {

}
