package com.blogapp.web.controllers;

import com.blogapp.data.models.Post;
import com.blogapp.service.post.PostService;
import com.blogapp.web.dto.PostDto;
import com.blogapp.web.exceptions.PostDoesNotExistsException;
import com.blogapp.web.exceptions.PostObjectIsNullException;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.rule.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/posts")
@ControllerAdvice
public class PostController {

    @Autowired
    PostService postServiceImpl;

    @GetMapping()
    public String getIndex(Model model){
        List<Post> postList = postServiceImpl.findPostInDescOrder();
        model.addAttribute("postList", postList);

        return "index";
    }

    @GetMapping("/create")
    public String getPostForm(Model model){
        model.addAttribute("error", false);
        return "create";
    }

    @PostMapping("/save")
    public String savePost(@ModelAttribute("postDto") @Valid PostDto postDto,
                           BindingResult result, Model model){
        log.info("Post dto received --> {}", postDto);

        if(result.hasErrors()){
            return "create";
        }
        try{
            postServiceImpl.savePost(postDto);
        }catch (PostObjectIsNullException pe) {
            log.info("Exception occurred --> {}", pe.getMessage());
        }catch (DataIntegrityViolationException dx){
            log.info("Constraint exception occurred --> {}", dx.getMessage());
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Title Not Accepted, Already Exists!");
            return "create";
        }

        return "redirect:/posts";
    }

    @ModelAttribute
    public void createPostModel(Model model){
        model.addAttribute("postDto", new PostDto());
    }


    @GetMapping("/info/{postId}")
    public String getPostDetails(@PathVariable("postId") Integer postId, Model model){
        log.info("Request for a post path --> {}", postId);
        try {
            Post savedPost = postServiceImpl.findById(postId);
            model.addAttribute("postInfo", savedPost);
        }catch (PostDoesNotExistsException px){
            log.info("Exception occurred");
        }
        return "post";
    }
}
