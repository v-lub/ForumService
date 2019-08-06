package telran.java29.forum.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import telran.java29.forum.dao.UserAccountRepository;
import telran.java29.forum.domain.UserAccount;
import telran.java29.forum.dto.UserProfileDto;
import telran.java29.forum.dto.UserRegDto;
import telran.java29.forum.exceptions.UserConflictException;

@Service
public class AccountServiceImpl implements AccountService {
	@Autowired
	UserAccountRepository userRepository;
	
	long expPerod = 30;

	@Override
	public UserProfileDto addUser(UserRegDto userRegDto) {
		if (userRepository.existsById(userRegDto.getLogin())) {
			throw new UserConflictException();
		}
		UserAccount userAccount = UserAccount.builder()
				.login(userRegDto.getLogin())
				.password(userRegDto.getPassword())
				.firstName(userRegDto.getFirstName())
				.lastName(userRegDto.getLastName())
				.role("User")
				.expdate(LocalDateTime.now().plusDays(expPerod))
				.build();
		userRepository.save(userAccount);
		return convertToUserProfileDto(userAccount);
	}

	@Override
	public UserProfileDto findUserById(String login) {
		UserAccount userAccount = userRepository.findById(login).orElse(null);
		return userAccount == null ? null : convertToUserProfileDto(userAccount);
	}
	
	private UserProfileDto convertToUserProfileDto(UserAccount userAccount) {
		return UserProfileDto.builder()
				.firstName(userAccount.getFirstName())
				.lastName(userAccount.getLastName())
				.login(userAccount.getLogin())
				.roles(userAccount.getRoles())
				.build();
	}

}
