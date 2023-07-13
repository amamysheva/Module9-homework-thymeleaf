import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String currentTime = getFormattedCurrentTime(req, resp);

        Context simpleContext = new Context(req.getLocale(), Map.of("time", currentTime));

        engine.process("time", simpleContext, resp.getWriter());
        resp.getWriter().close();
    }

    private static String getFormattedCurrentTime(HttpServletRequest req, HttpServletResponse resp) {
        String timezone = req.getParameter("timezone");
        if (timezone == null) {
            timezone = getTimezoneFromCookies(req);
        } else {
            timezone = timezone.replace(' ', '+');
            resp.addCookie(new Cookie("lastTimezone", timezone));
        }

        return ZonedDateTime
                .now(ZoneId.of(timezone))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ")) + timezone;
    }

    private static String getTimezoneFromCookies(HttpServletRequest req) {
        String cookies = req.getHeader("Cookie");

        Map<String, String> result = new HashMap<>();

        if (cookies != null) {
            String[] separateCookies = cookies.split(";");
            for (String pair : separateCookies) {
                String[] keyValue = pair.split("=");
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result.getOrDefault("lastTimezone", "UTC");
    }
}