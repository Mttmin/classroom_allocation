import json
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
import matplotlib.patches as patches
import seaborn as sns
import pandas as pd
import numpy as np
import sys
import os

def create_room_map(data, output_dir):
    rooms_data = data['allocation']['rooms']
    unallocated_data = data['allocation']['unallocatedCourses']
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
    
    # Calculate grid layout
    num_cols = 3  # Changed to 3 columns to accommodate unallocated courses
    num_rows = ((num_types + 1) // 2)  # Keep same number of rows
    
    # Create figure with a specific size
    plt.figure(figsize=(20, num_rows * 5))  # Increased figure size
    
    # Create a grid of subplots
    gs = gridspec.GridSpec(num_rows, num_cols, hspace=0.4, wspace=0.3)
    
    # Track the maximum rooms in any type for consistent scaling
    max_rooms = max(type_counts)
    
    # Plot each room type
    for idx, (room_type, count) in enumerate(type_counts.items()):
        row = idx // 2
        col = (idx % 2) * 1  # Use only columns 0 and 1
        ax = plt.subplot(gs[row, col])
        
        # Get rooms of this type
        type_rooms = df[df['type'] == room_type].copy()
        
        # Calculate grid dimensions for this room type
        n_rooms = len(type_rooms)
        grid_cols = min(4, n_rooms)  # Max 4 rooms per row (increased from 5)
        grid_rows = (n_rooms + grid_cols - 1) // grid_cols
        
        # Plot each room in this type
        for i, (_, room) in enumerate(type_rooms.iterrows()):
            # Calculate position in grid
            x = (i % grid_cols) * 2.0  # Increased spacing
            y = (grid_rows - 1 - (i // grid_cols)) * 2.0  # Increased spacing
            
            # Create room rectangle
            rect = patches.Rectangle((x, y), 1.5, 1.5, linewidth=1, 
                                  edgecolor='black', facecolor='white')
            ax.add_patch(rect)
            
            # Add room name
            ax.text(x + 0.75, y + 1.2, room['name'], 
                   ha='center', va='center', fontsize=10, fontweight='bold')
            
            # Add capacity
            ax.text(x + 0.75, y + 0.9, f"Capacity: {room['capacity']}", 
                   ha='center', va='center', fontsize=9)
            
            # If room is occupied, add a colored dot based on utilization
            if room['course']:
                utilization = room['utilization']
                color = cmap(utilization)
                circle = patches.Circle((x + 0.75, y + 0.6), 0.2, 
                                     color=color)
                ax.add_patch(circle)
                
                # Add course name and size
                course_text = f"{room['course']['name']}\n({room['course']['size']} students)"
                ax.text(x + 0.75, y + 0.3, course_text,
                       ha='center', va='center', fontsize=8)
        
        # Set axis limits and title
        ax.set_xlim(-0.5, grid_cols * 2.0 + 0.5)  # Adjusted for new spacing
        ax.set_ylim(-0.5, grid_rows * 2.0 + 0.5)  # Adjusted for new spacing
        ax.set_title(f"{room_type}\n({n_rooms} rooms)", pad=10, fontsize=12, fontweight='bold')
        ax.axis('off')
    
    # Add unallocated courses section
    if unallocated_data:
        # Create a new subplot for unallocated courses
        for row in range(num_rows):
            ax = plt.subplot(gs[row, -1])  # Use the last column
            if row == 0:  # Only set title for first row
                ax.set_title("Unallocated Courses\n", 
                           pad=10, fontsize=12, fontweight='bold')
            
            # Calculate which courses to show in this row
            courses_per_row = 10
            start_idx = row * courses_per_row
            end_idx = min((row + 1) * courses_per_row, len(unallocated_data))
            row_courses = unallocated_data[start_idx:end_idx]
            
            if row_courses:  # If there are courses to show in this row
                for i, course in enumerate(row_courses):
                    y = (courses_per_row - 1 - i) * 0.8  # Adjusted spacing
                    
                    # Create rectangle for unallocated course
                    rect = patches.Rectangle((0, y), 2, 0.6, linewidth=1,
                                          edgecolor='red', facecolor='mistyrose')
                    ax.add_patch(rect)
                    
                    # Add course information
                    course_text = f"{course['name']}\n({course['size']} students)"
                    ax.text(1, y + 0.3, course_text,
                           ha='center', va='center', fontsize=8)
                
                # Set axis limits
                ax.set_xlim(-0.5, 2.5)
                ax.set_ylim(-0.5, courses_per_row * 0.8 + 0.5)
            ax.axis('off')
    
    # Add colorbar
    cax = plt.axes([0.92, 0.1, 0.02, 0.8])
    norm = plt.Normalize(0, 1)
    plt.colorbar(plt.cm.ScalarMappable(norm=norm, cmap=cmap), 
                cax=cax, label='Utilization Rate')
    
    # Save the plot
    plt.savefig(os.path.join(output_dir, 'room_map.png'), 
                bbox_inches='tight', dpi=300)
    plt.close()
    print(f"Generated room map visualization")

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