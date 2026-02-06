package com.tander.flowable.client.controller;

import com.tander.flowable.client.service.HazelcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final HazelcastService hazelcastService;

    @GetMapping("/lock")
    public String lock() {
        if (hazelcastService.tryLock("test_lock")) {
                return "Lock acquired by thread " + Thread.currentThread().getId();
        } else {
            return "Failed to acquire lock by thread " + Thread.currentThread().getId();
        }
    }

    @GetMapping("/unlock")
    public String unlock() {
        hazelcastService.unlock("test_lock");
        return "lock released by thread " + Thread.currentThread().getId();
    }

    @GetMapping("/lock-count")
    public Long getLockCount() {
        return hazelcastService.getLockCount();
    }

}
