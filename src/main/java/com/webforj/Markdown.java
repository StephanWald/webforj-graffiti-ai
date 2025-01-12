package com.webforj;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.Div;
import com.webforj.component.text.Label;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

// TODO: we need a component in webforJ for this!
public class Markdown extends Composite<Div> {

  public static final String PRISM_URL = "https://cdn.jsdelivr.net/npm/prismjs@1.29.0/prism.js";
  public static final String PRISM_CSS = "https://cdn.jsdelivr.net/npm/prismjs@1.29.0/themes/prism-tomorrow.min.css";
  public static final String PRISM_LANG_URL = "https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-%%language%%.min.js";
  public static final String PRISM_TB_URL = "https://cdn.jsdelivr.net/npm/prismjs@1.29.0/plugins/toolbar/prism-toolbar.min.js";
  public static final String PRISM_TB_CSS = "https://cdn.jsdelivr.net/npm/prismjs@1.29.0/plugins/toolbar/prism-toolbar.min.css";
  public static final String PRISM_CLIPBOARD_URL = "https://cdn.jsdelivr.net/npm/prismjs@1.29.0/plugins/copy-to-clipboard/prism-copy-to-clipboard.min.js";

  Label content = new Label();

  public Markdown() {
    getBoundComponent().add(content);
    loadPrismLib();
  }

  public Markdown setText(String text) {
    final String markdown = getMarkdown(text);
    System.out.println(text);
    System.out.println(markdown);
    getBoundComponent().setHtml(markdown);
    return this;
  }

  private void loadPrismLib() {
      String script = "console.log('loading prism');var link =  document.createElement('script');link.setAttribute('type','module');link.setAttribute('src','" + PRISM_URL + "');" + "document.head.appendChild(link);" + "var csslink =  document.createElement('link');" + "csslink.setAttribute('rel','stylesheet');" + "csslink.setAttribute('href','" + PRISM_CSS + "');" + "document.head.appendChild(csslink);";
      App.getPage().executeJsAsync(script);
      addLanguage("json");
      addLanguage("markdown");
      addLanguage("bash");
  }

  private void loadLanguages(String text) {
    String[] langtags = text.split("\n");
    for (int i=0; i<langtags.length; i++){
      if (langtags[i].startsWith("```")&&langtags[i].length()>3){
        String lang=langtags[i].substring(3);
        addLanguage(lang);
      }
    }

  }

  public Markdown addLanguage(String language){

      String url = PRISM_LANG_URL.replace("%%language%%", language);

      String scr = "function whenPrismLoaded (callback) { if (typeof Prism === 'undefined') {setTimeout (function () {whenPrismLoaded (callback);}, 100);} else { callback (); }}";
      App.getPage().addInlineJavaScript(scr);

      scr = "function whenPrismLang" + language + "Loaded (callback) { if (typeof Prism.languages." + language + " === 'undefined' || typeof Prism === 'undefined') {setTimeout (function () {whenPrismLang" + language + "Loaded (callback);}, 100);} else { callback (); }}";
      App.getPage().addInlineJavaScript(scr);

      scr = "whenPrismLoaded(function() {var link2 =  document.createElement('script');link2.setAttribute('type','module');link2.setAttribute('src','" + url + "');" + "document.head.appendChild(link2);whenPrismLang" + language + "Loaded(function() {Prism.highlightAll();}) });";
      App.getPage().addInlineJavaScript(scr);


        scr = "whenPrismLoaded(function() {var link =  document.createElement('script');link.setAttribute('type','module');link.setAttribute('src','" + PRISM_TB_URL + "');" + "document.head.appendChild(link);"+
            "var csslink =  document.createElement('link');" + "csslink.setAttribute('rel','stylesheet');" + "csslink.setAttribute('href','" + PRISM_TB_CSS + "');" + "document.head.appendChild(csslink);})";
        App.getPage().executeJsAsync(scr);

        scr = "whenPrismLoaded(function() {var link =  document.createElement('script');link.setAttribute('type','module');link.setAttribute('src','" + PRISM_CLIPBOARD_URL + "');" + "document.head.appendChild(link);})";
      App.getPage().executeJsAsync(scr);


    return this;
  }
  private String getMarkdown(String code){

    Parser parser = Parser.builder().build();
    Node document = parser.parse(code);
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    return renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
  }

}