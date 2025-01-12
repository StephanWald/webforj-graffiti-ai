package com.webforj.views;

import com.webforj.App;
import com.webforj.Application;
import com.webforj.BusyIndicator;
import com.webforj.GraffitiAssistant;
import com.webforj.Markdown;
import com.webforj.UploadedFile;
import com.webforj.component.Component;
import com.webforj.component.Theme;
import com.webforj.component.button.Button;
import com.webforj.component.button.ButtonTheme;
import com.webforj.component.button.event.ButtonClickEvent;
import com.webforj.component.dialog.Dialog;
import com.webforj.component.drawer.Drawer;
import com.webforj.component.element.event.ElementClickEvent;
import com.webforj.component.field.TextArea;
import com.webforj.component.html.elements.Div;
import com.webforj.component.html.elements.Img;
import com.webforj.component.icons.Icon;
import com.webforj.component.icons.IconButton;
import com.webforj.component.icons.TablerIcon;
import com.webforj.component.layout.flexlayout.FlexContentAlignment;
import com.webforj.component.layout.flexlayout.FlexJustifyContent;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.optiondialog.FileChooserFilter;
import com.webforj.component.optiondialog.FileUploadDialog;
import com.webforj.component.toast.Toast;
import com.webforj.graffiti.model.util.PodLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.webforj.ImageBase64Encoder.encodeImageToBase64Src;

public class GraffitiAIPanel {

  private final String apiKey;
  final static String assistantId = "asst_aYPu0Q9JNcAm0He9vHbbzCLd";
  final GraffitiAssistant assistant = new GraffitiAssistant();

  private String runId;
  private String threadId;
  private String fileId;
  private String lastJson = "";
  private String lastResponse = "(no last respone)";

  private Drawer drawer;
  private TextArea instructionInput;
  private Button runButton;
  private IconButton imgButton;
  private IconButton debugButton;
  private IconButton drawerButton;
  private Div previewBox;
  private Component preview=null;

  private Consumer<String> onDoneCallback;
  private Dialog debugDialog;
  private Markdown debugMarkdown;
  private String pendingFile = "";

  public GraffitiAIPanel(){
    this.apiKey = getApiKey();
    if (apiKey == null){
      Toast.show("API Key not set!", Theme.DANGER);
    }

    drawerButton = new IconButton(TablerIcon.create("robot"));
    drawerButton.addClickListener(this::toggleDrawer);

  }

  private void toggleDrawer(ElementClickEvent<Icon> iconElementClickEvent) {
    drawer.toggle(!drawer.isOpened());
  }

  public void onGenerate(Consumer<String> callback){
    this.onDoneCallback = callback;
  }

  public Drawer getDrawer() {
    if (drawer == null) {
      drawer = new Drawer();
      drawer.setLabel("AI Tool");
      drawer.open();

      instructionInput = new TextArea();
      instructionInput.setPlaceholder("Type instruction, like 'Create an address form.'");
      instructionInput.setLabel("What can I help with?");
      instructionInput.setText("create a simple hello world form with two fields and a button.");
      instructionInput.setStyle("width","100%");
      instructionInput.setStyle("height","40%");

      FlexLayout btnLayout = new FlexLayout()
          .create()
          .horizontal()
          .build();
      btnLayout.setMargin("9px 0px 0px 0px")
          .setWidth("100%")
          .setJustifyContent(FlexJustifyContent.END)
          .setAlignContent(FlexContentAlignment.END);

      debugButton = new IconButton(TablerIcon.create("bug"));
      debugButton.setStyle("align-self","center");
      debugButton.addClickListener(this::onDebugButtonClick);
      debugButton.setVisible(false);

      imgButton = new IconButton(TablerIcon.create("photo"));
      imgButton.setStyle("align-self","center");
      imgButton.addClickListener(this::onImageButtonClick);
      
      runButton = new Button("Run");
      runButton.setTheme(ButtonTheme.SUCCESS);
      runButton.setSuffixComponent(TablerIcon.create("robot"));
      runButton.addClickListener(this::onRun);

      btnLayout.add(debugButton,imgButton,runButton);

      previewBox = new Div()
          .setMaxWidth("66%")
          .setHeight("100px")
          .setStyle("margin-top","20px");

      drawer.add(instructionInput,btnLayout, previewBox);

      drawer.addClassName("drawer");
    }
    return drawer;
  }

  private void onDebugButtonClick(ElementClickEvent<Icon> iconElementClickEvent) {

    if (debugDialog == null) {
      debugDialog = new  Dialog();
      drawer.add(debugDialog);
      debugDialog.setBackdrop(true);
      debugDialog.setCloseable(true);
      debugMarkdown = new Markdown();
      debugDialog.add(debugMarkdown);
    }

    debugMarkdown.setText(lastResponse);

    debugDialog.open();
    String scr = "if (typeof Prism != 'undefined'){Prism.highlightAll();}";
    App.getPage().executeJsAsync(scr);
  }



  private void clearPreview() {
    if (preview != null) {
      preview.destroy();
      preview = null;
    }
  }

  private void onImageButtonClick(ElementClickEvent<Icon> iconElementClickEvent) {

    FileUploadDialog dialog = new FileUploadDialog(
        "Upload a picture",
        Arrays.asList(new FileChooserFilter("Image Files", "*.png,*,.jpg")));
    UploadedFile result = dialog.show();

    if (result==null) return;

    if (!"PNG JPG WEBP GIF BMP".contains(result.getClientExtension().toUpperCase())) {
      Toast.show("Unknown File Type!", Theme.DANGER);
      result.delete();
      return;
    }

    String myfile=null;
    try {
      myfile = result.move(System.currentTimeMillis()+result.getSanitizedClientName()).getAbsolutePath();
    } catch (IOException e) {
      Toast.show("File upload failed!", Theme.DANGER);
      return;
    }

    this.pendingFile = myfile;
    if (instructionInput.getText().isBlank())
      instructionInput.setText("Create a form like in the picture.");

    clearPreview();

    preview = new Img()
        .setWidth("100%")
        .setHeight("fit-content")
        .setSrc(encodeImageToBase64Src(myfile));
    previewBox.add(preview);
  }

  private void onRun(ButtonClickEvent buttonClickEvent) {

    BusyIndicator busyIndicator =  App.getBusyIndicator();
    busyIndicator.setText("Submitting form...")
        .setBackdropVisible(true);
    busyIndicator.getSpinner().setTheme(Theme.PRIMARY);
    busyIndicator.open();

    debugButton.setVisible(true);

    String commandText = instructionInput.getText();

    if (threadId == null) {
      threadId = assistant.createThread(apiKey);
    }

    int retry = 0;
    boolean done_okay=false;
    while (retry < 3 && !done_okay) {
      retry++;

      assistant.sendMessage(apiKey, threadId, commandText, this.pendingFile);
      this.pendingFile="";


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

      onDoneCallback.accept(jsonForm);
      lastJson = jsonForm;
      lastResponse = response;
      drawer.close();

      try {
        PodLoader loader = new PodLoader();
        loader.fromJson(jsonForm).load();
        done_okay=true;
      } catch (Exception e) {
        busyIndicator.setText("Error "+e.getMessage()+" Retrying to generate...");
        commandText="Please retry. This form produced the following error: "+e.getMessage();
      }


    }
    busyIndicator.setVisible(false);
    instructionInput.setText("");
    instructionInput.focus();

  }

  private static String getApiKey() {
    String apikey="";
    try (InputStream inputStream = Application.class.getClassLoader().getResourceAsStream("apikey.txt"); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

      if (inputStream == null) {
        return null;
      }

      apikey=reader.lines().collect(Collectors.joining(System.lineSeparator()));

    } catch (IOException e) {
      return null;
    }

    if (apikey.isBlank() || apikey.equals("replace this with your apikey")){
      return null;
    }

    return apikey;
  }

  public IconButton getDrawerButton() {
    return drawerButton;
  }
}

