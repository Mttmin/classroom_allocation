package com.roomallocation.visualization;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class PythonVisualizer {
    private final String pythonExecutable;
    
    public PythonVisualizer(String pythonExecutable) {
        this.pythonExecutable = pythonExecutable;
    }
    
    private void extractAndRunPythonScript(String resourcePath, String jsonPath) throws IOException, InterruptedException {
        // Create output directory if it doesn't exist
        Path jsonFilePath = Paths.get(jsonPath);
        Files.createDirectories(jsonFilePath.getParent());
        
        // Copy Python script from resources to a temporary file
        Path tempScript = extractPythonScript(resourcePath);
        
        try {
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(tempScript.toString());
            command.add(jsonPath);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read and print the Python script's output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script failed with exit code: " + exitCode);
            }
        } finally {
            // Clean up the temporary script file
            try {
                Files.deleteIfExists(tempScript);
            } catch (IOException e) {
                System.err.println("Warning: Could not delete temporary script file: " + e.getMessage());
            }
        }
    }

    private Path extractPythonScript(String resourcePath) throws IOException {
        // Create a temporary file for the Python script
        Path tempScript = Files.createTempFile("visualize_", ".py");
        
        // Copy the script from resources to the temporary file
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Could not find " + resourcePath + " in resources");
            }
            Files.copy(is, tempScript, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return tempScript;
    }

    public void visualize(String jsonPath) throws IOException, InterruptedException {
        System.out.println("Generating standard visualizations...");
        extractAndRunPythonScript("/visualize.py", jsonPath);
        
        System.out.println("\nGenerating room map visualization...");
        extractAndRunPythonScript("/visualize_map.py", jsonPath);
        
        System.out.println("\nVisualization completed successfully!");
        System.out.println("Plots saved in directory: " + Paths.get(jsonPath).getParent());
    }
}