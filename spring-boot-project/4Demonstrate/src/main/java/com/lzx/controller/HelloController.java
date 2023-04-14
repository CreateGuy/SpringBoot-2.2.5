package com.lzx.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuzhixuan
 * @date 2023-04-12
 */
@RestController
@RequestMapping
public class HelloController {

	@GetMapping(value = "hello")
	public String hello() {
		return "hello";
	}
}
