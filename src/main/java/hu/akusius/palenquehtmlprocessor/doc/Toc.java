package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import hu.akusius.palenquehtmlprocessor.config.SessionConfig;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Bujdosó Ákos
 */
public class Toc extends DocProcessor {

  private static final Logger logger = Logger.getLogger(Toc.class.getName());

  private static final String INDEX = "index.html";

  private Map<String, Page> pages;

  private Set<Page> processedPages;

  @Override
  public void sessionStart(SessionConfig config) {
    super.sessionStart(config);
    this.pages = new HashMap<>(50);
    this.processedPages = new HashSet<>(50);
  }

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    Page page = getPage(config.getPage());
    page.setTitle(doc.title());
    page.setDesc(getDescription(doc));
    processedPages.add(page);

    Element nav = doc.getElementById("navigation");
    if (nav != null) {
      if (!hasElement(nav, ".toc")) {
        nav.prependElement("a").attr("href", "#").attr("accesskey", "t").addClass("toc")
                .attr("title", "Table of Contents (access key: T)").text("≡");
        nav.prependText("\n");
      }

      for (Element a : nav.getElementsByTag("a")) {
        if (a.hasClass("toc")) {
          continue;
        }
        Page ref = getPage(a.attr("href"));
        if (a.hasClass("prev")) {
          page.setPrev(ref);
        } else if (a.hasClass("up")) {
          page.setParent(ref);
        } else if (a.hasClass("next")) {
          page.setNext(ref);
        } else {
          throw new AssertionError();
        }
      }

      if (doc.getElementById("toc") == null) {
        nav.after("\n<div id=\"toc\"></div>");
      }

      Element head = doc.head();
      addJqueryScript(head);
      addScriptFileIfNeeded(head, "js/toc.js");
    }

    Page next = page.getNext();
    if (next != null) {
      Elements np = doc.select(".next-page a");
      if (np.size() > 0) {
        if (np.size() > 1) {
          logger.warning("Multiple next page links!");
        }
        String href = np.get(0).attr("href");
        if (!href.equals(next.getName())) {
          logger.warning("Next page link mismatch!");
        }
      }
    }

    for (Element link : doc.getElementsByTag("a")) {
      String href = link.attr("href");
      if (href.startsWith("http") || href.startsWith("mailto") || href.startsWith("#") || href.contains(":")) {
        continue;
      }
      if (href.contains("#")) {
        href = href.substring(0, href.indexOf('#'));
      }
      if (href.isEmpty()) {
        continue;
      }
      if ("./".equals(href)) {
        href = INDEX;
      }
      if (href.contains("/")) {
        continue;
      }
      getPage(href);
    }
  }

  @Override
  public void sessionEnd(SessionConfig config) {
    super.sessionEnd(config);
    logger.fine("Writing toc.xml...");

    Page index = pages.get(INDEX);
    index.sortChildren(true);

    Path dir = config.getDestDir();
    if (dir == null) {
      dir = config.getSourceDir();
    }

    try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(dir.resolve("toc.xml")))) {
      XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(os, "UTF-8");
      writer.writeStartDocument();
      writer.writeCharacters("\n");
      writer.writeStartElement("Toc");

      index.write(writer, 1, "  ");

      writer.writeCharacters("\n");
      writer.writeEndElement();
      writer.writeEndDocument();
      writer.close();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    logger.fine("Checking inner links...");
    if (processedPages.size() < pages.size()) {
      for (Page p : pages.values()) {
        if (!processedPages.contains(p)) {
          logger.log(Level.WARNING, "Missing inner link: {0}", p);
        }
      }
    }
  }

  private Page getPage(String name) {
    if ("./".equals(name)) {
      name = INDEX;
    }
    if (name == null || name.isEmpty() || name.contains("/") || name.contains("#")) {
      throw new IllegalArgumentException("Invalid page name: " + name);
    }
    if (!pages.containsKey(name)) {
      pages.put(name, new Page(name));
    }
    return pages.get(name);
  }

  private static class Page {

    private final String name;

    private final boolean index;

    private final List<Page> children = new ArrayList<>(3);

    private String title;

    private String desc;

    private Page prev;

    private Page parent;

    private Page next;

    Page(String name) {
      this.name = name;
      this.index = INDEX.equals(name);
    }

    public String getName() {
      return name;
    }

    public boolean isIndex() {
      return index;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getDesc() {
      return desc;
    }

    public void setDesc(String desc) {
      this.desc = desc;
    }

    public List<Page> getChildren() {
      return Collections.unmodifiableList(children);
    }

    public Page getPrev() {
      return prev;
    }

    public void setPrev(Page p) {
      if (p.next != null && p.next != this) {
        throw new IllegalArgumentException("Page has already another next page: " + p.name);
      }
      p.next = this;
      this.prev = p;
    }

    public Page getNext() {
      return next;
    }

    public void setNext(Page n) {
      if (n.prev != null && n.prev != this) {
        throw new IllegalArgumentException("Page has already another previous page: " + n.name);
      }
      n.prev = this;
      this.next = n;
    }

    public Page getParent() {
      return parent;
    }

    public void setParent(Page parent) {
      parent.addChild(this);
    }

    public void addChild(Page page) {
      if (page.parent != null && page.parent != this) {
        throw new IllegalArgumentException("Page has already another parent: " + page.name);
      }
      if (page.parent == null) {
        page.parent = this;
        this.children.add(page);
      }
    }

    public void sortChildren(boolean recursive) {
      children.sort(childrenComparator);
      if (recursive) {
        for (Page child : children) {
          child.sortChildren(recursive);
        }
      }
    }

    private static final Comparator<Page> childrenComparator = (Page p1, Page p2) -> {
      if (p1.parent != p2.parent) {
        throw new IllegalArgumentException();
      }
      for (Page ref = p1; ref != null && !ref.isIndex(); ref = ref.next) {
        if (ref == p2) {
          return -1;  // p1 is before p2
        }
      }
      for (Page ref = p1; ref != null && !ref.isIndex(); ref = ref.prev) {
        if (ref == p2) {
          return +1;  // p1 is after p2
        }
      }
      throw new IllegalArgumentException(String.format("Cannot determine relation: %s - %s", p1, p2));
    };

    public synchronized void write(XMLStreamWriter writer, int level, String levelIndent) throws XMLStreamException {
      boolean hasChildren = children.size() > 0;

      writeIndentation(writer, level, levelIndent);

      if (hasChildren) {
        writer.writeStartElement("Page");
      } else {
        writer.writeEmptyElement("Page");
      }
      writer.writeAttribute("name", name);
      writer.writeAttribute("title", title);
      writer.writeAttribute("desc", desc);

      for (Page child : children) {
        child.write(writer, level + 1, levelIndent);
      }

      if (hasChildren) {
        writeIndentation(writer, level, levelIndent);
        writer.writeEndElement();
      }
    }

    private static void writeIndentation(XMLStreamWriter writer, int level, String levelIndent) throws XMLStreamException {
      writer.writeCharacters("\n");
      for (int i = 0; i < level; i++) {
        writer.writeCharacters(levelIndent);
      }
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
