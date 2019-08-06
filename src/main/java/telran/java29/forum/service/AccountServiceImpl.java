package telran.java29.forum.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import telran.java29.forum.configuration.AccountConfiguration;
import telran.java29.forum.configuration.AccountUserCredentials;
import telran.java29.forum.dao.UserAccountRepository;
import telran.java29.forum.domain.UserAccount;
import telran.java29.forum.dto.UserProfileDto;
import telran.java29.forum.dto.UserRegDto;
import telran.java29.forum.exceptions.UserAuthenticationException;
import telran.java29.forum.exceptions.UserConflictException;
import telran.java29.forum.exceptions.ForbiddenException;

@Service
public class AccountServiceImpl implements AccountService {
	@Autowired
	UserAccountRepository userRepository;
	
	@Autowired
	AccountConfiguration accountConfiguration;

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
				.expdate(LocalDateTime.now().plusDays(accountConfiguration.getExpPeriod()))
				.build();
		userRepository.save(userAccount);
		return convertToUserProfileDto(userAccount);
	}

	@Override
	public UserProfileDto findUserById(String login, String auth) {
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(auth);
		
		UserAccount userAccount = userRepository.findById(credentials.getLogin())
				.orElseThrow(() -> new UserAuthenticationException());
		if(!userAccount.getPassword().equals(credentials.getPassword())) {
			throw new UserAuthenticationException();
		}
		if(!userAccount.getLogin().equals(login)) {
			throw new ForbiddenException();
		}
		return convertToUserProfileDto(userAccount);
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
