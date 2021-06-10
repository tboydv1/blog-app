package com.blogapp.service.post;

import com.blogapp.data.models.Comment;
import com.blogapp.data.models.Post;
import com.blogapp.data.repository.PostRepository;
import com.blogapp.service.cloud.CloudStorageService;
import com.blogapp.web.dto.PostDto;
import com.blogapp.web.exceptions.PostDoesNotExistsException;
import com.blogapp.web.exceptions.PostObjectIsNullException;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    CloudStorageService cloudStorageService;

    @Override
    public Post savePost(PostDto postDto) throws PostObjectIsNullException{

        if(postDto == null){
            throw new PostObjectIsNullException("Post cannot be null");
        }

        Post post = new Post();

        if(postDto.getImageFile() != null && !postDto.getImageFile().isEmpty()){
            try {
                Map<?,?> uploadResult =
                        cloudStorageService.uploadImage(postDto.getImageFile(),
                ObjectUtils.asMap(
                        "public_id", "blogapp/"+ extractFileName(
                                Objects.requireNonNull(
                                        postDto.getImageFile().getOriginalFilename()))
                ));

                post.setCoverImageUrl(String.valueOf(uploadResult.get("url")));
                log.info("Image url --> {}", uploadResult.get("url"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        log.info("Post object before saving --> {}", post);


        try {
            return postRepository.save(post);
        }catch (DataIntegrityViolationException ex){
            log.info("Exception occurred --> {}", ex.getMessage());
            throw ex;
        }
    }

    @Override
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public List<Post> findPostInDescOrder() {
        return postRepository.findByOrderByDateCreatedDesc();
    }

    @Override
    public Post updatePost(PostDto postDto) {
        return null;
    }

    @Override
    public Post findById(Integer id) throws PostDoesNotExistsException {
            if(id == null){
                throw new NullPointerException("Post id cannot be null");
            }
            Optional<Post> post = postRepository.findById(id);

            if (post.isPresent()) {
                return post.get();
            } else {
                throw new PostDoesNotExistsException("Post with Id --> {}");
            }


    }

    @Override
    public void deletePostById(Integer id) {

    }

    @Override
    public Post addCommentToPost(Integer id, Comment comment) {
        return null;
    }

    private String extractFileName(String fileName){
        return fileName.split("\\.")[0];
    }
}
