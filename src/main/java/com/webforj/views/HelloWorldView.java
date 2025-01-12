package com.webforj.views;

import com.webforj.component.Component;
import com.webforj.component.Composite;
import com.webforj.component.Theme;
import com.webforj.component.button.Button;
import com.webforj.component.field.TextArea;
import com.webforj.component.html.elements.H2;
import com.webforj.component.html.elements.H3;
import com.webforj.component.layout.flexlayout.FlexContentAlignment;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexJustifyContent;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.toast.Toast;
import com.webforj.graffiti.model.util.PodLoader;
import com.webforj.router.annotation.Route;

@Route("/")
public class HelloWorldView extends Composite<FlexLayout> {

  private FlexLayout self = getBoundComponent();
  private TextArea command = new TextArea("Enter command:");
  private Button btn = new Button("GO");
  private Button btnImage = new Button("Image");

  private Button btnDrawerOpen;

  private TextArea json = new TextArea("JSON");
  private Component preview = null;
  PodLoader loader = new PodLoader();




  public HelloWorldView(){
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

    header.add(new H2("Preview:"),aiAssistantPanel.getDrawerButton());

    self.add(header);

  }

  private void updateForm(String json) {
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

    self.add(preview);


  }
/*
  private void addImage(ButtonClickEvent buttonClickEvent) {
      UploadedFile result = OptionDialog.showFileUploadDialog("Upload a file");

      if (result==null) return;
      String myfile=null;
      try {
        myfile = result.move(System.currentTimeMillis()+result.getSanitizedClientName()).getAbsolutePath();
    } catch (IOException e) {
        App.msgbox("file upload failed");
        return;
    }
      fileId = assistant.uploadFile(apiKey,myfile,"vision");
      command.setText("Create a form like in the picture.");

      if (preview != null){
        preview.destroy();
        preview=null;
      }

     preview = new Img()
         .setStyle("height","250px")
         .setStyle("width","fit-content")
         .setSrc(encodeImageToBase64Src(myfile));
     self.add(preview);
  }

  private void go(ButtonClickEvent buttonClickEvent) {

    BusyIndicator busyIndicator =  App.getBusyIndicator();
    busyIndicator.setText("Submitting form... Please wait.")
        .setBackdropVisible(true);
    busyIndicator.getSpinner().setTheme(Theme.PRIMARY);
    busyIndicator.open();


    String commandText = command.getText();

    if (threadId == null) {
      threadId = assistant.createThread(apiKey);
    }

    int retry = 0;
    boolean done_okay=false;
    while (retry < 4 && !done_okay) {
      retry++;
      if (fileId != null) {
        assistant.sendMessage(apiKey, threadId, commandText, fileId);
      } else {
        assistant.sendMessage(apiKey, threadId, commandText);
      }

      fileId = null;

      runId = assistant.createRun(apiKey, threadId, assistantId);

      String waittext = "please wait.";

      busyIndicator.setText(waittext);

      Boolean is_finished = false;

      while (!is_finished) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        is_finished = assistant.isRunFinished(apiKey, threadId, runId);
        waittext = waittext + ".";
        busyIndicator.setText(waittext);

      }

      String response = assistant.getLastMessage(apiKey, threadId);

      String jsonForm = GraffitiAssistant.getForm((response));

      json.setText(jsonForm);

      if (preview != null) {
        preview.destroy();
      }

      try {
        preview = loader.fromJson(jsonForm).load();
        done_okay=true;
      } catch (Exception e) {
        preview = new Label("<html><h1>Error generating preview: " + e.getMessage() + "</h1><p>" + response + "</p>");
        commandText = "Please retry. Getting "+e.getMessage();
        command.setText(commandText);
      }

      self.add(preview);

    }
    busyIndicator.setVisible(false);
    command.setText("");
    command.focus();
  }

 */
}
