package com.webforj.views;

import com.webforj.component.Component;
import com.webforj.component.Composite;
import com.webforj.component.Theme;
import com.webforj.component.button.Button;
import com.webforj.component.field.TextArea;
import com.webforj.component.html.elements.Div;
import com.webforj.component.html.elements.H2;
import com.webforj.component.html.elements.Img;
import com.webforj.component.layout.flexlayout.FlexContentAlignment;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexJustifyContent;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.tabbedpane.TabbedPane;
import com.webforj.component.text.Label;
import com.webforj.component.toast.Toast;
import com.webforj.graffiti.model.util.PodLoader;
import com.webforj.router.annotation.Route;

@Route("/")
public class GraffitiAiTestbedView extends Composite<FlexLayout> {

  private final Label formJsonLbl;
  private FlexLayout self = getBoundComponent();
  private Component preview = null;
  private Div previewDiv = new Div();
  PodLoader loader = new PodLoader();

  public GraffitiAiTestbedView(){
    self.setDirection(FlexDirection.COLUMN);
    self.setStyle("padding", "20px");

    GraffitiAIPanel aiAssistantPanel = new GraffitiAIPanel();
    self.add(aiAssistantPanel.getDrawer());

    aiAssistantPanel.onGenerate(this::updateForm);

    FlexLayout header = new FlexLayout()
        .setAlignContent(FlexContentAlignment.CENTER)
        .setJustifyContent(FlexJustifyContent.CENTER)
        .setDirection(FlexDirection.ROW)
        .setStyle("border-bottom","1px solid");

    header.add(
        new Img("https://webforj.com/wp-content/uploads/2024/04/cropped-Logo_webforJ_RGB_1-removebg-preview.png.webp")
            .setHeight("20px")
            .setStyle("align-self","center"),
        new H2("AI Form Generator Demo")
        );

        Component drawerButton = aiAssistantPanel.getDrawerButton()
            .setStyle("align-self", "center")
            .setStyle("position", "fixed")
            .setStyle("right", "30px")
            .setStyle("top", "30px")
            ;
    self.add(header,drawerButton);

    TabbedPane tab = new TabbedPane("output");
    tab.addTab("Preview",previewDiv);

    Div jsonDiv = new Div();
    jsonDiv.setStyle("overflow","scroll");

    formJsonLbl = new Label("");
    jsonDiv.add(formJsonLbl);

    tab.addTab("JSON", jsonDiv);

    self.add(tab);

  }

  private void updateForm(String json) {

    formJsonLbl.setText(json);

    if (preview != null) {
      preview.destroy();
    }

    try {
      preview = loader.fromJson(json).load();
    } catch (Exception e) {
      e.printStackTrace();
      Toast.show("Error, cannot display generated result. Please retry.",Theme.WARNING);
      return;
    }

    previewDiv.add(preview);



  }
}
