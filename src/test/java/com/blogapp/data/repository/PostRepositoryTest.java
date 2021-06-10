package com.blogapp.data.repository;

import com.blogapp.data.models.Author;
import com.blogapp.data.models.Comment;
import com.blogapp.data.models.Post;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;

import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Sql(scripts = {"classpath:db/insert.sql"})
@Transactional
class PostRepositoryTest {

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    void savePostToDBTest(){

        Post blogPost = new Post();
        blogPost.setTitle("What is Fintech?");
        blogPost.setContent("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");

        log.info("Created a blog post --> {}", blogPost);
        postRepository.save(blogPost);
        assertThat(blogPost.getId()).isNotNull();
    }

    @Test
    void throwExceptionWhenSavingPostWithDuplicateTitle(){

        Post blogPost = new Post();
        blogPost.setTitle("What is Fintech?");
        blogPost.setContent("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
        postRepository.save(blogPost);
        assertThat(blogPost.getId()).isNotNull();
        log.info("Created a blog post --> {}", blogPost);

        Post blogPost2 = new Post();
        blogPost2.setTitle("What is Fintech?");
        blogPost2.setContent("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
        log.info("Created a blog post --> {}", blogPost2);
        assertThrows(DataIntegrityViolationException.class, ()-> postRepository.save(blogPost2));

    }

    @Test
    void whenPostIsSaved_thenSaveAuthorTest(){

        Post blogPost = new Post();
        blogPost.setTitle("What is Fintech?");
        blogPost.setContent("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
        log.info("Created a blog post --> {}", blogPost);

        Author author = new Author();
        author.setFirstname("John");
        author.setLastname("Wick");
        author.setEmail("john@mail.com");
        author.setPhoneNumber("090799697896");

        //map relationships
        blogPost.setAuthor(author);
        author.addPost(blogPost);

        postRepository.save(blogPost);
        log.info("Blog post After saving --> {}", blogPost);

        Post savedPost = postRepository.findByTitle("What is Fintech?");
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getAuthor()).isNotNull();

    }

    @Test
    void findAllPostInTheDbTest(){

        List<Post> existingPosts = postRepository.findAll();
        assertThat(existingPosts).isNotNull();
        assertThat(existingPosts).hasSize(5);


    }

    @Test
    @Rollback(value = false)
    void deletePostByIdTest(){

        Post savedPost = postRepository.findById(41).orElse(null);
        assertThat(savedPost).isNotNull();
        log.info("Post fetched from the database ---> {}", savedPost);

        //delete post
        postRepository.deleteById(savedPost.getId());

        Post deletedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertThat(deletedPost).isNull();

    }

    @Test
    void updatedSavedPostTitleTest(){

        Post savedPost = postRepository.findById(41).orElse(null);
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("Title post 1");

        //update post tile
        savedPost.setTitle("Pentax Post title");
        postRepository.save(savedPost);

        Post updatedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getTitle()).isEqualTo("Pentax Post title");

    }

    @Test
    @Rollback(value = false)
    void updatePostAuthorTest(){
        Post savedPost = postRepository.findById(41).orElse(null);
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getAuthor()).isNull();

        log.info("Saved post object --> {}", savedPost);

        Author author = new Author();
        author.setLastname("Brown");
        author.setFirstname("Blue");
        author.setPhoneNumber("0908574653");
        author.setEmail("blue@brownmail.com");
        author.setProfession("Musician");

        savedPost.setAuthor(author);
        postRepository.save(savedPost);

        Post updatedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getAuthor()).isNotNull();
        assertThat(updatedPost.getAuthor().getLastname()).isEqualTo("Brown");

        log.info("Updated Post --> {}", updatedPost);
    }

    @Test
    @Rollback(value = false)
    void addCommentToPostTest(){

        //Fetch the post from the db
        Post savedPost = postRepository.findById(43).orElse(null);
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getComments()).hasSize(0);
        //create a comment object
        Comment comment1 = new Comment("Billy Goat", "Really insightful post");
        Comment comment2 = new Comment("Peter Bread", "Nice one!");
        //map the post and comments
        savedPost.addComment(comment1, comment2);
        //save the post
        postRepository.save(savedPost);

        Post commentedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertThat(commentedPost).isNotNull();
        assertThat(commentedPost.getComments()).hasSize(2);
        log.info("commented post --> {}", commentedPost);
    }

    @Test
    @Rollback(value = false)
    void findAllPostInDescendingOrderTest(){

        List<Post> allPosts = postRepository.findByOrderByDateCreatedDesc();
        assertThat(allPosts).isNotEmpty();
        log.info("All posts --> {}", allPosts);
        assertTrue(allPosts.get(0).getDateCreated().isAfter(allPosts.get(1).getDateCreated()));
//        assertFalse(allPosts.get(0).getDateCreated().isBefore(allPosts.get(1).getDateCreated()));
        allPosts.forEach(post -> {
            log.info("Post Date {}", post.getDateCreated());
        });
    }
















}