package eu.trentorise.smartcampus.permissionprovider.auth.google;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GoogleProviderFilter implements Filter {

	private static final String ATTR_EMAIL = "OIDC_CLAIM_email";

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse httpResp = (HttpServletResponse) resp;
		HttpServletRequest httpReq = (HttpServletRequest) req;
		String googleEmail = req.getParameter(ATTR_EMAIL);
		if (googleEmail == null) {
			httpResp.sendRedirect("http://localhost:8080/aac/auth/google-oauth");
		} else {
			filterChain.doFilter(req, resp);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

}
