package telran.java29.forum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import telran.java29.forum.dto.UserProfileDto;
import telran.java29.forum.dto.UserRegDto;
import telran.java29.forum.service.AccountService;

@RestController
@RequestMapping("/account")

public class AccountManagmentController {
	
	@Autowired
	AccountService accountService;
	
	@PostMapping
	public UserProfileDto register(@RequestBody UserRegDto userRegDto) {
		return accountService.addUser(userRegDto);
	}
	
	@GetMapping("/{login}")
	public UserProfileDto findUser(@PathVariable String login) {
		return accountService.findUserById(login);
	}

}
