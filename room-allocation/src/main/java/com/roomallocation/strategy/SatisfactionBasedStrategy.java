package com.roomallocation.strategy;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.roomallocation.model.Course;
import com.roomallocation.model.Room;
import com.roomallocation.model.RoomType;

public class SatisfactionBasedStrategy extends PreferenceGenerationStrategy {
    private final Map<RoomType, Double> satisfactionScores;
    private final Map<RoomType, Integer> maxCapacities;
    private final double temperatureFactor = 2.0; // Controls how much we favor higher-rated rooms

    public SatisfactionBasedStrategy(int numPreferences, List<Room> rooms) {
        super(numPreferences, "satisfaction");
        
        // Initialize satisfaction scores based on survey data
        this.satisfactionScores = new EnumMap<>(RoomType.class);
        satisfactionScores.put(RoomType.NOUVEAUX_AMPHIS, 4.24);
        satisfactionScores.put(RoomType.SALLES_100, 3.67);
        satisfactionScores.put(RoomType.COULOIR_SCOLARITE, 3.62);
        satisfactionScores.put(RoomType.SALLES_LANGUES, 3.40);
        satisfactionScores.put(RoomType.COULOIR_LABOS, 3.29);
        satisfactionScores.put(RoomType.GRANDS_AMPHIS, 3.29);
        satisfactionScores.put(RoomType.SALLES_INFO, 3.10);
        satisfactionScores.put(RoomType.COULOIR_VANNEAU, 3.00);
        satisfactionScores.put(RoomType.AMPHIS_80_100, 2.57);
        satisfactionScores.put(RoomType.AMPHI_COULOIR_BINETS, 2.33);

        // Track maximum capacity for each room type
        this.maxCapacities = new EnumMap<>(RoomType.class);
        for (Room room : rooms) {
            maxCapacities.merge(room.getType(), room.getCapacity(), Math::max);
        }
    }

    @Override
    public List<RoomType> generatePreferences(Course course) {
        int courseSize = course.getCohortSize();
        
        // Filter room types that can accommodate the course
        List<RoomType> suitableTypes = maxCapacities.entrySet().stream()
            .filter(e -> e.getValue() >= courseSize)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
            
        // If no suitable rooms found, return empty list
        if (suitableTypes.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate weights based on satisfaction scores and apply softmax
        double[] weights = new double[suitableTypes.size()];
        double maxScore = 0;
        for (int i = 0; i < suitableTypes.size(); i++) {
            weights[i] = Math.exp(satisfactionScores.get(suitableTypes.get(i)) * temperatureFactor);
            maxScore = Math.max(maxScore, weights[i]);
        }

        // Normalize weights
        for (int i = 0; i < weights.length; i++) {
            weights[i] /= maxScore;
        }

        // Generate preferences using weighted random selection
        List<RoomType> preferences = new ArrayList<>();
        List<RoomType> remainingTypes = new ArrayList<>(suitableTypes);
        List<Double> remainingWeights = new ArrayList<>();
        for (double weight : weights) {
            remainingWeights.add(weight);
        }

        while (!remainingTypes.isEmpty() && preferences.size() < numPreferences) {
            // Calculate sum of remaining weights
            double totalWeight = remainingWeights.stream().mapToDouble(Double::doubleValue).sum();
            
            // Generate random value
            double rand = random.nextDouble() * totalWeight;
            
            // Select room type based on weights
            double cumSum = 0;
            int selectedIndex = -1;
            for (int i = 0; i < remainingWeights.size(); i++) {
                cumSum += remainingWeights.get(i);
                if (rand <= cumSum) {
                    selectedIndex = i;
                    break;
                }
            }
            
            // Add selected type to preferences and remove from remaining options
            preferences.add(remainingTypes.get(selectedIndex));
            remainingTypes.remove(selectedIndex);
            remainingWeights.remove(selectedIndex);
        }

        return preferences;
    }
}