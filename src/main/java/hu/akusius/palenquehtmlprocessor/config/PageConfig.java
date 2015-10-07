package hu.akusius.palenquehtmlprocessor.config;

/**
 *
 * @author Bujdosó Ákos
 */
public class PageConfig extends SiteConfig {

  private final String page;

  private final boolean frontPage;

  private final String pageUrl;

  public PageConfig(SiteConfig config, String page) {
    super(config);
    if (page == null) {
      throw new IllegalArgumentException();
    }
    this.page = page;
    this.frontPage = isFrontPage(page);
    this.pageUrl = canonicalUrl(page);
  }

  public PageConfig(PageConfig config) {
    super(config);
    this.page = config.page;
    this.frontPage = config.frontPage;
    this.pageUrl = config.pageUrl;
  }

  public final String getPage() {
    return page;
  }

  public final boolean isFrontPage() {
    return frontPage;
  }

  public final String getPageUrl() {
    return pageUrl;
  }
}
