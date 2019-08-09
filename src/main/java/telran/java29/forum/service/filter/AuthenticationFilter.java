package telran.java29.forum.service.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import telran.java29.forum.configuration.AccountConfiguration;
import telran.java29.forum.configuration.AccountUserCredentials;
import telran.java29.forum.dao.UserAccountRepository;
import telran.java29.forum.domain.UserAccount;
import telran.java29.forum.exceptions.UserAuthenticationException;

@Service
public class AuthenticationFilter implements Filter {
	@Autowired
	UserAccountRepository repository;

	@Autowired
	AccountConfiguration configuration;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		String method = request.getMethod();
		String user = "";
		if (checkPointCut(path, method)) {
			String auth = request.getHeader("Authorization");
			if (auth == null) {
				response.sendError(401, "No header Authorization");
			} else {
				AccountUserCredentials credentials = configuration.tokenDecode(auth);
				UserAccount userAccount = repository.findById(credentials.getLogin()).orElse(null);
				if (userAccount == null) {
					response.sendError(401, "User not exists");
				} else {
					if (!BCrypt.checkpw(credentials.getPassword(), userAccount.getPassword())) {
						response.sendError(401, "User or password is not correct");
					}else {
						user = credentials.getLogin();
						chain.doFilter(new WrapperRequest(request, user), response);
					}
				}
			}
		}
		
	}
	
	private class WrapperRequest extends HttpServletRequestWrapper{
		String user;

		public WrapperRequest(HttpServletRequest request, String user) {
			super(request);
			this.user = user;
		}
		
		@Override
		public Principal getUserPrincipal(){
			return new Principal() {				
				@Override
				public String getName() {
					return user;
				}
			};
		}
		
	}

	private boolean checkPointCut(String path, String method) {
		return !("/account/".equalsIgnoreCase(path) && "Post".equalsIgnoreCase(method));
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void destroy() {

	}

}
