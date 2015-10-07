package hu.akusius.palenquehtmlprocessor.config;

/**
 *
 * @author Bujdosó Ákos
 */
public class SiteConfig extends SessionConfig {

  private final String base;

  public final String getBase() {
    return base;
  }

  public SiteConfig(SessionConfig config) {
    super(config);
    this.base = composeBase(isSecure(), getDomain(), getPath());
  }

  public SiteConfig(SiteConfig config) {
    super(config);
    this.base = config.base;
  }

  public final String canonicalUrl(String path) {
    path = path.trim();
    if (path.startsWith("/") || path.contains("./") || path.contains("/.")) {
      throw new IllegalArgumentException("Invalid relative path: " + path);
    }
    if (isWebsite() && isFrontPage(path)) {
      path = "";
    }
    return base + '/' + path;
  }

  public final String getImageUrl() {
    return canonicalUrl(getImagePath());
  }

  public static boolean isFrontPage(String path) {
    if (path.contains("#")) {
      throw new IllegalArgumentException("Path cannot contain fragment.");
    }
    return "./".equals(path) || "index.html".equals(path);
  }

  private static String composeBase(boolean secure, String domain, String path) {
    StringBuilder sb = new StringBuilder(100);
    sb.append("http");
    if (secure) {
      sb.append('s');
    }
    sb.append("://");
    sb.append(domain);
    sb.append('/');
    sb.append(path);
    return sb.toString();
  }
}
