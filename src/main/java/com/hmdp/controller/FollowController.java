package com.hmdp.controller;


import com.hmdp.dto.Result;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.hmdp.service.IFollowService;

import org.springframework.web.bind.annotation.PutMapping;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;


    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long folllowUserId, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(folllowUserId, isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Result isfollow(@PathVariable("id") Long folllowUserId) {
        return followService.isFollow(folllowUserId);
        
    }

    @GetMapping
    public Result followCommons(@PathVariable("id") Long id){
        return followService.followCommons(id);
    }

}
