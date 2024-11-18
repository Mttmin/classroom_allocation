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

    public void visualize(String jsonPath) throws IOException, InterruptedException {
        // Copy Python script from resources to a temporary file
        Path tempScript = extractPythonScript();
        
        try {
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(tempScript.toString());
            command.add(jsonPath);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            System.out.println("Executing Python visualization script...");
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
            
            System.out.println("Visualization completed successfully!");
        } finally {
            // Clean up the temporary script file
            try {
                Files.deleteIfExists(tempScript);
            } catch (IOException e) {
                System.err.println("Warning: Could not delete temporary script file: " + e.getMessage());
            }
        }
    }

    private Path extractPythonScript() throws IOException {
        // Create a temporary file for the Python script
        Path tempScript = Files.createTempFile("visualize_", ".py");
        
        // Copy the script from resources to the temporary file
        try (InputStream is = getClass().getResourceAsStream("/visualize.py")) {
            if (is == null) {
                throw new IOException("Could not find visualize.py in resources");
            }
            Files.copy(is, tempScript, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return tempScript;
    }
}