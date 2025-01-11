package com.webforj;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageBase64Encoder {

  public static String encodeImageToBase64Src(String imagePath) {
    try {
      // Read the image file into a byte array
      byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));

      // Encode the byte array to a Base64 string
      String base64String = Base64.getEncoder().encodeToString(imageBytes);

      // Determine the file type (e.g., jpg, png) from the file extension
      String fileType = getFileExtension(imagePath);

      // Return the full src attribute for an img tag
      return "data:image/" + fileType + ";base64," + base64String;
    } catch (Exception e) {
      e.printStackTrace();
      return null; // Return null in case of an error
    }
  }

  private static String getFileExtension(String filePath) {
    // Extract the file extension from the file path
    int lastDotIndex = filePath.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
      return filePath.substring(lastDotIndex + 1).toLowerCase();
    } else {
      return "unknown"; // Default if no extension is found
    }
  }

  public static void main(String[] args) {
    // Example usage
    String imagePath = "/Users/beff/Downloads/Forms.png"; // Replace with your image path
    String imgSrc = encodeImageToBase64Src(imagePath);

    if (imgSrc != null) {
      System.out.println("<img src=\"" + imgSrc + "\" />");
    } else {
      System.out.println("Failed to encode the image.");
    }
  }
}
