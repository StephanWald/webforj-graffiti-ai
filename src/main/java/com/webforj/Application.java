package com.webforj;

import com.webforj.annotation.AppTitle;
import com.webforj.annotation.Routify;
import com.webforj.annotation.StyleSheet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.webforj.component.optiondialog.OptionDialog;

/**
 * A simple HelloWorld app.
 */
@Routify(packages = "com.webforj.views")
@AppTitle("webforJ Hello World")
@StyleSheet("ws://app.css")
public class Application extends App {

  public static String getApiKey() {
    String apikey="";
    try (InputStream inputStream = Application.class.getClassLoader().getResourceAsStream("apikey.txt"); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

      if (inputStream == null) {
        throw new IllegalArgumentException("File not found in classpath: apikey.txt");
      }

      apikey=reader.lines().collect(Collectors.joining(System.lineSeparator()));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (apikey.isBlank() || apikey.equals("replace this with your apikey")){
      OptionDialog.showMessageDialog("API Key not set!");
      throw new RuntimeException(("API Key not set"));
    }

    return apikey;
  }
}
