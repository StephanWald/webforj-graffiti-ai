package com.webforj;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GraffitiAssistant {

  public static String getForm(String resp) {

    if (resp != null && resp.indexOf("```json\n")>0) {
      resp = resp.substring(resp.indexOf("```json\n") + 8);
      resp = resp.substring(0, resp.indexOf("\n```\n"));
    } else return "";

    return resp;
  }

  public String createThread(String apiKey) {
    String urlString = "https://api.openai.com/v1/threads";

    try {
      // Create a URL object
      URL url = new URL(urlString);

      // Open a connection
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set the request method to POST
      connection.setRequestMethod("POST");

      // Set request headers
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Authorization", "Bearer " + apiKey);
      connection.setRequestProperty("OpenAI-Beta", "assistants=v2");

      // Enable writing to the connection output stream
      connection.setDoOutput(true);

      // Write the empty JSON payload
      try (OutputStream outputStream = connection.getOutputStream()) {
        String payload = "{}"; // Empty JSON payload
        outputStream.write(payload.getBytes());
        outputStream.flush();
      }

      // Get the response code
      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read the response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
          StringBuilder response = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            response.append(line);
          }

          // Parse the JSON response using Gson
          JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
          if (jsonResponse.has("id")) {
            return jsonResponse.get("id").getAsString();
          }
        }
      }

      // Disconnect the connection
      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null; // Return null if there is an error or no thread ID
  }

  public String createRun(String apiKey, String threadId, String assistantId) {
    String urlString = "https://api.openai.com/v1/threads/" + threadId + "/runs";

    try {
      // Create a URL object
      URL url = new URL(urlString);

      // Open a connection
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set the request method to POST
      connection.setRequestMethod("POST");

      // Set request headers
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Authorization", "Bearer " + apiKey);
      connection.setRequestProperty("OpenAI-Beta", "assistants=v2");

      // Enable writing to the connection output stream
      connection.setDoOutput(true);

      // Write the JSON payload
      try (OutputStream outputStream = connection.getOutputStream()) {
        JsonObject payload = new JsonObject();
        payload.addProperty("assistant_id", assistantId);
        payload.addProperty("additional_instructions", "");
        payload.addProperty("tool_choice", "auto");
        outputStream.write(payload.toString().getBytes());
        outputStream.flush();
      }

      // Get the response code
      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read the response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
          StringBuilder response = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            response.append(line);
          }
          // Parse the JSON response using Gson
          JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
          if (jsonResponse.has("id")) {
            return jsonResponse.get("id").getAsString();
          }
        }
      } else {
        System.err.println(new String(connection.getErrorStream().readAllBytes()));
      }

      // Disconnect the connection
      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null; // Return null if there is an error or no run ID
  }

  public void sendMessage(String apiKey, String threadId, String messageContent, String imageAttachment, String fileAttachment) {

    String imageFileId=null;

    if (imageAttachment != null && !imageAttachment.isBlank()){
      imageFileId = uploadFile(apiKey,imageAttachment,"vision");
    }

    String attachmentFileId=null;

    if (fileAttachment != null && !fileAttachment.isBlank()){
      attachmentFileId = uploadFile(apiKey,fileAttachment,"assistants");
    }

    String urlString = "https://api.openai.com/v1/threads/" + threadId + "/messages";

    try {
      // Create a URL object
      URL url = new URL(urlString);

      // Open a connection
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set the request method to POST
      connection.setRequestMethod("POST");
      // Set request headers
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Authorization", "Bearer " + apiKey);
      connection.setRequestProperty("OpenAI-Beta", "assistants=v2");

      // Enable writing to the connection output stream
      connection.setDoOutput(true);

      // Write the JSON payload
      JsonObject payload = new JsonObject();
      payload.addProperty("role", "user");
      JsonArray contentArray = new JsonArray();

      if (imageFileId != null) {
        JsonObject fileObject = new JsonObject();
        fileObject.addProperty("type", "image_file");

        JsonObject imageFileObject = new JsonObject();
        imageFileObject.addProperty("file_id", imageFileId);
        imageFileObject.addProperty("detail", "auto");
        fileObject.add("image_file", imageFileObject);

        contentArray.add(fileObject);
      }

      JsonObject contentObject = new JsonObject();
      contentObject.addProperty("type", "text");
      contentObject.addProperty("text", messageContent);
      contentArray.add(contentObject);

      payload.add("content", contentArray);

      if (attachmentFileId != null) {

        JsonArray toolsArray = new JsonArray();

        JsonObject attachmentFileToolsObject = new JsonObject();
        attachmentFileToolsObject.addProperty("type", "file_search");
        toolsArray.add(attachmentFileToolsObject);

        JsonObject attachmentFile = new JsonObject();

        JsonObject attachmentFileObject = new JsonObject();
        attachmentFileObject.addProperty("file_id", attachmentFileId);
        attachmentFileObject.add("tools",toolsArray);

        JsonArray attachmentsArray = new JsonArray();
        attachmentsArray.add(attachmentFileObject);


        payload.add("attachments", attachmentsArray);
      }

      try (OutputStream outputStream = connection.getOutputStream()) {
        outputStream.write(payload.toString().getBytes());
        outputStream.flush();
      }

      int responseCode = connection.getResponseCode();

      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getLastMessage(String apiKey, String threadId) {
    String urlString = "https://api.openai.com/v1/threads/" + threadId + "/messages";

    try {
      // Create a URL object
      URL url = new URL(urlString);

      // Open a connection
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set the request method to GET
      connection.setRequestMethod("GET");

      // Set request headers
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Authorization", "Bearer " + apiKey);
      connection.setRequestProperty("OpenAI-Beta", "assistants=v2");

      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read the response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
          StringBuilder response = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            response.append(line);
          }
          // Parse the JSON response and extract the last message content

          JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
          JsonArray data = jsonResponse.getAsJsonArray("data");
          if (data.size() > 0) {
            JsonObject lastMessage = data.get(0).getAsJsonObject();
            JsonArray content = lastMessage.getAsJsonArray("content");
            for (int i = 0; i < content.size(); i++) {
              JsonObject contentItem = content.get(i).getAsJsonObject();
              if (contentItem.has("text")) {
                String  s = contentItem.getAsJsonObject("text").get("value").toString();
                s = s.replace("\\n", "\n");
                s = s.replace("\\\"", "\"");
                return s;
              }
            }
          }

        }
      }

      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null; // Return null if there is an error or no response
  }

  public Boolean isRunFinished(String apiKey, String threadId, String runId) {
    String urlString = "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId;

    try {
      // Create a URL object
      URL url = new URL(urlString);

      // Open a connection
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set the request method to GET
      connection.setRequestMethod("GET");

      // Set request headers
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Authorization", "Bearer " + apiKey);
      connection.setRequestProperty("OpenAI-Beta", "assistants=v2");

      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read the response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
          StringBuilder response = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            response.append(line);
          }
          connection.disconnect();
          return response.toString().contains("\"status\": \"completed\"");
        }
      }
      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }


  public String uploadFile(String apiKey, String filePath, String purpose) {
    String urlString = "https://api.openai.com/v1/files";
    String boundary = "---Boundary" + System.currentTimeMillis();

    try {
      // Create a URL object
      URL url = new URL(urlString);

      // Open a connection
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set the request method to POST
      connection.setRequestMethod("POST");

      // Set request headers
      connection.setRequestProperty("Authorization", "Bearer " + apiKey);
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

      // Enable writing to the connection output stream
      connection.setDoOutput(true);

      try (OutputStream outputStream = connection.getOutputStream()) {
        // Write the "purpose" field
        outputStream.write(("--" + boundary + "\r\n").getBytes());
        outputStream.write("Content-Disposition: form-data; name=\"purpose\"\r\n\r\n".getBytes());
        outputStream.write((purpose+ "\r\n").getBytes());

        // Write the file field
        outputStream.write(("--" + boundary + "\r\n").getBytes());
        outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + new File(filePath).getName() + "\"\r\n").getBytes());
        outputStream.write("Content-Type: application/jsonl\r\n\r\n".getBytes());

        // Write the file content
        Files.copy(new File(filePath).toPath(), outputStream);
        outputStream.write("\r\n".getBytes());

        // End of multipart
        outputStream.write(("--" + boundary + "--\r\n").getBytes());
      }

      // Get the response code
      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read the response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
          StringBuilder response = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            response.append(line);
          }

          // Parse the JSON response using Gson
          JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
          if (jsonResponse.has("id")) {
            return jsonResponse.get("id").getAsString();
          }
        }
      } else {
        System.err.println(new String(connection.getErrorStream().readAllBytes()));
      }

      // Disconnect the connection
      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null; // Return null if there is an error or no file ID
  }

}
