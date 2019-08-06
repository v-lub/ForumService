package telran.java29.forum.service;

import telran.java29.forum.dto.UserProfileDto;
import telran.java29.forum.dto.UserRegDto;

public interface AccountService {
	UserProfileDto addUser(UserRegDto userRegDto);

	UserProfileDto findUserById(String login, String auth);
}
