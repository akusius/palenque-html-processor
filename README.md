# PalenqueHtmlProcessor

A Java console utility to post-process the HTML files of the [Palenque Code](https://github.com/akusius/palenque) project.

### Technology

Java 8 / Maven / NetBeans 8.

#### Third party libraries

- [jsoup](http://jsoup.org/): for reading, processing and writing out the HTML files;
- [Argparse4j](http://argparse4j.sourceforge.net/): for parsing and managing the command line arguments;
- [URL builder](https://github.com/mikaelhg/urlbuilder): for creating the URLs of the share widgets (`+` replaced to `%20`).

### Procedure

The HTML files in the current (or in the specified) directory are parsed with `jsoup`, then processed with the defined processors, and finally (after potentially backing up the original files) they are written back using the `toString()` function of the `Document`.

The `jsoup` pretty-print function turned out to be too aggressive for the post-processing, so it was disabled, and instead the document is written out with the original whitespaces, while the newly added elements are formatted to a minimal degree (newlines are added by the program), which is sufficient to the NetBeans batch reformatting function to properly rewrap the document contents.

### Arguments

With the command line arguments can be specified, which processors to execute, and what information to insert into the HTML files:

````
usage: PalenqueHtmlProcessor [-h] [--src DIR] [--dest DIR] [--no-backup]
                             [--website T/F] [--secure] [--domain DOMAIN]
                             [--path PATH] [--image PATH] [--repo-base URL]
                             [--tw-hashtag TAG(S)] [--tw-via VIA] [-r]
                             [{Identity,Meta,Analytics,Anchors,Images,Disqus,Share,RepoLink,Links,Toc,Sitemap} 
                              [{Identity,Meta,Analytics,Anchors,Images,Disqus,Share,RepoLink,Links,Toc,Sitemap} ...]]

positional arguments:
  {Identity,Meta,Analytics,Anchors,Images,Disqus,Share,RepoLink,Links,Toc,Sitemap}
                         The processors to run  (or  to  disable in reverse
                         mode)

optional arguments:
  -h, --help             show this help message and exit
  --src DIR              Source directory (default: .)
  --dest DIR             Destination directory
  --no-backup            Do  not  backup   HTML   files   before  modifying
                         (default: false)
  --website T/F          Handle  as  a  website  (do  not  use  index.html)
                         (default: true)
  --secure               Use HTTPS (default: false)
  --domain DOMAIN        The domain  of  the  document  (default:  akusius.
                         github.io)
  --path PATH            The path of the  document  root  within the domain
                         (default: palenque)
  --image PATH           Path    of    the     social    image    (default:
                         media/social_logo.png)
  --repo-base URL        The  base  URL  of   the  repository  source  link
                         (default:                          https://github.
                         com/akusius/palenque/blob/master/public_html)
  --tw-hashtag TAG(S)    Twitter hashtag(s) (default: PalenqueCode)
  --tw-via VIA           Twitter via (default: Akusius)
  -r, --reverse-mode     Reverse mode  (disable  the  specified processors)
                         (default: false)
````

If called without arguments, it executes all the processors with the default values (and also backs up the original files).

In direct mode only the specified processors, while in reverse mode (`-r`) all the other processors are executed.

### Processors

There are two types of processors:
- `DocProcessor`: performs operations directly on the `Document` (the DOM);
- `HtmlProcessor`: manages and modifies the document as a single HTML-string.

The majority of the processors can be enabled/disabled from the command line, while some (utility) processors always run (and so they cannot be specified neither in direct, nor in reverse mode).

#### DocProcessors

- `Cleaner` (always): removes "\n" text nodes from the end of the body (for proper formatting);
- `Identity`: injects the canonical link into the head;
- `Meta`: injects the OpenGraph and Twitter meta tags into the head;
- `Analytics`: injects the reference of the `analytics.js` script into the head;
- `Anchors`: generates an `id` for each paragraph, and inserts an anchor to it;
- `Images`: puts a link around an image to open it separately;
- `Disqus`: generates the Disqus fragment into `<div id="comments"`>;
- `Share`: generates the share widgets into `<div id="share">`;
- `RepoLink`: creates (or refreshes) `<div id="repo-link">` with the link to the repository source of the file;
- `Links`: rewrites the front page links (`./` &harr; `index.html`), refreshes the inner link titles and checks the `external` link classes;
- `Toc`: checks the navigation links and creates a `toc.xml` file with the page hierarchy;
- `Sitemap`: generates `sitemap.txt` for the site;
- `Arranger` (always): relocates the `<style>` tag to the end of the head (if exists).

#### HtmlProcessors

- `EntityReplacer` (always): replaces back some commonly used HTML entity;
- `Formatter` (always): reformats the start and end of the document (for proper rewrapping).
