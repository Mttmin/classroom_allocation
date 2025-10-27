# Room Images Setup Guide

This guide explains how to add classroom images to your classroom allocation application.

## Overview

The classroom type selection UI displays cards with information about each room type, including image sliders. This document explains how to add and manage these images.

## Image Storage Options

### Option 1: Local Images (Recommended for Production)

1. **Create an images directory in your project:**
   ```
   frontend/public/images/rooms/
   ```

2. **Organize images by room type:**
   ```
   frontend/public/images/rooms/
   ├── couloir_vanneau/
   │   ├── room1.jpg
   │   ├── room2.jpg
   │   └── room3.jpg
   ├── couloir_scolarite/
   │   ├── room1.jpg
   │   └── room2.jpg
   ├── salles_info/
   │   ├── lab1.jpg
   │   ├── lab2.jpg
   │   └── lab3.jpg
   └── ... (other room types)
   ```

3. **Update the RoomTypeService.ts to use local images:**

   Edit `frontend/src/services/roomService.ts` and replace the placeholder URLs:

   ```typescript
   // Example for Couloir Vanneau
   images: [
     '/images/rooms/couloir_vanneau/room1.jpg',
     '/images/rooms/couloir_vanneau/room2.jpg',
     '/images/rooms/couloir_vanneau/room3.jpg'
   ]
   ```

### Option 2: External Image URLs

If your images are hosted externally (e.g., on a CDN or image server), simply update the `images` array in `roomService.ts` with the full URLs:

```typescript
images: [
  'https://yourdomain.com/images/rooms/couloir_vanneau_1.jpg',
  'https://yourdomain.com/images/rooms/couloir_vanneau_2.jpg'
]
```

### Option 3: Backend-Served Images (Most Flexible)

For a dynamic solution where images can be updated without redeploying the frontend:

1. **Create an images directory in the Java backend:**
   ```
   src/main/resources/static/images/rooms/
   ```

2. **Update RoomTypeService.java** to load image paths from a configuration file or database.

3. **Modify the `initializePlaceholderImages()` method** in `RoomTypeService.java`:

   ```java
   private void initializePlaceholderImages() {
       // Load from configuration or database
       roomTypeImages.put(RoomType.COULOIR_VANNEAU, Arrays.asList(
           "/static/images/rooms/couloir_vanneau/room1.jpg",
           "/static/images/rooms/couloir_vanneau/room2.jpg",
           "/static/images/rooms/couloir_vanneau/room3.jpg"
       ));
       // ... repeat for other room types
   }
   ```

## Current Room Types

Based on the `rooms.csv` file, you need images for these 10 room types:

| Room Type | Description | Folder Name |
|-----------|-------------|-------------|
| COULOIR_VANNEAU | Couloir Cour Vanneau | `couloir_vanneau` |
| COULOIR_SCOLARITE | Couloir de la Scolarité | `couloir_scolarite` |
| COULOIR_LABOS | Couloir des Labos | `couloir_labos` |
| SALLES_100 | Salles 100 | `salles_100` |
| AMPHI_COULOIR_BINETS | Amphi Couloir Binets | `amphi_couloir_binets` |
| SALLES_INFO | Salles Info (Computer Labs) | `salles_info` |
| SALLES_LANGUES | Salles Langues (Language Labs) | `salles_langues` |
| NOUVEAUX_AMPHIS | Nouveaux Amphis | `nouveaux_amphis` |
| GRANDS_AMPHIS | Grands Amphis | `grands_amphis` |
| AMPHIS_80_100 | Amphis 80-100 Places | `amphis_80_100` |

## Image Requirements

### Technical Specifications

- **Format:** JPG, PNG, or WebP
- **Resolution:** 800x600 pixels minimum (maintains 4:3 aspect ratio)
- **File Size:** < 500KB per image (optimize for web)
- **Naming Convention:** Use descriptive names (e.g., `room1.jpg`, `view_from_back.jpg`)

### Photography Guidelines

1. **Lighting:** Ensure good lighting that shows the room clearly
2. **Angles:** Take photos from multiple angles:
   - Front view (from student perspective)
   - Back view (showing the entire room)
   - Side views
   - Close-ups of special equipment/amenities
3. **Content:** Show:
   - Overall room layout
   - Seating arrangement
   - Whiteboard/projector
   - Special equipment (computers, lab equipment, etc.)

## Quick Start with Current Setup

Currently, the application uses **placeholder images** from placeholder.com. These are automatically generated based on room type names.

To replace them:

1. **Frontend (TypeScript):** Edit `frontend/src/services/roomService.ts`
   - Locate the `mockRoomData` array
   - Update the `images` property for each room type

2. **Backend (Java):** Edit `src/main/java/com/roomallocation/service/RoomTypeService.java`
   - Locate the `initializePlaceholderImages()` method
   - Replace placeholder URLs with actual image paths

## Example: Adding Images for "Salles Info"

### Frontend (roomService.ts)

```typescript
{
  type: RoomType.SALLES_INFO,
  name: 'Salles Info',
  building: 'Main',
  seatRange: { min: 18, max: 24 },
  roomCount: 7,
  amenities: [
    'Computer workstations',
    'Whiteboard',
    'Projector',
    'High-speed internet',
    'Software licenses'
  ],
  images: [
    '/images/rooms/salles_info/computer_lab_overview.jpg',
    '/images/rooms/salles_info/workstations.jpg',
    '/images/rooms/salles_info/projector_setup.jpg'
  ]
}
```

### Backend (RoomTypeService.java)

```java
roomTypeImages.put(RoomType.SALLES_INFO, Arrays.asList(
    "/static/images/rooms/salles_info/computer_lab_overview.jpg",
    "/static/images/rooms/salles_info/workstations.jpg",
    "/static/images/rooms/salles_info/projector_setup.jpg"
));
```

## Building Setup

Make sure you have the building name configured correctly. Currently, all rooms are assigned to "Main" building.

To customize buildings per room type:

### Frontend (roomService.ts)

Update the `building` property in each `RoomTypeInfo` object:

```typescript
{
  type: RoomType.SALLES_INFO,
  name: 'Salles Info',
  building: 'Science Building',  // <-- Change this
  // ... rest of the config
}
```

### Backend (RoomTypeService.java)

Modify the `aggregateRoomsByType` method to use dynamic building names:

```java
RoomTypeInfoDTO dto = new RoomTypeInfoDTO(
    type,
    type.getDisplayName(),
    getBuildingForRoomType(type),  // <-- Add this helper method
    seatRange,
    typeRooms.size(),
    amenities,
    images
);
```

Then add the helper method:

```java
private String getBuildingForRoomType(RoomType type) {
    // Map room types to buildings
    switch (type) {
        case SALLES_INFO:
            return "Science Building";
        case SALLES_LANGUES:
            return "Humanities Building";
        // ... add more mappings
        default:
            return "Main";
    }
}
```

## Troubleshooting

### Images Not Loading

1. **Check file paths:** Ensure paths are correct and case-sensitive
2. **Check file permissions:** Images must be readable by the web server
3. **Check browser console:** Look for 404 errors or CORS issues
4. **Verify image format:** Use standard formats (JPG, PNG, WebP)

### Images Too Large

1. **Optimize images:** Use tools like ImageOptim, TinyPNG, or Squoosh
2. **Resize images:** Scale down to 800x600 or 1200x900
3. **Use appropriate format:** JPG for photos, PNG for graphics

### CORS Issues (External URLs)

If using external image URLs, ensure the server allows CORS:

```
Access-Control-Allow-Origin: *
```

## Next Steps

1. Gather photos of each classroom type
2. Organize them in the appropriate folder structure
3. Update the image paths in `roomService.ts` (frontend) or `RoomTypeService.java` (backend)
4. Test the image slider functionality
5. Optimize images for web delivery

## Additional Resources

- [Image Optimization Guide](https://web.dev/fast/#optimize-your-images)
- [Responsive Images](https://developer.mozilla.org/en-US/docs/Learn/HTML/Multimedia_and_embedding/Responsive_images)
- [WebP Image Format](https://developers.google.com/speed/webp)
