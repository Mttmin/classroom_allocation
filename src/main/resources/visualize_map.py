import json
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
import matplotlib.patches as patches
import seaborn as sns
import pandas as pd
import numpy as np
import sys
import os
from math import ceil

def create_room_map(data, output_dir):
    rooms_data = data['allocation']['rooms']
    unallocated_data = data['allocation'].get('unallocatedCourses', [])
    df = pd.DataFrame(rooms_data)
    
    # Calculate utilization for occupied rooms
    df['utilization'] = df.apply(
        lambda x: x['course']['size'] / x['capacity'] if x['course'] else 0,
        axis=1
    )
    
    # Group by type and count rooms in each type
    type_counts = df.groupby('type').size()
    num_types = len(type_counts)
    
    # Create a color map for utilization
    cmap = plt.cm.RdYlGn  # Red (high utilization) to Yellow to Green (low utilization)
    
    # Determine if we need the unallocated courses column
    has_unallocated = len(unallocated_data) > 0
    num_cols = 3 if has_unallocated else 2
    
    # Calculate optimal layout
    total_plots = num_types + (1 if has_unallocated else 0)
    num_rows = ceil(total_plots / num_cols)
    
    # Create figure with a specific size
    fig_width = 20 if has_unallocated else 15
    plt.figure(figsize=(fig_width, num_rows * 4))
    
    # Create a grid of subplots with more space between them
    gs = gridspec.GridSpec(num_rows, num_cols, hspace=0.6, wspace=0.4)
    
    # Fixed dimensions for room rectangles
    room_width = 1.8
    room_height = 1.8
    spacing = 2.2  # Space between rooms
    max_cols = 5  # Maximum number of rooms per row in the grid
    
    # Plot each room type
    for idx, (room_type, count) in enumerate(type_counts.items()):
        row = idx // num_cols
        col = idx % num_cols
        ax = plt.subplot(gs[row, col])
        
        # Get rooms of this type
        type_rooms = df[df['type'] == room_type].copy()
        
        # Calculate grid dimensions for this room type
        n_rooms = len(type_rooms)
        grid_cols = min(max_cols, n_rooms)
        grid_rows = ceil(n_rooms / grid_cols)
        
        # Plot each room in this type
        for i, (_, room) in enumerate(type_rooms.iterrows()):
            # Calculate position in grid
            x = (i % grid_cols) * spacing
            y = (grid_rows - 1 - (i // grid_cols)) * spacing
            
            # Create room rectangle with fixed size
            rect = patches.Rectangle((x, y), room_width, room_height, 
                                  linewidth=1, edgecolor='black', 
                                  facecolor='white', alpha=0.8)
            ax.add_patch(rect)
            
            # Add room name only for "Grands Amphis" and "Amphis 80_100"
            if room_type in ["Grands Amphis", "Amphis 80_100"]:
                ax.text(x + room_width/2, y + room_height - 0.2, room['name'], 
                       ha='center', va='center', fontsize=9, fontweight='bold')
            
            # Add capacity
            ax.text(x + room_width/2, y + room_height - 0.5, 
                   f"Cap: {room['capacity']}", 
                   ha='center', va='center', fontsize=8)
            
            # If room is occupied, add utilization circle and course info
            if room['course']:
                utilization = room['utilization']
                color = cmap(utilization)
                
                # Add utilization circle
                circle = patches.Circle((x + room_width/2, y + 0.7), 0.25, 
                                     color=color)
                ax.add_patch(circle)
                
                # Add utilization percentage inside circle
                ax.text(x + room_width/2, y + 0.7, 
                       f"{int(utilization * 100)}%",
                       ha='center', va='center', 
                       fontsize=7, color='black' if utilization > 0.5 else 'white',
                       fontweight='bold')
                
                # Add course info
                course_text = f"{room['course']['name']}\n({room['course']['size']})"
                ax.text(x + room_width/2, y + 0.3, course_text,
                       ha='center', va='center', fontsize=8)
        
        # Set axis limits
        ax.set_xlim(-0.5, grid_cols * spacing + 0.5)
        ax.set_ylim(-0.5, grid_rows * spacing + 0.5)
        
        # Improve title readability for room types with many rooms
        title = f"{room_type}\n({n_rooms} rooms)"
        if n_rooms > 15:  # For room types with many rooms
            ax.set_title(title, pad=20, fontsize=11, fontweight='bold', 
                        bbox=dict(facecolor='white', edgecolor='none', 
                                alpha=0.8, pad=3.0))
        else:
            ax.set_title(title, pad=10, fontsize=11, fontweight='bold')
        
        ax.axis('off')
    
    # Add unallocated courses section if there are any
    if has_unallocated:
        ax = plt.subplot(gs[:, -1])  # Use the last column for all rows
        ax.set_title("Unallocated Courses\n", 
                    pad=10, fontsize=11, fontweight='bold')
        
        # Calculate rows needed for unallocated courses
        courses_per_row = 8
        rows_needed = ceil(len(unallocated_data) / courses_per_row)
        
        for i, course in enumerate(unallocated_data):
            row = i // courses_per_row
            col = i % courses_per_row
            
            # Calculate position
            x = col * 3
            y = (rows_needed - 1 - row) * 1.2
            
            # Create rectangle for unallocated course
            rect = patches.Rectangle((x, y), 2.5, 0.8, linewidth=1,
                                  edgecolor='red', facecolor='mistyrose',
                                  alpha=0.8)
            ax.add_patch(rect)
            
            # Add course information
            course_text = f"{course['name']}\n({course['size']})"
            ax.text(x + 1.25, y + 0.4, course_text,
                   ha='center', va='center', fontsize=8)
        
        # Set axis limits for unallocated courses
        ax.set_xlim(-0.5, courses_per_row * 3 + 0.5)
        ax.set_ylim(-0.5, rows_needed * 1.2 + 0.5)
        ax.axis('off')
    
    # Save the plot with high resolution
    plt.savefig(os.path.join(output_dir, 'room_map.png'), 
                bbox_inches='tight', dpi=300)
    plt.close()
    print(f"Generated improved room map visualization")

def main():
    if len(sys.argv) < 2:
        print("Usage: python visualize_map.py <json_file_path>")
        sys.exit(1)
    
    json_path = sys.argv[1]
    output_dir = os.path.dirname(json_path)
    
    print(f"Loading data from {json_path}")
    
    try:
        # Load data
        with open(json_path, 'r') as f:
            data = json.load(f)
        
        # Generate room map
        create_room_map(data, output_dir)
        
        print("Visualization completed successfully!")
        
    except Exception as e:
        print(f"Error during visualization: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()