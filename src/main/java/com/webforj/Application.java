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




}
