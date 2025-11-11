package com.roomallocation.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Exports statistics and generates HTML visualizations with charts
 */
public class VisualizationExporter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);
    private static final String OUTPUT_DIR = "output/visualizations";

    /**
     * Export comparison results to HTML with interactive charts
     */
    public static void exportComparison(MetricsComparator comparator, String filename) throws IOException {
        // Create output directory if it doesn't exist
        Path outputPath = Paths.get(OUTPUT_DIR);
        Files.createDirectories(outputPath);

        // Generate timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Generate HTML
        String html = generateComparisonHTML(comparator, timestamp);

        // Write HTML file
        Path htmlFile = outputPath.resolve(filename + ".html");
        try (FileWriter writer = new FileWriter(htmlFile.toFile())) {
            writer.write(html);
        }

        // Also export JSON data
        exportJSON(comparator.toMap(), filename + ".json");

        System.out.println("Visualization exported to: " + htmlFile.toAbsolutePath());
    }

    /**
     * Export individual scheduler statistics to HTML
     */
    public static void exportSchedulerStats(SchedulerStatistics stats, String algorithmName, String filename) throws IOException {
        Path outputPath = Paths.get(OUTPUT_DIR);
        Files.createDirectories(outputPath);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String html = generateSchedulerHTML(stats, algorithmName, timestamp);

        Path htmlFile = outputPath.resolve(filename + ".html");
        try (FileWriter writer = new FileWriter(htmlFile.toFile())) {
            writer.write(html);
        }

        // Export JSON
        exportJSON(stats.toMap(), filename + ".json");

        System.out.println("Statistics exported to: " + htmlFile.toAbsolutePath());
    }

    /**
     * Export data as JSON
     */
    public static void exportJSON(Map<String, Object> data, String filename) throws IOException {
        Path outputPath = Paths.get(OUTPUT_DIR);
        Files.createDirectories(outputPath);

        Path jsonFile = outputPath.resolve(filename);
        MAPPER.writeValue(jsonFile.toFile(), data);
    }

    private static String generateComparisonHTML(MetricsComparator comparator, String timestamp) {
        List<MetricsComparator.AlgorithmResult> results = comparator.getResults();

        // Prepare data for charts
        StringBuilder algorithmNames = new StringBuilder();
        StringBuilder schedulingRates = new StringBuilder();
        StringBuilder allocationRates = new StringBuilder();
        StringBuilder totalScores = new StringBuilder();
        StringBuilder executionTimes = new StringBuilder();
        StringBuilder avgGaps = new StringBuilder();
        StringBuilder correlationConflicts = new StringBuilder();
        StringBuilder businessHours = new StringBuilder();
        StringBuilder earlyMorning = new StringBuilder();

        for (int i = 0; i < results.size(); i++) {
            MetricsComparator.AlgorithmResult result = results.get(i);
            if (i > 0) {
                algorithmNames.append(", ");
                schedulingRates.append(", ");
                allocationRates.append(", ");
                totalScores.append(", ");
                executionTimes.append(", ");
                avgGaps.append(", ");
                correlationConflicts.append(", ");
                businessHours.append(", ");
                earlyMorning.append(", ");
            }

            algorithmNames.append("'").append(result.getAlgorithmName()).append("'");
            schedulingRates.append(result.getSchedulerStats().getSchedulingRate() * 100);

            if (result.getAllocationStats() != null) {
                allocationRates.append(result.getAllocationStats().getAllocationRate() * 100);
            } else {
                allocationRates.append("0");
            }

            totalScores.append(result.getSchedulerStats().getTotalScore());
            executionTimes.append(result.getSchedulerStats().getExecutionTimeMs());
            avgGaps.append(result.getSchedulerStats().getAverageGapMinutes());
            correlationConflicts.append(result.getSchedulerStats().getCorrelationConflicts());
            businessHours.append(result.getSchedulerStats().getCoursesInBusinessHours());
            earlyMorning.append(result.getSchedulerStats().getCoursesEarlyMorning());
        }

        return String.format(HTML_COMPARISON_TEMPLATE,
            timestamp,                                                      // %s #1 - timestamp
            comparator.generateComparisonReport().replace("\n", "<br>"),   // %s #2 - detailed report
            algorithmNames.toString(),                                      // %s #3 - algorithms array
            schedulingRates.toString(),                                     // %s #4 - scheduling rates
            allocationRates.toString(),                                     // %s #5 - allocation rates
            totalScores.toString(),                                         // %s #6 - total scores
            executionTimes.toString(),                                      // %s #7 - execution times
            avgGaps.toString(),                                             // %s #8 - avg gaps
            correlationConflicts.toString(),                                // %s #9 - correlation conflicts
            businessHours.toString(),                                       // %s #10 - business hours (9am-5pm)
            earlyMorning.toString()                                         // %s #11 - early morning (8am-10am)
        );
    }

    private static String generateSchedulerHTML(SchedulerStatistics stats, String algorithmName, String timestamp) {
        // Prepare time slot distribution data
        Map<String, Integer> timeSlots = stats.getTimeSlotDistribution();
        StringBuilder timeLabels = new StringBuilder();
        StringBuilder timeValues = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Integer> entry : timeSlots.entrySet()) {
            if (i > 0) {
                timeLabels.append(", ");
                timeValues.append(", ");
            }
            timeLabels.append("'").append(entry.getKey()).append("'");
            timeValues.append(entry.getValue());
            i++;
        }

        // Prepare day distribution data
        Map<String, Integer> dayDist = new LinkedHashMap<>();
        stats.getDayDistribution().forEach((day, count) -> dayDist.put(day.toString(), count));
        StringBuilder dayLabels = new StringBuilder();
        StringBuilder dayValues = new StringBuilder();
        i = 0;
        for (Map.Entry<String, Integer> entry : dayDist.entrySet()) {
            if (i > 0) {
                dayLabels.append(", ");
                dayValues.append(", ");
            }
            dayLabels.append("'").append(entry.getKey()).append("'");
            dayValues.append(entry.getValue());
            i++;
        }

        // Prepare score breakdown data
        Map<String, Double> scoreBreakdown = stats.getScoreBreakdown();
        StringBuilder scoreLabels = new StringBuilder();
        StringBuilder scoreValues = new StringBuilder();
        i = 0;
        for (Map.Entry<String, Double> entry : scoreBreakdown.entrySet()) {
            if (i > 0) {
                scoreLabels.append(", ");
                scoreValues.append(", ");
            }
            scoreLabels.append("'").append(entry.getKey()).append("'");
            scoreValues.append(entry.getValue());
            i++;
        }

        return String.format(HTML_SINGLE_TEMPLATE,
            algorithmName,
            timestamp,
            algorithmName,
            stats.getScheduledCourses(),
            stats.getTotalCourses(),
            String.format("%.2f", stats.getSchedulingRate() * 100),
            String.format("%.2f", stats.getTotalScore()),
            stats.getExecutionTimeMs(),
            timeLabels.toString(),
            timeValues.toString(),
            dayLabels.toString(),
            dayValues.toString(),
            scoreLabels.toString(),
            scoreValues.toString(),
            stats.toString().replace("\n", "<br>")
        );
    }

    private static final String HTML_COMPARISON_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Algorithm Comparison Report</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            max-width: 1400px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        h1, h2 {
            color: #333;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .timestamp {
            font-size: 14px;
            opacity: 0.9;
        }
        .chart-container {
            background: white;
            padding: 20px;
            margin: 20px 0;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .chart-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
            gap: 20px;
        }
        .report-section {
            background: white;
            padding: 20px;
            margin: 20px 0;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            font-family: 'Courier New', monospace;
            white-space: pre-wrap;
            overflow-x: auto;
        }
        canvas {
            max-height: 400px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Algorithm Comparison Report</h1>
        <div class="timestamp">Generated: %s</div>
    </div>

    <div class="chart-grid">
        <div class="chart-container">
            <h2>Scheduling Rate (%%)</h2>
            <canvas id="schedulingRateChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Allocation Rate (%%)</h2>
            <canvas id="allocationRateChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Total Score (Lower is Better)</h2>
            <canvas id="totalScoreChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Execution Time (ms)</h2>
            <canvas id="executionTimeChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Average Professor Gaps (minutes)</h2>
            <canvas id="avgGapsChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Correlation Conflicts</h2>
            <canvas id="conflictsChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Business Hours Classes (9am-5pm)</h2>
            <canvas id="businessHoursChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Early Morning Classes (8am-10am)</h2>
            <canvas id="earlyMorningChart"></canvas>
        </div>
    </div>

    <div class="report-section">
        <h2>Detailed Comparison Report</h2>
        %s
    </div>

    <script>
        const algorithms = [%s];
        const colors = [
            'rgba(54, 162, 235, 0.8)',
            'rgba(255, 99, 132, 0.8)',
            'rgba(75, 192, 192, 0.8)',
            'rgba(255, 206, 86, 0.8)',
            'rgba(153, 102, 255, 0.8)',
            'rgba(255, 159, 64, 0.8)'
        ];

        const borderColors = colors.map(c => c.replace('0.8', '1'));

        function createBarChart(ctx, label, data) {
            return new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: algorithms,
                    datasets: [{
                        label: label,
                        data: data,
                        backgroundColor: colors.slice(0, data.length),
                        borderColor: borderColors.slice(0, data.length),
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: { beginAtZero: true }
                    }
                }
            });
        }

        // Create all charts
        createBarChart(
            document.getElementById('schedulingRateChart'),
            'Scheduling Rate (%%)',
            [%s]
        );

        createBarChart(
            document.getElementById('allocationRateChart'),
            'Allocation Rate (%%)',
            [%s]
        );

        createBarChart(
            document.getElementById('totalScoreChart'),
            'Total Score',
            [%s]
        );

        createBarChart(
            document.getElementById('executionTimeChart'),
            'Execution Time (ms)',
            [%s]
        );

        createBarChart(
            document.getElementById('avgGapsChart'),
            'Average Gaps (min)',
            [%s]
        );

        createBarChart(
            document.getElementById('conflictsChart'),
            'Correlation Conflicts',
            [%s]
        );

        createBarChart(
            document.getElementById('businessHoursChart'),
            'Business Hours (9am-5pm)',
            [%s]
        );

        createBarChart(
            document.getElementById('earlyMorningChart'),
            'Early Morning (8am-10am)',
            [%s]
        );
    </script>
</body>
</html>
""";

    private static final String HTML_SINGLE_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>%s - Statistics Report</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            max-width: 1400px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .stats-summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            text-align: center;
        }
        .stat-value {
            font-size: 32px;
            font-weight: bold;
            color: #667eea;
        }
        .stat-label {
            color: #666;
            margin-top: 10px;
        }
        .chart-container {
            background: white;
            padding: 20px;
            margin: 20px 0;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .chart-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
            gap: 20px;
        }
        .report-section {
            background: white;
            padding: 20px;
            margin: 20px 0;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            font-family: 'Courier New', monospace;
            white-space: pre-wrap;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>%s - Statistics Report</h1>
        <div class="timestamp">Generated: %s</div>
    </div>

    <div class="stats-summary">
        <div class="stat-card">
            <div class="stat-value">%d/%d</div>
            <div class="stat-label">Courses Scheduled</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">%s%%</div>
            <div class="stat-label">Success Rate</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">%s</div>
            <div class="stat-label">Total Score</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">%dms</div>
            <div class="stat-label">Execution Time</div>
        </div>
    </div>

    <div class="chart-grid">
        <div class="chart-container">
            <h2>Time Slot Distribution</h2>
            <canvas id="timeSlotChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Day of Week Distribution</h2>
            <canvas id="dayChart"></canvas>
        </div>

        <div class="chart-container">
            <h2>Score Breakdown</h2>
            <canvas id="scoreChart"></canvas>
        </div>
    </div>

    <div class="report-section">
        <h2>Detailed Statistics</h2>
        %s
    </div>

    <script>
        // Time slot distribution
        new Chart(document.getElementById('timeSlotChart'), {
            type: 'pie',
            data: {
                labels: [%s],
                datasets: [{
                    data: [%s],
                    backgroundColor: [
                        'rgba(255, 206, 86, 0.8)',
                        'rgba(54, 162, 235, 0.8)',
                        'rgba(153, 102, 255, 0.8)'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });

        // Day distribution
        new Chart(document.getElementById('dayChart'), {
            type: 'bar',
            data: {
                labels: [%s],
                datasets: [{
                    label: 'Sessions',
                    data: [%s],
                    backgroundColor: 'rgba(75, 192, 192, 0.8)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });

        // Score breakdown
        new Chart(document.getElementById('scoreChart'), {
            type: 'bar',
            data: {
                labels: [%s],
                datasets: [{
                    label: 'Penalty Score',
                    data: [%s],
                    backgroundColor: 'rgba(255, 99, 132, 0.8)',
                    borderColor: 'rgba(255, 99, 132, 1)',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
    </script>
</body>
</html>
""";
}
