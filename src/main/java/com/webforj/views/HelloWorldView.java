package com.webforj.views;

import com.webforj.App;
import com.webforj.Application;
import com.webforj.BusyIndicator;
import com.webforj.GraffitiAssistant;
import com.webforj.UploadedFile;
import com.webforj.component.Component;
import com.webforj.component.Composite;
import com.webforj.component.Theme;
import com.webforj.component.button.Button;
import com.webforj.component.button.ButtonTheme;
import com.webforj.component.button.event.ButtonClickEvent;
import com.webforj.component.field.TextArea;
import com.webforj.component.html.elements.H3;
import com.webforj.component.html.elements.Img;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.optiondialog.OptionDialog;
import com.webforj.component.text.Label;
import com.webforj.graffiti.model.util.PodLoader;
import com.webforj.router.annotation.Route;

import java.io.IOException;

import static com.webforj.ImageBase64Encoder.encodeImageToBase64Src;

@Route("/")
public class HelloWorldView extends Composite<FlexLayout> {

  private FlexLayout self = getBoundComponent();
  private TextArea command = new TextArea("Enter command:");
  private Button btn = new Button("GO");
  private Button btnImage = new Button("Image");
  private TextArea json = new TextArea("JSON");
  private Component preview;
  PodLoader loader = new PodLoader();

  private String runId;
  private String threadId;
  private String fileId;

  String apiKey = Application.getApiKey();

  final String assistantId = "asst_aYPu0Q9JNcAm0He9vHbbzCLd";

  GraffitiAssistant assistant = new GraffitiAssistant();


  public HelloWorldView(){
    self.setDirection(FlexDirection.COLUMN);

    self.setStyle("padding", "20px");

    btn.setTheme(ButtonTheme.PRIMARY)
        .addClickListener(this::go);

    btnImage.addClickListener(this::addImage);

    self.add(command, btnImage,btn, json, new H3("Preview:") {
    });
  }

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
}
