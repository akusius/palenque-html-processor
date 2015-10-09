package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public class RepoLink extends DocProcessor {

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    Element repoLink = doc.getElementById("repo-link");
    if (repoLink == null) {
      Element content = doc.getElementById("content");
      if (content == null) {
        throw new IllegalArgumentException("'content' element not found!");
      }
      content.after("\n<div id=\"repo-link\"></div>");
      repoLink = doc.getElementById("repo-link");
    } else {
      repoLink.empty();
    }

    repoLink.appendText("\n");
    Element a = repoLink.appendElement("a");
    a.addClass("external");
    a.attr("href", config.getRepoBase() + '/' + config.getPage());
    a.attr("title", String.format("The '%s' file in the source repository (access key: S)", config.getPage()));
    a.attr("accesskey", "s");
    a.attr("target", "_blank");
    a.appendText("\n");
    a.appendElement("span");
    a.appendText("\n");
    repoLink.appendText("\n");
  }

}
