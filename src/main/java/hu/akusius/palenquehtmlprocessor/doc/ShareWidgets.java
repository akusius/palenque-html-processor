package hu.akusius.palenquehtmlprocessor.doc;

import gumi.builders.UrlBuilder;
import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public final class ShareWidgets extends DocProcessor {

  private static final Logger logger = Logger.getLogger(ShareWidgets.class.getName());

  @Override
  public String getParamName() {
    return "Share";
  }

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    Element body = doc.body();
    Element share = getOrCreateTagAttr(body, "div", "id", "share");
    share.empty();

    share.appendText("\n");

    String title = doc.title();
    if (title.isEmpty()) {
      logger.log(Level.WARNING, "Missing title: {0}", config.getPage());
    }

    String description = getDescription(doc);
    if (description == null || description.isEmpty()) {
      logger.log(Level.WARNING, "Missing description: {0}", config.getPage());
      description = "";
    }

    String url = config.getPageUrl();

    for (Widget widget : Widget.Widgets) {
      Element a = share.appendElement("a");
      a.attr("href", widget.getShareUrl(title, description, url, config).replace("+", "%20"));
      a.attr("title", widget.getShareTitle());
      if (widget.isNewTab()) {
        a.attr("target", "_blank");
      }
      Element span = a.appendElement("span");
      span.addClass(widget.getClassName());
      share.appendText("\n");
    }
  }

  private static abstract class Widget {

    private final String name;

    private final String className;

    Widget(String name) {
      this(name, name.toLowerCase());
    }

    Widget(String name, String className) {
      this.name = name;
      this.className = className;
    }

    public String getName() {
      return name;
    }

    public String getClassName() {
      return className;
    }

    public String getShareTitle() {
      return "Share on " + getName();
    }

    public String getShareUrl(String title, String description, String url) {
      return "#";
    }

    public String getShareUrl(String title, String description, String url, ProcessConfig config) {
      return getShareUrl(title, description, url);
    }

    public boolean isNewTab() {
      return true;
    }

    public static final Widget Facebook = new Widget("Facebook") {
      @Override
      public String getShareUrl(String title, String description, String url) {
        return UrlBuilder
                .fromString("https://www.facebook.com/sharer/sharer.php")
                .addParameter("u", url)
                .toString();
      }
    };

    public static final Widget Twitter = new Widget("Twitter") {
      @Override
      public String getShareUrl(String title, String description, String url, ProcessConfig config) {
        UrlBuilder ub = UrlBuilder
                .fromString("https://twitter.com/share")
                .addParameter("url", url);

        if (config.getTwitterHashtag() != null) {
          ub = ub.addParameter("hashtags", config.getTwitterHashtag());
        }

        if (config.getTwitterVia() != null) {
          ub = ub.addParameter("via", config.getTwitterVia());
        }

        return ub.toString();
      }
    };

    public static final Widget GPlus = new Widget("Google+", "gplus") {
      @Override
      public String getShareUrl(String title, String description, String url) {
        return UrlBuilder
                .fromString("https://plus.google.com/share")
                .addParameter("url", url)
                .toString();
      }
    };

    public static final Widget LinkedIn = new Widget("LinkedIn") {
      @Override
      public String getShareUrl(String title, String description, String url) {
        return UrlBuilder
                .fromString("https://www.linkedin.com/shareArticle")
                .addParameter("mini", "true")
                .addParameter("url", url)
                .addParameter("title", title)
                .toString();
      }
    };

    public static final Widget Tumblr = new Widget("Tumblr") {
      @Override
      public String getShareUrl(String title, String description, String url) {
        return UrlBuilder
                .fromString("https://tumblr.com/widgets/share/tool")
                .addParameter("canonicalUrl", url)
                .addParameter("title", title)
                .toString();
      }
    };

    public static final Widget Reddit = new Widget("Reddit") {
      @Override
      public String getShareUrl(String title, String description, String url) {
        return UrlBuilder
                .fromString("https://www.reddit.com/submit/")
                .addParameter("url", url)
                .addParameter("title", title)
                .toString();
      }
    };

    public static final Widget Email = new Widget("Email") {

      @Override
      public String getShareTitle() {
        return "Share by email";
      }

      @Override
      public boolean isNewTab() {
        return false;
      }

      @Override
      public String getShareUrl(String title, String description, String url) {
        return UrlBuilder
                .fromString("mailto:")
                .addParameter("subject", title)
                .addParameter("body", url)
                .toString();
      }
    };

    public static final Widget[] Widgets = new Widget[]{Facebook, Twitter, GPlus, LinkedIn, Tumblr, Reddit, Email};
  }
}
