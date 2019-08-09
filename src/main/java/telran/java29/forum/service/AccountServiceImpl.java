package telran.java29.forum.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import telran.java29.forum.configuration.AccountConfiguration;
import telran.java29.forum.configuration.AccountUserCredentials;
import telran.java29.forum.dao.UserAccountRepository;
import telran.java29.forum.domain.UserAccount;
import telran.java29.forum.dto.UserEditDto;
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
		String hashPassword = BCrypt.hashpw(userRegDto.getPassword(), BCrypt.gensalt());
		UserAccount userAccount = UserAccount.builder()
				.login(userRegDto.getLogin())
				.password(hashPassword)
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
		if (!BCrypt.checkpw(credentials.getPassword(), userAccount.getPassword())) {
			throw new UserAuthenticationException();
		}
		if (!userAccount.getLogin().equals(login)) {
			throw new ForbiddenException();
		}
		return convertToUserProfileDto(userAccount);
	}

	@Override
	public UserProfileDto editUser(UserEditDto userEditDto, String auth) {
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(auth);
		UserAccount userAccount = userRepository.findById(credentials.getLogin())
				.orElseThrow(() -> new UserAuthenticationException());
		if (!BCrypt.checkpw(credentials.getPassword(), userAccount.getPassword())) {
			throw new UserAuthenticationException();
		}
		if (userEditDto.getFirstName() != null) {
			userAccount.setFirstName(userEditDto.getFirstName());
		}
		if (userEditDto.getLastName() != null) {
			userAccount.setLastName(userEditDto.getLastName());
		}
		userRepository.save(userAccount);

		return convertToUserProfileDto(userAccount);
	}

	@Override
	public UserProfileDto removeUser(String auth) {
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(auth);
		UserAccount userAccount = userRepository.findById(credentials.getLogin())
				.orElseThrow(() -> new UserAuthenticationException());
		if (!BCrypt.checkpw(credentials.getPassword(), userAccount.getPassword())) {
			throw new UserAuthenticationException();
		}
		userRepository.delete(userAccount);
		return convertToUserProfileDto(userAccount);
	}
	
	@Override
	public Set<String> addRole(String id, String role, String auth) {
		AccountUserCredentials credentials = accountConfiguration.tokenDecode(auth);
		UserAccount admin = userRepository.findById(credentials.getLogin())
				.orElseThrow(() -> new UserAuthenticationException());
		if (!BCrypt.checkpw(credentials.getPassword(), admin.getPassword())) {
			throw new UserAuthenticationException();
		}
		if (!admin.getRoles().contains("Admin")) {
			throw new ForbiddenException();
		}
		UserAccount userAccount = userRepository.findById(id)
				.orElseThrow(() -> new UserConflictException());
		userAccount.addRole(role);
		userRepository.save(userAccount);
		return userAccount.getRoles();
	}

	private UserProfileDto convertToUserProfileDto(UserAccount userAccount) {
		return UserProfileDto.builder()
				.firstName(userAccount.getFirstName())
				.lastName(userAccount.getLastName())
				.login(userAccount.getLogin())
				.roles(userAccount.getRoles())
				.build();
	}

	@Override
	public void changePassword(String login, String password) {
		UserAccount userAccount = userRepository.findById(login).get();
		String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		userAccount.setPassword(hashPassword);
		userRepository.save(userAccount);
	}

}
